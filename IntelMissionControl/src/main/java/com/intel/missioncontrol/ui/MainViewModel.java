/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.VisibilityTracker;
import com.intel.missioncontrol.airspaces.services.Airmap2AirspaceService;
import com.intel.missioncontrol.api.IFlightPlanService;
import com.intel.missioncontrol.api.support.ISupportManager;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.DisplayDeviceInformation;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.linkbox.ILinkBoxConnectionService;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.annotation.airspaces.AirspacesToolTipController;
import com.intel.missioncontrol.map.credits.IMapCreditsManager;
import com.intel.missioncontrol.map.credits.MapCreditViewModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWMapModel;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.measure.AngleStyle;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionInfo;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.networking.MapTileDownloadStatusNotifier;
import com.intel.missioncontrol.networking.MapTileDownloadStatusSubscriber;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.settings.SrsSettings;
import com.intel.missioncontrol.ui.controls.StylesheetHelper;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.IVeryUglyDialogHelper;
import com.intel.missioncontrol.ui.dialogs.tasks.BackgroundTasksViewModel;
import com.intel.missioncontrol.ui.dialogs.tasks.LinkBoxStatusViewModel;
import com.intel.missioncontrol.ui.dialogs.tasks.MavlinkEventLogDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.warnings.WarningsPopoverViewModel;
import com.intel.missioncontrol.ui.menu.MainMenuCommandManager;
import com.intel.missioncontrol.ui.menu.MainMenuModel;
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
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
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
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedAsyncCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedDelegateCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedFutureCommand;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.desktop.gui.widgets.wkt.SpatialReferenceChooserViewModel;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import eu.mavinci.desktop.main.debug.profiling.requests.WWDredrawRequest;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import java.time.Duration;
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
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncListProperty;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ScopeProvider(scopes = {MainScope.class, PlanningScope.class, FlightScope.class})
public class MainViewModel extends DialogViewModel<Void, Void> {

    private static Logger LOGGER = LoggerFactory.getLogger(MainViewModel.class);
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
    private final ObjectProperty<MSpatialReference> srs = new SimpleObjectProperty<>();
    private final ObjectProperty<MSpatialReference> srsMission = new SimpleObjectProperty<>();
    private final BooleanProperty linkBoxConnected = new SimpleBooleanProperty(false);
    private final BooleanProperty linkBoxAuthorized = new SimpleBooleanProperty(false);
    private final Command changeSrsCommand;
    private final Command deleteSelectionCommand;
    private final Command cancelCommand;
    private final ParameterizedCommand<Object> mapActionCommand;
    private final Command reloadStylesheetsCommand;
    private final ParameterizedAsyncCommand<Point2D> showWarningsCommand;
    private final ParameterizedAsyncCommand<Point2D> showBackgroundTasksCommand;
    private final ParameterizedAsyncCommand<Point2D> showLinkBoxStatusCommand;
    private final ParameterizedAsyncCommand<Point2D> showMavlinkEventLogsCommand;
    private final Dispatcher mapDispatcher;
    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final IValidationService validationService;
    private final ILanguageHelper languageHelper;
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
    private final IWWMapModel mapModel;
    private final IWWMapView mapView;
    private final IMapController mapController;
    private final PositionFormatter positionFormatter;
    private final UIAsyncListProperty<MapCreditViewModel> mapCredits = new UIAsyncListProperty<>(this);
    private final NavigationRules navigationRules = new NavigationRules();
    private final Mission.Factory missionFactory;
    private final SrsSettings srsSettings;
    private final IFlightPlanService flightPlanService;
    private final ChangeListener<String> missionRenameListener =
        (observable, oldValue, newValue) -> title.set(createWindowTitle(newValue));
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();
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
    private final ILinkBoxConnectionService linkBoxConnectionService;

    @InjectScope
    private MainScope mainScope;

    @InjectScope
    private FlightScope flightScope;

    @InjectContext
    private Context context;

    private MainMenuCommandManager mainMenuCommandManager; // store a reference so it doesn't get GC'd
    private java.util.Timer updateStatisTimer;
    private WWDredrawRequest wwdRedrawRequest;
    private RenderingListener gpuPerformanceHintListener;

    @Inject
    public MainViewModel(
            @Named(MapModule.DISPATCHER) Dispatcher mapDispatcher,
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
            IWWMapView mapView,
            IMapController mapController,
            IElevationModel elevationModel,
            ISelectionManager selectionManager,
            ISrsManager srsManager,
            VisibilityTracker visibilityTracker,
            IMapCreditsManager mapCreditsManager,
            Mission.Factory missionFactory,
            ILinkBoxConnectionService linkBoxConnectionService) {
        this.mapDispatcher = mapDispatcher;
        this.mapModel = mapModel;
        this.mapView = mapView;
        this.mapController = mapController;
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
        this.coverageLegendVisible.bind(visibilityTracker.coverageLegendVisibleProperty());
        this.selectionManager = selectionManager;
        this.missionFactory = missionFactory;
        this.linkBoxConnectionService = linkBoxConnectionService;
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
                    Dispatcher.platform().runLater(() -> currentCoordinates.set(formattedPosition));
                });

        this.changeSrsCommand = new DelegateCommand(this::handleChangeSrs);
        this.deleteSelectionCommand = new DelegateCommand(mapModel::deleteSelectionAsync);
        this.cancelCommand = new DelegateCommand(this::cancelOperation);
        this.mapActionCommand = new ParameterizedDelegateCommand<>(this::onMapObjectAction);

        this.reloadStylesheetsCommand =
            new DelegateCommand(
                () -> new StylesheetHelper().reloadStylesheets(),
                settingsManager
                    .getSection(GeneralSettings.class)
                    .operationLevelProperty()
                    .isEqualTo(OperationLevel.DEBUG));

        this.showWarningsCommand = new ParameterizedFutureCommand<>(this::showWarningsAsync);
        this.showBackgroundTasksCommand = new ParameterizedFutureCommand<>(this::showBackgroundTasksAsync);
        this.showLinkBoxStatusCommand = new ParameterizedFutureCommand<>(this::showLinkBoxStatusAsync);
        this.showMavlinkEventLogsCommand = new ParameterizedFutureCommand<>(this::showMavlinkEventLogsAsync);

        //  >:/ This is ugly and stupid, but what's the 'correct' way to to this. Where does this get injected
        // from? Also, at least this limits the pollution to two classes
        Airmap2AirspaceService.initDownloadNotifier(mapTileDownloadStatusNotifier);
        subscribeToAirspaceDownloads(mapTileDownloadStatusNotifier);
        updateManager.setMainViewModel(this);
        missionManager.setMainViewModel(this);

        gamepadAvailable.bind(
            settingsManager.getSection(GeneralSettings.class).operationLevelProperty().isEqualTo(OperationLevel.DEBUG));
        idlAvailable.bind(
            settingsManager.getSection(GeneralSettings.class).operationLevelProperty().isEqualTo(OperationLevel.DEBUG));
        showMotorsOn.bind(
            settingsManager.getSection(GeneralSettings.class).operationLevelProperty().isEqualTo(OperationLevel.DEBUG));

        navigationRules.apply(navigationService, applicationContext, settingsManager);
        linkBoxConnected.bind(
            Bindings.createBooleanBinding(
                () ->
                    linkBoxConnectionService.linkBoxStatusProperty().get()
                        != ILinkBoxConnectionService.LinkBoxStatus.OFFLINE,
                linkBoxConnectionService.linkBoxStatusProperty()));

        linkBoxAuthorized.bind(
            Bindings.createBooleanBinding(
                () ->
                    linkBoxConnectionService.linkBoxStatusProperty().get()
                        != ILinkBoxConnectionService.LinkBoxStatus.UNAUTHENTICATED,
                linkBoxConnectionService.linkBoxStatusProperty()));
    }

    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    ReadOnlyStringProperty srsNameProperty() {
        return srsName;
    }

    ReadOnlyStringProperty currentStatusProperty() {
        return mainScope.mainMenuStatusTextProperty();
    }

    ReadOnlyObjectProperty<String[]> currentCoordinatesProperty() {
        return currentCoordinates;
    }

    ObjectProperty<MainStatus> mainStatusProperty() {
        return mainStatus;
    }

    ReadOnlyListProperty<MapCreditViewModel> mapCreditsProperty() {
        return mapCredits.getReadOnlyProperty();
    }

    ReadOnlyBooleanProperty backgroundTasksRunningProperty() {
        return backgroundTasksRunning;
    }

    ReadOnlyIntegerProperty warningsCountProperty() {
        return warningsCount;
    }

    ObjectProperty<ValidationMessageCategory> warningMessageCategoryProperty() {
        return warningMessageCategory;
    }

    ReadOnlyObjectProperty<NavBarDialog> currentNavBarDialogProperty() {
        return navigationService.navBarDialogProperty();
    }

    ReadOnlyObjectProperty<WorkflowStep> currentWorkflowStepProperty() {
        return navigationService.workflowStepProperty();
    }

    ReadOnlyObjectProperty<SidePaneTab> currentSidePaneTabProperty() {
        return navigationService.sidePaneTabProperty();
    }

    Property<Boolean> coverageLegendVisibleProperty() {
        return coverageLegendVisible;
    }

    ReadOnlyBooleanProperty demoMissionWarningVisibleProperty() {
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

    Command getChangeSrsCommand() {
        return changeSrsCommand;
    }

    Command getDeleteSelectionCommand() {
        return deleteSelectionCommand;
    }

    Command getCancelCommand() {
        return cancelCommand;
    }

    ParameterizedCommand<Object> getMapActionCommand() {
        return mapActionCommand;
    }

    ParameterizedAsyncCommand<Point2D> getShowWarningsCommand() {
        return showWarningsCommand;
    }

    ParameterizedAsyncCommand<Point2D> getShowBackgroundTasksCommand() {
        return showBackgroundTasksCommand;
    }

    ParameterizedAsyncCommand<Point2D> getShowLinkBoxStatusCommand() {
        return showLinkBoxStatusCommand;
    }

    public ParameterizedAsyncCommand<Point2D> getShowMavlinkEventLogsCommand() {
        return showMavlinkEventLogsCommand;
    }

    Command getReloadStylesheetsCommand() {
        return reloadStylesheetsCommand;
    }

    ReadOnlyBooleanProperty linkBoxConnectedProperty() {
        return linkBoxConnected;
    }

    ReadOnlyBooleanProperty linkBoxAuthorizedProperty() {
        return linkBoxAuthorized;
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
                hardwareConfigurationManager,
                flightScope);

        applicationContext.currentMissionProperty().addListener(this::currentMissionChanged);

        // Bind the warnings count to the output of the validation service.
        warningsCount.bind(
            Bindings.createIntegerBinding(
                () -> {
                    int warningCount = 0;
                    if (linkBoxConnectionService.linkBoxResolvableMessagesProperty().size() > 0) {
                        warningCount++;
                    }

                    switch (currentWorkflowStepProperty().get()) {
                    case PLANNING:
                        return warningCount + validationService.planningValidationMessagesProperty().size();
                    case FLIGHT:
                        return warningCount + validationService.flightValidationMessagesProperty().size();
                    case DATA_PREVIEW:
                        return warningCount + validationService.datasetValidationMessagesProperty().size();
                    default:
                        return warningCount;
                    }
                },
                validationService.planningValidationMessagesProperty(),
                validationService.flightValidationMessagesProperty(),
                validationService.datasetValidationMessagesProperty(),
                currentWorkflowStepProperty(),
                linkBoxConnectionService.linkBoxResolvableMessagesProperty().sizeProperty()));

        warningMessageCategory.bind(
            Bindings.createObjectBinding(
                () -> {
                    List<ResolvableValidationMessage> tmp =
                        new SimpleListProperty<>(FXCollections.observableArrayList());
                    if (linkBoxConnectionService.linkBoxResolvableMessagesProperty().size() > 0) {
                        tmp.addAll(linkBoxConnectionService.linkBoxResolvableMessagesProperty().get());
                    }

                    switch (currentWorkflowStepProperty().get()) {
                    default:
                    case PLANNING:
                        tmp.addAll(validationService.planningValidationMessagesProperty().get());
                        break;
                    case FLIGHT:
                        tmp.addAll(validationService.flightValidationMessagesProperty().get());
                        break;
                    case DATA_PREVIEW:
                        tmp.addAll(validationService.datasetValidationMessagesProperty().get());
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
                currentWorkflowStepProperty(),
                linkBoxConnectionService.linkBoxResolvableMessagesProperty().sizeProperty()));

        demoMissionWarningVisible.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .selectReadOnlyString(Mission::nameProperty)
                .isEqualTo(Mission.DEMO_MISSION_NAME));

        Dispatcher.platform().runLaterAsync(this::updateNetworkConnectivity, Duration.ZERO, Duration.ofMillis(250));

        title.set(createWindowTitle(null));

        srs.addListener((obj, oldVal, newVal) -> MainViewModel.this.srsName.set(newVal.toStringTiny()));
        if (srs.get() != null) {
            srsName.setValue(srs.get().toStringTiny());
        }
    }

    private Future<Void> showWarningsAsync(Point2D location) {
        return dialogService.requestPopoverDialogAsync(this, WarningsPopoverViewModel.class, location).cast();
    }

    private Future<Void> showBackgroundTasksAsync(Point2D location) {
        return dialogService.requestPopoverDialogAsync(this, BackgroundTasksViewModel.class, location).cast();
    }

    private Future<Void> showLinkBoxStatusAsync(Point2D location) {
        return dialogService.requestPopoverDialogAsync(this, LinkBoxStatusViewModel.class, () -> this, location).cast();
    }

    private Future<Void> showMavlinkEventLogsAsync(Point2D location) {
        return dialogService.requestPopoverDialogAsync(this, MavlinkEventLogDialogViewModel.class, location).cast();
    }

    @SuppressLinter(value = "IllegalViewModelMethod", reviewer = "mstrauss", justification = "Marked for removal.")
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

        gpuPerformanceHintListener =
            event -> {
                if (!event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP)) {
                    return;
                }

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
                            .setTimeout(Toast.MEDIUM_TIMEOUT)
                            .create());
                }

                worldWindow.removeRenderingListener(gpuPerformanceHintListener);
            };

        worldWindow.addRenderingListener(gpuPerformanceHintListener);
    }

    private void onMapObjectAction(Object mapObject) {
        if (mapObject instanceof MissionInfo) {
            MissionInfo missionInfo = (MissionInfo)mapObject;
            Mission mission = missionFactory.create(missionInfo);

            applicationContext
                .loadMissionAsync(mission)
                .whenSucceeded(
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
        } else if (!networkInformation.internetAvailableProperty().get()) {
            mainStatus.set(
                new MainStatus(
                    languageHelper.getString("eu.mavinci.desktop.gui.wwext.MStatusBar.noInternetConnection"),
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

    public MainScope getMainScope() {
        return mainScope;
    }

    public FlightScope getFlightScope() {
        return flightScope;
    }
}
