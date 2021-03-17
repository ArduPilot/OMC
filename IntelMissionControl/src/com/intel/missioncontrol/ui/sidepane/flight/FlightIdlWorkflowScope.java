/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import com.intel.missioncontrol.ui.sidepane.flight.PreflightChecks.IdlWorkflowState;
import de.saxsys.mvvmfx.Scope;
import eu.mavinci.core.plane.AirplaneFlightmode;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.sendableobjects.CommandResultData;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class FlightIdlWorkflowScope implements Scope {
    private ObjectProperty<IdlWorkflowState> currentIdlWorkflowState =
        new SimpleObjectProperty<>(IdlWorkflowState.MOTORS_OFF);
    private BooleanProperty sendFlightPlanInProgress = new SimpleBooleanProperty(false);

    public FlightIdlWorkflowScope() {}

    public ObjectProperty<IdlWorkflowState> currentIdlWorkflowStateProperty() {
        return currentIdlWorkflowState;
    }

    public BooleanProperty sendFlightPlanInProgressProperty() {
        return sendFlightPlanInProgress;
    }

    public void processUAVCommandResultData(CommandResultData commandResultData) {
        if (commandResultData == null) {
            System.out.println("CommandResultData is null-----");
            return;
        }

        switch (commandResultData.uavCommandResult) {
            case SUCCESS:
                System.out.println("-------received " + commandResultData.uavCommand.getDisplayName() + " ack SUCCESS!!!!");
                //update idlworkflow state
                switch(commandResultData.uavCommand) {
                    case TAKE_OFF:
                        currentIdlWorkflowStateProperty().set(IdlWorkflowState.TAKE_OFF_WITHOUT_RUN_FP_AUTOMATICALLY);
                        break;
                    case SEND_MISSION:
                        break;
                    case RUN_MISSION:
                    case RESUME_MISSION:
                        currentIdlWorkflowStateProperty().set(IdlWorkflowState.EXECUTING_FLIGHT_PLAN);
                        break;
                    case PAUSE_MISSION:
                        currentIdlWorkflowStateProperty().set(IdlWorkflowState.EXECUTING_FLIGHT_PLAN_PAUSED);
                        break;
                    case RETURN_TO_LAUNCH:
                        currentIdlWorkflowStateProperty().set(IdlWorkflowState.RETURN_TO_HOME);
                        break;
                    case UAV_LAND:
                        currentIdlWorkflowStateProperty().set(IdlWorkflowState.LANDING);
                        break;
                }
                break;
            default:
                System.out.println("-------received " + commandResultData.uavCommand.getDisplayName() + " ack error :(");
                break;
        }

    }

    public void updateCurrentIdlWorkflowStateWithFlightPhase(AirplaneFlightphase flightphase) {
        System.out.println("---flight phase: " + flightphase);
        switch (flightphase) {
            case ground:
                currentIdlWorkflowStateProperty().set(IdlWorkflowState.MOTORS_ON);
                break;
            case takeoff:
                currentIdlWorkflowStateProperty()
                        .set(IdlWorkflowState.TAKE_OFF_WITHOUT_RUN_FP_AUTOMATICALLY);
                break;
            case startFlight:
                currentIdlWorkflowStateProperty().set(IdlWorkflowState.EXECUTING_FLIGHT_PLAN);
                break;
            case airborne:
            case holdPosition:
                currentIdlWorkflowStateProperty().set(IdlWorkflowState.HOVER_ON_SPOT);
                break;
            case landing:
                currentIdlWorkflowStateProperty().set(IdlWorkflowState.LANDING);
                break;
            case returnhome:
                currentIdlWorkflowStateProperty().set(IdlWorkflowState.RETURN_TO_HOME);
                break;
            case descending:
            default:
                currentIdlWorkflowStateProperty().set(IdlWorkflowState.MOTORS_OFF);
                break;
        }
    }

    public void updateCurrentIdlWorkflowStateWithFlightMode(AirplaneFlightmode airplaneFlightmode) {
        System.out.println("---flight mode: " + airplaneFlightmode);
        switch(airplaneFlightmode) {
            case RTL:
                currentIdlWorkflowStateProperty().set(IdlWorkflowState.RETURN_TO_HOME);
                break;
            case Land:
                currentIdlWorkflowStateProperty().set(IdlWorkflowState.LANDING);
                break;
            case Brake:
                currentIdlWorkflowStateProperty().set(IdlWorkflowState.EXECUTING_FLIGHT_PLAN_PAUSED);
                break;
            case AutomaticFlight:
                currentIdlWorkflowStateProperty().set(IdlWorkflowState.EXECUTING_FLIGHT_PLAN);
                break;
            case Guided:
            case Loiter:
            case Placeholder4:
            case MotorShutdownNLocked:
            case ManualControl:
            case AssistedFlying:
                break;
        }
    }
}
