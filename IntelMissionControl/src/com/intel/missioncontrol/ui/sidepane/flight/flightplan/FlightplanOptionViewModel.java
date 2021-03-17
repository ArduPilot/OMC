/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.flightplan;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.Uav;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import com.intel.missioncontrol.ui.sidepane.flight.FlightIdlWorkflowScope;
import com.intel.missioncontrol.ui.sidepane.flight.PreflightChecks.IdlWorkflowState;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.flightplan.visitors.WaypointByIndexVisitor;
import eu.mavinci.core.plane.UavCommand;
import eu.mavinci.core.plane.sendableobjects.CommandResultData;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;

public class FlightplanOptionViewModel extends ViewModelBase {

    private static final int SEND_BUTTON_TIMER_MILLISECOND = 25000;
    private ObjectProperty<FlightPlan> currentFlightPlanSelection = new SimpleObjectProperty<>();
    private IntegerProperty currentWaypoint = new SimpleIntegerProperty(0);
    private IntegerProperty nextWaypoint = new SimpleIntegerProperty(0);
    private IntegerProperty waypointPositions = new SimpleIntegerProperty(0);
    private BooleanProperty isFPExecutionPaused = new SimpleBooleanProperty(false);
    private final ObjectProperty<Mission> mission = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Uav> uav = new SimpleObjectProperty<>();

    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;

    private ChangeListener<FlightPlan> flightPlanChangeListener =
        (observable, oldValue, newValue) -> {
            // set current waypoint
            waypointPositions.setValue(getCountOfWaypointsCurrentFP());
        };

    private Timer sendTimer = null;

    @InjectScope
    private PlanningScope planningScope;

    @InjectScope
    private FlightIdlWorkflowScope flightIdlWorkflowScope;

    @Inject
    public FlightplanOptionViewModel(IApplicationContext applicationContext, INavigationService navigationService) {
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;

        currentFlightPlanSelection.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .selectObject(Mission::currentFlightPlanProperty));
        currentFlightPlanSelection.addListener(new WeakChangeListener<>(flightPlanChangeListener));

        this.mission.addListener((observable, oldValue, newValue) -> missionChanged());
        uav.addListener((observable, oldValue, newValue) -> planeChanged());
    }

    public void initializeViewModel() {
        super.initializeViewModel();

        mission.bind(applicationContext.currentMissionProperty());
//        flightIdlWorkflowScope
//            .currentIdlWorkflowStateProperty()
//            .addListener(
//                (observable, oldValue, newValue) -> {
//                    if (newValue.equals(IdlWorkflowState.EXECUTING_FLIGHT_PLAN_PAUSED)) {
//                        isFPExecutionPaused.set(true);
//                    } else {
//                        isFPExecutionPaused.set(false);
//                    }
//                });
    }

    public ObjectProperty<FlightPlan> currentFlightplanSelectionProperty() {
        return currentFlightPlanSelection;
    }

    public Command getEditFlightplanCommand() {
        return editFlightplanCommand;
    }

    private Command editFlightplanCommand =
        new DelegateCommand(
            () ->
                new Action() {
                    @Override
                    protected void action() {
                        setEditFlightplanCommand();
                    }
                });

    private Command sendCommand =
        new DelegateCommand(
            () ->
                new Action() {
                    @Override
                    protected void action() {
                        sendFlightPlan();
                    }
                });

    private void sendFlightPlan() {
        Uav uav = applicationContext.getCurrentMission().uavProperty().get();
        if (uav == null) return;
        uav.commandResultDataObjectProperty().set(null);
        uav.getLegacyPlane().getFPmanager().sendFP(currentFlightPlanSelection.get().getLegacyFlightplan(), false);
        sendInProgressProperty().set(true);
        sendTimer = new Timer();

        TimerTask sendTask = new TimerTask()
        {
            public void run()
            {
                Platform.runLater(() -> {
                    sendInProgressProperty().set(false);
                    System.out.println("-------send timer is up----: " + SEND_BUTTON_TIMER_MILLISECOND + "milliseconds");
                    if (sendTimer != null) {
                        sendTimer.cancel();
                        sendTimer = null;
                    }
                });
            }

        };

        sendTimer.schedule(sendTask, SEND_BUTTON_TIMER_MILLISECOND);
        uav.commandResultDataObjectProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.uavCommand == UavCommand.SEND_MISSION) {
                switch(newValue.uavCommandResult) {
                    case SUCCESS:
                        System.out.println("-------received send ack SUCCESS!!!");
                        break;
                    case ERROR:
                        System.out.println("-------received send ack error :(");
                        break;
                    case DENIED:
                        System.out.println("-------received send ack denied :(");
                        break;
                    case INVALID:
                        System.out.println("-------received send ack invalid :(");
                        break;
                    case OTHER:
                        System.out.println("-------received send ack OTHER error");
                        break;
                    case TIMEOUT:
                    default:
                        System.out.println("-------received send ack timeout :(");
                        break;
                }
                sendInProgressProperty().set(false);
                if(sendTimer != null) {
                    sendTimer.cancel();
                    sendTimer = null;
                }
            }
        });
    }

    public Command getSendCommand() {
        return sendCommand;
    }

    private void setEditFlightplanCommand() {
        navigationService.navigateTo(SidePanePage.EDIT_FLIGHTPLAN);
    }

    public int getCountOfWaypointsCurrentFP() {
        FlightPlan flightPlan = currentFlightPlanSelection.get();
        if (flightPlan == null) {
            return 0;
        }

        WaypointByIndexVisitor wVisitor = new WaypointByIndexVisitor(1);
        wVisitor.startVisit(flightPlan.getLegacyFlightplan());
        return wVisitor.getCountOfWaypoints();
    }

    public IntegerProperty getCurrentWaypoint() {
        return currentWaypoint;
    }

    public IntegerProperty getWaypointCount() {
        return waypointPositions;
    }

    public ReadOnlyListProperty<FlightPlan> flightPlansProperty() {
        return PropertyPath.from(applicationContext.currentMissionProperty())
            .selectReadOnlyList(Mission::flightPlansProperty);
    }

    public DoubleExpression currentwpProgressProperty() {
        return Bindings.createDoubleBinding(
            () -> currentWaypoint.doubleValue() / waypointPositions.doubleValue(), currentWaypoint, waypointPositions);
    }

    public DoubleExpression nextwpProgressProperty() {
        return Bindings.createDoubleBinding(
            () -> {
                if (isFPExecutionPaused.get()) {
                    return nextWaypoint.doubleValue() / waypointPositions.doubleValue();
                } else {
                    return 0.0;
                }
            },
            nextWaypoint,
            waypointPositions);
    }

    public ReadOnlyBooleanProperty IsFPExecutionPausedProperty() {
        return isFPExecutionPaused;
    }

    public IntegerProperty nextWaypointProperty() {
        return nextWaypoint;
    }

//    public void setFPExecutionToPause(boolean bToPause) {
//        if (bToPause) {
//            // notify the scope
//            flightIdlWorkflowScope.currentIdlWorkflowStateProperty().set(IdlWorkflowState.EXECUTING_FLIGHT_PLAN_PAUSED);
//        } else {
//            // when reset has been clicked, leave the state as it is; expecting user to click on resume
//            nextWaypoint.set(0);
//        }
//    }

    public Mission getMission() {
        return mission.get();
    }

    private void missionChanged() {
        uav.unbind();
        currentWaypoint.unbind();
        Mission obtainMission = getMission();
        if (obtainMission != null) {
            uav.bind(obtainMission.uavProperty());
            currentWaypoint.bind(uav.get().getCurrentWaypointProperty());
        }
    }

    private void planeChanged() {}

    public BooleanProperty sendInProgressProperty() {
        return flightIdlWorkflowScope.sendFlightPlanInProgressProperty();
    }

}
