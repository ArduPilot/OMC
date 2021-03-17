/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.ui.MainScope;
import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/** Currently this class is the only way to somehow bind Vertex table to context of editing AOI */
public class VertexTableScope implements Scope {
    private ObjectProperty<AreaOfInterest> currentAoi = new SimpleObjectProperty<>();
    private final MainScope mainScope;

    public static VertexTableScope forAoi(AreaOfInterest currentAoi, MainScope mainScope) {
        return new VertexTableScope(currentAoi, mainScope);
    }

    protected VertexTableScope(AreaOfInterest currentAoi, MainScope mainScope) {
        this.mainScope = mainScope;
        this.currentAoi.set(currentAoi);
    }

    public AreaOfInterest getCurrentAoi() {
        return currentAoi.get();
    }

    public MainScope getMainScope() {
        return mainScope;
    }

    public ObjectProperty<AreaOfInterest> currentAoiProperty() {
        return currentAoi;
    }
}
