/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api.support;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.logging.AppLogsCollector;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionConstants;
import com.intel.missioncontrol.mission.MissionInfo;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.MatchingImagesUsage;
import com.intel.missioncontrol.ui.dialogs.MatchingsTableRowData;
import com.intel.missioncontrol.ui.dialogs.SendSupportRetryViewModel;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.utils.IVersionProvider;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import eu.mavinci.core.helper.MProperties;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.licence.AllowedUser;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.Licence;
import eu.mavinci.core.update.UpdateURL;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.AMapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoFile;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.desktop.helper.ErrorDownloading;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.helper.uploader.UploadFile;
import eu.mavinci.desktop.helper.uploader.Uploader;
import eu.mavinci.desktop.helper.uploader.UploaderMAVinciSCP;
import eu.mavinci.desktop.main.debug.Debug;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the {@ISupportManager} interface and provides methods for the basic back-end operations
 * available in the "Support Request" dialog: upload support files (with possibility to cancel in the process), upload
 * later and upload old support requests.
 *
 * @author aiacovici
 */
@Singleton
public class SupportManager implements ISupportManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupportManager.class);

    private static final String ERROR_DOWNLOADS_FOLDER = "errorDownloads";

    private final BooleanProperty hasOldSupportRequests = new SimpleBooleanProperty();

    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final IMissionManager missionManager;
    private final IDialogService dialogService;
    private final IProfilingManager profilingManager;
    private final IPathProvider pathProvider;
    private final AppLogsCollector appLogsCollector;
    private final IVersionProvider versionProvider;
    private final ILicenceManager licenceManager;
    private final GeneralSettings generalSettings;
    private final Mission.Factory missionFactory;

    private int curIdx = 0;
    private MProperties prop = new MProperties();
    private Vector<File> screenshots;

    @Inject
    public SupportManager(
            IApplicationContext applicationContext,
            ILanguageHelper languageHelper,
            IMissionManager missionManager,
            IDialogService dialogService,
            IProfilingManager profilingManager,
            IPathProvider pathProvider,
            IVersionProvider versionProvider,
            ILicenceManager licenceManager,
            ISettingsManager settingsManager,
            Mission.Factory missionFactory) {
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        this.missionManager = missionManager;
        this.dialogService = dialogService;
        this.profilingManager = profilingManager;
        this.pathProvider = pathProvider;
        this.versionProvider = versionProvider;
        this.licenceManager = licenceManager;
        this.generalSettings = settingsManager.getSection(GeneralSettings.class);
        this.appLogsCollector = new AppLogsCollector(pathProvider.getLogDirectory());
        this.missionFactory = missionFactory;
        Dispatcher.platform().runLater(this::scanReportFolder, Duration.ofSeconds(15));

        // FIXME use normal trusted certificate import instead
        TrustManager[] trustAllCerts =
            new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
            };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {
        }
    }

    @Override
    public void scanReportFolder() {
        scanReportFolder(false);
    }

    @Override
    public void scanReportFolder(boolean showToast) {
        checkErrorReports();
        if (hasOldSupportRequests.getValue()) {
            for (File file : getErrorReports()) {
                dialogService.requestDialogAndWait(
                    WindowHelper.getPrimaryViewModel(), SendSupportRetryViewModel.class, () -> file);
            }
        } else if (showToast) {
            applicationContext.addToast(
                Toast.of(ToastType.INFO)
                    .setText(languageHelper.getString("supportSettingsView.noSupportRequest"))
                    .setTimeout(Toast.MEDIUM_TIMEOUT)
                    .create());
        }
    }

    private File[] getErrorReports() {
        File errorReportsFolder = pathProvider.getErrorUploadDirectory().toFile();
        File[] files = errorReportsFolder.listFiles(File::isDirectory);
        return files != null ? files : new File[0];
    }

    @Override
    public void checkErrorReports() {
        Optional.ofNullable(getErrorReports()).ifPresent(files -> hasOldSupportRequests.setValue(files.length > 0));
    }

    @Override
    public BooleanProperty hasOldSupportRequestsProperty() {
        return hasOldSupportRequests;
    }

    @Override
    public File prepareFilesForTransfer(
            ErrorCategory category,
            Priority priority,
            String problemDescrition,
            Map<String, Boolean> options,
            List<MatchingsTableRowData> matchingsData,
            List<File> additionals,
            List<String> recipients,
            String fullName,
            String country,
            String ticketIdOld) {
        final File folder = new File(pathProvider.getErrorUploadDirectory().toFile(), "" + System.currentTimeMillis());
        boolean created = folder.mkdirs();

        if (!created) {
            LOGGER.error("could not create file structure");
        }

        long oldWarningCount = Debug.reportedIssuesCount();
        File meta = new File(folder, SupportConstants.FILENAME_META);

        prop.clear();
        prop.setUnchanged();
        prop.setProperty(SupportConstants.KEY_STORED_COMMENT, problemDescrition);
        prop.setProperty(SupportConstants.KEY_STORED_ERR_COUNT, "" + oldWarningCount);
        prop.setProperty(SupportConstants.KEY_STORED_AUTOMATIC, "false");

        prop.setProperty(SupportConstants.KEY_PRIORITY, "" + priority);
        prop.setProperty(SupportConstants.KEY_UPTIME, "" + StringHelper.secToShortDHMS(Debug.getUptimeMSec() / 1000.));
        prop.setProperty(SupportConstants.KEY_FULLNAME, "" + fullName);
        prop.setProperty(SupportConstants.KEY_COUNTRY, "" + country);
        prop.setProperty(SupportConstants.KEY_CATEGORY, "" + category);
        prop.setProperty(SupportConstants.KEY_TICKETIDOLD, "" + ticketIdOld);

        // pre store it, so it can be added to files list!
        try {
            prop.storeToXML(meta);
        } catch (IOException e) {
            LOGGER.warn("could not save upload task description", e);
        }

        curIdx = 0;

        fileToProp(meta, null);

        if (options.get(SupportConstants.OPTION_APPLICATION_SETTINGS)) {
            // newest logfiles are sent by default
            try {
                for (File f : appLogsCollector.getCurrentLogFilesSnapShot()) {
                    File to = new File(folder, f.getName());
                    FileHelper.copyFile(f, to);
                    fileToProp(to, null);
                }
            } catch (IOException e) {
                LOGGER.warn("could not snapshot logfiles", e);
            }
            // after the IMC-813 we do not need to send all logs, only affected by the application run

            // installation log is sent by default - seems to be set and handled
            // only in Windows
            File installationFile = null;
            if (versionProvider.getSystem().isWindows()) {
                installationFile = new File(versionProvider.getInstallDir(), SupportConstants.FILENAME_INSTALL_LOG);
            }

            if (installationFile != null && installationFile.exists()) {
                fileToProp(installationFile, installationFile.getParentFile());
            }

            // the application settings are send by default
            try {
                File f = pathProvider.getSettingsFile().toFile();
                if (f.exists()) {
                    File to = new File(folder, f.getName());
                    FileHelper.copyFile(f, to);
                    fileToProp(to, null);
                }
            } catch (IOException e) {
                LOGGER.warn("could not snapshot settings.json", e);
            }

            try {
                File f = pathProvider.getLegacySettingsFile().toFile();
                if (f.exists()) {
                    File to = new File(folder, f.getName());
                    FileHelper.copyFile(f, to);
                    fileToProp(to, null);
                }
            } catch (IOException e) {
                LOGGER.warn("could not snapshot appsettings", e);
            }

            // TODO FIXME: add all HW description files to the support request

            if (profilingManager.isActive()) {
                for (final File f :
                    FileHelper.scanFiles(
                        MFileFilter.notHiddenFilesFilter, pathProvider.getProfilingDirectory().toFile(), 3)) {
                    fileToProp(f, null);
                }
            }
        }
        // check if Screenshots of the application are allowed
        if (options.get(SupportConstants.OPTION_SCREENSHOTS)) {
            // use available screenshots
            for (File f : screenshots) {
                try {
                    File to = new File(folder, f.getName());
                    FileHelper.copyFile(f, to);
                    fileToProp(to, null);
                } catch (IOException e) {
                    LOGGER.warn("could not move screenshots", e);
                }
            }
        }

        if (options.get(SupportConstants.OPTION_SESSION_SETTINGS)) {
            Mission mission = getCurrentOrLastMission();
            fileToProp(MissionConstants.getNewtConfigFile(mission.getDirectory()).toFile());

            for (FlightPlan fp : getCurrentOrLastMission().flightPlansProperty()) {
                fileToProp(fp.getResourceFile());
            }

            final File flightplanAutosaveFolder = MissionConstants.getFlightplanAutosaveFolder(mission.getDirectory());
            if (flightplanAutosaveFolder != null
                    && flightplanAutosaveFolder.exists()
                    && MFileFilter.fmlFilter != null
                    && MFileFilter.fmlFilter.getWithoutFolders() != null) {
                File[] fmlFiles = flightplanAutosaveFolder.listFiles(MFileFilter.fmlFilter.getWithoutFolders());
                if (fmlFiles != null) {
                    for (File f : fmlFiles) {
                        fileToProp(f);
                    }
                }
            }

            for (File f : mission.getImcFlightLogFiles()) {
                fileToProp(f);
            }

            File ftpFolder = MissionConstants.getFlightLogsFolder(mission.getDirectory());
            if (Files.exists(ftpFolder.toPath())) {
                File[] ftpFolderFiles = ftpFolder.listFiles(MFileFilter.notHiddenFilesFilter);
                if (ftpFolderFiles != null) {
                    for (File f : ftpFolderFiles) {
                        fileToProp(f);
                    }
                }
            }

            for (File f : mission.getKMLs()) {
                fileToProp(f);
            }

            for (File f : mission.getPlaneConfigs()) {
                fileToProp(f);
            }

            File planeConfigAutosave = MissionConstants.getPlaneConfigAutosaveFolder(mission.getDirectory());
            MFileFilter configFilter = MFileFilter.configFilter;
            if (planeConfigAutosave != null && planeConfigAutosave.exists()) {
                if (configFilter != null && configFilter.getWithoutFolders() != null) {
                    File[] autosaveFiles = planeConfigAutosave.listFiles(configFilter.getWithoutFolders());
                    if (autosaveFiles != null) {
                        for (File f : autosaveFiles) {
                            fileToProp(f);
                        }
                    }
                }
            }

            for (MatchingsTableRowData data : matchingsData) {
                if (data.getMatchingImagesUsage() == null || data.getMatchingImagesUsage() == MatchingImagesUsage.NO) {
                    continue;
                }

                File matchingFolder = data.getMatchingFolder();

                if (!matchingFolder.exists()) {
                    continue;
                }

                File save = new File(matchingFolder, AMapLayerMatching.DEFAULT_FILENAME);
                if (save.exists()) {
                    fileToProp(save);
                }

                File fpFolder = new File(matchingFolder, MapLayerMatching.FOLDER_NAME_FLIGHTPLANS_SUBFOLDER);
                if (fpFolder.exists() && MFileFilter.fmlFilter != null) {
                    File[] fpFolderFiles = fpFolder.listFiles(MFileFilter.fmlFilter.getWithoutFolders());
                    if (fpFolderFiles != null) {
                        for (File f : fpFolderFiles) {
                            fileToProp(f);
                        }
                    }
                }

                File[] matchingFolderLogFiles = matchingFolder.listFiles(MFileFilter.logFilter.getWithoutFolders());
                if (matchingFolderLogFiles != null) {
                    for (File f : matchingFolderLogFiles) {
                        fileToProp(f);
                    }
                }

                File[] photoLogFiles = matchingFolder.listFiles(MFileFilter.photoLogFilter.getWithoutFolders());
                if (photoLogFiles != null) {
                    for (File f : photoLogFiles) {
                        fileToProp(f);
                    }
                }

                if (data.getMatchingImagesUsage() == MatchingImagesUsage.ALL) {
                    File imgFolder = new File(matchingFolder, MapLayerMatching.FOLDER_NAME_PICS_SUBFOLDER);
                    if (imgFolder.exists()) {
                        File[] imgFolderFiles = imgFolder.listFiles(MFileFilter.jpegFilter.getWithoutFolders());
                        if (imgFolderFiles != null) {
                            for (File f : imgFolderFiles) {
                                fileToProp(f);
                            }
                        }
                    }
                }

                if (data.getMatchingImagesUsage() == MatchingImagesUsage.PREVIEW) {
                    File imgFolder = new File(matchingFolder, PhotoFile.FOLDER_PREVIEW_IMG);
                    if (imgFolder.exists()) {
                        File[] previewImgFiles =
                            imgFolder.listFiles(MFileFilter.jpegFilterInclThumps.getWithoutFolders());
                        if (previewImgFiles != null) {
                            for (File f : previewImgFiles) {
                                fileToProp(f);
                            }
                        }
                    }
                }
            }
        }

        HashMap<File, Vector<File>> folderContentMap = new HashMap<File, Vector<File>>();

        for (File file : additionals) {
            if (!FileHelper.canRead(file, null)) {
                continue;
            }

            if (file.isDirectory()) {
                Vector<File> list = FileHelper.listFiles(file);
                folderContentMap.put(file, list);
            }
        }

        for (File file : additionals) {
            if (!file.exists()) {
                continue;
            }

            if (file.isDirectory()) {
                Vector<File> list = folderContentMap.getOrDefault(file, new Vector<>());
                for (File f : list) {
                    if (f.isDirectory()) {
                        continue;
                    }

                    fileToProp(f, file.getParentFile());
                }
            } else {
                fileToProp(file, file.getParentFile());
            }
        }

        try {
            prop.setProperty(SupportConstants.KEY_RECIPIENTS, String.join(",", recipients));
            prop.setProperty(SupportConstants.KEY_DATE, LocalDateTime.now().toString());
            prop.setProperty(
                SupportConstants.KEY_SIZE,
                String.valueOf(
                    Files.walk(folder.toPath())
                        .filter(path -> path.toFile().isFile())
                        .mapToLong(path -> path.toFile().length())
                        .sum()));
            prop.storeToXML(meta);
            Debug.resetReportedIssues();
        } catch (IOException e) {
            LOGGER.warn("could not save upload task description", e);
        }

        return folder;
    }

    @Override
    public List<File> getFilesForRequest(Map<String, Boolean> options, List<MatchingsTableRowData> matchingsData) {
        List<File> files = new ArrayList<>();

        if (options.get(SupportConstants.OPTION_APPLICATION_SETTINGS)) {
            // newest logfiles are sent by default
            try {
                files.addAll(appLogsCollector.getCurrentLogFilesSnapShot());
            } catch (IOException e) {
                LOGGER.warn("could not snapshot logfiles", e);
            }

            // after the IMC-813 we do not need to send all logs, only affected by the application run

            // installation log is sent by default - seems to be set and handled
            // only in Windows
            if (versionProvider.getSystem().isWindows()) {
                File installationFile =
                    new File(versionProvider.getInstallDir().getAbsolutePath(), SupportConstants.FILENAME_INSTALL_LOG);
                if (installationFile.exists()) {
                    files.add(installationFile);
                }
            }
            // the application settings are send by default
            File settings = pathProvider.getSettingsFile().toFile();
            if (settings.exists()) {
                files.add(settings);
            }

            settings = pathProvider.getLegacySettingsFile().toFile();
            if (settings.exists()) {
                files.add(settings);
            }

            /*

                        // TODO FIXME: add all HW description files to the support request

            */

            if (profilingManager.isActive()) {
                files.addAll(
                    FileHelper.scanFiles(
                        MFileFilter.notHiddenFilesFilter, pathProvider.getProfilingDirectory().toFile(), 3));
            }
        }

        // check if Screenshots of the application are allowed
        if (options.get(SupportConstants.OPTION_SCREENSHOTS)) {
            screenshots = getScreenshots(); // generate new screenshots
            files.addAll(screenshots);
        }

        if (options.get(SupportConstants.OPTION_SESSION_SETTINGS)) {
            Mission mission = getCurrentOrLastMission();
            if (mission != null) {
                files.add(MissionConstants.getLegacyConfigFile(mission.getDirectory()).toFile());
                files.add(MissionConstants.getNewtConfigFile(mission.getDirectory()).toFile());

                files.addAll(
                    mission.flightPlansProperty()
                        .stream()
                        .map(flightPlan -> flightPlan.getLegacyFlightplan().getResourceFile())
                        .collect(Collectors.toList()));

                final File flightplanAutosaveFolder =
                    MissionConstants.getFlightplanAutosaveFolder(mission.getDirectory());
                if (flightplanAutosaveFolder != null
                        && flightplanAutosaveFolder.exists()
                        && MFileFilter.fmlFilter != null
                        && MFileFilter.fmlFilter.getWithoutFolders() != null) {
                    File[] fmlFiles = flightplanAutosaveFolder.listFiles(MFileFilter.fmlFilter.getWithoutFolders());
                    if (fmlFiles != null) {
                        for (File f : fmlFiles) {
                            files.add(f);
                        }
                    }
                }

                files.addAll(mission.getImcFlightLogFiles());

                File ftpFolder = MissionConstants.getFlightLogsFolder(mission.getDirectory());
                if (Files.exists(MissionConstants.getFlightLogsFolder(mission.getDirectory()).toPath())) {
                    Optional.ofNullable(ftpFolder.listFiles(MFileFilter.notHiddenFilesFilter))
                        .ifPresent(files1 -> Collections.addAll(files, files1));
                }

                files.addAll(mission.getKMLs());

                files.addAll(mission.getPlaneConfigs());

                File planeConfigAutosave = MissionConstants.getPlaneConfigAutosaveFolder(mission.getDirectory());
                if (planeConfigAutosave != null && planeConfigAutosave.exists()) {
                    if (MFileFilter.configFilter.getWithoutFolders() != null) {
                        Optional.ofNullable(planeConfigAutosave.listFiles(MFileFilter.configFilter.getWithoutFolders()))
                            .ifPresent(files1 -> Collections.addAll(files, files1));
                    }
                }

                for (MatchingsTableRowData data : matchingsData) {
                    if (data.getMatchingImagesUsage() == null
                            || data.getMatchingImagesUsage() == MatchingImagesUsage.NO) {
                        continue;
                    }

                    File matchingFolder = data.getMatchingFolder();

                    if (!matchingFolder.exists()) {
                        continue;
                    }

                    File save = new File(matchingFolder, AMapLayerMatching.DEFAULT_FILENAME);
                    if (save.exists()) {
                        files.add(save);
                    }

                    File fpFolder = new File(matchingFolder, MapLayerMatching.FOLDER_NAME_FLIGHTPLANS_SUBFOLDER);
                    if (fpFolder.exists() && MFileFilter.fmlFilter != null) {
                        File[] fpFolderFiles = fpFolder.listFiles(MFileFilter.fmlFilter.getWithoutFolders());
                        if (fpFolderFiles != null) {
                            Collections.addAll(files, fpFolderFiles);
                        }
                    }

                    File[] matchingFolderLogFiles = matchingFolder.listFiles(MFileFilter.logFilter.getWithoutFolders());
                    if (matchingFolderLogFiles != null) {
                        Collections.addAll(files, matchingFolderLogFiles);
                    }

                    File[] photoLogFiles = matchingFolder.listFiles(MFileFilter.photoLogFilter.getWithoutFolders());
                    if (photoLogFiles != null) {
                        Collections.addAll(files, photoLogFiles);
                    }

                    if (data.getMatchingImagesUsage() == MatchingImagesUsage.ALL) {
                        File imgFolder = new File(matchingFolder, MapLayerMatching.FOLDER_NAME_PICS_SUBFOLDER);
                        if (imgFolder.exists()) {
                            File[] imgFolderFiles = imgFolder.listFiles(MFileFilter.jpegFilter.getWithoutFolders());
                            if (imgFolderFiles != null) {
                                Collections.addAll(files, imgFolderFiles);
                            }
                        }
                    }

                    if (data.getMatchingImagesUsage() == MatchingImagesUsage.PREVIEW) {
                        File imgFolder = new File(matchingFolder, PhotoFile.FOLDER_PREVIEW_IMG);
                        if (imgFolder.exists()) {
                            File[] previewImgFiles =
                                imgFolder.listFiles(MFileFilter.jpegFilterInclThumps.getWithoutFolders());
                            if (previewImgFiles != null) {
                                Collections.addAll(files, previewImgFiles);
                            }
                        }
                    }
                }
            }
        }

        return files;
    }

    @Override
    public void sendFilesToServer(
            ErrorCategory category,
            Priority priority,
            String problemDescrition,
            Map<String, Boolean> options,
            List<MatchingsTableRowData> matchings,
            List<File> additionals,
            List<String> recipients,
            IMProgressMonitor monitor,
            String fullName,
            String country,
            String ticketIdOld) {
        monitor.setNote("Prepare report folder for transfer...");

        // first prepare the files ( copy them in the timestemap folder and make
        // ZIP files out of them)
        File folder =
            prepareFilesForTransfer(
                category,
                priority,
                problemDescrition,
                options,
                matchings,
                additionals,
                recipients,
                fullName,
                country,
                ticketIdOld);

        checkErrorReports();

        doUpload(folder, monitor);
    }

    protected void fileToProp(File file) {
        fileToProp(file, false);
    }

    protected void fileToProp(File file, boolean resend) {
        fileToProp(file, false, getCurrentOrLastMission().getDirectory().toFile().getParentFile());
    }

    protected void fileToProp(File file, File base) {
        fileToProp(file, false, base);
    }

    protected void fileToProp(File file, boolean resend, File base) {
        if (!file.exists()) {
            return;
        }

        String id = SupportConstants.NUMBER_PREFIX_FORMAT.format(curIdx);
        prop.setProperty(id, file.getAbsolutePath());
        if (resend) {
            prop.setProperty(id + SupportConstants.KEY_SUFFIX_RESEND, Boolean.TRUE.toString());
        }

        if (base != null) {
            prop.setProperty(id + SupportConstants.KEY_SUFFIX_BASEPATH, base.getAbsolutePath());
        }
        // System.out.println("file: " + file + " base:" + base);
        curIdx++;
    }

    /**
     * This method is taking screenshots of the windows of the opened Mission Control application, for support purposes,
     * and returns them as a list of image files in JPG format.
     *
     * @return a list of image files, representing the screenshots taken of the application for helping the support.
     */
    private Vector<File> getScreenshots() {
        Vector<File> screenshots = new Vector<>();

        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.run(
            () -> {
                for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                    int width = (int)w.sceneProperty().get().getWidth();
                    int height = (int)w.sceneProperty().get().getHeight();
                    if (width * height <= 0) {
                        continue;
                    }

                    WritableImage image = new WritableImage(width, height);
                    w.sceneProperty().get().snapshot(image);

                    try {
                        File f = File.createTempFile("Window_", ".png");
                        BufferedImage bufImage = SwingFXUtils.fromFXImage(image, null);
                        ImageIO.write(bufImage, "png", f);
                        screenshots.add(f);
                        f.deleteOnExit();
                    } catch (Exception e1) {
                        LOGGER.warn("Failed to create a screenshot", e1);
                    }
                }
            });

        return screenshots;
    }

    private Mission getCurrentOrLastMission() {
        Mission result = applicationContext.getCurrentMission();
        if (result == null) {
            try (LockedList<MissionInfo> missionInfos = missionManager.recentMissionInfosProperty().lock()) {
                return missionFactory.create(missionInfos.iterator().next());
            }
        }

        return result;
    }

    /**
     * This method will check id the error report folder is empty and exit if it is. If the folder is not empty, it will
     * locate the meta file and load the properties from it. It then performs some checks on the files listed in the
     * meta file and after that calls the uploadForMissionControl() method in Uploader, which will do the actual
     * transfer to the FTP server. uploadForMissionControl() is a copy of upload() to allow the use of the
     * IntelMissionControl.properties and eventually a redesign of the ProgressMonitor that pops up showing the progress
     * of the transfer. A ticket ID is assign to this upload to support and if and only if the transfer was successful,
     * prepares an email addressed to support, with relevant information about the upload. It opens an e-mail client on
     * the user's machine with pre-filled subject and content.
     *
     * @see Debug doUpload the original source of this code
     * @param errorReportFolder - the directory where the files to be uploaded to support are stored
     */
    public void doUpload(final File errorReportFolder, IMProgressMonitor monitor) {
        monitor.setNote("Read prepared files...");

        File[] tmp = errorReportFolder.listFiles();
        if (tmp == null || tmp.length == 0) {
            FileHelper.deleteDir(languageHelper, errorReportFolder, true);
            // ignore empty folders, because they may just be created and will cause no
            // pain!
            return;
        }

        prop = getReportProperties(errorReportFolder);

        // try {
        final String desc = prop.getProperty(SupportConstants.KEY_STORED_COMMENT, "");

        int idx = 0;
        String id = SupportConstants.NUMBER_PREFIX_FORMAT.format(idx);
        Vector<UploadFile> files = new Vector<UploadFile>();
        long size = 0;
        while (prop.containsKey(id)) {
            String getP = prop.getProperty(id);
            Expect.notNull(getP, "getP");
            File f = new File(getP);
            boolean resend = prop.contains(id + SupportConstants.KEY_SUFFIX_RESEND);

            String base = prop.getProperty(id + SupportConstants.KEY_SUFFIX_BASEPATH);
            size += f.length();
            if (base != null) {
                File baseFile = new File(base);
                files.add(new UploadFile(f, baseFile, resend));
            } else {
                files.add(new UploadFile(f, resend));
            }

            idx++;
            id = SupportConstants.NUMBER_PREFIX_FORMAT.format(idx);
        }

        String recipients =
            Optional.ofNullable(prop.getProperty(SupportConstants.KEY_RECIPIENTS))
                .orElseGet(() -> UpdateURL.getSupportEmail());
        String fullName = Optional.ofNullable(prop.getProperty(SupportConstants.KEY_FULLNAME)).orElse("");
        String country = Optional.ofNullable(prop.getProperty(SupportConstants.KEY_COUNTRY)).orElse("");
        Uploader up = new UploaderMAVinciSCP();

        Licence licence = licenceManager.getActiveLicence();
        String ticketId = (licence == null ? null : licence.getLicenceId()) + "_" + errorReportFolder.getName();

        String ticketIdOld = Optional.ofNullable(prop.getProperty(SupportConstants.KEY_TICKETIDOLD)).orElse("");
        String category = Optional.ofNullable(prop.getProperty(SupportConstants.KEY_CATEGORY)).orElse("");

        // had to rewrite the upload() method because of bundles and other issues
        boolean ok = up.uploadForMissionControl(files, ticketId, monitor);

        if (!ok) {
            LOGGER.info("Could not upload error report. Retrying on next application startup.");
            throw new CouldNotSendReportException(errorReportFolder);
        }

        sendEmailToSupport(ticketId, category, desc, files.size(), size, recipients, fullName, country, ticketIdOld);
        FileHelper.deleteDir(languageHelper, errorReportFolder, true);
        checkErrorReports();
    }

    public void doDownload(String ticketId, IMProgressMonitor monitor, IApplicationContext applicationContext) {
        File baseFolder = new File(pathProvider.getSettingsDirectory().toFile(), ERROR_DOWNLOADS_FOLDER);
        ticketId = ticketId.trim(); // remove spaces from user input
        File file = ErrorDownloading.downloadErrorUploadImc(ticketId, baseFolder, monitor);
        if (file == null || file.isFile()) {
            applicationContext.addToast(
                Toast.of(ToastType.ALERT)
                    .setShowIcon(true)
                    .setText(languageHelper.getString("supportSettingsView.cantDownloadToast", ticketId))
                    .create());
            return;
        }

        try {
            FileHelper.openFile(file);
        } catch (IOException e) {
            LOGGER.error("cant open folder of download:" + file, e);
        }

        Arrays.stream(file.listFiles())
            .filter(missionManager::isMissionFolder)
            .findFirst()
            .ifPresent(
                folder -> applicationContext.loadMissionAsync(missionFactory.create(new MissionInfo(folder.toPath()))));
    }

    /**
     * Prepares an email addressed to support, with relevant information about the upload to support It opens an e-mail
     * client on the user's machine with pre-filled subject and content.
     *
     * @param ticketId - the ticket id that was generated based on hash and timestamp for the current upload to support
     *     action
     * @param userComment - the comment input by the user in the upload to support dialog
     * @param filesCount - the number of files being uploaded
     * @param size - the total size of the files being uploaded
     */
    private void sendEmailToSupport(
            String ticketId,
            String category,
            String userComment,
            int filesCount,
            long size,
            String recipients,
            String fullName,
            String country,
            String ticketIdOld) {
        String body = "";
        boolean wasAutomatic = false;
        try {
            wasAutomatic = Boolean.parseBoolean(prop.getProperty(SupportConstants.KEY_STORED_AUTOMATIC));
        } catch (Exception e) {
            LOGGER.error("Error parsing boolean value: " + SupportConstants.KEY_STORED_AUTOMATIC, e);
        }

        /*String receiver =
        wasAutomatic
            ? UpdateURL.ENTERPRISE_EMAIL
            : UpdateURL.getSupportEmail();*/

        String receiver = UpdateURL.getSupportEmail();
        Licence licence = licenceManager.getActiveLicence();
        AllowedUser user = licence.getAllowedUser();
        body += "ResellerID: " + licence == null ? null : licence.getResellerID() + "\n";
        body += "Responsible for this Support: " + receiver + "\n\n";

        body += "TicketID: " + ticketId + "\n";

        if (!ticketIdOld.isEmpty()) {
            body += "Belongs to TicketID: " + ticketIdOld + "\n";
        }

        String prio = prop.getProperty(SupportConstants.KEY_PRIORITY);
        body += "Priority: " + prio + "\n";
        body += "Category: " + category + "\n";

        // body += "path: " + up.getFolderPath() + "\n";
        body += "Number of files: " + filesCount + "\n";
        body += "Size of files (before zipping): " + StringHelper.bytesToIngName(size, -4, false) + "\n";
        body += "Client: " + user == null ? null : user.getDisplayName() + "\n";
        body += "Name: " + fullName + "\n";
        body += "Country: " + country + "\n";
        body += "EMail: " + recipients + "\n";
        body += "UserComment: " + userComment + "\n";
        body += "WarningCount: " + prop.getProperty(SupportConstants.KEY_STORED_ERR_COUNT) + "\n";
        body +=
            "Version: "
                + versionProvider.getHumanReadableVersion()
                + " for "
                + versionProvider.getSystem().name()
                + "\n";
        body += "Uptime: " + prop.getProperty(SupportConstants.KEY_UPTIME) + " D:HH:MM:SS\n";
        body += "LastAP: " + generalSettings.lastSeenAPForSupportProperty().get() + "\n"; // lastSeenAPForSupport
        body +=
            "LastConnector: "
                + generalSettings.lastSeenConnectorForSupportProperty().get()
                + "\n"; // lastSeenConnectorForSupport

        String emailSubjectPrefix = languageHelper.getString("supportSettingsView.email.subjectPrefix");

        FileHelper.sendEMail(receiver, recipients, emailSubjectPrefix + " " + prio + " " + ticketId, body);
    }

    @Override
    public MProperties getReportProperties(File errorReportFolder) {
        MProperties prop = new MProperties();
        try {
            prop.loadFromXML(new File(errorReportFolder, SupportConstants.FILENAME_META));
        } catch (Exception e) {
            LOGGER.warn("deleting old broken upload request", e); // WARNING
            FileHelper.deleteDir(languageHelper, errorReportFolder, true);
            throw new CouldNotSendReportException(errorReportFolder);
        }

        return prop;
    }

    public static File getTicketFolder(String ticketId, IPathProvider pathProvider) {
        File baseFolder = new File(pathProvider.getSettingsDirectory().toFile(), ERROR_DOWNLOADS_FOLDER);
        return new File(baseFolder, ticketId);
    }
}
