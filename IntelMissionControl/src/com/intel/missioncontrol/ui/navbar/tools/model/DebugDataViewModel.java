/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.sendableobjects.DebugData;
import javafx.beans.property.ObjectProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Created by eivanchenko on 8/7/2017. */
public class DebugDataViewModel extends SmartDataViewModel<DebugData> {

    private static final Map<String, Integer> ARRAYED_FIELDS;

    static {
        HashMap<String, Integer> fMap = new HashMap<>();
        fMap.put("accl", 3);
        fMap.put("manualServos", PlaneConstants.MANUAL_SERVO_COUNT);
        ARRAYED_FIELDS = Collections.unmodifiableMap(fMap);
    }

    public DebugDataViewModel() {
        this(null);
    }

    public DebugDataViewModel(Mission mission) {
        super(mission, DebugData.class, ARRAYED_FIELDS);
    }

    @Override
    protected ObjectProperty<DebugData> propertyToBind() {
        return getUav().debugDataRawProperty();
    }
}
