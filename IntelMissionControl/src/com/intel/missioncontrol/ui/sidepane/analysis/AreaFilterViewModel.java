/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.intel.missioncontrol.mission.AreaFilter;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AreaFilterViewModel implements ViewModel {

    private final BooleanProperty enabled = new SimpleBooleanProperty();
    private final StringProperty name = new SimpleStringProperty();

    private AreaFilter areaFilter;

    public AreaFilterViewModel(AreaFilter areaFilter) {
        this.areaFilter = areaFilter;
        enabled.bindBidirectional(areaFilter.enabledProperty());
        name.bindBidirectional(areaFilter.nameProperty());
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void delete() {
        areaFilter.delete();
    }

}
