/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import eu.mavinci.core.flightplan.PlanType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AoiBox extends VBox {

    private PlanType area;

    public AoiBox(PlanType area) {
        this.area = area;
    }

    public PlanType getArea() {
        return area;
    }
}
