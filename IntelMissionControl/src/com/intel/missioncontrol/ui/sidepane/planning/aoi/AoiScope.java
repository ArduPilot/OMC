/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.intel.missioncontrol.geometry.AreaOfInterest;
import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

class AoiScope implements Scope {

    private final ObjectProperty<AreaOfInterest> areaOfInterest = new SimpleObjectProperty<>();

    public ObjectProperty<AreaOfInterest> areaOfInterestProperty() {
        return areaOfInterest;
    }

    public AreaOfInterest getAreaOfInterest() {
        return areaOfInterest.get();
    }

    public void setAreaOfInterest(AreaOfInterest areaOfInterest) {
        this.areaOfInterest.set(areaOfInterest);
    }

}
