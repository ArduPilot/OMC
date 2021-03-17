/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

public enum FlightValidatorType {
    COMBINED,
    HARDWARE_COMPATIBILITY,
    FLIGHTPLAN_WARNINGS,
    BATTERY,
    TAKEOFF_POSITION,
    DAYLIGHT,
    RESTRICTED_COUNTRY,
    SENSOR_CALIBRATION,
    REMOTE_CONTROL,
    AUTOMATIC_MODE,
    STORAGE,
    GNSS_FIX,
    CAMERA,
    ANNOYING_TEST
}
