/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.plane.sendableobjects.BackendInfo;
import javafx.beans.property.ObjectProperty;

/** Created by eivanchenko on 8/7/2017. */
public class BackendInfoViewModel extends SmartDataViewModel<BackendInfo> {

    public BackendInfoViewModel() {
        this(null);
    }

    public BackendInfoViewModel(Mission mission) {
        super(mission, BackendInfo.class);
    }

    @Override
    protected ObjectProperty<BackendInfo> propertyToBind() {
        return getUav().backendInfoRawProperty();
    }
}
