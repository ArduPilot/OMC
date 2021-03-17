/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import javafx.beans.property.ObjectProperty;

/** Created by eivanchenko on 8/7/2017. */
public class PositionOrientationDataViewModel extends SmartDataViewModel<PositionOrientationData> {

    public PositionOrientationDataViewModel() {
        this(null);
    }

    public PositionOrientationDataViewModel(Mission mission) {
        super(mission, PositionOrientationData.class, null);
    }

    @Override
    protected ObjectProperty<PositionOrientationData> propertyToBind() {
        return getUav().positionOrientationDataRawProperty();
    }
}
