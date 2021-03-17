/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import com.intel.missioncontrol.ui.ViewModelBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class WindViewModel extends ViewModelBase {

    private final Property<String> speedProperty = new SimpleStringProperty("");
    private final DoubleProperty directionProperty = new SimpleDoubleProperty(0.0);
    private final Property<WindDirection> compassProperty = new SimpleObjectProperty<>();

    public WindViewModel() {
        directionProperty.addListener(
            listener -> compassProperty.setValue(WindDirection.getDirection(directionProperty.getValue())));
    }

    public Property<String> speedProperty() {
        return speedProperty;
    }

    public DoubleProperty directionProperty() {
        return directionProperty;
    }

    public Property<WindDirection> compassProperty() {
        return compassProperty;
    }

}
