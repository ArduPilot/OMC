/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import com.intel.missioncontrol.ui.ViewModelBase;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class AlertAwareViewModel extends ViewModelBase {

    private final Property<AlertLevel> alertPropery = new SimpleObjectProperty<>();

    public Property<AlertLevel> alertPropery() {
        return alertPropery;
    }

}
