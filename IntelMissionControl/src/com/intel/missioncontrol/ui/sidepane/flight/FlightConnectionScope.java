/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FlightConnectionScope implements Scope {
    private StringProperty currentUavConnected = new SimpleStringProperty();

    public FlightConnectionScope() {}

    public StringProperty currentUavConnectedProperty() {
        return currentUavConnected;
    }
}
