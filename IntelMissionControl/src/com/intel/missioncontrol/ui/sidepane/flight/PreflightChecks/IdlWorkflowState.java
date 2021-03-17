/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.PreflightChecks;

/*IdlWorkflowState
 * - represents state responded from backend
 * - each state can trigger different UI representation
 * - each enum state may not be dependency of one before it, however, the order is in start to end of the idl workflow
 *
 *
 * Author: valina li
 * */
public enum IdlWorkflowState {
    MOTORS_OFF,
    MOTORS_ON,
    TAKE_OFF_WITH_RUN_FP_AUTOMATICALLY,
    TAKE_OFF_WITHOUT_RUN_FP_AUTOMATICALLY,
    HOVER_ON_SPOT,
    EXECUTING_FLIGHT_PLAN,
    EXECUTING_FLIGHT_PLAN_PAUSED,
    FLIGHT_PLAN_COMPLETED,
    RETURN_TO_HOME,
    LANDING
}
