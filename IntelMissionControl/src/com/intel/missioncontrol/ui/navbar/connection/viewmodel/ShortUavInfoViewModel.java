/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.UnmannedAerialVehicle;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;

public class ShortUavInfoViewModel extends ViewModelBase {

    // for unit tests
    @InjectScope
    UavConnectionScope scope;

    public MapProperty<UavDataKey, String> uavDataProperty() {
        return scope.uavDataProperty();
    }

    public ObjectProperty<UnmannedAerialVehicle> selectedUavProperty() {
        return scope.selectedUavProperty();
    }

    public ObservableValue<? extends Number> getUpdateParametersProgress() {
        return scope.getUpdateParametersProgress();
    }
}
