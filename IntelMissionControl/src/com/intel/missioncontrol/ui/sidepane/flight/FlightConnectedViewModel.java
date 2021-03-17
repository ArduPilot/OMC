/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.api.LocateMeApi;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.Uav;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.EmergencyProceduresViewModel;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.ManualControlsDialogViewModel;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionScope;
import com.intel.missioncontrol.ui.navbar.layers.IMapClearingCenter;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import com.intel.missioncontrol.ui.sidepane.flight.PreflightChecks.IdlWorkflowState;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneFlightphase;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@ScopeProvider(scopes = FlightIdlWorkflowScope.class)
public class FlightConnectedViewModel extends ViewModelBase {

    @InjectScope
    private PlanningScope planningScope;

    @InjectScope
    private MainScope mainScope;

    @InjectScope
    private FlightConnectionScope flightConnectionScope;

    @InjectScope
    private UavConnectionScope connectionScope;

    @InjectScope
    private FlightIdlWorkflowScope flightIdlWorkflowScope;

    private IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private IDialogService dialogService;
    private final LocateMeApi locateMeApi;

    private final StringProperty systemStatus = new SimpleStringProperty();
    private final StringProperty batteryStatus = new SimpleStringProperty();
    private final ObjectProperty<Number> batteryPercentage = new SimpleObjectProperty<>();
    private final StringProperty rtkGpsStatus = new SimpleStringProperty();
    private final StringProperty onGroundStatus = new SimpleStringProperty();
    private final StringProperty altitudeAglStatus = new SimpleStringProperty();
    private final StringProperty remainingStatus = new SimpleStringProperty();
    private final StringProperty autoPilotStatus = new SimpleStringProperty();
    private final ObjectProperty<AlertLevel> alertBatteryLevel = new SimpleObjectProperty<>();
    private final ObjectProperty<Number> gpsLevel = new SimpleObjectProperty<>();
    private final ObjectProperty<AirplaneFlightphase> flightPhase = new SimpleObjectProperty<>();
    private final StringProperty uavIPandPort = new SimpleStringProperty("127.0.0.1:14550");

    private final ObjectProperty<Mission> mission = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Uav> uav = new SimpleObjectProperty<>();
    private StringProperty currentUavName = new SimpleStringProperty();
    private final ICommand showOnMapCommand;
    private final ICommand renameMissionCommand;

    private BooleanProperty isTakeoff = new SimpleBooleanProperty(false);
    private BooleanProperty isRunFlightplanAutomatically = new SimpleBooleanProperty(false);
    private BooleanProperty isFlightPlanExecutionCompleted = new SimpleBooleanProperty(false);
    private BooleanProperty isFlightPlanExecutionStarted = new SimpleBooleanProperty(false);

    private final ObjectProperty<AirplaneConnectorState> connectorState =
        new SimpleObjectProperty<>(AirplaneConnectorState.unconnected);

    @Inject
    public FlightConnectedViewModel(
            IApplicationContext applicationContext,
            ILanguageHelper languageHelper,
            IDialogService dialogService,
            LocateMeApi locateMeApi) {
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        this.locateMeApi = locateMeApi;
        this.dialogService = dialogService;

        this.mission.addListener((observable, oldValue, newValue) -> missionChanged());
        uav.addListener((observable, oldValue, newValue) -> planeChanged());

        this.showOnMapCommand =
            new DelegateCommand(
                () -> {
                    FlightPlan fp = planningScope.getCurrentFlightplan();
                    // TODO: fix this, APIs are missing
                    //                            if (fp != null) {
                    //                                locateMeApi.moveTo(
                    //                                        getWindowContext().getWorldWindow().getView(),
                    // fp.getSector(), fp.getMaxElev());
                    //                            }
                });

        renameMissionCommand = new DelegateCommand(applicationContext::renameCurrentMission);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        mission.bind(applicationContext.currentMissionProperty());
        currentUavName.bind(
            Bindings.createStringBinding(
                () -> {
                    return connectionScope.selectedUavProperty().get() == null
                        ? ""
                        : connectionScope.selectedUavProperty().get().name;
                },
                connectionScope.selectedUavProperty()));
        uavIPandPort.bind(
            Bindings.createStringBinding(
                () -> {
                    return connectionScope.selectedUavProperty().get() == null
                        ? ""
                        : (connectionScope.getConnectionError().get() == null
                            ? languageHelper.getString(
                                "com.intel.missioncontrol.ui.sidepane.flight.FlgihtConnectedView.connectingToIPandPort",
                                connectionScope.selectedUavProperty().get().connectionInfo.host,
                                connectionScope.selectedUavProperty().get().connectionInfo.port)
                            : languageHelper.getString(
                                    "com.intel.missioncontrol.ui.sidepane.flight.FlgihtConnectedView.connectingToIPandPort",
                                    connectionScope.selectedUavProperty().get().connectionInfo.host,
                                    connectionScope.selectedUavProperty().get().connectionInfo.port)
                                + connectionScope.getConnectionError().get());
                },
                connectionScope.selectedUavProperty(),
                connectionScope.getConnectionError()));

        alertBatteryLevel.set(AlertLevel.YELLOW);
    }

    public Mission getMission() {
        return mission.get();
    }

    public ICommand getShowOnMapCommand() {
        return showOnMapCommand;
    }

    public ICommand getRenameMissionCommand() {
        return renameMissionCommand;
    }

    public ReadOnlyStringProperty missionNameProperty() {
        return PropertyPath.from(applicationContext.currentMissionProperty())
            .selectReadOnlyString(Mission::nameProperty);
    }

    public ReadOnlyStringProperty currentUavNameProperty() {
        return currentUavName;
    }

    public ReadOnlyStringProperty systemStatusProperty() {
        return systemStatus;
    }

    public ReadOnlyStringProperty batteryStatusProperty() {
        return batteryStatus;
    }

    public ObjectProperty<Number> batteryPercentageProperty() {
        return batteryPercentage;
    }

    public ReadOnlyObjectProperty<Number> gpsLevelProperty() {
        return gpsLevel;
    }

    public ReadOnlyStringProperty rtkGpsStatusProperty() {
        return rtkGpsStatus;
    }

    public ReadOnlyStringProperty onGroundStatusProperty() {
        return onGroundStatus;
    }

    public ReadOnlyObjectProperty<AirplaneFlightphase> flightPhaseProperty() {
        return flightPhase;
    }

    public ReadOnlyStringProperty altitudeAglStatusProperty() {
        return altitudeAglStatus;
    }

    public ReadOnlyStringProperty remainingStatusProperty() {
        return remainingStatus;
    }

    public ReadOnlyStringProperty autoPilotStatusProperty() {
        return autoPilotStatus;
    }

    public BooleanProperty isTakeoffProperty() {
        return isTakeoff;
    }

    public BooleanProperty isRunFlightplanAutomaticallyProperty() {
        return isRunFlightplanAutomatically;
    }

    public ICommand getBackToDiscoveryCopterCommand() {
        return backToDiscoveryCopterCommand;
    }

    private ICommand backToDiscoveryCopterCommand =
        new DelegateCommand(
            () -> {
                // TODO: this API is missing fix this
                //                getNavigationService().navigateTo(SidePanePage.FLIGHT_DISCONNECTED)
            });

    private void bindUav() {
        connectorState.bind(uav.getValue().connectionProperty());

        connectorState.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != AirplaneConnectorState.fullyConnected) {
                    flightIdlWorkflowScope.currentIdlWorkflowStateProperty().set(IdlWorkflowState.MOTORS_OFF);
                }
            });

        batteryStatus.bind(
            Bindings.createStringBinding(
                () ->
                    String.format(
                        "%s %.2fV",
                        uav.getValue().batteryPercentageProperty().getValue(),
                        uav.getValue().batteryVoltageValueProperty().getValue()),
                uav.getValue().batteryPercentageProperty(),
                uav.getValue().batteryVoltageValueProperty()));

        batteryPercentage.bind(uav.get().batteryPercentageValueProperty());

        alertBatteryLevel.bind(
            Bindings.createObjectBinding(
                () -> {
                    if (uav.get().batteryPercentageValueProperty().getValue().intValue() <= 10) {
                        return AlertLevel.RED;
                    } else if (uav.get().batteryPercentageValueProperty().getValue().intValue() <= 25) {
                        return AlertLevel.YELLOW;
                    } else {
                        return AlertLevel.GREEN;
                    }
                },
                uav.get().batteryPercentageValueProperty()));

        rtkGpsStatus.bind(
            Bindings.createStringBinding(
                () -> uav.getValue().gpsQualityProperty().getValue().getName(), uav.getValue().gpsQualityProperty()));

        gpsLevel.bind(uav.get().gpsProperty());

        uav.get()
            .flightPhaseProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    flightIdlWorkflowScope.updateCurrentIdlWorkflowStateWithFlightPhase(newValue);
                });

        flightPhase.bind(uav.getValue().flightPhaseProperty());
        onGroundStatus.bind(
            Bindings.createStringBinding(
                () ->
                    uav.getValue().flightPhaseProperty().getValue() == null
                        ? "--"
                        : languageHelper.getString(
                            (uav.getValue().flightPhaseProperty().getValue().getDisplayNameKey())),
                uav.getValue().flightPhaseProperty()));

        altitudeAglStatus.bind(
            Bindings.createStringBinding(
                () ->
                    languageHelper.getString(
                            "com.intel.missioncontrol.ui.sidepane.flight.FlightConnectedView.status.altitudeAGL")
                        + uav.getValue().altitudeProperty().getValue(),
                uav.getValue().altitudeProperty()));
        remainingStatus.bind(
            Bindings.createStringBinding(
                () ->
                    languageHelper.getString(
                            "com.intel.missioncontrol.ui.sidepane.flight.FlightConnectedView.status.remainingFlightTime")
                        + (uav.getValue().flightTimeProperty().getValue() == null
                            ? "00:00"
                            : uav.getValue().flightTimeProperty().getValue()),
                uav.getValue().flightTimeProperty()));

        autoPilotStatus.bind(
            Bindings.createStringBinding(
                () ->
                    (uav.getValue().flightModeProperty().getValue() == null
                        ? "--"
                        : languageHelper.getString(uav.getValue().flightModeProperty().getValue().getDisplayNameKey())),
                uav.getValue().flightModeProperty()));

        systemStatus.bind(
            Bindings.createStringBinding(
                () -> {
                    if (!getIsBatteryGreaterThanZero().get()) { // not started yet
                        return languageHelper.getString(
                            "com.intel.missioncontrol.ui.sidepane.flight.FlightConnectedView.status.system.waitingUAVdata");
                    } else {
                        switch (alertBatteryLevelProperty().get()) {
                        case GREEN:
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.sidepane.flight.FlightConnectedView.status.system.ok");
                        case YELLOW:
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.sidepane.flight.FlightConnectedView.status.UAVbatteryWeak");
                        case RED:
                        default:
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.sidepane.flight.FlightConnectedView.status.UAVbatteryEmpety");
                        }
                    }
                },
                getIsBatteryGreaterThanZero(),
                alertBatteryLevelProperty()));

        uav.getValue()
            .flightModeProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    flightIdlWorkflowScope.updateCurrentIdlWorkflowStateWithFlightMode(newValue);
                });

        uav.get()
            .getUavMsgProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.startsWith("Mission Execution finished!")) {
                        currentIdlWorkflowStateProperty().set(IdlWorkflowState.FLIGHT_PLAN_COMPLETED);
                    }
                });

        this.mission
            .get()
            .currentFlightPlanProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    // TODO: fix this, seems to hide track
                    // clearTrack();
                });
    }

    private void clearTrack() {
        DependencyInjector.getInstance().getInstanceOf(IMapClearingCenter.class).clearTrackLog();
    }

    private void unbindUav() {
        batteryStatus.unbind();
        alertBatteryLevel.unbind();
        batteryPercentage.unbind();
        rtkGpsStatus.unbind();
        gpsLevel.unbind();
        flightPhase.unbind();
        onGroundStatus.unbind();
        altitudeAglStatus.unbind();
        remainingStatus.unbind();
        autoPilotStatus.unbind();
        systemStatus.unbind();
        connectorState.unbind();
    }

    private void missionChanged() {
        if (connectionScope.connectionStateProperty().get() == ConnectionState.CONNECTED) {
            System.out.println("-----connectionScope.connectionStateProperty()----CONNECTED!");
            connectionScope.setDisconnectUAVNow(true);
            flightIdlWorkflowScope.currentIdlWorkflowStateProperty().set(IdlWorkflowState.MOTORS_OFF);
        }

        uav.unbind();
        unbindUav();
        Mission obtainMission = getMission();
        if (obtainMission != null) {
            uav.bind(obtainMission.uavProperty());
            bindUav();
        }
    }

    private void planeChanged() {}

    public ObjectProperty<AlertLevel> alertBatteryLevelProperty() {
        return alertBatteryLevel;
    }

    private final ICommand disconnectCommand =
        new DelegateCommand(
            () -> {
                // TODO: call backend to disconnet UAV

            });

    // TODO add some binding for the enable state of this button
    private final ICommand takeoffCommand =
        new DelegateCommand(
            () -> {
                System.out.println("sent takeoffffff1");
                uav.get().commandResultDataObjectProperty().set(null);
                uav.get()
                    .commandResultDataObjectProperty()
                    .addListener(
                        (observable, oldValue, newValue) -> {
                            flightIdlWorkflowScope.processUAVCommandResultData(newValue);
                        });
                uav.get().getLegacyPlane().setFlightPhase(AirplaneFlightphase.takeoff);
            });

    private final ICommand abortTakeoffCommand =
        new DelegateCommand(
            () -> {
                // TODO: to call backend method to abort take off
                System.out.println("abort takeoffffff1");
            });

    private final ICommand runFlightplan =
        new DelegateCommand(
            () -> {
                System.out.println("*** start plan execution ***");
                flightIdlWorkflowScope.currentIdlWorkflowStateProperty().set(IdlWorkflowState.EXECUTING_FLIGHT_PLAN);
                uav.get().commandResultDataObjectProperty().set(null);
                uav.get()
                    .commandResultDataObjectProperty()
                    .addListener(
                        (observable, oldValue, newValue) -> {
                            flightIdlWorkflowScope.processUAVCommandResultData(newValue);
                        });
                uav.get().getLegacyPlane().setFlightPhase(AirplaneFlightphase.startFlight);
            });

    private final ICommand runNextFlightplan =
        new DelegateCommand(
            () -> { // TODO: To call backend to run next flight plan after current flight plan is done
                System.out.println("--RUN NEXT FLIGHT PLAN CALLED");
            });

    private final ICommand returnToHome =
        new DelegateCommand(
            () -> {
                System.out.println("--RETURN TO HOME CALLED");
                uav.get().commandResultDataObjectProperty().set(null);
                uav.get()
                    .commandResultDataObjectProperty()
                    .addListener(
                        (observable, oldValue, newValue) -> {
                            flightIdlWorkflowScope.processUAVCommandResultData(newValue);
                        });
                uav.get().getLegacyPlane().setFlightPhase(AirplaneFlightphase.returnhome);
            });

    private final ICommand land =
        new DelegateCommand(
            () -> {
                System.out.println("--LAND CALLED");
                uav.get().commandResultDataObjectProperty().set(null);
                uav.get()
                    .commandResultDataObjectProperty()
                    .addListener(
                        (observable, oldValue, newValue) -> {
                            flightIdlWorkflowScope.processUAVCommandResultData(newValue);
                        });
                isTakeoff.set(false);
                uav.get().getLegacyPlane().setFlightPhase(AirplaneFlightphase.jumpToLanding);
            });

    private final ICommand abortLanding =
        new DelegateCommand(
            () -> { // TODO: To call backend to abort landing
                System.out.println("--ABORT LANDING CALLED");
            });

    private final ICommand pauseFlight =
        new DelegateCommand(
            () -> {
                System.out.println("--PAUSE CALLED");
                uav.get().commandResultDataObjectProperty().set(null);
                uav.get()
                    .commandResultDataObjectProperty()
                    .addListener(
                        (observable, oldValue, newValue) -> {
                            flightIdlWorkflowScope.processUAVCommandResultData(newValue);
                        });
                uav.get().getLegacyPlane().setFlightPhase(AirplaneFlightphase.holdPosition);
            });

    private final ICommand ResumeFlight =
        new DelegateCommand(
            () -> {
                System.out.println("--RESUME CALLED");
                uav.get().commandResultDataObjectProperty().set(null);
                uav.get()
                    .commandResultDataObjectProperty()
                    .addListener(
                        (observable, oldValue, newValue) -> {
                            flightIdlWorkflowScope.processUAVCommandResultData(newValue);
                        });
                uav.get().getLegacyPlane().setFlightPhase(AirplaneFlightphase.startFlight);
            });

    private final ICommand flyByClickCommand =
        new DelegateCommand(
            () -> {
                // TODO: To call backend set to fly by click
            });

    private final ICommand manualControlsCommand =
        new DelegateCommand(
            () ->
                Futures.addCallback(
                    dialogService.requestDialog(this, ManualControlsDialogViewModel.class, true),
                    new FutureCallback<>() {
                        @Override
                        public void onSuccess(ManualControlsDialogViewModel manualControlsDialogViewModel) {}

                        @Override
                        public void onFailure(Throwable throwable) {}

                    }));

    private final ICommand emergencyCommand =
        new DelegateCommand(
            () -> {
                // TODO: To call backend set to emergency(to stop all procedures and just hover over the air)
            });

    private final ICommand emergencyDialog =
        new DelegateCommand(
            () ->
                Futures.addCallback(
                    dialogService.requestDialog(this, EmergencyProceduresViewModel.class, true),
                    new FutureCallback<>() {
                        @Override
                        public void onSuccess(EmergencyProceduresViewModel emergencyProceduresViewModel) {}

                        @Override
                        public void onFailure(Throwable throwable) {}

                    }));

    public ICommand getDisconnectCommand() {
        return disconnectCommand;
    }

    public ICommand getTakeoffCommand() {
        return takeoffCommand;
    }

    public ICommand getAbortTakeoffCommand() {
        return abortTakeoffCommand;
    }

    public ICommand getRunFlightplanCommand() {
        return runFlightplan;
    }

    public ICommand getRunNextFlightPlanCommand() {
        return runNextFlightplan;
    }

    public ICommand getReturnToHomeCommand() {
        return returnToHome;
    }

    public ICommand getLandCommand() {
        return land;
    }

    public ICommand getAbortLandingCommand() {
        return abortLanding;
    }

    public ICommand getPauseFlightCommand() {
        return pauseFlight;
    }

    public ICommand getResumeFlightCommand() {
        return ResumeFlight;
    }

    public ICommand getFlyByClickCommand() {
        return flyByClickCommand;
    }

    public ICommand getManualControlsCommand() {
        return manualControlsCommand;
    }

    public ICommand getEmergencyCommand() {
        return emergencyCommand;
    }

    public ICommand getEmergencyDialog() {
        return emergencyDialog;
    }

    public BooleanBinding isTakeOffWithoutRunFpAutomaticallyProperty() {
        return Bindings.and(isTakeoffProperty(), isRunFlightplanAutomaticallyProperty().not());
    }

    public BooleanBinding isTakeOffWithRunFpAutomaticallyProperty() {
        return Bindings.and(isTakeoffProperty(), isRunFlightplanAutomaticallyProperty());
    }

    public ObjectProperty<IdlWorkflowState> currentIdlWorkflowStateProperty() {
        return flightIdlWorkflowScope.currentIdlWorkflowStateProperty();
    }

    public BooleanProperty isFlightPlanExecutionCompletedProperty() {
        return isFlightPlanExecutionCompleted;
    }

    public BooleanProperty isFlightPlanExecutionStartedProperty() {
        return isFlightPlanExecutionStarted;
    }

    public BooleanExpression getShouldCoverPage() {
        return Bindings.createBooleanBinding(
            () -> connectorState.getValue() != AirplaneConnectorState.fullyConnected, connectorState);
    }

    public BooleanExpression getIsBatteryGreaterThanZero() {
        return Bindings.createBooleanBinding(
            () ->
                batteryPercentageProperty().get() == null ? false : batteryPercentageProperty().get().floatValue() > 0f,
            batteryPercentageProperty());
    }

    public StringProperty uavIPandPortProperty() {
        return uavIPandPort;
    }

    public BooleanProperty disableFlightFlowActions() {
        return flightIdlWorkflowScope.sendFlightPlanInProgressProperty();
    }
}
