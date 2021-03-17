/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import javafx.beans.property.ObjectProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Created by eivanchenko on 8/7/2017. */
public class OrientationDataViewModel extends SmartDataViewModel<OrientationData> {

    private static final Map<String, Integer> ARRAYED_FIELDS;

    static {
        HashMap<String, Integer> fMap = new HashMap<>();
        fMap.put("accl", 3);
        fMap.put("manualServos", PlaneConstants.MANUAL_SERVO_COUNT);
        ARRAYED_FIELDS = Collections.unmodifiableMap(fMap);
    }

    public OrientationDataViewModel() {
        this(null);
    }

    public OrientationDataViewModel(Mission mission) {
        super(mission, OrientationData.class, ARRAYED_FIELDS);
    }

    @Override
    protected ObjectProperty<OrientationData> propertyToBind() {
        return getUav().orientationDataRawproperty();
    }
}
