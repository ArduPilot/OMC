/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;

public class IconAndLabelExtendedViewModel extends IconAndLabelViewModel {

    private final Property<String> labelLeftProperty = new SimpleStringProperty("");
    private final Property<String> labelRightProperty = new SimpleStringProperty("");
    private final Property<String> separatorProperty = new SimpleStringProperty("/");

    public IconAndLabelExtendedViewModel() {
        alertPropery().setValue(AlertLevel.RED);
    }

    public Property<String> labelLeftProperty() {
        return labelLeftProperty;
    }

    public Property<String> labelRightProperty() {
        return labelRightProperty;
    }

    public Property<String> separatorProperty() {
        return separatorProperty;
    }

}
