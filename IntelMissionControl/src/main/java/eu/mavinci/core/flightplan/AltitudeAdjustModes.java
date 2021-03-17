/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import eu.mavinci.core.obfuscation.IKeepAll;
import eu.mavinci.core.plane.APTypes;
import java.util.LinkedList;

public enum AltitudeAdjustModes implements IKeepAll {
    CONSTANT_OVER_R, // over takeoff
    // contstantOnLine, // not used
    STEPS_ON_LINE, // not used
    FOLLOW_TERRAIN; // Follow terrain surface

    public static LinkedList<AltitudeAdjustModes> getSelectableValues(APTypes apType) {
        LinkedList<AltitudeAdjustModes> modes = new LinkedList<AltitudeAdjustModes>();
        modes.add(CONSTANT_OVER_R);
        if (apType.canLinearClimbOnLine()) {
            modes.add(AltitudeAdjustModes.FOLLOW_TERRAIN);
        }

        return modes;
    }

    public boolean isAltRelativeToStart() {
        return this == CONSTANT_OVER_R;
    }

    public boolean usesAbsoluteHeights() {
        return this != CONSTANT_OVER_R;
    }

    public boolean useLinearMode() {
        return this == FOLLOW_TERRAIN;
    }

    public boolean shouldRefine() {
        return this == FOLLOW_TERRAIN;
    }
}
