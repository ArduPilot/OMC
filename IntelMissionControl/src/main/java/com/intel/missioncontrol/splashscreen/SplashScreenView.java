/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.splashscreen;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

public class SplashScreenView {

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label versionLabel;

    public void setVersion(String version) {
        this.versionLabel.setText(version);
    }

    public void setProgress(double progress) {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.getKeyFrames()
            .add(new KeyFrame(Duration.millis(150), new KeyValue(progressBar.progressProperty(), progress)));
        timeline.play();
    }
}
