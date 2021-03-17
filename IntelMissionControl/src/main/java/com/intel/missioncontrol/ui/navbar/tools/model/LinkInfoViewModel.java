/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.plane.sendableobjects.LinkInfo;
import javafx.beans.property.ObjectProperty;

/** Created by eivanchenko on 8/7/2017. */
public class LinkInfoViewModel extends SmartDataViewModel<LinkInfo> {

    public LinkInfoViewModel() {
        this(null);
    }

    public LinkInfoViewModel(Mission mission) {
        super(mission, LinkInfo.class);
    }

    @Override
    protected ObjectProperty<LinkInfo> propertyToBind() {
        return getUav().linkInfoRawProperty();
    }
}
