/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.datatransfer.popup;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.mission.Drone;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionConstants;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.ProgressTask;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.analysis.AnalysisSettings;
import com.intel.missioncontrol.ui.sidepane.analysis.IMatchingService;
import com.intel.missioncontrol.ui.sidepane.analysis.MatchingService;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.InjectScope;
import eu.mavinci.desktop.helper.FileHelper;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.concurrent.Dispatcher;

/** Created by eivanchenko on 9/8/2017. */
@SuppressLinter(value = "IllegalViewModelMethod", reviewer = "mstrauss", justification = "legacy file")
public class TransferDataPopupViewModel extends ViewModelBase {

    public static final String SAMPLE_DOWNLOAD_COMPLETE_TITLE =
        "com.intel.missioncontrol.ui.analysis.datatransfer.popup.DataTransferPopupView.downloadCompleteTitle";
    public static final String SAMPLE_DOWNLOAD_COMPLETE_MESSAGE =
        "com.intel.missioncontrol.ui.analysis.datatransfer.popup.DataTransferPopupView.downloadCompleteMessage";

    private static final String CHOOSE_MATCHING_DIALOG_HEADER =
        "com.intel.missioncontrol.ui.analysis.datatransfer.popup.DataTransferPopupView.chooseMatchingSample";

    private static final String CHOOSE_MATCHING_DIALOG_DOWNLOAD =
        "com.intel.missioncontrol.ui.analysis.datatransfer.popup.DataTransferPopupView.download";

    private static final String SAMPLE_DOWNLOAD_STARTED =
        "com.intel.missioncontrol.ui.analysis.datatransfer.popup.DataTransferPopupView.downloadStarted";

    private static final String SAMPLE_DOWNLOAD_CANCELED =
        "com.intel.missioncontrol.ui.analysis.datatransfer.popup.DataTransferPopupView.downloadCanceled";

    @InjectScope
    private MainScope mainScope;

    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final ILanguageHelper languageHelper;
    private final IHardwareConfigurationManager hardwareConfigurationManager;

    @Inject
    private IMatchingService matchingService;

    @Inject
    protected IBackgroundTaskManager taskManager;

    @Inject
    private IDialogService dialogService;

    @Inject
    private IMissionManager missionManager;

    private AnalysisSettings settings;
    private ObjectProperty<Drone> currentUav = new SimpleObjectProperty<>();
    private IMapView mapView;
    private BooleanProperty executingProperty = new SimpleBooleanProperty(false);
    private File matchingsFolder;
    private Mission mission;

    @Inject
    public TransferDataPopupViewModel(
            IApplicationContext applicationContext,
            INavigationService navigationService,
            ILanguageHelper languageHelper,
            ISettingsManager settingsManager,
            IMapView mapView,
            IHardwareConfigurationManager hardwareConfigurationManager) {
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.languageHelper = languageHelper;
        this.mapView = mapView;
        this.hardwareConfigurationManager = hardwareConfigurationManager;
        this.settings = settingsManager.getSection(AnalysisSettings.class);
        currentUav.bind(PropertyPath.from(currentMissionProperty()).selectReadOnlyObject(Mission::droneProperty));
        setExecutingProperty(false);
    }

    public Mission getCurrentMission() {
        return currentMissionProperty().get();
    }

    public ReadOnlyObjectProperty<Mission> currentMissionProperty() {
        return applicationContext.currentMissionProperty();
    }

    public Drone getCurrentUav() {
        return currentUav.get();
    }

    public ReadOnlyObjectProperty<Drone> currentUavProperty() {
        return currentUav;
    }

    public String getUavTypeName() {
        Mission mission = getCurrentMission();
        if (mission != null) {
            Drone uav = mission.droneProperty().get();
            if (uav != null) {
                IPlatformDescription platformDesc = uav.getPlatformDescription();
                if (platformDesc != null) {
                    return platformDesc.getAirplaneType().name();
                }
            }
        }

        return "";
    }

    public void getSampleMatchings() {
        if (!applicationContext.getCurrentMission().checkAndRenameDemo()) {
            return;
        }

        mission = applicationContext.getCurrentMission();
        matchingsFolder = MissionConstants.getMatchingsFolder(mission.getDirectoryFile());
        setExecutingProperty(true);
        ProgressTask progressMonitor =
            new ProgressTask(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory.download.sample.matching.title"),
                dialogService,
                0) {
                @Override
                protected Void call() throws Exception {
                    List<MatchingService.FolderHolder> folderHolders = matchingService.getSampleMatchingFolders(this);
                    if (!folderHolders.isEmpty()) {
                        Dispatcher dispatcher = Dispatcher.platform();
                        dispatcher.run(
                            () -> {
                                List<String> folders =
                                    getSampleData()
                                        .stream()
                                        .map(folderHolder -> folderHolder.getTitle())
                                        .collect(Collectors.toList());

                                String selectedFolder =
                                    dialogService.showChoicesDialog(
                                        languageHelper.getString(CHOOSE_MATCHING_DIALOG_HEADER),
                                        languageHelper.getString(CHOOSE_MATCHING_DIALOG_DOWNLOAD),
                                        folders);
                                if (selectedFolder != null) {
                                    Optional<MatchingService.FolderHolder> selected =
                                        getSampleData()
                                            .stream()
                                            .filter(folderHolder -> folderHolder.getTitle().equals(selectedFolder))
                                            .findFirst();
                                    downloadSampleData(selected.get().getName(), selected.get().getFiles());
                                } else {
                                    setExecutingProperty(false);
                                }
                            });
                    } else {
                        applicationContext.addToast(
                            Toast.of(ToastType.INFO)
                                .setText(
                                    languageHelper.getString(
                                        "com.intel.missioncontrol.ui.analysis.datatransfer.popup.DataTransferPopupView.dataset.not.found.message"))
                                .create());
                        setExecutingProperty(false);
                    }

                    return null;
                }
            };
        taskManager.submitTask(progressMonitor);
    }

    public void downloadSampleData(String sampleMatchingsFolder, TreeMap<String, Long> remoteFiles) {
        ProgressTask progressMonitor =
            new ProgressTask(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory.download.sample.data.title"),
                dialogService,
                0) {
                @Override
                protected Void call() throws Exception {
                    final String setNameLocal = FileHelper.urlToFileName(sampleMatchingsFolder);
                    File matchingFolder = new File(matchingsFolder, setNameLocal);
                    if (matchingFolder.exists()) {
                        if (!dialogService.requestConfirmation(
                                languageHelper.getString(
                                    "com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory.download.sample.data.completeTitle"),
                                languageHelper.getString(
                                    "com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory.download.sample.data.completeMessage",
                                    matchingFolder.getName()))) {
                            applicationContext.addToast(
                                Toast.of(ToastType.INFO)
                                    .setText(languageHelper.getString(SAMPLE_DOWNLOAD_CANCELED))
                                    .create());
                            setExecutingProperty(false);
                            return null;
                        }
                    }

                    boolean ok =
                        matchingService.downloadMatchings(sampleMatchingsFolder, remoteFiles, matchingFolder, this);

                    if (!ok) {
                        setExecutingProperty(false);
                        return null;
                    }
                    /*dialogService.showInfoMessage(
                    languageHelper.getString(SAMPLE_DOWNLOAD_COMPLETE_TITLE),
                    languageHelper.getString(SAMPLE_DOWNLOAD_COMPLETE_MESSAGE, sampleMatchingsFolder));*/

                    Matching matching = new Matching(matchingFolder, hardwareConfigurationManager);
                    mission.setMatching(matching, matchingFolder);

                    Dispatcher dispatcher = Dispatcher.platform();
                    dispatcher.run(
                        () -> {
                            mission.setCurrentMatching(matching);
                            missionManager.makeDefaultScreenshot(mission);
                            if (applicationContext.getCurrentMission() == mission) {
                                navigationService.navigateTo(WorkflowStep.DATA_PREVIEW);
                                navigationService.navigateTo(SidePanePage.VIEW_DATASET);
                                mapView.goToSectorAsync(matching.getSector(), matching.getMaxElev());
                            } else {
                                missionManager.saveMission(mission);
                            }
                        });
                    applicationContext.addToast(
                        Toast.of(ToastType.INFO)
                            .setText(languageHelper.getString(SAMPLE_DOWNLOAD_COMPLETE_MESSAGE, sampleMatchingsFolder))
                            .create());
                    setExecutingProperty(false);
                    return null;
                }
            };
        taskManager.submitTask(progressMonitor);
        applicationContext.addToast(
            Toast.of(ToastType.INFO)
                .setText(languageHelper.getString(SAMPLE_DOWNLOAD_STARTED))
                .create());
    }

    public List<MatchingService.FolderHolder> getSampleData() {
        return matchingService.getMatchingDirs();
    }

    public void closeMe() {
        settings.dataTransferPopupEnabledProperty().set(false);
        navigationService.navigateTo(SidePanePage.DATA_IMPORT);
    }

    public ObservableValue<Boolean> isExecutingProperty() {
        return executingProperty;
    }

    public void setExecutingProperty(boolean executing) {
        executingProperty.set(executing);
    }
}
