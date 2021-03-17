/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation;

import com.intel.missioncontrol.helper.ScaleHelper;
import de.saxsys.mvvmfx.utils.validation.ValidationMessage;
import de.saxsys.mvvmfx.utils.validation.ValidationStatus;
import de.saxsys.mvvmfx.utils.validation.visualization.ControlsFxVisualizer;
import java.util.Optional;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.asyncfx.concurrent.Dispatcher;

public class LabelValidationVisualizer extends ControlsFxVisualizer {

    private static final double WARNING_ICON_FIT_HEIGHT = ScaleHelper.emsToPixels(1.5);

    public LabelValidationVisualizer() {}

    public void initVisualization(ValidationStatus result, Control control, boolean required) {
        if (required) {
            this.applyRequiredVisualization(control, required);
        }

        this.applyVisualization(control, result.getHighestMessage(), required);
        result.getMessages()
            .addListener(
                (ListChangeListener<ValidationMessage>)
                    c -> {
                        while (c.next()) {
                            Dispatcher.platform()
                                .runLater(
                                    () ->
                                        LabelValidationVisualizer.this.applyVisualization(
                                            control, result.getHighestMessage(), required));
                        }
                    });
    }

    void applyRequiredVisualization(Control control, boolean required) {}

    void applyVisualization(Control control, Optional<ValidationMessage> message, boolean required) {
        if (!(control instanceof Label)) {
            throw new IllegalArgumentException("Visualizer control must be javafx.scene.control.Label");
        }

        if (message.isPresent()) {
            Tooltip tooltip = new Tooltip(message.get().getMessage());
            tooltip.setWrapText(true);
            tooltip.setAutoHide(true);

            control.setOnMouseClicked(
                event -> {
                    Bounds bounds = control.localToScreen(control.getBoundsInLocal());
                    double x = bounds.getMaxX();
                    double y = bounds.getMaxY();
                    double width = control.getParent().getBoundsInLocal().getWidth();
                    tooltip.setMaxWidth(width);
                    tooltip.show(control, x - width / 2, y);
                });

            handleSuccess(control.getId(), message.get());

            ((Label)control).setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            Image image = new Image("/com/intel/missioncontrol/icons/icon_warning(fill=theme-warning-color).svg");
            ImageView imageView = new ImageView();
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(WARNING_ICON_FIT_HEIGHT);
            imageView.setPickOnBounds(true);
            ((Label)control).setGraphic(imageView);
            control.setVisible(true);
        } else {
            handleFailure(control.getId());
            control.setVisible(false);
        }
    }

    protected void handleSuccess(String controlHash, ValidationMessage validationMessage) {}

    protected void handleFailure(String controlHash) {}
}
