/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class PercentProgressBar extends StackPane {

    private static final String CSS_PERCENT_PROGRESS_BAR = "percent-progress-bar";

    private static final String TEXT_FORMAT = "%d%%";

    private final ProgressBar progressBar = new ProgressBar();
    private final Text text = new Text("");

    public PercentProgressBar() {
        progressBar.setMaxWidth(Double.POSITIVE_INFINITY);
        progressBar.getStyleClass().add(CSS_PERCENT_PROGRESS_BAR);

        updateProgress(getProgress());
        progressProperty().addListener((observable, oldValue, newValue) -> updateProgress(newValue));

        getChildren().setAll(progressBar, text);
    }

    private void updateProgress(Number progress) {
        double progressValue = extractPositiveDoubleValue(progress);
        long progressPercent = Math.round(progressValue * 100.0);

        text.setText(String.format(TEXT_FORMAT, progressPercent));
    }

    private static double extractPositiveDoubleValue(Number value) {
        if (value == null) {
            return 0.0;
        }

        double positiveValue = Math.max(value.doubleValue(), 0.0);
        return Math.min(positiveValue, 1.0);
    }

    public DoubleProperty progressProperty() {
        return progressBar.progressProperty();
    }

    public double getProgress() {
        return progressProperty().get();
    }

    public void setProgress(double value) {
        progressProperty().set(value);
    }

}
