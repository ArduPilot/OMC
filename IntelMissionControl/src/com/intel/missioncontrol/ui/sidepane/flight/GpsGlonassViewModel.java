/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class GpsGlonassViewModel extends AlertAwareViewModel {

    private final IntegerProperty gpsProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty glonassProperty = new SimpleIntegerProperty(0);

    public GpsGlonassViewModel() {
        alertPropery().setValue(AlertLevel.RED);
    }

    public IntegerProperty gpsProperty() {
        return gpsProperty;
    }

    public IntegerProperty glonassProperty() {
        return glonassProperty;
    }

}
