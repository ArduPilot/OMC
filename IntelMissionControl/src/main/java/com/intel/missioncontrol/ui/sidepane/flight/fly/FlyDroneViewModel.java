/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.drone.AutopilotState;
import com.intel.missioncontrol.drone.DroneConnectionException;
import com.intel.missioncontrol.drone.FlightPlanWithWayPointIndex;
import com.intel.missioncontrol.drone.FlightSegment;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.connection.DroneMessage;
import com.intel.missioncontrol.drone.connection.IConnectionItem;
import com.intel.missioncontrol.drone.connection.IDroneConnectionService;
import com.intel.missioncontrol.drone.validation.IFlightValidationService;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.layers.aircraft.AircraftLayerVisibilitySettings;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.project.SuspendedInteractionRequest;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.menu.MenuModel;
import com.intel.missioncontrol.ui.navbar.layers.IMapClearingCenter;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import com.intel.missioncontrol.ui.sidepane.flight.fly.start.StartPlanDialogResult;
import com.intel.missioncontrol.ui.sidepane.flight.fly.start.StartPlanDialogViewModel;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.AsyncCommand;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.commands.FutureCommand;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.util.Duration;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;
import org.asyncfx.concurrent.AggregateException;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlyDroneViewModel extends ViewModelBase {

    @InjectScope
    private FlightScope flightScope;

    private static final Logger LOGGER = LoggerFactory.getLogger(FlyDroneViewModel.class);

    private final UIAsyncStringProperty missionName = new UIAsyncStringProperty(this);
    private final UIAsyncBooleanProperty landButtonVisibility = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty pausePlanButtonVisibility = new UIAsyncBooleanProperty(this);
    private final UIAsyncIntegerProperty activeNextWaypointIndex = new UIAsyncIntegerProperty(this);

    private final AsyncCommand takeoffCommand;
    private final AsyncCommand abortTakeoffCommand;
    private final AsyncCommand landCommand;
    private final AsyncCommand abortLandingCommand;
    private final AsyncCommand runFlightplanCommand;
    private final AsyncCommand pauseFlightPlanCommand;
    private final AsyncCommand returnToHomeCommand;
    private final Command showOnMapCommand;
    private final Command renameMissionCommand;

    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<Position> position = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<Quaternion> attitude = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<AutopilotState> autopilotState = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<FlightSegment> flightSegment = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<FlightPlan> activeFlightPlan = new UIAsyncObjectProperty<>(this);
    private final UIAsyncBooleanProperty startPlanDialogShowing = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty disallowFlight = new UIAsyncBooleanProperty(this);
    private final UIAsyncStringProperty flightDisallowedReason = new UIAsyncStringProperty(this);

    private final ReadOnlyAsyncObjectProperty<IHardwareConfiguration> hardwareConfiguration;

    private final IApplicationContext applicationContext;
    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final IDroneConnectionService droneConnectionService;
    private final INavigationService navigationService;
    private final IMapClearingCenter mapClearingCenter;
    private final AircraftLayerVisibilitySettings aircraftLayerVisibilitySettings;
    private final ListProperty<FlightPlan> availableFlightPlans = new SimpleListProperty<>();
    private FlyDroneMenuModel flyDroneMenuModel;

    private final List<WeakReference<Toast>> droneMessageToasts;
    private final SuspendedInteractionRequest suspendedInteractionRequest = new SuspendedInteractionRequest(0);
    private WeakReference<Toast> connectionErrorToast;

    @InjectScope
    private MainScope mainScope;

    @Inject
    public FlyDroneViewModel(
            IApplicationContext applicationContext,
            IDialogService dialogService,
            IDroneConnectionService droneConnectionService,
            INavigationService navigationService,
            ILanguageHelper languageHelper,
            AircraftLayerVisibilitySettings aircraftLayerVisibilitySettings,
            IMapView mapView,
            IElevationModel elevationModel,
            IFlightValidationService flightValidationService,
            IHardwareConfigurationManager hardwareConfigurationManager,
            IMapClearingCenter mapClearingCenter) {
        this.applicationContext = applicationContext;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        this.droneConnectionService = droneConnectionService;
        this.navigationService = navigationService;
        this.mapClearingCenter = mapClearingCenter;
        this.aircraftLayerVisibilitySettings = aircraftLayerVisibilitySettings;

        droneMessageToasts = new ArrayList<>();

        AsyncObjectProperty<Mission> mission = new UIAsyncObjectProperty<>(this);
        mission.bind(applicationContext.currentLegacyMissionProperty());

        missionName.bind(
            PropertyPath.from(applicationContext.currentLegacyMissionProperty()).selectReadOnlyString(Mission::nameProperty));

        availableFlightPlans.bind(PropertyPath.from(mission).selectReadOnlyList(Mission::flightPlansProperty));

        activeFlightPlan.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::activeFlightPlanProperty));

        activeNextWaypointIndex.bind(
            PropertyPath.from(drone).selectReadOnlyAsyncInteger(IDrone::activeFlightPlanWaypointIndexProperty));

        activeFlightPlan.addListener(
            (observable, oldValue, newValue) -> {
                if (oldValue == null && newValue != null) {
                    applicationContext.addToast(
                        Toast.of(ToastType.INFO)
                            .setText(languageHelper.getString(FlyDroneViewModel.class, "flightPlanStarted"))
                            .create());
                }
                // TODO .flightPlanFinished
            });

        drone.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    newValue.addListener(this::showDroneMessageToast);
                    newValue.addListener(this::onDroneConnectionException);
                }
            });

        position.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::positionProperty));
        position.addListener(
            (observable, oldValue, newValue) -> {
                // TODO remove legacy reference, i.e. make 3d model bindable to position property.
                if (newValue != null && mission.get() != null) {
                    // Set 3D model position on world map (only if no legacyAirplane is the source)
                    IAirplane legacyAirplane = mission.get().getLegacyPlane();
                    var positionData = new PositionData();
                    long time = System.currentTimeMillis();
                    positionData.time_sec = (int)(time / 1000);
                    positionData.time_usec = (int)(time - positionData.time_sec) * 1000;
                    positionData.lat = newValue.latitude.degrees;
                    positionData.lon = newValue.longitude.degrees;
                    positionData.altitude = (int)Math.round(newValue.elevation * 100.0);
                    legacyAirplane.getRootHandler().recv_position(positionData);

                    aircraftLayerVisibilitySettings.model3DProperty().set(true);
                    aircraftLayerVisibilitySettings.coveragePreviewProperty().set(false);

                    if (navigationService.getWorkflowStep() == WorkflowStep.FLIGHT) {
                        aircraftLayerVisibilitySettings.flightPlanProperty().set(true);
                    }
                } else {
                    aircraftLayerVisibilitySettings.model3DProperty().set(false);
                }
            },
            Dispatcher.platform());

        attitude.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::attitudeProperty));
        attitude.addListener(
            (observable, oldValue, newValue) -> {
                // TODO remove legacy reference, i.e. make 3d model bindable to attitude property.
                if (newValue != null && mission.get() != null) {
                    // Set 3D model orientation on world map (only if no legacyAirplane is the source)
                    IAirplane legacyAirplane = mission.get().getLegacyPlane();
                    OrientationData orientationData = new OrientationData();
                    Quaternion q = new Quaternion(newValue.z, newValue.y, newValue.x, newValue.w);

                    if (q.getRotationX() != null && q.getRotationY() != null && q.getRotationZ() != null) {
                        orientationData.yaw = q.getRotationX().getDegrees();
                        orientationData.pitch = q.getRotationY().getDegrees();
                        orientationData.roll = -q.getRotationZ().getDegrees();
                        orientationData.cameraRoll = orientationData.roll;
                        orientationData.cameraPitch = orientationData.pitch;
                        orientationData.cameraYaw = orientationData.yaw;
                    }

                    legacyAirplane.getRootHandler().recv_orientation(orientationData);
                } else {
                    aircraftLayerVisibilitySettings.model3DProperty().set(false);
                }
            },
            Dispatcher.platform());

        autopilotState.bind(
            PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::autopilotStateProperty, AutopilotState.UNKNOWN));
        autopilotState.addListener(
            (observable, oldValue, newValue) -> {
                if (oldValue == null || oldValue == AutopilotState.UNKNOWN) {
                    return;
                }

                switch (newValue) {
                case MANUAL:
                    applicationContext.addToast(
                        Toast.of(ToastType.INFO)
                            .setText(languageHelper.getString(FlyDroneViewModel.class, "switchedToManualMode"))
                            .create());
                    break;
                case AUTOPILOT:
                    applicationContext.addToast(
                        Toast.of(ToastType.INFO)
                            .setText(languageHelper.getString(FlyDroneViewModel.class, "switchedToAutopilotMode"))
                            .create());
                    break;
                default:
                    break;
                }
            });

        flightSegment.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::flightSegmentProperty));

        // If we switch to a "unsafe" flight segment, add a suspended interaction request.
        flightSegment.addListener(
            (observable, oldValue, newValue) -> {
                boolean oldSafeState =
                    oldValue == null || oldValue == FlightSegment.UNKNOWN || oldValue == FlightSegment.ON_GROUND;
                boolean newSafeState =
                    newValue == null || newValue == FlightSegment.UNKNOWN || newValue == FlightSegment.ON_GROUND;

                if (oldSafeState && !newSafeState) {
                    suspendedInteractionRequest.add();
                } else if (!oldSafeState && newSafeState) {
                    suspendedInteractionRequest.release();
                }
            });

        flightDisallowedReason.bind(
            Bindings.createStringBinding(
                () -> {
                    // Don't show a reason if in air
                    if (flightSegment.get() != FlightSegment.UNKNOWN
                            && flightSegment.get() != FlightSegment.ON_GROUND) {
                        return "";
                    }

                    var automaticChecksAlertStatus =
                        flightValidationService.combinedStatusProperty().get().getAlertType();
                    if (automaticChecksAlertStatus == AlertType.NONE
                            || automaticChecksAlertStatus == AlertType.LOADING
                            || automaticChecksAlertStatus == AlertType.ERROR) {
                        return languageHelper.getString(
                            FlyDroneViewModel.class, "flightDisallowedReason.safetyChecksError");
                    }

                    var apState = autopilotState.get();
                    if (apState != AutopilotState.AUTOPILOT) {
                        return languageHelper.getString(
                            FlyDroneViewModel.class, "flightDisallowedReason.notInAutopilotMode");
                    }

                    return "";
                },
                autopilotState,
                flightSegment,
                flightValidationService.combinedStatusProperty()));

        disallowFlight.bind(flightDisallowedReason.isNotEmpty());

        final Toast[] flightSegmentToast = new Toast[1];
        flightSegment.addListener(
            (observableValue, oldVal, newVal) -> {
                if (newVal != oldVal) {
                    if (flightSegmentToast[0] != null) {
                        flightSegmentToast[0].dismiss();
                    }

                    if (newVal == FlightSegment.LANDING) {
                        flightSegmentToast[0] = showToast("landing");
                    } else if (newVal == FlightSegment.TAKEOFF) {
                        flightSegmentToast[0] = showToast("takeoff");
                    }
                }
            });

        hardwareConfiguration =
            PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::hardwareConfigurationProperty);

        hardwareConfiguration.addListener(
            (observableValue, oldVal, newVal) -> {
                if (mission.get() != null && newVal != null) {
                    // Set 3D model position on world map (only if no legacyAirplane is the source)
                    IAirplane legacyAirplane = mission.get().getLegacyPlane();
                    if (legacyAirplane != null) {
                        legacyAirplane.setHardwareConfiguration(newVal);
                    }
                }
            });

        renameMissionCommand = new DelegateCommand(applicationContext::renameCurrentMission);

        takeoffCommand =
            new FutureCommand(
                () ->
                    Dispatcher.platform()
                        .runLaterAsync(() -> startPlanDialogShowing.set(true))
                        .thenGetAsync(
                            () ->
                                dialogService
                                    .requestDialogAsync(this, StartPlanDialogViewModel.class, true)
                                    .whenDone(vv -> startPlanDialogShowing.set(false)))
                        .thenAcceptAsync(
                            viewModel -> {
                                StartPlanDialogResult dialogResult;
                                dialogResult = viewModel.getDialogResult();

                                if (dialogResult != null) {
                                    FlightPlanWithWayPointIndex fp = flightPlanFromDialogResult(dialogResult);

                                    return showPreparingFlightToastIfTimeConsumingAsync(drone.get().takeOffAsync(fp));
                                }

                                return Futures.successful();
                            })
                        .whenCancelled(() -> LOGGER.debug("takeoffCommand cancelled")),
                this::onCommandError,
                flightSegment.isEqualTo(FlightSegment.ON_GROUND).and(disallowFlight.not()));

        abortTakeoffCommand =
            new FutureCommand(
                () -> drone.get().abortTakeOffAsync(),
                this::onCommandError,
                flightSegment.isEqualTo(FlightSegment.TAKEOFF).and(autopilotState.isEqualTo(AutopilotState.AUTOPILOT)));

        landCommand =
            new FutureCommand(
                () -> drone.get().landAsync(),
                this::onCommandError,
                flightSegment
                    .isEqualTo(FlightSegment.HOLD)
                    .or(flightSegment.isEqualTo(FlightSegment.PLAN_RUNNING))
                    .or(flightSegment.isEqualTo(FlightSegment.RETURN_TO_HOME))
                    .and(autopilotState.isEqualTo(AutopilotState.AUTOPILOT)));

        runFlightplanCommand =
            new FutureCommand(
                () ->
                    Dispatcher.platform()
                        .runLaterAsync(() -> startPlanDialogShowing.set(true))
                        .thenGetAsync(
                            f ->
                                dialogService
                                    .requestDialogAsync(this, StartPlanDialogViewModel.class, true)
                                    .whenDone(vv -> startPlanDialogShowing.set(false)))
                        .thenAcceptAsync(
                            viewModel -> {
                                StartPlanDialogResult dialogResult;
                                dialogResult = viewModel.getDialogResult();

                                if (dialogResult != null) {
                                    FlightPlanWithWayPointIndex fp = flightPlanFromDialogResult(dialogResult);

                                    return showPreparingFlightToastIfTimeConsumingAsync(
                                        drone.get().startFlightPlanAsync(fp));
                                }

                                return Futures.successful(null);
                            })
                        .whenCancelled(() -> LOGGER.info("runFlightplanCommand cancelled")),
                this::onCommandError,
                flightSegment.isEqualTo(FlightSegment.HOLD).and(disallowFlight.not()));

        pauseFlightPlanCommand =
            new FutureCommand(
                () -> drone.get().pauseFlightPlanAsync(),
                this::onCommandError,
                flightSegment
                    .isEqualTo(FlightSegment.PLAN_RUNNING)
                    .or(flightSegment.isEqualTo(FlightSegment.RETURN_TO_HOME))
                    .and(autopilotState.isEqualTo(AutopilotState.AUTOPILOT)));

        returnToHomeCommand =
            new FutureCommand(
                () -> drone.get().returnHomeAsync(),
                this::onCommandError,
                flightSegment
                    .isEqualTo(FlightSegment.HOLD)
                    .or(flightSegment.isEqualTo(FlightSegment.PLAN_RUNNING))
                    .and(autopilotState.isEqualTo(AutopilotState.AUTOPILOT)));

        abortLandingCommand =
            new FutureCommand(
                () -> drone.get().abortLandingAsync(),
                this::onCommandError,
                flightSegment.isEqualTo(FlightSegment.LANDING).and(autopilotState.isEqualTo(AutopilotState.AUTOPILOT)));

        landButtonVisibility.bind(getAbortLandingCommand().notExecutableProperty());
        pausePlanButtonVisibility.bind(
            getTakeoffCommand()
                .notExecutableProperty()
                .and(getTakeoffCommand().notRunningProperty())
                .and(getAbortTakeoffCommand().notExecutableProperty())
                .and(getAbortTakeoffCommand().notRunningProperty())
                .and(getRunFlightplanCommand().notExecutableProperty())
                .and(getRunFlightplanCommand().notRunningProperty()));

        showOnMapCommand =
            new DelegateCommand(
                () -> {
                    IDrone d = drone.get();
                    if (d == null) return;
                    Position p = d.positionProperty().get();
                    if (p == null) return;
                    IAirplane legacyAirplane = mission.get().getLegacyPlane();
                    try {
                        double offset = legacyAirplane.getAirplaneCache().getStartElevOverWGS84();
                        p = new Position(p, offset + p.elevation);
                        mapView.goToPositionAsync(p);

                    } catch (AirplaneCacheEmptyException e) {
                        p = elevationModel.getPositionOnGround(p);
                    }

                    mapView.goToPositionAsync(p);
                },
                PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::positionProperty).isNotNull());
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        flightScope.flightSegmentProperty().bind(flightSegment);

        drone.bind(flightScope.currentDroneProperty());

        flyDroneMenuModel =
            new FlyDroneMenuModel(
                dialogService,
                languageHelper,
                droneConnectionService,
                navigationService,
                mapClearingCenter,
                aircraftLayerVisibilitySettings,
                flightScope,
                this);
    }

    private <T> Future<T> showPreparingFlightToastIfTimeConsumingAsync(Future<T> future) {
        java.time.Duration delay = java.time.Duration.ofMillis(2000);

        Dispatcher dispatcher = Dispatcher.background();
        Future<Void> fDelay =
            dispatcher.runLaterAsync(
                () -> {
                    Toast toast =
                        Toast.of(ToastType.INFO)
                            .setCloseable(true)
                            .setTimeout(Duration.INDEFINITE)
                            .setText(languageHelper.getString(FlyDroneViewModel.class, "preparingForFlight"))
                            .setAction(
                                languageHelper.getString(FlyDroneViewModel.class, "cancel"),
                                false,
                                true,
                                () -> future.cancel(false),
                                MoreExecutors.directExecutor())
                            .create();

                    future.whenDone(f -> toast.dismiss());

                    applicationContext.addToast(toast);
                },
                delay);

        future.whenDone(f -> fDelay.cancel(false));
        return future;
    }

    private Toast showToast(String messageKey) {
        Toast toast =
            Toast.of(ToastType.INFO)
                .setCloseable(true)
                .setTimeout(Duration.INDEFINITE)
                .setText(languageHelper.getString(FlyDroneViewModel.class, messageKey))
                .create();

        Dispatcher.background()
            .runLaterAsync(() -> applicationContext.addToast(toast), java.time.Duration.ofMillis(10));
        return toast;
    }

    private FlightPlanWithWayPointIndex flightPlanFromDialogResult(StartPlanDialogResult dialogResult) {
        switch (dialogResult.getStartPlanType()) {
        case RESUME_PLAN:
            return new FlightPlanWithWayPointIndex(activeFlightPlan.get(), activeNextWaypointIndex.getValue());
        case START_PLAN_FROM_BEGINNING:
            return new FlightPlanWithWayPointIndex(flightScope.selectedFlightPlanProperty().get(), 0);
        case START_PLAN_FROM_WAYPOINT:
            return new FlightPlanWithWayPointIndex(
                flightScope.selectedFlightPlanProperty().get(), dialogResult.getStartingWaypoint() - 1);
        default:
            throw new IllegalArgumentException("Invalid StartPlanType");
        }
    }

    private void showDroneMessageToast(@SuppressWarnings("unused") IDrone sender, DroneMessage message) {
        if (!Platform.isFxApplicationThread()) {
            Dispatcher.platform().runLater(() -> showDroneMessageToast(sender, message));
            return;
        }

        ToastType toastType = message.getSeverity() == DroneMessage.Severity.INFO ? ToastType.INFO : ToastType.ALERT;

        String msg = message.getMessage();

        // filter some messages
        if (msg == null || message.getSeverity() == DroneMessage.Severity.INFO) {
            return;
        }

        Duration timeout = Duration.seconds(10.0);
        String text = languageHelper.getString(FlyDroneViewModel.class, "droneMessage", msg);

        // Refresh toast timeout if we have a toast with this text showing already:
        var it = droneMessageToasts.listIterator();
        while (it.hasNext()) {
            Toast toast = it.next().get();
            if (toast == null) {
                it.remove();
            } else if (toast.isShowingProperty().get() && toast.getText().equals(text)) {
                Toast.Accessor.setRemainingTime(toast, (int)timeout.toMillis());
                return;
            }
        }

        Toast toast =
            Toast.of(toastType).setShowIcon(toastType == ToastType.ALERT).setText(text).setTimeout(timeout).create();

        droneMessageToasts.add(new WeakReference<>(toast));
        applicationContext.addToast(toast);
    }

    private void onDroneConnectionException(IDrone sender, DroneConnectionException e) {
        showErrorWithReconnectToast(e, e.isRecoverable(), droneConnectionService.getConnectionItemForDrone(sender));
    }

    private void showErrorWithReconnectToast(Throwable e, boolean isRecoverable, IConnectionItem connectionItem) {
        if (!Platform.isFxApplicationThread()) {
            Dispatcher.platform().runLater(() -> showErrorWithReconnectToast(e, isRecoverable, connectionItem));
            return;
        }

        String actionString;
        Runnable onAction;

        Throwable ex = e;
        if (ex instanceof ExecutionException) {
            ex = ex.getCause();
        }

        // TODO: default messages depending on exception type
        String message;

        if (ex instanceof DroneConnectionException) {
            message = languageHelper.getString(FlyDroneViewModel.class, "linkLost");
        } else {
            message = ex.getMessage();
        }

        Toast toast;

        if (isRecoverable) {
            toast = Toast.of(ToastType.ALERT).setShowIcon(true).setCloseable(true).setText(message).create();
        } else {
            actionString = languageHelper.getString(FlyDroneViewModel.class, "reconnect");
            onAction =
                () ->
                    // reconnect
                    droneConnectionService
                        .disconnectAsync(drone.get())
                        .whenDone(
                            f ->
                                droneConnectionService
                                    .connectAsync(connectionItem)
                                    .whenSucceeded(
                                        drone -> {
                                            flightScope.currentDroneProperty().set(drone);
                                            navigationService.navigateTo(SidePanePage.FLY_DRONE);
                                        })
                                    .whenFailed(
                                        err -> {
                                            navigationService.navigateTo(SidePanePage.CONNECT_DRONE);
                                            showErrorWithReconnectToast(err, false, connectionItem);
                                        }));
            toast =
                Toast.of(ToastType.ALERT)
                    .setShowIcon(true)
                    .setCloseable(true)
                    .setTimeout(Duration.INDEFINITE)
                    .setText(message)
                    .setAction(actionString, false, true, onAction, MoreExecutors.directExecutor())
                    .create();
        }

        Toast previousToast = connectionErrorToast == null ? null : connectionErrorToast.get();
        if (previousToast != null) {
            previousToast.dismiss();
        }

        connectionErrorToast = new WeakReference<>(toast);

        applicationContext.addToast(toast);
    }

    private synchronized void onCommandError(Throwable e) {
        String msg;
        if (e instanceof AggregateException) {
            e = ((AggregateException)e).getFirstCause();
        }

        if (e instanceof CancellationException) {
            return;
        }

        if (e instanceof TimeoutException) {
            msg = languageHelper.getString(FlyDroneViewModel.class, "commandTimeout");
        } else {
            msg = e.getMessage();
            if (msg == null || msg.isEmpty()) {
                LOGGER.error("onCommandError empty message: ", e);
                msg = e.toString();
            }

            msg = "Error: " + msg;
        }

        LOGGER.debug(msg, e);

        final String errorMessage = msg;

        applicationContext.addToast(Toast.of(ToastType.ALERT).setShowIcon(true).setText(errorMessage).create());
    }

    public MainScope getScope() {
        return mainScope;
    }

    MenuModel getFlyDroneMenuModel() {
        return flyDroneMenuModel;
    }

    public Command getRenameMissionCommand() {
        return renameMissionCommand;
    }

    ReadOnlyListProperty<FlightPlan> availableFlightPlansListProperty() {
        return availableFlightPlans;
    }

    ReadOnlyProperty<Boolean> landButtonVisibilityProperty() {
        return landButtonVisibility;
    }

    ReadOnlyProperty<Boolean> startPlanDialogShowingProperty() {
        return startPlanDialogShowing;
    }

    ReadOnlyProperty<Boolean> pausePlanButtonVisibilityProperty() {
        return pausePlanButtonVisibility;
    }

    ReadOnlyProperty<Boolean> disallowFlightProperty() {
        return disallowFlight;
    }

    ReadOnlyProperty<String> flightDisallowedReasonProperty() {
        return flightDisallowedReason;
    }

    ReadOnlyProperty<String> missionNameProperty() {
        return missionName;
    }

    AsyncCommand getTakeoffCommand() {
        return takeoffCommand;
    }

    AsyncCommand getAbortTakeoffCommand() {
        return abortTakeoffCommand;
    }

    AsyncCommand getRunFlightplanCommand() {
        return runFlightplanCommand;
    }

    AsyncCommand getReturnToHomeCommand() {
        return returnToHomeCommand;
    }

    AsyncCommand getLandCommand() {
        return landCommand;
    }

    AsyncCommand getAbortLandingCommand() {
        return abortLandingCommand;
    }

    AsyncCommand getPauseFlightPlanCommand() {
        return pauseFlightPlanCommand;
    }

    Command getShowOnMapCommand() {
        return showOnMapCommand;
    }

}
