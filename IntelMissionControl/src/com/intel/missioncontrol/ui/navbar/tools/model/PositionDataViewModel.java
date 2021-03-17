/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import javafx.beans.property.ObjectProperty;

/** Created by eivanchenko on 8/7/2017. */
public class PositionDataViewModel extends SmartDataViewModel<PositionData> {

    public PositionDataViewModel() {
        this(null);
    }

    public PositionDataViewModel(Mission mission) {
        super(mission, PositionData.class);
    }

    @Override
    protected ObjectProperty<PositionData> propertyToBind() {
        return getUav().positionDataRawProperty();
    }
}
