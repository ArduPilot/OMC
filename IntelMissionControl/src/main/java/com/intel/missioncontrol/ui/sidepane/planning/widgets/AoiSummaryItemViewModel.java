/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;

public class AoiSummaryItemViewModel implements ViewModel {

    private final Property<String> key = new SimpleStringProperty("");
    private final Property<String> value = new SimpleStringProperty("");

    public AoiSummaryItemViewModel() {}

    public AoiSummaryItemViewModel(String key, String value) {
        this.key.setValue(key);
        this.value.setValue(value);
    }

    public Property<String> keyProperty() {
        return key;
    }

    public Property<String> valueProperty() {
        return value;
    }

    public String getKey() {
        return key.getValue();
    }

    public String getValue() {
        return value.getValue();
    }

}
