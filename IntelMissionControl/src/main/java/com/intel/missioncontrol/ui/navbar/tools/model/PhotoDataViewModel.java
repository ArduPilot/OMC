/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import javafx.beans.property.ObjectProperty;

/** Created by eivanchenko on 8/7/2017. */
public class PhotoDataViewModel extends SmartDataViewModel<PhotoData> {

    public PhotoDataViewModel() {
        this(null);
    }

    public PhotoDataViewModel(Mission mission) {
        super(mission, PhotoData.class);
    }

    @Override
    protected ObjectProperty<PhotoData> propertyToBind() {
        return getUav().photoDataRawProperty();
    }
}
