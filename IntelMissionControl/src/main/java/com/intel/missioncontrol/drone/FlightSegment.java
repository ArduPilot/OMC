/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.Localizable;
import eu.mavinci.core.obfuscation.IKeepAll;

/** Flight segment, independent of ArmedState and AutopilotState */
@Localizable
public enum FlightSegment implements IKeepAll {
    UNKNOWN,

    /** On ground before or after flight. */
    ON_GROUND,

    /** Ascent */
    TAKEOFF,

    /** Hold without any active task */
    HOLD,

    /** Running a flight plan */
    PLAN_RUNNING,

    /** Returning to home position" */
    RETURN_TO_HOME,

    /** Descent until touching down on ground */
    LANDING,

    /** Motors disarmed. Aka kill switch */
    LOCKDOWN
}
