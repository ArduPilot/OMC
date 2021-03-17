/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation;

import de.saxsys.mvvmfx.utils.validation.ValidationStatus;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.Label;

public class LabelValidationRichVisualizer extends LabelValidationVisualizer {

    private Label control;

    public LabelValidationRichVisualizer(
            ReadOnlyObjectProperty<ValidationStatus> validationStatusProperty, Label control) {
        this.control = control;
        validationStatusProperty.addListener((observable, oldValue, newValue) -> reinit(newValue));
        reinit(validationStatusProperty.get());
    }

    private void reinit(ValidationStatus validationStatus) {
        if (validationStatus == null) {
            return;
        }

        initVisualization(validationStatus, control);
    }

}
