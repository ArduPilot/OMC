/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import eu.mavinci.core.flightplan.PlanType;
import javafx.scene.control.MenuItem;

public class IntelMenuItem extends MenuItem {

    private PlanType area;

    public IntelMenuItem(PlanType area) {
        this.area = area;
    }

    public PlanType getArea() {
        return area;
    }
}
