/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.checklist;

import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ChecklistScope implements Scope {

    private ObjectProperty<ListProperty<ChecklistViewModel>> currentChecklist = new SimpleObjectProperty<>();
    private IntegerProperty checkedCount = new SimpleIntegerProperty(0);
    private IntegerProperty totalCount = new SimpleIntegerProperty(0);

    public ObjectProperty<ListProperty<ChecklistViewModel>> currentChecklistProperty() {
        return currentChecklist;
    }

    public IntegerProperty checkedCountProperty() {
        return checkedCount;
    }

    public IntegerProperty totalCountProperty() {
        return totalCount;
    }
}
