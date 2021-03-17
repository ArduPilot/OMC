/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import com.intel.missioncontrol.Localizable;

public enum LandingModes implements Localizable {
    CUSTOM_LOCATION, // copters will use this for custom auto landing location
    LAND_AT_TAKEOFF, // copters will use this for auto landing on Same as actual takeoff location
    LAST_WAYPOINT, // copters will stay airborne on last waypoint, fixedwing will go to startprocedure
    ;
}
