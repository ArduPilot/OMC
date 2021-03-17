/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

public enum IMC_FLIGHTMODE {
    IMC_FLIGHTMODE_NOT_AVAILABLE("com.intel.missioncontrol.ui.sidepane.flight.IMC_FLIGHTMODE.NOT_AVAILABLE"),
    IMC_FLIGHTMODE_GPS("com.intel.missioncontrol.ui.sidepane.flight.IMC_FLIGHTMODE.GPS"),
    IMC_FLIGHTMODE_HEIGHT("com.intel.missioncontrol.ui.sidepane.flight.IMC_FLIGHTMODE.HEIGHT"),
    IMC_FLIGHTMODE_MANUAL("com.intel.missioncontrol.ui.sidepane.flight.IMC_FLIGHTMODE.MANUAL");

    private String displayedNameKey;

    public String getDisplayNameKey() {
        return displayedNameKey;
    }

    IMC_FLIGHTMODE(String s) {
        displayedNameKey = s;
    }
}