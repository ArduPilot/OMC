/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public enum LandingModes {
    DESC_CIRCLE, // =0 //copters will use this for custom auto landing location
    DESC_HOLDYAW, // =1 //copters will use this for auto landing on Same as actual takeoff location
    DESC_PARACHUTE, // =2 //was never in use!
    DESC_STAYAIRBORNE, // =3 //copters will stay airborne on last waypoint, fixedwing will go to startprocedure
                       // location==same as landing but stay on alt
    DESC_FULL3d, // =4
    ;

    public boolean usesYawAngle() {
        return (this == DESC_CIRCLE || this == LandingModes.DESC_FULL3d);
    }

    public boolean usesLandingAngle() {
        return (this == LandingModes.DESC_FULL3d);
    }

    public boolean isAutoLanding() {
        return (this != DESC_STAYAIRBORNE);
    }

    public boolean isLandingAltitudeRelevant() {
        return (this == DESC_STAYAIRBORNE || this == DESC_FULL3d);
    }

    public boolean hasPreApproach() {
        return (this == DESC_FULL3d);
    }

    public boolean isLandingAltitudeBreakoutRelevant() {
        return (this == LandingModes.DESC_CIRCLE
            || this == LandingModes.DESC_FULL3d
            || this == LandingModes.DESC_HOLDYAW);
    }

    public boolean hasUserDefinablePosition() {
        return (this != DESC_FULL3d);
    }

}
