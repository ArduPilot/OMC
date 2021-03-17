/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.diagnostics;

import eu.mavinci.core.obfuscation.IKeepClassname;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class StatisticsView extends GridPane implements IKeepClassname {

    @FXML
    private Label frameCountLabel;

    @FXML
    private Label numFrames0Label;

    @FXML
    private Label numFrames1Label;

    @FXML
    private Label numFrames2Label;

    @FXML
    private Label maxFrameTimeLabel;

    private final ObjectProperty<StatisticsInfo> statistics = new SimpleObjectProperty<>();

    private final DecimalFormat decimalFormat = (DecimalFormat)NumberFormat.getInstance(Locale.getDefault());
    private final ChangeListener<Number> frameCountListener =
        (observable, oldValue, newValue) -> frameCountLabel.setText(Integer.toString(newValue.intValue()));
    private final ChangeListener<Number> percentFramesBelow16Listener =
        (observable, oldValue, newValue) -> numFrames0Label.setText(decimalFormat.format(newValue.doubleValue() * 100));
    private final ChangeListener<Number> percentFramesBelow33Listener =
        (observable, oldValue, newValue) -> numFrames1Label.setText(decimalFormat.format(newValue.doubleValue() * 100));
    private final ChangeListener<Number> percentFramesBelow66Listener =
        (observable, oldValue, newValue) -> numFrames2Label.setText(decimalFormat.format(newValue.doubleValue() * 100));
    private final ChangeListener<Number> maxFrameTimeListener =
        (observable, oldValue, newValue) -> maxFrameTimeLabel.setText(newValue.intValue() + " ms");

    public StatisticsView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("StatisticsView.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        loader.load();

        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMinimumFractionDigits(1);
        decimalFormat.setMaximumFractionDigits(1);

        statistics.addListener(
            (observable, oldValue, newValue) -> {
                if (oldValue != null) {
                    oldValue.frameCountProperty().removeListener(frameCountListener);
                    oldValue.percentFramesBelow16msProperty().removeListener(percentFramesBelow16Listener);
                    oldValue.percentFramesBelow33msProperty().removeListener(percentFramesBelow33Listener);
                    oldValue.percentFramesBelow66msProperty().removeListener(percentFramesBelow66Listener);
                    oldValue.maxFrameTimeMillisProperty().removeListener(maxFrameTimeListener);
                }

                if (newValue != null) {
                    newValue.frameCountProperty().addListener(new WeakChangeListener<>(frameCountListener));
                    newValue.percentFramesBelow16msProperty()
                        .addListener(new WeakChangeListener<>(percentFramesBelow16Listener));
                    newValue.percentFramesBelow33msProperty()
                        .addListener(new WeakChangeListener<>(percentFramesBelow33Listener));
                    newValue.percentFramesBelow66msProperty()
                        .addListener(new WeakChangeListener<>(percentFramesBelow66Listener));
                    newValue.maxFrameTimeMillisProperty().addListener(maxFrameTimeListener);
                }
            });
    }

    public ObjectProperty<StatisticsInfo> statisticsProperty() {
        return statistics;
    }

}
