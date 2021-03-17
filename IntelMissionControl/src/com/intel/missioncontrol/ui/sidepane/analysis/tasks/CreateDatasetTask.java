/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.tasks;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionConstants;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.dialogs.DialogResult;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.AMapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.ExifInfos;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.ITaggingAlgorithm;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatch;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoCube;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoFile;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.TaggingAlgorithmA;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.TaggingException;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.TaggingSyncVisWindow;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonGeotags.Geotag;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.InterruptedByUserException;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.main.debug.profiling.requests.CreateDatasetRequest;
import eu.mavinci.flightplan.Flightplan;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Vladimir Iordanov */
public class CreateDatasetTask extends IBackgroundTaskManager.BackgroundTask {

    public static final String DONE_EVENT_NAME = "matchingCreated";
    public static final String FAIL_EVENT_NAME = "matchingCreationFailed";
    public static final Event DONE_EVENT = new Event(new EventType<>(EventType.ROOT, DONE_EVENT_NAME));
    public static final Event FAIL_EVENT = new Event(new EventType<>(EventType.ROOT, FAIL_EVENT_NAME));
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDatasetTask.class);

    private final File picFolder;
    private final File baseFolder;
    private final ITaggingAlgorithm taggingAlgorithm;
    private final List<Flightplan> flightPlans;
    private final boolean allowMultipleFeedbacks;
    private final List<File> logFiles;
    private final boolean isAscTecMatching;
    private final FileHelper.GetFotosResult getPhotosRes;
    private final String matchingName;
    private final IHardwareConfiguration hardwareConfiguration;
    private final ILanguageHelper languageHelper;
    private final boolean eraseLogsAfterCopying;
    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final CreateDatasetRequest createDatasetRequest;
    private final GeneralSettings generalSettings;
    private final IMapView mapView;
    private final ISelectionManager selectionManager;
    private final Mission mission;

    private MapLayerMatching legacyMatching;
    private Matching matching;
    private TaggingException exception;

    public CreateDatasetTask(
            IMapView mapView,
            File picFolder,
            File baseFolder,
            List<Flightplan> flightPlans,
            List<File> logFiles,
            FileHelper.GetFotosResult getPhotosRes,
            IHardwareConfiguration hardwareConfiguration,
            ILanguageHelper languageHelper,
            boolean eraseLogsAfterCopying,
            Matching matching,
            IApplicationContext applicationContext,
            INavigationService navigationService,
            GeneralSettings generalSettings,
            ISelectionManager selectionManager,
            Mission mission) {
        super(generateMatchingName(picFolder, baseFolder, flightPlans, logFiles));
        this.picFolder = picFolder;
        this.baseFolder = baseFolder;
        this.flightPlans = flightPlans;
        this.logFiles = logFiles;
        this.getPhotosRes = getPhotosRes;
        this.hardwareConfiguration = hardwareConfiguration;
        this.eraseLogsAfterCopying = eraseLogsAfterCopying;
        this.matching = matching;
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.generalSettings = generalSettings;
        this.mapView = mapView;
        this.selectionManager = selectionManager;
        this.mission = mission;

        matchingName = getName();
        this.name = "Importing " + matchingName;

        allowMultipleFeedbacks =
            hardwareConfiguration
                .getPrimaryPayload(IGenericCameraConfiguration.class)
                .getDescription()
                .getExifModels()
                .contains("RedEdge");
        isAscTecMatching = MFileFilter.ascTecLogFolder.acceptTrinityLog(logFiles.get(0));
        taggingAlgorithm = TaggingAlgorithmA.createNewDefaultTaggingAlgorithm();
        this.languageHelper = languageHelper;

        createDatasetRequest = new CreateDatasetRequest(matchingName);
        legacyMatching = (MapLayerMatching)matching.getLegacyMatching();
    }

    private static String generateMatchingName(
            File picFolder, File baseFolder, List<Flightplan> flightPlans, List<File> logFiles) {
        if (picFolder != null
                && picFolder.getName().equals(AMapLayerMatching.FOLDER_NAME_PICS_SUBFOLDER)
                && picFolder
                    .getAbsolutePath()
                    .startsWith(MissionConstants.getMatchingsFolder(baseFolder).getAbsolutePath())) {
            return picFolder.getParentFile().getName();
        } else if (flightPlans.size() >= 1) {
            return flightPlans.get(0).getName();
        } else if (MFileFilter.photoJsonFilter.accept(logFiles.get(0).getName())) {
            return logFiles.get(0).getParentFile().getName();
        }

        return MFileFilter.photoLogFilter.removeExtension(logFiles.get(0).getName());
    }

    @Override
    protected Void call() throws Exception {
        DependencyInjector.getInstance().getInstanceOf(IProfilingManager.class).requestStarting(createDatasetRequest);
        doTransfer();
        // dont check here if legacy matching is loaded, since this will done delayed in the UI thread
        /*if (getLegacyMatching() == null && exception == null && !isCancelled()) {
            exception = new TaggingException("no dataset created", "target dataset cant be found");
        }*/

        if (exception != null) {
            exception.printStackTrace();
            throw exception;
        }

        return null;
    }

    @Override
    protected void cancelled() {
        Debug.printStackTrace("cancelled");
        matching.statusProperty().set(MatchingStatus.NEW);
        super.cancelled();
        fireEvent(FAIL_EVENT);
        DependencyInjector.getInstance().getInstanceOf(IProfilingManager.class).requestFinished(createDatasetRequest);
    }

    @Override
    protected void failed() {
        Dispatcher.postToUI(
            () -> {
                matching.statusProperty().set(MatchingStatus.NEW);
                applicationContext.addToast(
                    Toast.of(ToastType.ALERT).setText(getTaskException().getShortMessage()).create());
            });
        super.failed();
        fireEvent(FAIL_EVENT);
        DependencyInjector.getInstance().getInstanceOf(IProfilingManager.class).requestFinished(createDatasetRequest);
    }

    @Override
    protected void succeeded() {
        Toast.ToastBuilder toastBuilder =
            Toast.of(ToastType.INFO)
                .setText(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.transferCompleteMessage"));

        // if mission was closed in the meantime, dont show the action
        if (applicationContext.getCurrentMission() == mission) {
            toastBuilder =
                toastBuilder.setAction(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.transferActionLinkMessage"),
                    false,
                    true,
                    () -> {
                        selectionManager.setSelection(matching.getLegacyMatching());
                        mission.setCurrentMatching(matching);
                        navigationService.navigateTo(WorkflowStep.DATA_PREVIEW);
                        navigationService.navigateTo(SidePanePage.VIEW_DATASET);
                        mapView.goToSectorAsync(matching.getSector(), matching.getMaxElev());
                    },
                    Platform::runLater);
        }

        applicationContext.addToast(toastBuilder.create());

        super.succeeded();
        fireEvent(DONE_EVENT);
        DependencyInjector.getInstance().getInstanceOf(IProfilingManager.class).requestFinished(createDatasetRequest);
    }

    enum CreateDatasetSubTasks {
        START(10),
        PARSE_LOG_FILES(2000),
        COPY_FLIGHT_PLANS(300),
        COPY_LOG_FILES(300),
        COPY_AUX_FILE(300),
        COPY_IMAGES(60000),
        LOAD_IMAGES(30000),
        OPTIMIZE_DATASET(10000),
        CREATE_LAYERS(100),
        MOVE_UNMATCHED_IMAGES(1000),
        IMPORT_FLIGHT_PLANS(300),
        SAVE_LAYERS(300),
        ERASE_SD(300);

        final long duration;

        CreateDatasetSubTasks(long duration) { // expected rudation in ms
            this.duration = duration;
        }

    }

    private long[] progressOffset = new long[CreateDatasetSubTasks.values().length];

    private long maxValue = 0;

    private void updateProgressMessage(
            CreateDatasetSubTasks step, double subProgress, double subTotal, Object... msParams) {
        long progress;
        if (subTotal <= 0) {
            progress = progressOffset[step.ordinal()];
        } else {
            progress = Math.round(progressOffset[step.ordinal()] + step.duration * subProgress / subTotal);
        }

        updateProgress(progress, maxValue);
        updateMessage(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.analysis.CreateDatasetTask." + step.name(), msParams));
    }

    Toast transferInProgressToast;

    private void doTransfer() throws Exception {
        File folder = new File(MissionConstants.getMatchingsFolder(this.baseFolder), matchingName);
        if (folder.exists()) {
            final String KEY = "com.intel.missioncontrol.ui.analysis.CreateDatasetTask";
            DialogResult result =
                DependencyInjector.getInstance()
                    .getInstanceOf(IDialogService.class)
                    .requestCancelableConfirmation(
                        languageHelper.getString(KEY + ".datasetExists.title"),
                        languageHelper.getString(KEY + ".datasetExists.message"));
            LOGGER.info(languageHelper.getString(KEY + ".datasetExists.message") + " " + result + " " + folder);
            if (result == DialogResult.CANCEL) {
                exception = new TaggingException("Dataset creation canceled", "folder " + baseFolder);
                return;
            } else if (result == DialogResult.NO) {
                folder = FileHelper.getNextFreeFilename(folder);
            } else {
                mission.removeMatching(matching, folder);
            }
        }

        Dispatcher.postToUI(
            () -> {
                matching.statusProperty().set(MatchingStatus.TRANSFERRING);
            });
        transferInProgressToast =
            Toast.of(ToastType.INFO)
                .setText(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.transferInProgressMessage"))
                .setOnDismissed(t -> transferInProgressToast = null)
                .create();

        applicationContext.addToast(transferInProgressToast);

        int i = 0;
        maxValue = 0;
        for (CreateDatasetSubTasks task : CreateDatasetSubTasks.values()) {
            progressOffset[task.ordinal()] = maxValue;
            maxValue += task.duration;
        }

        updateProgressMessage(CreateDatasetSubTasks.START, 0, 1);

        final List<File> images = getPhotosRes.fotos;

        File baseFolder = folder;
        if (!baseFolder.exists() && !baseFolder.mkdirs()) {
            exception = new TaggingException("folder creation failed", "Could not create folder " + baseFolder);
            return;
        }

        boolean jsonLogs = false;

        // parsing logfile
        i = 0;
        int jsonCount = 0;

        for (File logFile : logFiles) {
            try {
                updateProgressMessage(
                    CreateDatasetSubTasks.PARSE_LOG_FILES, i, logFiles.size(), logFile.getName(), i, logFiles.size());
                i++;
                if (MFileFilter.photoJsonFilter.accept(logFile.getName())) {
                    jsonCount++;
                    jsonLogs = true;
                }

                if (jsonCount != i && jsonCount != 0) {
                    final String[] message =
                        new String[] {
                            "mixed log file types", "not able to process JSON and non JSON logs at the same time"
                        };
                    LOGGER.error("{}, {}", message[0], message[1]);
                    exception = new TaggingException(message[0], message[1]);
                    return;
                }

                taggingAlgorithm.loadLogfile(logFile, allowMultipleFeedbacks);
            } catch (Exception e) {
                exception = new TaggingException("parsing failed", "unable to parse logfile: " + logFile, e);
                return;
            }
        }

        if (taggingAlgorithm.getLogsAll().isEmpty()) {
            final String[] message = new String[] {"log data corrupted", "log data corrupted: no log entries found"};
            LOGGER.error("{}, {}", message[0], message[1]);
            exception = new TaggingException(message[0], message[1]);
            return;
        }

        if (taggingAlgorithm.getLogsAll().first().getTimestamp() <= 0) {
            CPhotoLogLine photoLogLine = taggingAlgorithm.getLogsAll().first();
            LOGGER.error("log data corrupted: log timestamps are zero: {}", photoLogLine);
            exception =
                new TaggingException(
                    "log data corrupted", "log data corrupted: log timestamps are zero:" + photoLogLine);
            return;
        }

        // filename for legacyMatching file
        final File save = new File(baseFolder, AMapLayerMatching.DEFAULT_FILENAME);
        if (save.exists() && !save.delete()) {
            exception = new TaggingException("delete data failed", "Could not delete old files " + save);
            return;
        }

        if (isCancelled()) {
            return;
        }

        // copy flightplans
        File fpFolder = new File(baseFolder, AMapLayerMatching.FOLDER_NAME_FLIGHTPLANS_SUBFOLDER);
        if (!fpFolder.exists() && !fpFolder.mkdirs()) {
            exception = new TaggingException("folder creation failed", "Could not create folder " + fpFolder);
            return;
        }

        try {
            i = 0;
            for (Flightplan source : flightPlans) {
                updateProgressMessage(
                    CreateDatasetSubTasks.COPY_LOG_FILES,
                    i,
                    flightPlans.size(),
                    source.getName(),
                    i,
                    flightPlans.size());
                i++;
                source.saveToLocation(new File(fpFolder, source.getName()));
                // FileHelper.copyFile(source, new File(fpFolder, source.getName()));
            }
        } catch (Exception e2) {
            exception = new TaggingException("Copy Flight Plan Failes", "Unable to save Flightplan to new folder", e2);
            return;
        }

        if (isCancelled()) {
            return;
        }

        // copy logfile if nessesary
        boolean deleteLogs = true;
        if (isAscTecMatching) {
            i = 0;
            for (File logFile : logFiles) {
                updateProgressMessage(
                    CreateDatasetSubTasks.COPY_LOG_FILES, i, logFiles.size(), logFile.getName(), i, logFiles.size());
                i++;
                if (logFile.getParentFile().equals(baseFolder)) {
                    deleteLogs = false;
                    continue;
                }

                File fTarget = new File(baseFolder, "asctec_log_" + logFile.getName());
                if (fTarget.equals(logFile)) {
                    deleteLogs = false;
                    continue;
                }

                fTarget = FileHelper.getNextFreeFilename(fTarget);
                try {
                    FileHelper.copyDirectorySynchron(logFile, fTarget);
                } catch (Exception e2) {
                    exception =
                        new TaggingException(
                            "unable to copy logs;" + logFile,
                            "Unable to copy asctec log folder " + logFile + " to " + fTarget,
                            e2);
                    return;
                }
            }
        } else {
            i = 0;
            for (File logFile : logFiles) {
                updateProgressMessage(
                    CreateDatasetSubTasks.COPY_LOG_FILES, i, logFiles.size(), logFile.getName(), i, logFiles.size());
                i++;
                if (!logFile.exists()) {
                    deleteLogs = false;
                    continue;
                }

                if (logFile.getName().startsWith(CPhotoLogLine.PREFIX_MATCHED_PLG)
                        && FileHelper.equals(logFile.getParentFile(), baseFolder)) {
                    deleteLogs = false;
                    continue;
                }

                File logFileNew = new File(baseFolder, CPhotoLogLine.PREFIX_MATCHED_PLG + logFile.getName());

                if (jsonLogs) {
                    // each image in a json log can be from a differnet folder
                    int baseSourcePathStringLen =
                        logFile.getParentFile().getParentFile().getAbsolutePath().length() + 1;

                    String name1 =
                        logFile.getAbsolutePath()
                            .substring(baseSourcePathStringLen)
                            .replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("_"));

                    logFileNew = new File(baseFolder, CPhotoLogLine.PREFIX_MATCHED_PLG + name1);
                }

                try {
                    FileHelper.copyFile(logFile, logFileNew);
                } catch (IOException e2) {
                    exception = new TaggingException("Unable to move images", "Unable to move Picture logfile", e2);
                    return;
                }
            }
        }

        if (isCancelled()) {
            return;
        }

        // if rededge dataset, copy metadata if possible
        final File picFolderParent = picFolder != null ? picFolder.getParentFile() : null;
        if (picFolderParent != null && picFolderParent.exists()) {
            File[] auxFiles =
                new File[] {new File(picFolderParent, "diag.dat"), new File(picFolderParent, "paramlog.dat")};

            i = 0;
            for (File fAux : auxFiles) {
                updateProgressMessage(
                    CreateDatasetSubTasks.COPY_AUX_FILE, i, auxFiles.length, fAux.getName(), i, auxFiles.length);
                i++;
                if (!fAux.exists()) {
                    continue;
                }

                try {
                    File fAuxTarget = new File(baseFolder, fAux.getName());
                    FileHelper.copyDirectorySynchron(fAux, fAuxTarget);
                } catch (IOException e2) {
                    exception =
                        new TaggingException("unable to copy aux file", "copying this file failed: " + fAux, e2);
                    return;
                }
            }
        }

        if (jsonLogs) {
            // copy potentially falcon payload logfiles for backup
            File[] auxFiles = new File[] {new File(picFolder, "logs"), new File(picFolder, "occupancy.map")};

            i = 0;
            for (File fAux : auxFiles) {
                updateProgressMessage(
                    CreateDatasetSubTasks.COPY_AUX_FILE, i, auxFiles.length, fAux.getName(), i, auxFiles.length);
                i++;
                if (!fAux.exists()) {
                    continue;
                }

                try {
                    File fAuxTarget = new File(baseFolder, fAux.getName());
                    FileHelper.copyDirectorySynchron(fAux, fAuxTarget);
                } catch (IOException e2) {
                    exception =
                        new TaggingException("unable to copy aux file", "copying this file failed: " + fAux, e2);
                    return;
                }
            }
        }

        if (isCancelled()) {
            return;
        }

        // if nessesary, copy images to new place, make sure that names become unique, since some compatibleCameraIds
        // are using different
        // folder to keep non unique file names
        boolean deletePics = true;
        File targetFolder = new File(baseFolder, AMapLayerMatching.FOLDER_NAME_PICS_SUBFOLDER);
        if (picFolder != null && !FileHelper.equals(targetFolder, picFolder)) {
            if (!targetFolder.exists() && !targetFolder.mkdirs()) {
                exception = new TaggingException("folder creation failed", "Could not create folder " + targetFolder);
                return;
            }

            File[] imageFiles = targetFolder.listFiles();
            List<File> fetchedFiles = imageFiles == null ? Collections.emptyList() : Arrays.asList(imageFiles);
            TreeSet<File> targetFiles = new TreeSet<>(fetchedFiles);
            try {
                int baseSourcePathStringLen = getPhotosRes.picFolder.getAbsolutePath().length() + 1;
                i = 0;
                for (File f : images) {
                    updateProgressMessage(
                        CreateDatasetSubTasks.COPY_IMAGES, i, images.size(), f.getName(), i, images.size());
                    i++;
                    if (isCancelled()) {
                        return;
                    }

                    if (f.isDirectory()) {
                        continue;
                    }

                    if (FileHelper.equals(targetFolder, f.getParentFile())) {
                        deletePics = false;
                        targetFiles.remove(f); // dont drop existing file
                        continue; // dont rename files if they dont have to be moved to another folder
                    }

                    if (jsonLogs) {
                        // each image in a json log can be from a differnet folder
                        baseSourcePathStringLen = f.getParentFile().getParentFile().getAbsolutePath().length() + 1;
                    }

                    String name1 =
                        f.getAbsolutePath()
                            .substring(baseSourcePathStringLen)
                            .replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("_"));
                    File targetFileTmp = new File(targetFolder.getAbsolutePath(), name1);

                    final File targetFile;
                    if (jsonLogs) {
                        targetFile = targetFileTmp;
                    } else {
                        final String imgNameInject =
                            "_" + Math.round(taggingAlgorithm.getLogsAll().first().getTimestamp() * 1000);

                        if (name1.contains(imgNameInject)) {
                            targetFile =
                                targetFileTmp; // dont copy all images again and again on second legacyMatching try
                        } else {
                            targetFile = FileHelper.injectIntoFileName(targetFileTmp, imgNameInject);
                        }
                    }

                    if (targetFile.exists() && f.length() == targetFile.length()) {
                        // dont overwrite existing file -> speed up
                        targetFiles.remove(targetFile); // make sure existing file is not dropped after end of this loop
                        continue;
                    }

                    FileHelper.copyFile(f, targetFile);
                    targetFiles.remove(targetFile);
                }

                // cleanup afterwards
                if (!targetFiles.isEmpty()) {
                    File photoParent = targetFiles.first().getParentFile();
                    File targetFolderUnmatched = new File(photoParent, TaggingAlgorithmA.UNMATCHED_FOLDER);
                    if (!targetFolderUnmatched.exists()) {
                        targetFolderUnmatched.mkdirs();
                    }

                    for (File unused : targetFiles) {
                        if(unused.isFile()) {
                            try {
                                Debug.getLog().fine("move unused target image: " + unused);
                                File unusedNew =
                                        FileHelper.getNextFreeFilename(new File(targetFolderUnmatched, unused.getName()));
                                FileHelper.move(unused, unusedNew);
                            } catch (Exception e) {
                                Debug.getLog().log(Level.WARNING, "Could not move image: " + unused);
                            }
                        }
                    }
                }

            } catch (Exception e1) {
                exception = new TaggingException("could not move images", "copying images into project failed", e1);
                return;
            }
        }

        if (isCancelled()) {
            return;
        }

        matching.startTransferring(baseFolder, hardwareConfiguration);

        if (jsonLogs) {
            i = 0;
            legacyMatching.getPicsLayer().setMute(true);
            for (File logFile : logFiles) {
                boolean fromMatchedData = logFile.getName().startsWith(CPhotoLogLine.PREFIX_MATCHED_PLG);

                try (InputStream stream = new FileInputStream(logFile);
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        updateProgressMessage(
                            CreateDatasetSubTasks.LOAD_IMAGES,
                            i,
                            taggingAlgorithm.getLogsAll().size(),
                            logFile.getName(),
                            i,
                            taggingAlgorithm.getLogsAll().size());
                        if (isCancelled()) {
                            return;
                        }

                        try {
                            strLine = strLine.trim();
                            if (strLine.startsWith("[")) {
                                strLine = strLine.substring(1, strLine.length());
                            }

                            if (strLine.endsWith(",") || strLine.endsWith("]")) {
                                strLine = strLine.substring(0, strLine.length() - 1);
                            }

                            strLine = strLine.trim();
                            if (strLine.isEmpty()) {
                                continue;
                            }

                            Geotag geotag = Geotag.fromJson(strLine);
                            CPhotoLogLine plg = new CPhotoLogLine(geotag);

                            int baseSourcePathStringLen =
                                logFile.getParentFile().getParentFile().getAbsolutePath().length() + 1;
                            String name1 =
                                logFile.getParentFile()
                                        .getAbsolutePath()
                                        .substring(baseSourcePathStringLen)
                                        .replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("_"))
                                    + Matcher.quoteReplacement("_")
                                    + geotag.filename;

                            File image = new File(targetFolder.getAbsolutePath(), name1);

                            /*if (!fromMatchedData) {
                                final String imgNameInject = logFile.getParentFile().getName();
                                image = FileHelper.injectIntoFileName(image, imgNameInject);
                            }*/

                            PhotoCube photoCube = new PhotoCube(image);
                            MapLayerMatch match = new MapLayerMatch(photoCube, plg, legacyMatching);
                            photoCube.setMatch(match);
                            legacyMatching.getPicsLayer().addMapLayer(match);
                            match.generatePreview();
                        } catch (Throwable e1) {
                            exception =
                                new TaggingException(
                                    "cant add image to dataset", "can't load single legacyMatching", e1);
                            return;
                        }

                        i++;
                    }
                } catch (Exception e) {
                    exception = new TaggingException("log parser err", "can't load parse logfile " + logFile, e);
                    return;
                }
            }

            legacyMatching.getPicsLayer().setMute(false);
        } else {
            IGenericCameraConfiguration cameraConfiguration =
                hardwareConfiguration.getPrimaryPayload(IGenericCameraConfiguration.class);
            IGenericCameraDescription cameraDescription = cameraConfiguration.getDescription();
            ILensDescription lensDescription = cameraConfiguration.getLens().getDescription();
            IPlatformDescription platformDescription = hardwareConfiguration.getPlatformDescription();

            ITaggingAlgorithm.ProgressCallbackImgLoading progressCallbackImgLoading =
                new ITaggingAlgorithm.ProgressCallbackImgLoading() {

                    @Override
                    public void progress(File image, long no, long total) {
                        updateProgressMessage(CreateDatasetSubTasks.LOAD_IMAGES, no, total, image.getName(), no, total);
                    }

                    @Override
                    public boolean isCanceled() {
                        return CreateDatasetTask.this.isCancelled();
                    }
                };

            try {
                taggingAlgorithm.loadPictures(
                    targetFolder, true, cameraDescription.getBandNamesSplit().length, true, progressCallbackImgLoading);
            } catch (InterruptedByUserException e) {
                return;
            } catch (Throwable e1) {
                exception = new TaggingException("Unable to load images", "Unable to load images", e1);
                return;
            }

            if (isCancelled()) {
                return;
            }

            ITaggingAlgorithm.ProgressCallbackOptimizing progressCallbackOptimizing =
                new ITaggingAlgorithm.ProgressCallbackOptimizing() {
                    @Override
                    public void progress(String algName, long no, long total) {
                        updateProgressMessage(CreateDatasetSubTasks.OPTIMIZE_DATASET, no, total, algName, no, total);
                    }

                    @Override
                    public boolean isCanceled() {
                        return CreateDatasetTask.this.isCancelled();
                    }
                };

            try {
                if (!taggingAlgorithm.getPhotosAll().isEmpty()) {
                    taggingAlgorithm.optimizeMatching(
                        lensDescription.getMaxTimeVariation().convertTo(Unit.SECOND).getValue().doubleValue(),
                        platformDescription.getGpsType(),
                        null,
                        progressCallbackOptimizing);
                } else {
                    int k = 0;
                    for (CPhotoLogLine log : taggingAlgorithm.getLogsAll()) {
                        k++;
                        File dummyFile = new File(targetFolder, "dummyImage_" + k + ".png");
                        FileHelper.writeResourceToFile("com/intel/missioncontrol/gfx/dummyImage.jpg", dummyFile);
                        ExifInfos exifInfos = new ExifInfos();
                        exifInfos.timestamp = log.getTimestamp();
                        PhotoCube photo = new PhotoCube(dummyFile, exifInfos);
                        photo.logTmp = log;
                        taggingAlgorithm.getPhotosMatched().add(photo);
                    }
                }
            } catch (InterruptedByUserException e) {
                return;
            } catch (TaggingException e) {
                exception = e;
                return;
            } catch (Throwable e1) {
                exception =
                    new TaggingException("Can't geotag data", "failed to find match between metadata and images", e1);
                return;
            }

            if (isCancelled()) {
                return;
            }

            try {
                legacyMatching.setRTKAvaiable(taggingAlgorithm);
                legacyMatching.setBandNames(cameraDescription.getBandNamesSplit());
                i = 0;
                for (PhotoCube photoCube : taggingAlgorithm.getPhotosMatched()) {
                    updateProgressMessage(
                        CreateDatasetSubTasks.CREATE_LAYERS,
                        i,
                        taggingAlgorithm.getPhotosMatched().size(),
                        i,
                        taggingAlgorithm.getPhotosMatched().size());
                    i++;
                    if (isCancelled()) {
                        return;
                    }

                    if (photoCube.logTmp == null) {
                        continue;
                    }

                    try {
                        MapLayerMatch match = new MapLayerMatch(photoCube, photoCube.logTmp, legacyMatching);
                        photoCube.setMatch(match);
                        legacyMatching.getPicsLayer().addMapLayer(match);

                    } catch (Throwable e1) {
                        exception =
                            new TaggingException("cant add image to dataset", "can't load single legacyMatching", e1);
                        return;
                    }
                }

                if (isCancelled()) {
                    return;
                }

                // if nessesary, move unmatchable images to new place
                try {
                    if (taggingAlgorithm.getPhotosUnmatched().size() > 0) {
                        i = -1;
                        for (PhotoCube photoCube : taggingAlgorithm.getPhotosUnmatched()) {
                            i++;
                            for (PhotoFile photo : photoCube) {
                                try {
                                    File photoParent = photo.getFile().getParentFile();

                                    updateProgressMessage(
                                        CreateDatasetSubTasks.MOVE_UNMATCHED_IMAGES,
                                        i,
                                        taggingAlgorithm.getPhotosUnmatched().size(),
                                        photoParent.getName(),
                                        i,
                                        taggingAlgorithm.getPhotosUnmatched().size());
                                    // System.out.println("file:"+file);
                                    if (photoParent.getName().equalsIgnoreCase(TaggingAlgorithmA.UNMATCHED_FOLDER)) {
                                        continue;
                                    }

                                    File targetFolderUnmatched =
                                        new File(photoParent, TaggingAlgorithmA.UNMATCHED_FOLDER);
                                    targetFolderUnmatched.mkdirs();
                                    FileHelper.move(
                                        photo.getFile(), new File(targetFolderUnmatched, photo.getFile().getName()));

                                } catch (Throwable e1) {
                                    exception =
                                        new TaggingException(
                                            "image move failed", "can't move unmatchable image:" + photo.getFile(), e1);
                                    return;
                                }
                            }
                        }
                    }
                } catch (Exception e1) {
                    exception = new TaggingException("image move failed", "can't move unmatchable images", e1);
                    return;
                }

                // create sync statistics and write it to a file. in user level==Debugging, show the window
                Dispatcher.postToUI(
                    () -> {
                        new TaggingSyncVisWindow(
                            taggingAlgorithm,
                            new File(baseFolder, "syncPattern-" + System.currentTimeMillis() + ".png"),
                            generalSettings.getOperationLevel() == OperationLevel.DEBUG,
                            cameraConfiguration,
                            hardwareConfiguration);
                    });

                if (isCancelled()) {
                    return;
                }
            } catch (Throwable e) {
                exception = new TaggingException("finalization failed", "can't initialize final dataset", e);
                return;
            }
        }

        // import s from flightplans
        i = 0;
        for (Flightplan fp : flightPlans) {
            updateProgressMessage(
                CreateDatasetSubTasks.IMPORT_FLIGHT_PLANS, i, flightPlans.size(), fp.getName(), i, flightPlans.size());
            i++;
            if (isCancelled()) {
                return;
            }

            legacyMatching.getPicAreasLayer().tryAddPicAreasFromFlightplan(fp);
        }

        if (isCancelled()) {
            return;
        }

        updateProgressMessage(CreateDatasetSubTasks.SAVE_LAYERS, 0, 0);
        legacyMatching.getCoverage().updateCameraCorners();

        Dispatcher.postToUI(
            () -> {
                // dont do the preview generation whith the layer above, because it is only a temporary one for
                // generating the settings file!
                this.legacyMatching.guessGoodFilters();
                this.legacyMatching.setVisible(true);
                this.legacyMatching.getPicAreasLayer().setVisible(true);
                this.legacyMatching.mapLayerVisibilityChanged(this.legacyMatching, true);
                matching.saveResourceFile();
            });

        if (eraseLogsAfterCopying) {
            if (deletePics) {
                i = 0;
                for (File f : getPhotosRes.fotos) {
                    updateProgressMessage(
                        CreateDatasetSubTasks.ERASE_SD,
                        i,
                        getPhotosRes.fotos.size(),
                        f.getName(),
                        i,
                        getPhotosRes.fotos.size());
                    try {
                        FileHelper.deleteDir(languageHelper, f.getParentFile(), true);
                    } catch (Exception e) {
                        Debug.getLog().log(Level.INFO, "issues deleting file", e);
                    }
                }
            }

            if (deleteLogs) {
                if (jsonLogs) {
                    for (File f : logFiles) {
                        updateProgressMessage(
                            CreateDatasetSubTasks.ERASE_SD, i, logFiles.size(), f.getName(), i, logFiles.size());
                        try {
                            FileHelper.deleteDir(languageHelper, f.getParentFile(), true);
                        } catch (Exception e) {
                            Debug.getLog().log(Level.INFO, "issues deleting file", e);
                        }
                    }
                } else {
                    for (File f : logFiles) {
                        updateProgressMessage(
                            CreateDatasetSubTasks.ERASE_SD, i, logFiles.size(), f.getName(), i, logFiles.size());
                        try {
                            FileHelper.deleteDir(languageHelper, f, true);
                        } catch (Exception e) {
                            Debug.getLog().log(Level.INFO, "issues deleting file", e);
                        }
                    }
                }
            }
        }
    }

    public MapLayerMatching getLegacyMatching() {
        return legacyMatching;
    }

    public TaggingException getTaskException() {
        return exception;
    }

}
