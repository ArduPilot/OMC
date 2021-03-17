/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.map;

import com.intel.missioncontrol.custom.ActionDelegate;
import com.intel.missioncontrol.ui.ViewModelBase;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/** @author Vladimir Iordanov */
public class GpsLostEmergencyInstructionsViewModel extends ViewModelBase {

    private Property<ActionDelegate> closeDelegate = new SimpleObjectProperty<>();

    public Property<ActionDelegate> closeDelegateProperty() {
        return closeDelegate;
    }
}
