/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import javafx.beans.property.ObjectProperty;

/** Created by eivanchenko on 8/7/2017. */
public class AndroidStateViewModel extends SmartDataViewModel<AndroidState> {

    public AndroidStateViewModel() {
        this(null);
    }

    public AndroidStateViewModel(Mission mission) {
        super(mission, AndroidState.class);
    }

    @Override
    protected ObjectProperty<AndroidState> propertyToBind() {
        return getUav().androidStateRawproperty();
    }
}
