/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;

public class IconAndLabelViewModel extends AlertAwareViewModel {

    private final Property<String> textProperty = new SimpleStringProperty("");
    private final Property<Image> imagePropery = new SimpleObjectProperty<>();
    private final Property<Number> imageRotateProperty = new SimpleDoubleProperty(0.0);

    public Property<String> textProperty() {
        return textProperty;
    }

    public Property<Image> imageProperty() {
        return imagePropery;
    }

    public Property<Number> imageRotateProperty() {
        return imageRotateProperty;
    }

}
