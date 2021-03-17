/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionScope;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.property.MapProperty;

public class UavConnectorInfoViewModel extends ViewModelBase {

    // for unit tests
    @InjectScope
    protected UavConnectionScope scope;

    public MapProperty<UavDataKey, String> uavDataProperty() {
        return scope.uavDataProperty();
    }
}
