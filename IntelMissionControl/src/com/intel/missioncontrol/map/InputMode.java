/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

public enum InputMode {
    DEFAULT,
    ADD_POINTS,
    SET_REF_POINT,
    SET_TAKEOFF_POINT,
    SET_LANDING_POINT,
    SET_MODEL_ORIGIN,
    ADD_MEASURMENT_POINTS,
    SET_SIMULATION_TAKEOFF;

    public boolean isSelecting() {
        return (this == DEFAULT || this == ADD_POINTS );
    }
}
