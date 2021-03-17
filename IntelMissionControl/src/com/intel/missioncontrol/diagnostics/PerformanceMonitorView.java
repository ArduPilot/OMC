/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.diagnostics;

import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PerformanceMonitorView extends DialogView<PerformanceMonitorViewModel> {

    @InjectViewModel
    private PerformanceMonitorViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private ToggleSwitch enabledSwitch;

    @FXML
    private ScrollPane barChartContainer;

    @FXML
    private HBox barChart;

    @FXML
    private Label frameRateLabel;

    @FXML
    private StatisticsView totalStatsView;

    @FXML
    private StatisticsView lastSegmentStatsView;

    @FXML
    private Pane selectedFrameContainer;

    @FXML
    private Label frameDataLabel;

    @FXML
    private Label drawCallsLabel;

    @FXML
    private Label eventHandlerTimeoutsLabel;

    private final Background highlightBackground =
        new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY));

    @Override
    protected void initializeView() {
        super.initializeView();

        DecimalFormat decimalFormat = (DecimalFormat)NumberFormat.getInstance(Locale.getDefault());
        decimalFormat.setMinimumFractionDigits(1);
        decimalFormat.setMaximumFractionDigits(1);

        barChart.setOnMouseExited(this::handleBarChartMouseExited);
        barChartContainer.widthProperty().addListener(this::barChartWidthChanged);
        viewModel.framesProperty().addListener(this::listChanged);
        enabledSwitch.selectedProperty().bindBidirectional(viewModel.enabledProperty());
        totalStatsView.statisticsProperty().bind(viewModel.totalStatsProperty());
        lastSegmentStatsView.statisticsProperty().bind(viewModel.lastSegmentStatsProperty());
        selectedFrameContainer.visibleProperty().bind(viewModel.selectedBarIndexProperty().isNotEqualTo(-1));

        viewModel
            .frameRateProperty()
            .addListener(
                (observable, oldValue, newValue) -> frameRateLabel.setText(decimalFormat.format(newValue) + " fps"));

        viewModel
            .drawCallsProperty()
            .addListener(
                (observable, oldValue, newValue) -> drawCallsLabel.setText(Integer.toString(newValue.intValue())));

        viewModel
            .eventHandlerTimeoutsProperty()
            .addListener(
                (observable, oldValue, newValue) ->
                    eventHandlerTimeoutsLabel.setText(Integer.toString(newValue.intValue())));

        viewModel
            .frameDataProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (var pair : newValue) {
                        stringBuilder.append(pair.getKey());
                        stringBuilder.append(": ");
                        stringBuilder.append(pair.getValue());
                        stringBuilder.append("\n");
                    }

                    frameDataLabel.setText(stringBuilder.toString());
                });

        drawCallsLabel.setText(Integer.toString(viewModel.drawCallsProperty().get()));
        frameRateLabel.setText(decimalFormat.format(viewModel.frameRateProperty().get()) + " fps");
        eventHandlerTimeoutsLabel.setText(Integer.toString(viewModel.eventHandlerTimeoutsProperty().get()));
    }

    private void barChartWidthChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        final double barWidth = 5;
        double width = newValue.doubleValue();
        int barsCount = (int)(width / (barChart.getSpacing() + barWidth));

        var bars = new ArrayList<VBox>();
        for (int i = 0; i < barsCount; ++i) {
            var box = new VBox();
            box.setSnapToPixel(false);
            box.setAlignment(Pos.BOTTOM_LEFT);
            box.setOnMouseEntered(this::handleBarMouseEntered);
            box.setOnMouseExited(this::handleBarMouseExited);

            var rect = new Rectangle();
            rect.setWidth(barWidth);
            rect.setHeight(0);
            rect.setFill(Color.BLUE);
            box.getChildren().add(rect);

            rect = new Rectangle();
            rect.setWidth(barWidth);
            rect.setHeight(0);
            rect.setFill(Color.GREEN);
            box.getChildren().add(rect);

            rect = new Rectangle();
            rect.setWidth(barWidth);
            rect.setHeight(0);
            rect.setFill(Color.RED);
            box.getChildren().add(rect);

            bars.add(box);
        }

        barChart.getChildren().setAll(bars);
    }

    private void handleBarMouseEntered(MouseEvent event) {
        var source = (VBox)event.getSource();
        source.setBackground(highlightBackground);
        Object userData = source.getUserData();
        if (userData != null) {
            viewModel.selectedBarIndexProperty().set((int)userData);
        }
    }

    private void handleBarMouseExited(MouseEvent event) {
        var source = (VBox)event.getSource();
        source.setBackground(null);
    }

    private void handleBarChartMouseExited(MouseEvent event) {
        viewModel.selectedBarIndexProperty().set(-1);
    }

    private void listChanged(ListChangeListener.Change<? extends FrameInfo> change) {
        while (change.next()) {
            var dataItems = change.getList();
            var bars = barChart.getChildren();
            long maxHeight = (long)barChart.getHeight() - 5;
            int count = dataItems.size();

            for (int i = 0; i < Math.min(count, bars.size()); ++i) {
                VBox box = (VBox)bars.get(bars.size() - i - 1);
                box.setUserData(count - i - 1);
                long renderDurationMillis = dataItems.get(count - i - 1).getRenderDurationMillis();
                long copyPixelsDurationMillis = dataItems.get(count - i - 1).getCopyPixelsDurationMillis();
                long frameDurationMillis = dataItems.get(count - i - 1).getFrameDurationMillis();
                long miscDurationMillis =
                    Math.max(0, frameDurationMillis - renderDurationMillis - copyPixelsDurationMillis);

                Rectangle copyPixelsRect = (Rectangle)box.getChildren().get(0);
                Rectangle renderTimeRect = (Rectangle)box.getChildren().get(1);
                Rectangle miscTimeRect = (Rectangle)box.getChildren().get(2);

                copyPixelsRect.setHeight(
                    Math.min(
                        copyPixelsDurationMillis, Math.max(0, maxHeight - miscDurationMillis - renderDurationMillis)));

                renderTimeRect.setHeight(Math.min(renderDurationMillis, Math.max(0, maxHeight - miscDurationMillis)));

                if (miscDurationMillis > 150) {
                    miscTimeRect.setFill(Color.YELLOW);
                    miscTimeRect.setHeight(20);
                } else {
                    miscTimeRect.setFill(Color.RED);
                    miscTimeRect.setHeight(Math.min(miscDurationMillis, maxHeight));
                }
            }
        }
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper("Performance monitor");
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }
}
