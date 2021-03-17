/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.savechanges;

import com.intel.missioncontrol.mission.ISaveable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;

public class ChangedItemViewModel {

    private ReadOnlyStringWrapper name = new ReadOnlyStringWrapper();
    private BooleanProperty needsToSave = new SimpleBooleanProperty(true);

    private ISaveable changedItem;

    public ChangedItemViewModel(ISaveable changedItem) {
        this.changedItem = changedItem;
        name.set(changedItem.getName());
    }

    public ISaveable getChangedItem() {
        return changedItem;
    }

    public ReadOnlyStringProperty nameProperty() {
        return name.getReadOnlyProperty();
    }

    public BooleanProperty needsToSaveProperty() {
        return needsToSave;
    }
}
