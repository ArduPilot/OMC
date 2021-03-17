/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import eu.mavinci.core.plane.AirplaneEventActions;

final class PX4EmergencyActions {

    private PX4EmergencyActions() {}

    /**
     * Corresponds to px4 link_loss_actions_t in state_machine_helper.h, for use with NAV_RCL_ACT and NAV_DLL_ACT
     * parameters
     */
    public enum LinkLossAction {
        DISABLED(0),
        AUTO_LOITER(1),
        AUTO_RTL(2),
        AUTO_LAND(3),
        AUTO_RECOVER(4),
        TERMINATE(5),
        LOCKDOWN(6);

        private final int value;

        LinkLossAction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        static LinkLossAction fromValue(int value) {
            return (value >= 0 && value < values().length) ? values()[value] : null;
        }
    }

    // TODO: replace AirplaneEventActions
    static LinkLossAction convertLinkLossAction(AirplaneEventActions airplaneEventAction) {
        switch (airplaneEventAction) {
        case positionHold:
        case positionHoldCopter:
            return LinkLossAction.AUTO_LOITER;
        case returnToStart:
        case returnToStartCopter:
            return LinkLossAction.AUTO_RTL;
        case jumpLanging:
        case landImmediatelyCopter:
            return LinkLossAction.AUTO_LAND;
        case warnCopter:
        case ignoreCopter:
        case ignore:
        case circleDown:
        default:
            return LinkLossAction.DISABLED;
        }
    }

    /** Corresponds to px4 low_battery_action_tin state_machine_helper.h, for use with parameter COM_LOW_BAT_ACT */
    public enum LowBatAction {
        WARNING(0),
        RETURN(1),
        LAND(2),
        RETURN_OR_LAND(3);

        private final int value;

        LowBatAction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        static LowBatAction fromValue(int value) {
            return (value >= 0 && value < values().length) ? values()[value] : null;
        }
    }

    /** Corresponds to px4 geofence actions in geofence_result.h, for use with GF_ACTION parameter */
    public enum GeoFenceAction {
        NONE(0),
        WARN(1),
        LOITER(2),
        RTL(3),
        TERMINATE(4);

        private final int value;

        GeoFenceAction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        static GeoFenceAction fromValue(int value) {
            return (value >= 0 && value < values().length) ? values()[value] : null;
        }
    }

    // TODO: replace AirplaneEventActions
    static GeoFenceAction convertGeofenceAction(AirplaneEventActions airplaneEventAction) {
        switch (airplaneEventAction) {
        case positionHold:
        case positionHoldCopter:
            return GeoFenceAction.LOITER;
        case returnToStart:
        case returnToStartCopter:
            return GeoFenceAction.RTL;
        case jumpLanging:
        case landImmediatelyCopter:
        case warnCopter:
            return GeoFenceAction.WARN;
        case ignoreCopter:
        case ignore:
        case circleDown:
        default:
            return GeoFenceAction.NONE;
        }
    }

    /** Corresponds to px4 position control navigation loss response actions, for use with COM_POSCTL_NAVL parameter */
    public enum PositionLossAction {
        ALTITUDE_MODE_OR_MANUAL(0),
        LAND_OR_TERMINATE(1);

        private final int value;

        PositionLossAction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        static PositionLossAction fromValue(int value) {
            return (value >= 0 && value < values().length) ? values()[value] : null;
        }
    }

    static PositionLossAction convertPositionLossAction(AirplaneEventActions airplaneEventAction) {
        switch (airplaneEventAction) {
        case positionHold:
        case positionHoldCopter:
            return PositionLossAction.ALTITUDE_MODE_OR_MANUAL;
        case jumpLanging:
        case landImmediatelyCopter:
            return PositionLossAction.LAND_OR_TERMINATE;
        case returnToStart:
        case returnToStartCopter:
        case warnCopter:
        case ignoreCopter:
        case ignore:
        case circleDown:
        default:
            return PositionLossAction.LAND_OR_TERMINATE;
        }
    }
}
