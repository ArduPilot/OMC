/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.menu;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.api.IFlightPlanService;
import com.intel.missioncontrol.diagnostics.PerformanceReporter;
import com.intel.missioncontrol.diagnostics.PerformanceTracker;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.map.worldwind.IWWMapModel;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.ISaveable;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionConstants;
import com.intel.missioncontrol.mission.MissionInfo;
import com.intel.missioncontrol.project.SuspendedInteractionRequest;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.settings.PathSettings;
import com.intel.missioncontrol.ui.common.components.RenameDialog;
import com.intel.missioncontrol.ui.controls.StylesheetHelper;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.SendSupportDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.about.AboutDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.savechanges.SaveChangesDialogViewModel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.desktop.helper.FileFilter;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.main.debug.Debug;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.concurrent.Dispatcher;

/** Contains implementations for main menu commands that don't better fit somewhere else. */
public class MainMenuCommandManager {

    private static final String MANUAL_PATH_ARG = "manual_path";
    private static final String DEFAULT_MANUAL_FOLDER = "../manuals";
    private static final String MANUAL_FILE = "manual.pdf";
    private static final String QUICK_START_GUIDE_FILE = "imc-quick-start-guide.pdf";

    private final ViewModel ownerViewModel;
    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final IMissionManager missionManager;
    private final IFlightPlanService flightPlanService;
    private final IDialogService dialogService;
    private final INavigationService navigationService;
    private final ILicenceManager licenceManager;
    private final ISettingsManager settingsManager;

    public MainMenuCommandManager(
            ViewModel ownerViewModel,
            MenuModel menuModel,
            INavigationService navigationService,
            IApplicationContext applicationContext,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            IMissionManager missionManager,
            IFlightPlanService flightPlanService,
            ISettingsManager settingsManager,
            ILicenceManager licenceManager,
            IWWMapModel mapModel,
            IWWMapView mapView,
            IHardwareConfigurationManager hardwareConfigurationManager) {
        this.ownerViewModel = ownerViewModel;
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        this.missionManager = missionManager;
        this.navigationService = navigationService;
        this.flightPlanService = flightPlanService;
        this.dialogService = dialogService;
        this.licenceManager = licenceManager;
        this.settingsManager = settingsManager;

        BooleanBinding importedBinding =
            PropertyPath.from(applicationContext.currentLegacyMissionProperty())
                .select(Mission::currentMatchingProperty)
                .selectReadOnlyObject(Matching::statusProperty)
                .isEqualTo(MatchingStatus.IMPORTED);

        menuModel
            .find(MainMenuModel.Project.EXIT)
            .setActionHandler(WindowHelper::closePrimaryStage, SuspendedInteractionRequest.activeProperty());

        menuModel
            .find(MainMenuModel.Project.OPEN)
            .setActionHandler(this::openMission, SuspendedInteractionRequest.activeProperty());

        menuModel
            .find(MainMenuModel.Project.CLOSE)
            .setActionHandler(
                applicationContext::unloadCurrentMission,
                applicationContext
                    .currentLegacyMissionProperty()
                    .isNotNull()
                    .and(SuspendedInteractionRequest.activeProperty()));

        menuModel
            .find(MainMenuModel.Project.RENAME)
            .setActionHandler(
                () -> Dispatcher.platform().run(applicationContext::renameCurrentMission),
                applicationContext
                    .currentLegacyMissionProperty()
                    .isNotNull()
                    .and(SuspendedInteractionRequest.activeProperty()));

        menuModel
            .find(MainMenuModel.Project.SHOW)
            .setActionHandler(
                () -> {
                    try {
                        Desktop.getDesktop()
                            .browse(applicationContext.getCurrentLegacyMission().getDirectory().toUri());
                    } catch (IOException e) {
                        Debug.getLog()
                            .log(
                                Level.WARNING,
                                "cant browse folder:" + applicationContext.getCurrentLegacyMission().getDirectory(),
                                e);
                        applicationContext.addToast(
                            Toast.of(ToastType.ALERT)
                                .setText(
                                    languageHelper.getString(
                                        "com.intel.missioncontrol.ui.menu.cant_browse_folder_exception"))
                                .setShowIcon(true)
                                .create());
                    }
                },
                applicationContext.currentLegacyMissionProperty().isNotNull());

        menuModel
            .find(MainMenuModel.Dataset.OPEN)
            .setActionHandler(
                () -> {
                    String title =
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.analysis.AnalysisView.FileChooser.dialogTitle");
                    Mission mission = applicationContext.currentLegacyMissionProperty().get();
                    Path matchingFolder = MissionConstants.getMatchingsFolder(mission.getDirectory()).toPath();
                    Path selectedFile =
                        dialogService.requestFileOpenDialog(ownerViewModel, title, matchingFolder, FileFilter.PTG);
                    if (selectedFile != null) {
                        try {
                            Matching matching = new Matching(selectedFile.toFile(), hardwareConfigurationManager);
                            mission.getMatchings().add(matching);
                            mission.setCurrentMatching(matching);
                            navigationService.navigateTo(WorkflowStep.DATA_PREVIEW);
                            navigationService.navigateTo(SidePanePage.VIEW_DATASET);
                            mapView.goToSectorAsync(matching.getSector(), matching.getMaxElev());
                        } catch (Exception e) {
                            Debug.getLog().log(Level.WARNING, "cant load dataset:" + matchingFolder, e);
                            applicationContext.addToast(
                                Toast.of(ToastType.ALERT)
                                    .setText(
                                        languageHelper.getString(
                                            "com.intel.missioncontrol.ui.analysis.AnalysisView.FileChooser.isNotMatchingFileAlert"))
                                    .setShowIcon(true)
                                    .create());
                        }
                    }
                },
                applicationContext
                    .currentLegacyMissionProperty()
                    .isNotNull()
                    .and(applicationContext.currentMissionIsNoDemo()));

        menuModel
            .find(MainMenuModel.Dataset.CLOSE)
            .setActionHandler(
                () -> {
                    ObservableList<Matching> matchings = applicationContext.getCurrentLegacyMission().getMatchings();
                    if (!matchings.isEmpty()) {
                        Mission mission = applicationContext.getCurrentLegacyMission();
                        Matching currentMatching = mission.getCurrentMatching();
                        if (!askToSaveChangesAndProceed(mission, currentMatching)) {
                            return;
                        }

                        matchings.remove(currentMatching);

                        if (!matchings.isEmpty()) {
                            mission.setCurrentMatching(matchings.get(0));
                        } else {
                            mission.setCurrentMatching(null);
                        }
                    }
                },
                importedBinding);

        menuModel
            .find(MainMenuModel.Dataset.SAVE)
            .setActionHandler(
                () -> {
                    applicationContext.getCurrentLegacyMission().getCurrentMatching().saveResourceFile();
                },
                importedBinding.and(
                    PropertyPath.from(applicationContext.currentLegacyMissionProperty())
                        .select(Mission::currentMatchingProperty)
                        .selectReadOnlyBoolean(Matching::matchingLayerChangedProperty)));

        menuModel
            .find(MainMenuModel.FlightPlan.CLOSE)
            .setActionHandler(
                () -> {
                    Mission mission = applicationContext.getCurrentLegacyMission();
                    FlightPlan flightPlan = mission.getCurrentFlightPlan();
                    if (!askToSaveChangesAndProceed(mission, flightPlan)) {
                        return;
                    }

                    mission.closeFlightPlan(flightPlan);
                    if (flightPlan == null) {
                        mission.setCurrentFlightPlan(null);
                    } else {
                        mission.setCurrentFlightPlan(mission.getFirstFlightPlan());
                    }
                },
                PropertyPath.from(applicationContext.currentLegacyMissionProperty())
                    .selectReadOnlyObject(Mission::currentFlightPlanProperty)
                    .isNotNull()
                    .and(applicationContext.currentMissionIsNoDemo()));

        menuModel
            .find(MainMenuModel.FlightPlan.SHOW)
            .setActionHandler(
                () -> {
                    try {
                        Desktop.getDesktop()
                            .browse(
                                applicationContext
                                    .getCurrentLegacyMission()
                                    .getCurrentFlightPlan()
                                    .getLegacyFlightplan()
                                    .getResourceFile()
                                    .getParentFile()
                                    .toURI());
                    } catch (IOException e) {
                        Debug.getLog()
                            .log(
                                Level.WARNING,
                                "cant browse folder:"
                                    + applicationContext
                                        .getCurrentLegacyMission()
                                        .getCurrentFlightPlan()
                                        .getLegacyFlightplan()
                                        .getResourceFile()
                                        .getParentFile(),
                                e);
                        applicationContext.addToast(
                            Toast.of(ToastType.ALERT)
                                .setText(
                                    languageHelper.getString(
                                        "com.intel.missioncontrol.ui.menu.cant_browse_folder_exception"))
                                .setShowIcon(true)
                                .create());
                    }
                },
                PropertyPath.from(applicationContext.currentLegacyMissionProperty())
                    .selectReadOnlyObject(Mission::currentFlightPlanProperty)
                    .isNotNull());

        menuModel
            .find(MainMenuModel.Dataset.SHOW)
            .setActionHandler(
                () -> {
                    ObservableList<Matching> matchings = applicationContext.getCurrentLegacyMission().getMatchings();
                    if (matchings.isEmpty()) {
                        return;
                    }

                    Matching currentMatching = applicationContext.getCurrentLegacyMission().getCurrentMatching();

                    try {
                        Desktop.getDesktop().browse(currentMatching.getResourceFile().getParentFile().toURI());
                    } catch (IOException e) {
                        Debug.getLog()
                            .log(
                                Level.WARNING,
                                "cant browse folder:"
                                    + applicationContext
                                        .getCurrentLegacyMission()
                                        .getCurrentMatching()
                                        .getResourceFile()
                                        .getParentFile(),
                                e);
                        applicationContext.addToast(
                            Toast.of(ToastType.ALERT)
                                .setText(
                                    languageHelper.getString(
                                        "com.intel.missioncontrol.ui.menu.cant_browse_folder_exception"))
                                .setShowIcon(true)
                                .create());
                    }
                },
                importedBinding);

        menuModel
            .find(MainMenuModel.FlightPlan.NEW)
            .setActionHandler(
                () -> {
                    Mission mission = applicationContext.getCurrentLegacyMission();
                    mission.setCurrentFlightPlan(null);
                },
                applicationContext
                    .currentLegacyMissionProperty()
                    .isNotNull()
                    .and(applicationContext.currentMissionIsNoDemo()));

        menuModel
            .find(MainMenuModel.FlightPlan.RENAME)
            .setActionHandler(
                this::renameFlightPlan,
                PropertyPath.from(applicationContext.currentLegacyMissionProperty())
                    .selectReadOnlyObject(Mission::currentFlightPlanProperty)
                    .isNotNull()
                    .and(applicationContext.currentMissionIsNoDemo()));

        menuModel.find(MainMenuModel.Help.USER_MANUAL).setActionHandler(() -> openManual(MANUAL_FILE));

        menuModel.find(MainMenuModel.Help.QUICK_START_GUIDE).setActionHandler(() -> openManual(QUICK_START_GUIDE_FILE));

        menuModel.find(MainMenuModel.Help.DEMO_MISSION).setActionHandler(() -> openDemoMission());

        menuModel
            .find(MainMenuModel.Help.SUPPORT_REQUEST)
            .setActionHandler(
                () -> {
                    if (!applicationContext.askUserForMissionSave()) {
                        return;
                    }

                    dialogService.requestDialogAsync(ownerViewModel, SendSupportDialogViewModel.class);
                });

        menuModel
            .find(MainMenuModel.Help.ABOUT)
            .setActionHandler(() -> dialogService.requestDialogAsync(ownerViewModel, AboutDialogViewModel.class));

        menuModel
            .find(MainMenuModel.Debug.MENU_CAPTION)
            .visibleProperty()
            .bind(
                settingsManager
                    .getSection(GeneralSettings.class)
                    .operationLevelProperty()
                    .isEqualTo(OperationLevel.DEBUG));

        menuModel
            .find(MainMenuModel.Debug.RELOAD_CSS)
            .setActionHandler(() -> new StylesheetHelper().reloadStylesheets());

        menuModel
            .find(MainMenuModel.Debug.REPORT_DIAGNOSTICS)
            .checkedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        PerformanceReporter.start();
                    } else {
                        PerformanceReporter.stop();
                    }
                });

        menuModel
            .find(MainMenuModel.Debug.BREAK_AFTER_50)
            .checkedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        PerformanceTracker.setBreakAfterMillis(50);
                    }
                });

        menuModel
            .find(MainMenuModel.Debug.BREAK_AFTER_100)
            .checkedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        PerformanceTracker.setBreakAfterMillis(100);
                    }
                });

        menuModel
            .find(MainMenuModel.Debug.BREAK_AFTER_250)
            .checkedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        PerformanceTracker.setBreakAfterMillis(250);
                    }
                });

        menuModel
            .find(MainMenuModel.Debug.BREAK_AFTER_500)
            .checkedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        PerformanceTracker.setBreakAfterMillis(500);
                    }
                });

        menuModel
            .find(MainMenuModel.Debug.BREAK_AFTER_1000)
            .checkedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        PerformanceTracker.setBreakAfterMillis(1000);
                    }
                });

        menuModel
            .find(MainMenuModel.Debug.BREAK_AFTER_NEVER)
            .checkedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        PerformanceTracker.setBreakAfterMillis(0);
                    }
                });

        menuModel.find(MainMenuModel.Debug.BREAK_AFTER_NEVER).checkedProperty().set(true);

        menuModel
            .find(MainMenuModel.Debug.WIREFRAME)
            .checkedProperty()
            .addListener((observable, oldValue, newValue) -> mapModel.setShowWireframeInterior(newValue));
    }

    // returns true if the operation should proceed (otherwise it it was canceled)
    private boolean askToSaveChangesAndProceed(Mission mission, ISaveable saveable) {
        if (saveable.hasUnsavedChanges()) {
            SaveChangesDialogViewModel viewModel =
                dialogService.requestDialogAndWait(
                    WindowHelper.getPrimaryViewModel(),
                    SaveChangesDialogViewModel.class,
                    () ->
                        new SaveChangesDialogViewModel.Payload(
                            Arrays.asList(saveable), mission, SaveChangesDialogViewModel.DialogTypes.close));
            boolean shouldProceed = viewModel.shouldProceedProperty().get();
            if (!shouldProceed) {
                return false;
            }

            boolean shouldSaveChanges = viewModel.shouldSaveChangesProperty().get();
            boolean wasSelected = viewModel.needsToSaveItem(saveable);
            if (shouldSaveChanges && wasSelected) {
                if (saveable instanceof FlightPlan) {
                    flightPlanService.saveFlightPlan(mission, ((FlightPlan)saveable));
                } else {
                    saveable.save();
                }

                missionManager.saveMission(mission);
                if (!viewModel.nameProperty().get().equals(mission.getName())) {
                    missionManager.renameMission(mission, viewModel.nameProperty().get());
                }
            }
        }

        return true;
    }

    private void openManual(String fileName) {
        try {
            final String manualFolder = System.getProperty(MANUAL_PATH_ARG, DEFAULT_MANUAL_FOLDER);
            File file = Paths.get(manualFolder, Locale.getDefault().getLanguage(), fileName).toFile();
            if (!file.exists()) {
                file = Paths.get(manualFolder, Locale.ENGLISH.getLanguage(), fileName).toFile();
            }

            if (!file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }

            FileHelper.openFile(file);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isFlightplanNameValid(String flightPlanName) {
        Mission currentMission = applicationContext.getCurrentLegacyMission();
        int duplicates =
            currentMission
                .flightPlansProperty()
                .filtered(flightPlan -> flightPlan.getName().equals(flightPlanName))
                .size();
        return duplicates == 0 && flightPlanName.trim().length() > 1;
    }

    private void renameFlightPlan() {
        Mission mission = applicationContext.getCurrentLegacyMission();
        // mission.closeFlightPlan(mission.getFirstFlightPlan());

        FlightPlan flightPlan = mission.getCurrentFlightPlan();
        String oldName = mission.getCurrentFlightPlan().getName();
        String newName =
            RenameDialog.requestNewMissionName(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.SidePaneView.selector.flightplan.rename.title"),
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.SidePaneView.selector.flightplan.rename.name"),
                    oldName,
                    languageHelper,
                    this::isFlightplanNameValid)
                .orElse(oldName);

        this.flightPlanService.renameFlightPlan(mission, flightPlan, newName);
    }

    private void openMission() {
        if (applicationContext.currentLegacyMissionProperty().get() != null) {
            // if unloading is cancelled by user input, we also abort open mission
            if (!applicationContext.unloadCurrentMission()) {
                return;
            }
        }

        Path projectsDirectory = settingsManager.getSection(PathSettings.class).getProjectFolder();
        Path missionFolder = dialogService.requestDirectoryChooser(ownerViewModel, null, projectsDirectory);
        if (missionFolder == null) {
            return;
        }

        if (!missionManager.isMissionFolder(missionFolder.toFile())) {
            Debug.getLog()
                .log(
                    Level.INFO,
                    languageHelper.getString("com.intel.missioncontrol.ui.menu.no_mission_folder")
                        + " "
                        + missionFolder.toFile());
            applicationContext.addToast(
                Toast.of(ToastType.INFO)
                    .setText(languageHelper.getString("com.intel.missioncontrol.ui.menu.no_mission_folder"))
                    .create());
            return;
        }

        openMission(missionManager.openMission(missionFolder), false);
    }

    public static final String DEMO_SESSION_NAME = "DEMO";

    private void openDemoMission() {
        Path projectsDirectory = settingsManager.getSection(PathSettings.class).getProjectFolder();
        Path missionFolder = projectsDirectory.resolve(DEMO_SESSION_NAME);
        boolean alreadyCreated = Files.exists(missionFolder);
        if (alreadyCreated) {
            try {
                FileUtils.deleteDirectory(missionFolder.toFile());
            } catch (IOException e) {
                Debug.getLog().log(Level.SEVERE, "Could not delete Demo Session data on disk", e);
            }
        }

        try {
            if (licenceManager.isFalconEditionProperty().get() || licenceManager.isGrayHawkEditionProperty().get()) {
                FileHelper.scanFilesJarAndWriteToDisk(
                    MFileFilter.allFilterNonSVN,
                    "com/intel/missioncontrol/demoSessions/falcon/",
                    missionFolder.toFile());
            } else {
                FileHelper.scanFilesJarAndWriteToDisk(
                    MFileFilter.allFilterNonSVN, "com/intel/missioncontrol/demoSessions/dji/", missionFolder.toFile());
            }

            Mission mission = missionManager.openMission(missionFolder);
            openMission(mission, false);
        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "Could not store Demo Session data on disk", e);
        }
    }

    public static boolean isDemo(MissionInfo mission) {
        return Optional.ofNullable(mission)
            .map(MissionInfo::getName)
            .filter(DEMO_SESSION_NAME::equalsIgnoreCase)
            .isPresent();
    }

    private void openMission(final Mission mission, boolean clone) {
        (clone ? applicationContext.loadClonedMissionAsync(mission) : applicationContext.loadMissionAsync(mission))
            .whenSucceeded(
                future -> {
                    SidePanePage newPage =
                        mission.flightPlansProperty().isEmpty()
                            ? (mission.matchingsProperty().isEmpty()
                                ? SidePanePage.START_PLANNING
                                : SidePanePage.VIEW_DATASET)
                            : SidePanePage.EDIT_FLIGHTPLAN;
                    if (mission.flightPlansProperty().isEmpty()
                            && !mission.matchingsProperty().isEmpty()
                            && (mission.matchingsProperty().get(0).getStatus() != MatchingStatus.NEW
                                || mission.getMatchings().size() > 1)) {
                        navigationService.navigateTo(SidePanePage.VIEW_DATASET);
                    } else {
                        navigationService.navigateTo(newPage);
                        missionManager.refreshRecentMissionInfos();
                        missionManager.refreshRecentMissionInfos();
                    }
                },
                Dispatcher.platform());
    }

}
