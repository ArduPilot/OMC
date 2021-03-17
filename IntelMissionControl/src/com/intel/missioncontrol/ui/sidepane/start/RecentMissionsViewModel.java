/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.start;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.property.UIAsyncListProperty;
import com.intel.missioncontrol.beans.property.UIPropertyMetadata;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.collections.LockedList;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.MavinciObjectFactory;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.mission.IMissionInfo;
import com.intel.missioncontrol.mission.IMissionInfoManager;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.PathSettings;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.menu.MainMenuModel;
import com.intel.missioncontrol.ui.menu.MenuModel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.utils.ConvertUtils;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.main.debug.profiling.MRequest;
import eu.mavinci.desktop.main.debug.profiling.requests.OpenMissionRequest;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.OptionalDouble;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

public class RecentMissionsViewModel extends ViewModelBase {

    private static final String YEAR_MONTH_DAY_DATE_FORMAT = "yyyy-MM-dd";
    private final UIAsyncListProperty<ViewModel> items =
        new UIAsyncListProperty<>(
            this,
            new UIPropertyMetadata.Builder<AsyncObservableList<ViewModel>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());
    private final ObjectProperty<MissionItemViewModel> selectedItem = new SimpleObjectProperty<>();
    private final BooleanProperty isBusy = new SimpleBooleanProperty();
    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final IMissionManager missionManager;
    private final IDialogService dialogService;
    private final ICommand createMissionCommand;
    private final ICommand openMissionCommand;
    private final ICommand cloneMissionCommand;
    private final IProfilingManager profilingManager;
    private final ISelectionManager selectionManager;
    private final ISettingsManager settingsManager;
    private final IMapView mapView;
    private final ILanguageHelper languageHelper;

    @InjectScope
    private MainScope mainScope;

    private ChangeListener<MissionItemViewModel> missionItemViewModelChangeListener;

    @Inject
    public RecentMissionsViewModel(
            IApplicationContext applicationContext,
            INavigationService navigationService,
            IMissionManager missionManager,
            IDialogService dialogService,
            IMissionInfoManager missionInfoManager,
            IProfilingManager profilingManager,
            IMapView mapView,
            ISelectionManager selectionManager,
            ISettingsManager settingsManager,
            MavinciObjectFactory mavinciObjectFactory,
            ILanguageHelper languageHelper,
            IHardwareConfigurationManager hardwareConfigurationManager,
            ISrsManager srsManager) {
        this.mapView = mapView;
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.missionManager = missionManager;
        this.dialogService = dialogService;
        this.profilingManager = profilingManager;
        this.selectionManager = selectionManager;
        this.settingsManager = settingsManager;
        this.languageHelper = languageHelper;

        createMissionCommand = new DelegateCommand(this::createMission, isBusy.not());
        openMissionCommand =
            new DelegateCommand(
                () ->
                    openMission(
                        new Mission(
                            selectedItem.get().getMissionInfo(),
                            mavinciObjectFactory,
                            settingsManager,
                            missionInfoManager,
                            languageHelper,
                            hardwareConfigurationManager,
                            srsManager),
                        false),
                isBusy.not().and(selectedItem.isNotNull()));
        cloneMissionCommand =
            new DelegateCommand(
                () ->
                    openMission(
                        new Mission(
                            selectedItem.get().getMissionInfo(),
                            mavinciObjectFactory,
                            settingsManager,
                            missionInfoManager,
                            languageHelper,
                            hardwareConfigurationManager,
                            srsManager),
                        true),
                isBusy.not().and(selectedItem.isNotNull()));

        // TODO! - add sorting and dates
        // items.bindContent(missionManager.recentMissionInfosProperty(), (value -> new MissionItemViewModel(value)));
    }

    private void selectionChange(Object userData) {
        if (!(userData instanceof IMissionInfo)) {
            return;
        }

        IMissionInfo missionInfo = (IMissionInfo)userData;

        try (LockedList<ViewModel> lockedList = items.lock()) {
            for (ViewModel item : lockedList) {
                if (item instanceof MissionItemViewModel) {
                    IMissionInfo otherMissionInfo = ((MissionItemViewModel)item).getMissionInfo();
                    if (missionInfo.getFolder().equals(otherMissionInfo.getFolder())
                            && missionInfo.getName().equals(otherMissionInfo.getName())) {
                        selectedItem.set((MissionItemViewModel)item);
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        refreshRecentMissionsList();
        navigationService.sidePanePageProperty().addListener(this::handleSidePanePageChanged);

        // Register handlers for the main menu
        MenuModel menuModel = mainScope.mainMenuModelProperty().get();
        menuModel.find(MainMenuModel.Project.NEW).setCommandHandler(this::createMission, isBusy.not());
        menuModel
            .find(MainMenuModel.Project.CLONE)
            .setCommandHandler(
                () -> openMission(applicationContext.getCurrentMission(), true),
                applicationContext.currentMissionProperty().isNotNull());

        selectionManager
            .currentSelectionProperty()
            .addListener((observable, oldValue, newValue) -> selectionChange(newValue), Dispatcher::dispatchToUI);

        missionItemViewModelChangeListener =
            (observable, oldValue, newValue) -> {
                if (newValue == null) {
                    return;
                }

                IMissionInfo missionInfo = newValue.getMissionInfo();
                if (missionInfo == null) {
                    return;
                }

                selectionManager.setSelection(missionInfo);
                mapView.goToSectorAsync(missionInfo.getSector(), OptionalDouble.empty());
            };
        selectedItem.addListener(new WeakChangeListener<>(missionItemViewModelChangeListener));

        missionManager.refreshRecentMissionListItems();
        items.bind(missionManager.recentMissionListItems());
    }

    public UIAsyncListProperty<ViewModel> itemsProperty() {
        return items;
    }

    public ObjectProperty<MissionItemViewModel> selectedItemProperty() {
        return selectedItem;
    }

    public BooleanProperty isBusyProperty() {
        return isBusy;
    }

    public ICommand getCreateMissionCommand() {
        return createMissionCommand;
    }

    public ICommand getOpenMissionCommand() {
        return openMissionCommand;
    }

    public ICommand getCloneMissionCommand() {
        return cloneMissionCommand;
    }

    private void handleSidePanePageChanged(
            ObservableValue<? extends SidePanePage> observable, SidePanePage oldValue, SidePanePage newValue) {
        if (newValue == SidePanePage.RECENT_MISSIONS) {
            refreshRecentMissionsList();
        }
    }

    public void refreshRecentMissionsList() {
        isBusy.set(true);
        missionManager.refreshRecentMissionInfos();
        missionManager.refreshRecentMissionListItems();
        isBusy.set(false);
    }

    private void createMission() {
        if (applicationContext.currentMissionProperty().get() != null) {
            // if unloading is cancelled by user input, we also abort creating a new mission
            if (!applicationContext.unloadCurrentMission()) {
                return;
            }
        }

        isBusy.set(true);
        applicationContext
            .loadNewMissionAsync()
            .onSuccess(future -> navigationService.navigateTo(SidePanePage.START_PLANNING), Platform::runLater)
            .onDone(future -> isBusy.set(false), Platform::runLater);
    }

    private void openMission(final Mission mission, boolean clone) {
        isBusy.set(true);
        MRequest openMissionRequest = new OpenMissionRequest(mission);
        profilingManager.requestStarting(openMissionRequest);
        (clone ? applicationContext.loadClonedMissionAsync(mission) : applicationContext.loadMissionAsync(mission))
            .onDone(
                future -> {
                    isBusy.set(false);
                    profilingManager.requestFinished(openMissionRequest);
                },
                Platform::runLater)
            .onSuccess(
                future -> {
                    SidePanePage newPage =
                        mission.flightPlansProperty().isEmpty()
                            ? SidePanePage.START_PLANNING
                            : SidePanePage.EDIT_FLIGHTPLAN;
                    if (mission.flightPlansProperty().isEmpty()
                            && !mission.matchingsProperty().isEmpty()
                            && (mission.matchingsProperty().get(0).getStatus() != MatchingStatus.NEW
                                || mission.getMatchings().size() > 1)) {
                        navigationService.navigateTo(WorkflowStep.DATA_PREVIEW);
                    } else {
                        navigationService.navigateTo(newPage);
                        missionManager.refreshRecentMissionInfos();
                        missionManager.refreshRecentMissionInfos();
                    }
                },
                Platform::runLater);
    }

    public void handleDatePicker(LocalDate localDate) {
        String localDateInFormat = localDate.toString();

        for (ViewModel viewModel : items) {
            if (viewModel instanceof MissionItemViewModel) {
                selectedItem.set(null);
            }
        }

        for (ViewModel viewModel : items) {
            if (viewModel instanceof MissionItemViewModel) {
                IMissionInfo missionInfo = ((MissionItemViewModel)viewModel).getMissionInfo();
                String lastModifiedDate =
                    ConvertUtils.convertLongToDate(missionInfo.getLastModified().getTime(), YEAR_MONTH_DAY_DATE_FORMAT);
                if (lastModifiedDate.equals(localDateInFormat)) {
                    selectedItem.set((MissionItemViewModel)viewModel);
                    break;
                }
            }
        }
    }

    public void openFromDisk() {
        Path projectsDirectory = settingsManager.getSection(PathSettings.class).getProjectFolder();
        Path missionFolder =
            dialogService.requestDirectoryChooser(RecentMissionsViewModel.this, null, projectsDirectory);
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

}
