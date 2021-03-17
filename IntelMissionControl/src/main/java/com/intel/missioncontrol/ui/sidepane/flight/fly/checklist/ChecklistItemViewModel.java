/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.checklist;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ChecklistItemViewModel implements ViewModel {

    private final StringProperty text = new SimpleStringProperty();
    private final BooleanProperty checked = new SimpleBooleanProperty();
    private final BooleanProperty disable = new SimpleBooleanProperty();

    public ChecklistItemViewModel(String text) {
        this.text.set(text);
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public boolean isChecked() {
        return checked.get();
    }

    public BooleanProperty checkedProperty() {
        return checked;
    }

    public boolean isDisabled() {
        return disable.get();
    }

    public BooleanProperty disableProperty() {
        return disable;
    }
}
