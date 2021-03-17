/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

import eu.mavinci.core.obfuscation.IKeepAll;

public enum AirplaneEventActions implements IKeepAll {
    ignore(0, "ignore"), // ==0
    circleDown(1, "circleDown"), // ==1
    positionHold(2, "positionHold"), // ==2
    returnToStart(3, "returnToStart"), // ==3
    jumpLanging(4, "jumpLanging"), // ==4
    // Copter options
    returnToStartOnSafetyAltitude(5, "Copter.returnToStartOnSafetyAltitude"), // ==5 Copter option
    positionHoldCopter(6, "Copter.positionHold"), // ==6
    circleDownCopter(7, "Copter.circleDown"), // ==7
    returnToStartCopter(8, "Copter.returnToStart"), // ==8
    ;
    private int value;
    private String displayNameKey;

    AirplaneEventActions(int value, String displayNameKey) {
        this.value = value;
        this.displayNameKey = displayNameKey;
    }

    public String getDisplayNameKey() {
        return displayNameKey;
    }
}
