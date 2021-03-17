/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.intel.missioncontrol.geometry.AreaOfInterest;
import de.saxsys.mvvmfx.Scope;
import javafx.beans.value.ObservableValue;

import java.util.LinkedHashMap;
import java.util.Map;

public class AoiSummaryScope implements Scope {

    private AreaOfInterest areaOfInterest;
    private final Map<String, ObservableValue<String>> keyValues = new LinkedHashMap<>();

    public AoiSummaryScope(AreaOfInterest areaOfInterest) {
        this.areaOfInterest = areaOfInterest;
    }

    public AreaOfInterest getAreaOfInterest() {
        return areaOfInterest;
    }

    public void setAreaOfInterest(AreaOfInterest areaOfInterest) {
        this.areaOfInterest = areaOfInterest;
    }

    public Map<String, ObservableValue<String>> getKeyValues() {
        return keyValues;
    }

    public ObservableValue<String> getValue(String key) {
        return keyValues.get(key);
    }

}
