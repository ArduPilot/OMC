/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.mission.AreaFilter;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AreaFilterViewModel implements ViewModel {

    private final BooleanProperty enabled = new SimpleBooleanProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty deleteDisabled = new SimpleBooleanProperty();

    private AreaFilter areaFilter;

    public AreaFilterViewModel(AreaFilter areaFilter) {
        this.areaFilter = areaFilter;
        enabled.bindBidirectional(areaFilter.enabledProperty());
        name.bindBidirectional(areaFilter.nameProperty());
        deleteDisabled.bindBidirectional(areaFilter.deleteDisabledPropery());
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public BooleanProperty deleteDisabledProperty() {
        return deleteDisabled;
    }

    public StringProperty nameProperty() {
        return name;
    }

    @SuppressLinter(value = "IllegalViewModelMethod", reviewer = "mstrauss", justification = "legacy file")
    public void delete() {
        areaFilter.delete();
    }

}
