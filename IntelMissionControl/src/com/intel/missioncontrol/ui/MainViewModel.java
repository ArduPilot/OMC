/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.VisibilityTracker;
import com.intel.missioncontrol.airspaces.services.Airmap2AirspaceService;
import com.intel.missioncontrol.api.IFlightPlanService;
import com.intel.missioncontrol.api.support.ISupportManager;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.beans.property.PropertyPathStore;
import com.intel.missioncontrol.beans.property.UIAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.UIAsyncListProperty;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.concurrent.SynchronizationContext;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.DisplayDeviceInformation;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.MavinciObjectFactory;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.annotation.airspaces.AirspacesToolTipController;
import com.intel.missioncontrol.map.credits.IMapCreditsManager;
import com.intel.missioncontrol.map.credits.MapCreditViewModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.IWWMapModel;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.measure.AngleStyle;
import com.intel.missioncontrol.mission.IMissionInfo;
import com.intel.missioncontrol.mission.IMissionInfoManager;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.networking.MapTileDownloadStatusNotifier;
import com.intel.missioncontrol.networking.MapTileDownloadStatusSubscriber;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.settings.SrsSettings;
import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.commands.IParameterizedCommand;
import com.intel.missioncontrol.ui.commands.ParameterizedDelegateCommand;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.IVeryUglyDialogHelper;
import com.intel.missioncontrol.ui.dialogs.tasks.BackgroundTasksViewModel;
import com.intel.missioncontrol.ui.dialogs.tasks.MavlinkEventLogDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.warnings.WarningsPopoverViewModel;
import com.intel.missioncontrol.ui.menu.MainMenuCommandManager;
import com.intel.missioncontrol.ui.menu.MainMenuModel;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionScope;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.NavBarDialog;
import com.intel.missioncontrol.ui.navigation.NavigationRules;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.navigation.SidePaneTab;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.notifications.MainStatus;
import com.intel.missioncontrol.ui.notifications.MainStatusType;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import com.intel.missioncontrol.ui.sidepane.analysis.DatasetMenuModel;
import com.intel.missioncontrol.ui.sidepane.planning.FlightPlanMenuModel;
import com.intel.missioncontrol.ui.update.IUpdateManager;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.desktop.gui.widgets.wkt.SpatialReferenceChooserViewModel;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import eu.mavinci.desktop.main.debug.profiling.requests.WWDredrawRequest;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.geom.Position;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ScopeProvider(scopes = {MainScope.class, UavConnectionScope.class, PlanningScope.class})
public class MainViewModel extends DialogViewModel<Void, Void> {

    private static Logger LOGGER = LoggerFactory.getLogger(MainViewModel.class);

    @InjectScope
    private MainScope mainScope;

    @InjectContext
    private Context context;

    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty srsName = new SimpleStringProperty();
    private final ObjectProperty<String[]> currentCoordinates = new SimpleObjectProperty<>();
    private final ObjectProperty<MainStatus> mainStatus = new SimpleObjectProperty<>();
    private final BooleanProperty backgroundTasksRunning = new SimpleBooleanProperty();
    private final IntegerProperty warningsCount = new SimpleIntegerProperty();
    private final ObjectProperty<ValidationMessageCategory> warningMessageCategory = new SimpleObjectProperty<>();
    private final BooleanProperty demoMissionWarningVisible = new SimpleBooleanProperty();
    private final BooleanProperty idlAvailable = new SimpleBooleanProperty(true);
    private final BooleanProperty gamepadAvailable = new SimpleBooleanProperty(true);
    private final BooleanProperty idlEnabled = new SimpleBooleanProperty(false);
    private final BooleanProperty gamepadEnabled = new SimpleBooleanProperty(false);
    private final BooleanProperty showMotorsOn = new SimpleBooleanProperty(true);
    private final UIAsyncBooleanProperty coverageLegendVisible = new UIAsyncBooleanProperty(this);

    private final ICommand changeSrsCommand;
    private final ICommand deleteSelectionCommand;
    private final ICommand cancelCommand;
    private final IParameterizedCommand<Object> mapActionCommand;

    private final SynchronizationContext synchronizationContext;
    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final IValidationService validationService;
    private final ILanguageHelper languageHelper;
    private final MavinciObjectFactory mavinciObjectFactory;
    private final IMissionInfoManager missionInfoManager;
    private final IHardwareConfigurationManager hardwareConfigurationManager;
    private final IUpdateManager updateManager;
    private final IDialogService dialogService;
    private final IVeryUglyDialogHelper dialogHelper;
    private final IMissionManager missionManager;
    private final ISupportManager supportManager;
    private final AsyncObjectProperty<AngleStyle> angleStyle;
    private final Set<String> airspacesDownloadSessions = ConcurrentHashMap.newKeySet();
    private final IProfilingManager profilingManager;
    private final INetworkInformation networkInformation;
    private final ILicenceManager licenceManager;
    private final ISettingsManager settingsManager;
    private final ISelectionManager selectionManager;
    private final VisibilityTracker visibilityTracker;
    private final ISrsManager srsManager;
    private final IWWMapModel mapModel;
    private final IWWGlobes globes;
    private final IWWMapView mapView;
    private final IMapController mapController;
    private final IElevationModel elevationModel;
    private final PositionFormatter positionFormatter;
    private final UIAsyncListProperty<MapCreditViewModel> mapCredits = new UIAsyncListProperty<>(this);
    private final NavigationRules navigationRules = new NavigationRules();

    private final ObjectProperty<MSpatialReference> srs = new SimpleObjectProperty<MSpatialReference>();
    private final ObjectProperty<MSpatialReference> srsMission = new SimpleObjectProperty<>();
    private final IFlightPlanService flightPlanService;
    private final ChangeListener<String> missionRenameListener =
        (observable, oldValue, newValue) -> title.set(createWindowTitle(newValue));
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    private MainMenuCommandManager mainMenuCommandManager; // store a reference so it doesn't get GC'd
    private Position lastMousePos;
    private java.util.Timer updateStatisTimer;
    private WWDredrawRequest wwdRedrawRequest;

    private final MapTileDownloadStatusSubscriber mapTileDownloadStatusSubscriber =
        new MapTileDownloadStatusSubscriber() {
            @Override
            public void downloadStarted(String downloadSessionId) {
                airspacesDownloadSessions.add(downloadSessionId);
            }

            @Override
            public void downloadFinished(String downloadSessionId) {
                airspacesDownloadSessions.remove(downloadSessionId);
            }
        };

    private final SrsSettings srsSettings;

    @Inject
    public MainViewModel(
            @Named(MapModule.SYNC_ROOT) SynchronizationRoot synchronizationRoot,
            IApplicationContext applicationContext,
            INavigationService navigationService,
            IValidationService validationService,
            IUpdateManager updateManager,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            IHardwareConfigurationManager hardwareConfigurationManager,
            IBackgroundTaskManager backgroundTaskManager,
            MapTileDownloadStatusNotifier mapTileDownloadStatusNotifier,
            IVeryUglyDialogHelper dialogHelper,
            IFlightPlanService flightPlanService,
            IMissionManager missionManager,
            ISupportManager supportManager,
            ISettingsManager settingsManager,
            IProfilingManager profilingManager,
            INetworkInformation networkInformation,
            ILicenceManager licenceManager,
            IWWMapModel mapModel,
            IWWGlobes globes,
            IWWMapView mapView,
            IMapController mapController,
            IElevationModel elevationModel,
            ISelectionManager selectionManager,
            ISrsManager srsManager,
            VisibilityTracker visibilityTracker,
            IMapCreditsManager mapCreditsManager,
            IMissionInfoManager missionInfoManager,
            MavinciObjectFactory mavinciObjectFactory) {
        this.synchronizationContext = synchronizationRoot;
        this.mapModel = mapModel;
        this.globes = globes;
        this.mapView = mapView;
        this.mapController = mapController;
        this.elevationModel = elevationModel;
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.validationService = validationService;
        this.updateManager = updateManager;
        this.languageHelper = languageHelper;
        this.hardwareConfigurationManager = hardwareConfigurationManager;
        this.dialogService = dialogService;
        this.dialogHelper = dialogHelper;
        this.flightPlanService = flightPlanService;
        this.missionManager = missionManager;
        this.supportManager = supportManager;
        this.angleStyle = settingsManager.getSection(GeneralSettings.class).angleStyleProperty();
        this.settingsManager = settingsManager;
        this.backgroundTasksRunning.bind(Bindings.greaterThan(backgroundTaskManager.taskCountProperty(), 0));
        this.warningsCount.set(0);
        this.profilingManager = profilingManager;
        this.networkInformation = networkInformation;
        this.licenceManager = licenceManager;
        this.srsManager = srsManager;
        this.visibilityTracker = visibilityTracker;
        this.coverageLegendVisible.bind(visibilityTracker.coverageLegendVisibleProperty());
        this.selectionManager = selectionManager;
        this.missionInfoManager = missionInfoManager;
        this.mavinciObjectFactory = mavinciObjectFactory;
        srsSettings = settingsManager.getSection(SrsSettings.class);

        srsMission.bindBidirectional(
            propertyPathStore.from(applicationContext.currentMissionProperty()).selectObject(Mission::srsProperty));
        srsMission.addListener(
            (observable, oldValue, newValue) ->
                srs.setValue(newValue == null ? srsSettings.getApplicationSrs() : newValue));

        srsSettings
            .applicationSrsProperty()
            .addListener(
                (observable, oldValue, newValue) ->
                    srs.setValue(srsMission.get() == null ? newValue : srsMission.get()));
        srs.setValue(srsMission.get() == null ? srsSettings.getApplicationSrs() : srsMission.get());

        srs.addListener(
            (observable, oldValue, newValue) -> {
                if (applicationContext.getCurrentMission() != null) {
                    srsMission.set(newValue);
                } else {
                    srsSettings.applicationSrsProperty().setValue(newValue);
                }
            });

        this.mapCredits.bind(mapCreditsManager.mapCreditsProperty());
        this.positionFormatter = new PositionFormatter(elevationModel, srsManager);

        mapController
            .pointerPositionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    String[] formattedPosition =
                        positionFormatter.formatPosition(newValue, angleStyle.get(), srs.get());
                    Platform.runLater(() -> currentCoordinates.set(formattedPosition));
                });

        this.changeSrsCommand = new DelegateCommand(this::handleChangeSrs);
        this.deleteSelectionCommand = new DelegateCommand(mapModel::deleteSelectionAsync);
        this.cancelCommand = new DelegateCommand(this::cancelOperation);
        this.mapActionCommand = new ParameterizedDelegateCommand<>(this::onMapObjectAction);

        //  >:/ This is ugly and stupid, but what's the 'correct' way to to this. Where does this get injected
        // from? Also, at least this limits the pollution to two classes
        Airmap2AirspaceService.initDownloadNotifier(mapTileDownloadStatusNotifier);
        subscribeToAirspaceDownloads(mapTileDownloadStatusNotifier);
        updateManager.setMainViewModel(this);

        gamepadAvailable.bind(
            settingsManager.getSection(GeneralSettings.class).operationLevelProperty().isEqualTo(OperationLevel.DEBUG));
        idlAvailable.bind(
            settingsManager.getSection(GeneralSettings.class).operationLevelProperty().isEqualTo(OperationLevel.DEBUG));
        showMotorsOn.bind(
            settingsManager.getSection(GeneralSettings.class).operationLevelProperty().isEqualTo(OperationLevel.DEBUG));

        navigationRules.apply(navigationService, applicationContext, settingsManager);
    }

    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    public ReadOnlyStringProperty srsNameProperty() {
        return srsName;
    }

    public ReadOnlyStringProperty currentStatusProperty() {
        return mainScope.mainMenuStatusTextProperty();
    }

    public ReadOnlyObjectProperty<String[]> currentCoordinatesProperty() {
        return currentCoordinates;
    }

    public ObjectProperty<MainStatus> mainStatusProperty() {
        return mainStatus;
    }

    public ReadOnlyListProperty<MapCreditViewModel> mapCreditsProperty() {
        return mapCredits.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty backgroundTasksRunningProperty() {
        return backgroundTasksRunning;
    }

    public ReadOnlyIntegerProperty warningsCountProperty() {
        return warningsCount;
    }

    public ObjectProperty<ValidationMessageCategory> warningMessageCategoryProperty() {
        return warningMessageCategory;
    }

    public ReadOnlyObjectProperty<NavBarDialog> currentNavBarDialogProperty() {
        return navigationService.navBarDialogProperty();
    }

    public ReadOnlyObjectProperty<WorkflowStep> currentWorkflowStepProperty() {
        return navigationService.workflowStepProperty();
    }

    public ReadOnlyObjectProperty<SidePaneTab> currentSidePaneTabProperty() {
        return navigationService.sidePaneTabProperty();
    }

    public Property<Boolean> coverageLegendVisibleProperty() {
        return coverageLegendVisible;
    }

    public ReadOnlyBooleanProperty demoMissionWarningVisibleProperty() {
        return demoMissionWarningVisible;
    }

    public ReadOnlyBooleanProperty getIDLAvailable() {
        return idlAvailable;
    }

    public ReadOnlyBooleanProperty getIDLEabled() {
        return idlEnabled;
    }

    public ReadOnlyBooleanProperty getGamepadAvailable() {
        return gamepadAvailable;
    }

    public ReadOnlyBooleanProperty getGamepadEnabled() {
        return gamepadEnabled;
    }

    public ReadOnlyBooleanProperty getShowMotorsOn() {
        return showMotorsOn;
    }

    public ICommand getChangeSrsCommand() {
        return changeSrsCommand;
    }

    public ICommand getDeleteSelectionCommand() {
        return deleteSelectionCommand;
    }

    public ICommand getCancelCommand() {
        return cancelCommand;
    }

    public IParameterizedCommand<Object> getMapActionCommand() {
        return mapActionCommand;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        mainScope.mainMenuModelProperty().set(MainMenuModel.create(languageHelper));

        mainScope
            .flightPlanMenuModelProperty()
            .set(new FlightPlanMenuModel(applicationContext, dialogService, languageHelper, flightPlanService));

        mainScope
            .datasetMenuModelProperty()
            .set(new DatasetMenuModel(applicationContext, hardwareConfigurationManager, dialogService, languageHelper));

        mainMenuCommandManager =
            new MainMenuCommandManager(
                this,
                mainScope.mainMenuModelProperty().get(),
                navigationService,
                applicationContext,
                dialogService,
                languageHelper,
                missionManager,
                flightPlanService,
                dialogHelper,
                supportManager,
                settingsManager,
                licenceManager,
                mapModel,
                mapView,
                hardwareConfigurationManager);

        applicationContext.currentMissionProperty().addListener(this::currentMissionChanged);

        // Bind the warnings count to the output of the validation service.
        warningsCount.bind(
            Bindings.createIntegerBinding(
                () -> {
                    switch (currentWorkflowStepProperty().get()) {
                    case PLANNING:
                        return validationService.planningValidationMessagesProperty().size();
                    case FLIGHT:
                        return validationService.flightValidationMessagesProperty().size();
                    case DATA_PREVIEW:
                        return validationService.datasetValidationMessagesProperty().size();
                    default:
                        return 0;
                    }
                },
                validationService.planningValidationMessagesProperty(),
                validationService.flightValidationMessagesProperty(),
                validationService.datasetValidationMessagesProperty(),
                currentWorkflowStepProperty()));

        warningMessageCategory.bind(
            Bindings.createObjectBinding(
                () -> {
                    List<ResolvableValidationMessage> tmp;
                    switch (currentWorkflowStepProperty().get()) {
                    default:
                    case PLANNING:
                        tmp = validationService.planningValidationMessagesProperty().get();
                        break;
                    case FLIGHT:
                        tmp = validationService.flightValidationMessagesProperty().get();
                        break;
                    case DATA_PREVIEW:
                        tmp = validationService.datasetValidationMessagesProperty().get();
                        break;
                    }

                    return tmp.stream()
                        .map(ResolvableValidationMessage::getCategory)
                        .min(
                            new Comparator<ValidationMessageCategory>() {
                                @Override
                                public int compare(ValidationMessageCategory o1, ValidationMessageCategory o2) {
                                    return Integer.compare(o1.ordinal(), o2.ordinal());
                                }
                            })
                        .orElse(ValidationMessageCategory.NOTICE);
                },
                validationService.planningValidationMessagesProperty(),
                validationService.flightValidationMessagesProperty(),
                validationService.datasetValidationMessagesProperty(),
                currentWorkflowStepProperty()));

        demoMissionWarningVisible.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .selectReadOnlyString(Mission::nameProperty)
                .isEqualTo(Mission.DEMO_MISSION_NAME));

        Dispatcher.scheduleOnUI(this::updateNetworkConnectivity, Duration.ZERO, Duration.millis(250));

        title.set(createWindowTitle(null));

        srs.addListener((obj, oldVal, newVal) -> MainViewModel.this.srsName.set(newVal.toStringTiny()));
        if (srs.get() != null) {
            srsName.setValue(srs.get().toStringTiny());
        }
    }

    public void showWarnings(Point2D location, Runnable onClosed) {
        dialogService
            .requestPopoverDialog(this, WarningsPopoverViewModel.class, location)
            .addListener(onClosed, Platform::runLater);
    }

    public void showBackgroundTasks(Point2D location, Runnable onClosed) {
        dialogService
            .requestPopoverDialog(this, BackgroundTasksViewModel.class, location)
            .addListener(onClosed, Platform::runLater);
    }

    public void showMavlinkEventLogs(Point2D location, Runnable onClosed) {
        dialogService
            .requestPopoverDialog(this, MavlinkEventLogDialogViewModel.class, location)
            .addListener(onClosed, Platform::runLater);
    }

    @Deprecated(forRemoval = true)
    void initializeWorldWindow(WorldWindow worldWindow) {
        AirspacesToolTipController.install(worldWindow, languageHelper);

        updateStatisTimer = new java.util.Timer();

        if (profilingManager.isActive()) {
            worldWindow.addRenderingListener(
                event -> {
                    if (event.getStage() != RenderingEvent.BEFORE_BUFFER_SWAP) {
                        return;
                    }

                    if (wwdRedrawRequest != null) {
                        profilingManager.requestFinished(wwdRedrawRequest);
                    }

                    wwdRedrawRequest = new WWDredrawRequest(worldWindow);
                    profilingManager.requestStarting(wwdRedrawRequest);
                });

            updateStatisTimer.schedule(
                new TimerTask() {
                    boolean lastBusyWW;
                    long lastBusyWWstart;
                    boolean lastBusyAirmap;
                    long lastBusyAirmapStart;

                    @Override
                    public void run() {
                        boolean busyWW = WorldWind.getRetrievalService().hasActiveTasks();
                        if (busyWW != lastBusyWW) {
                            lastBusyWW = busyWW;
                            if (busyWW) {
                                lastBusyWWstart = System.currentTimeMillis();
                            } else {
                                long duration = System.currentTimeMillis() - lastBusyWWstart;
                                LOGGER.info("PERFORMANCE WorldWind Retriver was busy for: " + duration + " ms");
                            }
                        }

                        boolean busyAirmap = !airspacesDownloadSessions.isEmpty();
                        if (busyAirmap != lastBusyAirmap) {
                            lastBusyAirmap = busyAirmap;
                            if (busyAirmap) {
                                lastBusyAirmapStart = System.currentTimeMillis();
                            } else {
                                long duration = System.currentTimeMillis() - lastBusyAirmapStart;
                                LOGGER.info("PERFORMANCE Airmap Retriver was busy for: " + duration + " ms");
                            }
                        }
                    }
                },
                0,
                100);
        }

        synchronizationContext.post(
            () -> {
                DisplayDeviceInformation.OptimalDeviceInfo info =
                    DisplayDeviceInformation.getOptimalDeviceInfo(worldWindow.getContext());

                if (!info.isRunningOnOptimalDevice()) {
                    String vendorSettings = null;
                    switch (info.getOptimalDevice().getVendor()) {
                    case NVIDIA:
                        vendorSettings = languageHelper.getString(MainViewModel.class.getName() + ".nvidiaHint");
                        break;
                    case AMD:
                        vendorSettings = languageHelper.getString(MainViewModel.class.getName() + ".amdHint");
                        break;
                    }

                    applicationContext.addToast(
                        Toast.of(ToastType.INFO)
                            .setText(
                                languageHelper.getString(MainViewModel.class.getName() + ".gpuPerformanceHint")
                                    + (vendorSettings != null ? "\n" + vendorSettings : ""))
                            .setCloseable(true)
                            .setTimeout(Duration.seconds(15))
                            .setShowIcon(true)
                            .create());
                }
            });
    }

    private void onMapObjectAction(Object mapObject) {
        if (mapObject instanceof IMissionInfo) {
            IMissionInfo missionInfo = (IMissionInfo)mapObject;
            Mission mission =
                new Mission(
                    missionInfo,
                    mavinciObjectFactory,
                    settingsManager,
                    missionInfoManager,
                    languageHelper,
                    hardwareConfigurationManager,
                    srsManager);

            applicationContext
                .loadMissionAsync(mission)
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
    }

    private void updateNetworkConnectivity() {
        if (!networkInformation.isNetworkAvailable()) {
            mainStatus.set(
                new MainStatus(
                    languageHelper.getString("eu.mavinci.desktop.gui.wwext.MStatusBar.noConnection"),
                    MainStatusType.ERROR));
        } else if (settingsManager.getSection(GeneralSettings.class).isOfflineMode()) {
            mainStatus.set(
                new MainStatus(
                    languageHelper.getString("eu.mavinci.desktop.gui.wwext.MStatusBar.offlineModeEnabled"),
                    MainStatusType.ERROR));
        } else if (!networkInformation.unreachableHostsProperty().isEmpty()) {
            mainStatus.set(
                new MainStatus(
                    languageHelper.getString("eu.mavinci.desktop.gui.wwext.MStatusBar.serverUnreachable"),
                    MainStatusType.ERROR));
        } else if (WorldWind.getRetrievalService().hasActiveTasks() || !airspacesDownloadSessions.isEmpty()) {
            mainStatus.set(
                new MainStatus(
                    languageHelper.getString("eu.mavinci.desktop.gui.wwext.MStatusBar.downloading"),
                    MainStatusType.INFORMATION));
        } else {
            mainStatus.set(null);
        }
    }

    private void handleChangeSrs() {
        dialogService.requestDialogAndWait(this, SpatialReferenceChooserViewModel.class, () -> srs).getDialogResult();
    }

    private void currentMissionChanged(
            ObservableValue<? extends Mission> observable, Mission oldValue, Mission newValue) {
        if (oldValue != null) {
            oldValue.nameProperty().removeListener(missionRenameListener);
        }

        if (newValue != null) {
            newValue.nameProperty().addListener(missionRenameListener);
            title.set(createWindowTitle(newValue.getName()));
        } else {
            title.set(createWindowTitle(null));
        }

        if (newValue == null) {
            navigationService.navigateTo(SidePanePage.RECENT_MISSIONS);
        }
    }

    private String createWindowTitle(@Nullable String missionName) {
        String title = updateManager.getApplicationName() + " " + updateManager.getCurrentMajor();
        return missionName == null || missionName.isEmpty() ? title : missionName + " - " + title;
    }

    private void subscribeToAirspaceDownloads(MapTileDownloadStatusNotifier notifier) {
        notifier.subscribe(mapTileDownloadStatusSubscriber);
    }

    private void cancelOperation() {
        if (!mapController.popLastMouseMode()) {
            // in case if we ESC editing
            selectionManager.setSelection(null);
        }
    }

}
