/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

import com.intel.missioncontrol.Localizable;

public enum AirplaneEventActions implements Localizable {
    ignore(0, "ignore"), // ==0
    circleDown(1, "circleDown"), // ==1
    positionHold(2, "positionHold"), // ==2
    returnToStart(3, "returnToStart"), // ==3
    jumpLanging(4, "jumpLanging"), // ==4
    // Copter options
    positionHoldCopter(5, "Copter.positionHold"), // ==5
    returnToStartCopter(6, "Copter.returnToStart"), // ==6
    landImmediatelyCopter(7, "Copter.landImmediately"), // ==7
    warnCopter(8, "Copter.warn"), // ==8
    ignoreCopter(9, "Copter.ignore"), // ==9
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
