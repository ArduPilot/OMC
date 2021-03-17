/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import eu.mavinci.core.flightplan.PlanType;
import javafx.scene.control.Label;

public class IntelLabel extends Label {

    private PlanType area;

    public IntelLabel(PlanType area) {
        this.area = area;
    }

    public PlanType getArea() {
        return area;
    }
}
