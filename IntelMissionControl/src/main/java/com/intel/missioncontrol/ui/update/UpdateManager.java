/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.update;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.InternetConnectivitySettings;
import com.intel.missioncontrol.settings.UpdateSettings;
import com.intel.missioncontrol.ui.MainViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.utils.DefaultBackgroundTaskManager;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import com.intel.missioncontrol.utils.IVersionProvider;
import com.intel.missioncontrol.utils.SubProcessHelper;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.Licence;
import eu.mavinci.core.update.EnumUpdateTargets;
import eu.mavinci.core.update.UpdateURL;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.main.core.Application;
import eu.mavinci.desktop.main.core.IAppListener;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.FileChooser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.asyncfx.concurrent.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by bulatnikov on 7/19/17. */
public class UpdateManager implements IUpdateManager {

    private static final Logger log = LoggerFactory.getLogger(UpdateManager.class);

    private static final String UPD_TARGET_KEY_BASE = "com.intel.missioncontrol.ui.update.UpdateManager.update.target.";

    private final ILanguageHelper languageHelper;
    private final UpdateSettings updateSettings;
    private final IDialogService dialogService;
    private final InternetConnectivitySettings internetConnectivitySettings;
    private final IBackgroundTaskManager backgroundTaskManager;
    private final INetworkInformation networkInformation;
    private final GeneralSettings generalSettings;
    private final IApplicationContext applicationContext;
    private final IPathProvider pathProvider;
    private final ILicenceManager licenceManager;
    private final IVersionProvider versionProvider;
    private final String currentTime;

    private long currentVersion;
    private String currentMajor;
    private String currentAppVersion;
    private String currentGitBranch;
    private String buildCommitId;
    private String applicationName;
    private String build;

    OkHttpClient client =
        new OkHttpClient.Builder()
            .retryOnConnectionFailure(true) // with false here, connection with intel proxy will always fail.. WTF!?!
            .connectTimeout(2500, TimeUnit.MILLISECONDS)
            .writeTimeout(3000, TimeUnit.MILLISECONDS)
            .readTimeout(3000, TimeUnit.MILLISECONDS)
            .build();

    private Map<EnumUpdateTargets, AvailableUpdate> availableUpdatesMap = new EnumMap<>(EnumUpdateTargets.class);
    private Collection<Response> activeDownloadResponses = Collections.synchronizedCollection(new ArrayList<>());
    private Map<EnumUpdateTargets, String> updatesForInstall = new EnumMap<>(EnumUpdateTargets.class);

    private MainViewModel mainViewModel;

    boolean autoCheckedOnce;

    IAppListener appListener =
        new IAppListener() {
            @Override
            public boolean appRequestClosing() {
                return true;
            }

            @Override
            public void appIsClosing() {}

            @Override
            public void guiReadyLoaded() {
                networkInformation
                    .internetAvailableProperty()
                    .addListener(
                        (observable, oldValue, newValue) -> {
                            if (!newValue) {
                                return;
                            }

                            checkForUpdates();
                        });

                if (generalSettings.softwareUpdateEnabledProperty().get()
                        && networkInformation.networkAvailableProperty().get()
                        && networkInformation.internetAvailableProperty().get()) {
                    checkForUpdates();
                }
            }

        };

    @Inject
    public UpdateManager(
            IPathProvider pathProvider,
            IVersionProvider versionProvider,
            ILanguageHelper languageHelper,
            ISettingsManager settingsManager,
            IDialogService dialogService,
            IBackgroundTaskManager backgroundTaskManager,
            INetworkInformation networkInformation,
            IApplicationContext applicationContext,
            ILicenceManager licenceManager) {
        this.pathProvider = pathProvider;
        this.applicationContext = applicationContext;
        this.networkInformation = networkInformation;
        internetConnectivitySettings = settingsManager.getSection(InternetConnectivitySettings.class);

        this.languageHelper = languageHelper;
        this.dialogService = dialogService;
        this.backgroundTaskManager = backgroundTaskManager;
        this.updateSettings = settingsManager.getSection(UpdateSettings.class);
        this.licenceManager = licenceManager;
        this.versionProvider = versionProvider;

        applicationName = versionProvider.getApplicationName();
        currentMajor = versionProvider.getAppMajorVersion();
        currentAppVersion = versionProvider.getAppVersion();
        currentGitBranch = versionProvider.getGitBranch();
        buildCommitId = versionProvider.getBuildCommitId();
        currentTime = versionProvider.getBuildCommitTimeAsString();
        currentVersion = versionProvider.getBuildCommitTimeAsLong();
        build = currentTime;
        generalSettings = settingsManager.getSection(GeneralSettings.class);
        log.info(
            applicationName + " Version: " + getCurrentFullVersion() + " " + currentGitBranch + " " + buildCommitId);
        Application.addApplicationListener(appListener);
    }

    private void checkForUpdates() {
        if (autoCheckedOnce) {
            return;
        }

        autoCheckedOnce = true;

        // by sheduling on UI, we make sure this is only starting after UI has been loaded:
        Dispatcher platform = Dispatcher.platform();
        platform.runLater(
            () -> {
                // anyway, the update check has to happen in a background thread
                Dispatcher background = Dispatcher.background();
                background.run(
                    () -> {
                        if (isAnyUpdateAvailable()) {
                            // and the window again has to appear on UI thread
                            Dispatcher.platform().runLater(this::showDialogInternal);
                        }
                    });
            },
            Duration.ofSeconds(10));
    }

    @Override
    public Map<EnumUpdateTargets, AvailableUpdate> getAvailableUpdatesMap() {
        return availableUpdatesMap;
    }

    @Override
    public String getCurrentFullVersion() {
        return UpdateURL.getHumanReadableVersion(currentMajor, currentVersion);
    }

    @Override
    public void runUpdate(EnumUpdateTargets target) {
        backgroundTaskManager.submitTask(new UpdateDownloadTask(target));
    }

    @Override
    public SimpleObjectProperty<LocalDateTime> lastCheckedDateTimeProperty() {
        return updateSettings.lastCheckedDatePropertry();
    }

    @Override
    public void setMainViewModel(MainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    protected HttpUriRequest prepareRequest(URL updateUrl, boolean isSecured) {
        return RequestBuilder.get(String.valueOf(updateUrl)).build();
    }

    private void download(
            Consumer<IBackgroundTaskManager.ProgressStageFirer> progressUpdater, EnumUpdateTargets target) {
        try {
            long version = getVersionForTarget(target);
            Licence licence = licenceManager.getActiveLicence();
            URL updateUrl =
                UpdateURL.getUpdateURLFile(
                    licence == null ? null : licence.getLicenceId(), target, currentMajor, version);
            Request request = new Request.Builder().url(updateUrl).build();
            try (Response response = client.newCall(request).execute()) {
                String filename = response.header("Content-Disposition");
                filename = filename.replaceFirst("(?i)^.*filename=\"?([^\"]+)\"?.*$", "$1");
                filename = URLDecoder.decode(filename, StandardCharsets.ISO_8859_1);

                String downloadFolderPath = FileHelper.getDownloadsFolder().getCanonicalPath();
                if (downloadFolderPath.charAt(downloadFolderPath.length() - 1) != File.separatorChar) {
                    downloadFolderPath += File.separator;
                }

                String filePathName = downloadFolderPath + filename;
                File file = new File(filePathName);
                long contentLength = response.body().contentLength();
                DownloadedUpdate downloadedUpdate = updateSettings.downloadedFilesProperty().get(target);
                if (downloadedUpdate != null && Files.exists(Paths.get(downloadedUpdate.getFilePath()))) {
                    // TODO-checksum: this check should be replaced with check based on checksum
                    // when checksum will be available
                    if (downloadedUpdate.getFileSize() == contentLength
                            && downloadedUpdate.getFileName().equals(filename)
                            && downloadedUpdate.getSerialId().equals(licence.getLicenceId())) {
                        downloadComplete(target, file);
                        return;
                    }
                }

                activeDownloadResponses.add(response);
                final AtomicBoolean interrupted = new AtomicBoolean(false);

                Dispatcher.background()
                    .getLaterAsync(
                        () -> {
                            try {
                                log.debug("Downloading started from URL: {}", String.valueOf(updateUrl));
                                Files.copy(
                                    response.body().byteStream(),
                                    Paths.get(filePathName),
                                    StandardCopyOption.REPLACE_EXISTING);
                                log.debug("Download complete from URL: {}", String.valueOf(updateUrl));
                            } catch (IOException e) {
                                interrupted.compareAndSet(false, true);
                                activeDownloadResponses.remove(response);
                                log.debug("Download interrupted from URL: {}", String.valueOf(updateUrl));
                            }

                            return null;
                        });

                IBackgroundTaskManager.ProgressStageFirer progressStageFirer =
                    new IBackgroundTaskManager.ProgressStageFirer(contentLength);
                do {
                    progressStageFirer.setCurrentStage(file.length());
                    progressUpdater.accept(progressStageFirer);
                } while (file.length() < contentLength && !interrupted.get());
                if (!interrupted.get()) {
                    activeDownloadResponses.remove(response);
                    downloadComplete(target, file);
                }
            }
        } catch (RuntimeException e) {
            handleException(e);
            throw e;
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void downloadComplete(EnumUpdateTargets target, File file) throws Exception {
        switch (target) {
        case GUI:
            saveUpdate(target, file);
            break;
        case LICENCE:
            installLicense(file);
            break;
        default:
            log.warn("Unsupported update received with type: " + target);
        }

        if (activeDownloadResponses.isEmpty()) {
            Dispatcher.platform().runLater(this::installUpdates);
        }
    }

    private void installLicense(File file) throws Exception {
        IBackgroundTaskManager.BackgroundTask applyLicenseTask =
            new IBackgroundTaskManager.BackgroundTask(
                languageHelper.getString("com.intel.missioncontrol.ui.update.UpdateManager.update.license.install")) {
                @Override
                protected Void call() throws Exception {
                    licenceManager.registerLicence(file);
                    availableUpdatesMap.remove(EnumUpdateTargets.LICENCE);
                    return null;
                }
            };
        // User will be notified that license update was applied when job succeeds
        backgroundTaskManager.submitTask(applyLicenseTask);
    }

    private void saveUpdate(EnumUpdateTargets updateKind, File file) {
        System.out.println("[update_debug]f:" + file);

        Licence licence = licenceManager.getActiveLicence();
        String licenceID = licence == null ? null : licence.getLicenceId();
        File folder = createTargetDir(updateKind, licenceID);
        File udpateFile = new File(folder, file.getName());
        try {
            updatesForInstall.put(updateKind, udpateFile.getAbsolutePath());
            if (udpateFile.exists()) {
                return;
            }

            FileHelper.move(file, udpateFile);
            if (!file.delete()) {
                Debug.getLog().log(Level.WARNING, "Could not delete installer file from temporary folder");
            }

            updateSettings
                .downloadedFilesProperty()
                .put(
                    updateKind,
                    new DownloadedUpdate(
                        licenceID,
                        udpateFile.getName(),
                        udpateFile.length(),
                        udpateFile.getAbsolutePath(),
                        "")); // TODO-checksum: change when checksum will be available
        } catch (IOException e) {
            Debug.getLog().log(Level.WARNING, "Could not copy installer file from temporary to updates folder", e);
        }
    }

    private void installGuiUpdate() {
        String updateFilePath = updatesForInstall.get(EnumUpdateTargets.GUI);
        if (updateFilePath == null) {
            return;
        }

        File guiUpdateFile = new File(updateFilePath);
        Dispatcher.platform().runLater(() -> openFileAfterAppCloseConfirmed(guiUpdateFile));
    }

    private boolean openFileAfterAppCloseConfirmed(File file) {
        try {
            if (file == null) {
                return false;
            }

            if (applicationContext.checkDroneConnected(false)) {
                applicationContext.checkDroneConnected(true);
                log.error("AvailableUpdate canceled!");
                return false; // TODO as long as not waiting
            } else {
                Debug.getLog().config("starting local update-before close request:" + file);
                if (isAppClosingConfirmed()) {
                    if (!((DefaultBackgroundTaskManager)backgroundTaskManager).canCloseApplication(true)) {
                        log.error("AvailableUpdate canceled!");
                        return false;
                    }

                    if (!applicationContext.askUserForMissionSave()) {
                        log.error("AvailableUpdate canceled!");
                        return false;
                    }

                    if (!Application.closeAppRequest()) {
                        log.error("AvailableUpdate failed! Failed to close application");
                        return false;
                    }

                    // will wait until update start
                    if (!executeUpdateFile(file.getCanonicalPath())) {
                        dialogService.showErrorMessage(
                            languageHelper.getString(
                                "com.intel.missioncontrol.ui.update.UpdateManager.run.update.failed.dlg.title"),
                            languageHelper.getString(
                                "com.intel.missioncontrol.ui.update.UpdateManager.run.update.failed.dlg.info"));
                        return false;
                    } else {
                        WindowHelper.closePrimaryStage();
                        return true;
                    }
                } else {
                    return false;
                }
            }
        } catch (IOException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public void installUpdates() {
        installGuiUpdate();
    }

    @Override
    public void stopDownloads() {
        log.debug("Stopping downloads");
        Dispatcher.background()
            .getLaterAsync(
                () -> {
                    activeDownloadResponses.forEach(
                        response -> {
                            try {
                                response.close();
                            } catch (Exception e) {
                                handleException(e);
                            }
                        });
                    return null;
                });
    }

    private void showDialogInternal() {
        dialogService.requestDialogAndWait(mainViewModel, UpdateViewModel.class);
    }

    @Override
    public void showDialog() {
        if (isAnyUpdateAvailable()) {
            dialogService.requestDialogAndWait(mainViewModel, UpdateViewModel.class);
        }
    }

    @Override
    public void showDialogNow() {
        dialogService.requestDialogAndWait(mainViewModel, UpdateViewModel.class);
    }

    @Override
    public void skipVersion(EnumUpdateTargets target) {
        AvailableUpdate targetAvailable = availableUpdatesMap.get(target);
        Expect.notNull(targetAvailable, "targetAvailable");
        updateSettings.skippedVersionsProperty().put(target, targetAvailable.getRevision());
        availableUpdatesMap.remove(target);
    }

    @Override
    public void showRevertDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(
            languageHelper.getString("com.intel.missioncontrol.ui.update.UpdateManager.revertDialog.title"));
        fileChooser.setInitialDirectory(constructSelectDir());
        fileChooser
            .getExtensionFilters()
            .addAll(
                new FileChooser.ExtensionFilter(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.update.UpdateManager.revertDialog.fileExtension"),
                    versionProvider.getSystem().isMac() ? "*.dmg" : "*.exe"),
                new FileChooser.ExtensionFilter(
                    languageHelper.getString("com.intel.missioncontrol.ui.update.UpdateManager.revertDialog.allFiles"),
                    "*.*"));

        File file = fileChooser.showOpenDialog(WindowHelper.getPrimaryStage());
        if (file != null) {
            openFileAfterAppCloseConfirmed(file);
        }
    }

    private File constructSelectDir() {
        File guiUpdatesDir =
            new File(
                pathProvider.getUpdatesDirectory().toFile().getPath()
                    + File.separator
                    + EnumUpdateTargets.GUI
                    + File.separator
                    + licenceManager.getActiveLicence().getLicenceId());

        return guiUpdatesDir.exists() ? guiUpdatesDir : FileHelper.getDownloadsFolder();
    }

    @Override
    public String getCurrentMajor() {
        return currentMajor;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public String getBuildNumber() {
        return build;
    }

    @Override
    public String getCurrentGitBranch() {
        return currentGitBranch;
    }

    @Override
    public String getBuildCommitId() {
        return buildCommitId;
    }

    @Override
    public boolean isAnyUpdateAvailable() {
        if (licenceManager.getActiveLicence() != null && licenceManager.getActiveLicence().getLicenceId() != null) {
            checkAvailableUpdates();
        } else {
            log.error("License is empty. Probably we don't have any or application is not initialized");
        }

        return !availableUpdatesMap.isEmpty();
    }

    /**
     * Not checks for available updates on update server it just use results of previously calls to
     * com.intel.missioncontrol.ui.update.UpdateManager#checkAvailableUpdates() to avoid cyclic calls from listeners
     * that triggered by this method.
     *
     * @param target to check in availableUpdatesMap
     * @return true if specified update is available
     */
    @Override
    public boolean isUpdateAvailable(EnumUpdateTargets target) {
        return availableUpdatesMap.get(target) != null;
    }

    private File createTargetDir(EnumUpdateTargets updateKind) {
        File f = new File(pathProvider.getUpdatesDirectory().toFile().getAbsolutePath(), updateKind.toString());
        return makeDirs(f);
    }

    private File createTargetDir(EnumUpdateTargets updateKind, String licenseId) {
        File f = createTargetDir(updateKind);
        if (licenseId != null) {
            f = new File(f.getAbsolutePath(), FileHelper.urlToFileName(licenseId));
        }

        return makeDirs(f);
    }

    private File makeDirs(File f) {
        try {
            if (!f.mkdirs()) {
                log.debug("Updates folder already exist");
            }

            return f;
        } catch (SecurityException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private void checkAvailableUpdates() {
        availableUpdatesMap.clear();
        Stream.of(new EnumUpdateTargets[] {EnumUpdateTargets.GUI, EnumUpdateTargets.LICENCE})
            .forEach(
                target -> {
                    if (target == EnumUpdateTargets.LICENCE && licenceManager.getActiveLicence().isBuildInLicence()) {
                        return; // dont check for updates for build in licence
                    }

                    Long version = getVersionForTarget(target);
                    URL updateUrl = null;
                    try {
                        Long revision;
                        updateUrl =
                            UpdateURL.getUpdateURLFile(
                                licenceManager.getActiveLicence().getLicenceId(), target, currentMajor, version);
                        String revisionString =
                            getFromURL(
                                UpdateURL.getUpdateURLRevision(
                                    licenceManager.getActiveLicence().getLicenceId(), target, currentMajor, version));
                        revision = Long.parseLong(revisionString);

                        Long skippedVersion =
                            MoreObjects.firstNonNull(
                                updateSettings.skippedVersionsProperty().get(target), Long.valueOf(0));

                        if (revision > version && revision > skippedVersion) {
                            String majorVersion =
                                getFromURL(
                                    UpdateURL.getUpdateURLMajorVersion(
                                        licenceManager.getActiveLicence().getLicenceId(),
                                        target,
                                        currentMajor,
                                        version));
                            availableUpdatesMap.put(
                                target,
                                AvailableUpdate.of(
                                    revision, UpdateURL.getHumanReadableVersion(majorVersion, revision)));
                        }

                    } catch (Exception e) {
                        if (updateUrl != null) {
                            String updateUrlStr = updateUrl.toString();
                            Toast toast =
                                Toast.of(ToastType.INFO)
                                    .setText(
                                        languageHelper.getString(
                                            "com.intel.missioncontrol.ui.update.UpdateManager.updateCheckFailed",
                                            languageHelper.getString(UPD_TARGET_KEY_BASE + target)))
                                    .setAction(
                                        languageHelper.getString(
                                            "com.intel.missioncontrol.ui.update.UpdateManager.checkManually"),
                                        false,
                                        true,
                                        () -> {
                                            try {
                                                FileHelper.openFileOrURL(updateUrlStr);
                                            } catch (IOException e1) {
                                                e1.printStackTrace();
                                            }
                                        },
                                        MoreExecutors.directExecutor())
                                    .setTimeout(Toast.LONG_TIMEOUT)
                                    .setCloseable(true)
                                    .create();
                            applicationContext.addToast(toast);
                        }

                        handleException(e);
                    }
                });

        updateSettings.lastCheckedDatePropertry().set(LocalDateTime.now());
    }

    private long getVersionForTarget(EnumUpdateTargets target) {
        switch (target) {
        case LICENCE:
            return licenceManager.getActiveLicence().getSVNrevision();
        default:
            return this.currentVersion;
        }
    }

    public LocalDateTime getLastCheckedDateTime() {
        return updateSettings.lastCheckedDatePropertry().get();
    }

    private String getFromURL(URL url) throws IOException {
        if (Platform.isFxApplicationThread()) {
            log.warn(
                "never check for updates onUI thread!!", new Exception("never make network calls on the UI thread"));
        }

        log.debug("Requesting URL: {}", String.valueOf(url));
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().string().trim();
    }

    private void handleException(Exception ex) {
        String message = ex.getMessage();
        if (Strings.isNullOrEmpty(message)) {
            message =
                String.format("Class %s: The %s was thrown", getClass().getSimpleName(), ex.getClass().getSimpleName());
        }

        log.error(message, ex);
    }

    private boolean isAppClosingConfirmed() {
        return this.dialogService.requestConfirmation(
            languageHelper.getString("com.intel.missioncontrol.ui.update.UpdateManager.closeAppDialog.title"),
            languageHelper.getString("com.intel.missioncontrol.ui.update.UpdateManager.closeAppDialog.prompt"));
    }

    private boolean executeUpdateFile(String path) {
        try {
            Process updateProcess = SubProcessHelper.executeFile(path);

            while (updateProcess.isAlive()) {
                Thread.sleep(1000);
                log.debug("Wait for update to start before exit.");
            }

            if (updateProcess.exitValue() == 0) {
                log.debug("Update started. Exit application.");
            } else {
                log.error("Update execution failed with exit value: " + updateProcess.exitValue());
                return false;
            }

            return true;
        } catch (IOException | InterruptedException | UnsupportedOperationException e) {
            handleException(e);
            return false;
        }
    }

    private class UpdateDownloadTask extends IBackgroundTaskManager.BackgroundTask {
        private static final String UPDATE_DOWNLOAD_TASK_NAME_KEY =
            "com.intel.missioncontrol.ui.update.UpdateManager.download.task.name";
        private static final String DOWNLOAD_TASK_MESSAGE_KEY =
            "com.intel.missioncontrol.ui.update.UpdateManager.download.task.message";

        EnumUpdateTargets target;

        UpdateDownloadTask(EnumUpdateTargets target) {
            super(
                languageHelper.getString(
                    UPDATE_DOWNLOAD_TASK_NAME_KEY, languageHelper.getString(UPD_TARGET_KEY_BASE + target)));
            this.target = target;
            this.setOnCancelled((event -> stopDownloads()));
            this.setOnFailed((event) -> stopDownloads());
        }

        @Override
        protected Void call() throws Exception {
            download(
                progressStageFirer -> {
                    long workDone = progressStageFirer.getCurrentStage();
                    long total = progressStageFirer.getStagesCount();
                    updateProgress(workDone, total);
                    updateMessage(
                        languageHelper.getString(
                            DOWNLOAD_TASK_MESSAGE_KEY,
                            languageHelper.getString(UPD_TARGET_KEY_BASE + target),
                            workDone / (double)total * 100));
                },
                target);
            return null;
        }
    }
}
