/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.map;

import com.intel.missioncontrol.ui.ViewModelBase;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/** @author Vladimir Iordanov */
public class GpsLostEmergencyInstructionsViewModel extends ViewModelBase {

    private Property<Runnable> closeDelegate = new SimpleObjectProperty<>();

    public Property<Runnable> closeDelegateProperty() {
        return closeDelegate;
    }
}
