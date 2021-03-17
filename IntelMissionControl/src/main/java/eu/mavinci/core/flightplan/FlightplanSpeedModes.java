/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import com.intel.missioncontrol.Localizable;

public enum FlightplanSpeedModes implements Localizable {
    MANUAL_CONSTANT, // manually the same for all waypoints
    AUTOMATIC_CONSTANT, // just use the one from platform description or maybe reduce it to some logic, but same for all
    // points
    AUTOMATIC_DYNAMIC; // compute individually max. speeds, but limit it by the manually given value

    public boolean isAutomaticallyAdjusting() {
        return this != MANUAL_CONSTANT;
    }

}
