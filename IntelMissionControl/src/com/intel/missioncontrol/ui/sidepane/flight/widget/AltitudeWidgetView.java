/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.widget;

import com.emxsys.chart.EnhancedLineChart;
import com.emxsys.chart.extension.XYAnnotations;
import com.emxsys.chart.extension.XYTextAnnotation;
import com.google.inject.Inject;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AltitudeWidgetView extends ViewBase<AltitudeWidgetViewModel> {
    private static final Integer LEFT_TIME_BOUND_SECONDS = -30;
    private static final Integer RIGHT_TIME_BOUND_SECONDS = 10;
    private static final String CSS_PLANE_ALTITUDE_CHART = "plane-altitude";
    private static final String CSS_PREDICTED_ALTITUDE_UP = "predicted-altitude-up";
    private static final String CSS_PREDICTED_ALTITUDE_DOWN = "predicted-altitude-down";
    private static final String CSS_RECOMMENDED_ALTITUDE = "recommended-altitude";

    @FXML
    private Pane rootNode;

    @FXML
    private EnhancedLineChart<Double, Double> altitudeChart;

    @FXML
    private NumberAxis xAxis;

    @InjectViewModel
    private AltitudeWidgetViewModel viewModel;

    private final ILanguageHelper languageHelper;
    private final QuantityFormat quantityFormat;

    private Map<String, Date> lastTimeMeasures = new HashMap<>();
    private XYTextAnnotation currentAltitude;
    private XYTextAnnotation predictedDeltaAltitude;
    private XYTextAnnotation recommendedAltitude;
    private XYTextAnnotation maxAllowedAltitude;
    private AltitudeWidgetViewModel.AltitudeEvent previousEvent;
    private XYChart.Series<Double, Double> planeCurrentAltData;
    private XYChart.Series<Double, Double> planePredictedAltData;
    private XYChart.Series<Double, Double> recommendedAltData;
    private XYChart.Series<Double, Double> maxAllowedAltData;

    @Inject
    public AltitudeWidgetView(ILanguageHelper languageHelper, IQuantityStyleProvider quantityStyleProvider) {
        this.languageHelper = languageHelper;
        quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
        quantityFormat.setMaximumFractionDigits(3);
    }

    @Override
    public void initializeView() {
        super.initializeView();

        initSeries();

        xAxis.setTickLabelFormatter(
            new StringConverter<Number>() {
                @Override
                public String toString(Number object) {
                    int value = object.intValue();
                    if (value == 0) {
                        return languageHelper.getString(
                            "com.intel.missioncontrol.ui.flight.widget.AltitudeWidgetView.tickLabelNow");
                    }

                    if (value == LEFT_TIME_BOUND_SECONDS) {
                        return languageHelper.getString(
                            "com.intel.missioncontrol.ui.flight.widget.AltitudeWidgetView.30SecondsAgo");
                    }

                    if (value == RIGHT_TIME_BOUND_SECONDS) {
                        return languageHelper.getString(
                            "com.intel.missioncontrol.ui.flight.widget.AltitudeWidgetView.plus10seconds");
                    }

                    if (value > 0 && value < RIGHT_TIME_BOUND_SECONDS) {
                        return "+" + String.valueOf(value);
                    }

                    if (value > RIGHT_TIME_BOUND_SECONDS) {
                        return "";
                    }

                    return String.valueOf(value);
                }

                @Override
                public Number fromString(String string) {
                    return 0;
                }
            });
        currentAltitude = new XYTextAnnotation("0", 0D, 0D);
        altitudeChart.getAnnotations().add(currentAltitude, XYAnnotations.Layer.FOREGROUND);

        viewModel.subscribe(
            AltitudeWidgetViewModel.EVENT_UAV_ALTITUDE_UPDATE,
            (key, payload) -> {
                AltitudeWidgetViewModel.AltitudeEvent event = (AltitudeWidgetViewModel.AltitudeEvent)payload[0];
                drawAltitudeOnChart(event.getAltitude(), event.getTimeStamp(), planeCurrentAltData);
                currentAltitude =
                    rebuildAltitudeAnnotation(
                        event.getAltitude(), currentAltitude, CSS_PLANE_ALTITUDE_CHART, Pos.TOP_LEFT);
                drawPredictionAltitude(previousEvent, event);
                altitudeChart.requestLayout();
                previousEvent = event;
            });

        viewModel.subscribe(
            AltitudeWidgetViewModel.EVENT_TARGET_ALTITUDE_UPDATE,
            (key, payload) -> {
                AltitudeWidgetViewModel.AltitudeEvent event = (AltitudeWidgetViewModel.AltitudeEvent)payload[0];
                drawAltitudeOnChart(event.getAltitude(), event.getTimeStamp(), recommendedAltData);
                recommendedAltitude =
                    rebuildAltitudeAnnotation(
                        event.getAltitude(), recommendedAltitude, CSS_RECOMMENDED_ALTITUDE, Pos.BOTTOM_LEFT);
                altitudeChart.requestLayout();
            });

        viewModel.subscribe(
            AltitudeWidgetViewModel.EVENT_MAX_ALLOWED_ALTITUDE_UPDATE,
            ((key, payload) -> {
                AltitudeWidgetViewModel.AltitudeEvent event = (AltitudeWidgetViewModel.AltitudeEvent)payload[0];
                drawAltitudeOnChart(event.getAltitude(), event.getTimeStamp(), maxAllowedAltData);
                maxAllowedAltitude =
                    rebuildAltitudeAnnotation(
                        event.getAltitude(), maxAllowedAltitude, CSS_RECOMMENDED_ALTITUDE, Pos.TOP_LEFT);
                altitudeChart.requestLayout();
            }));

        viewModel.subscribe(
            AltitudeWidgetViewModel.EVENT_STOP_AGGREGATION,
            ((key, payload) -> {
                reInitSeries();
                previousEvent = null;
                lastTimeMeasures.clear();
                altitudeChart.getAnnotations().clearTextAnnotations(XYAnnotations.Layer.BACKGROUND);
                altitudeChart.getAnnotations().clearTextAnnotations(XYAnnotations.Layer.FOREGROUND);
            }));
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public AltitudeWidgetViewModel getViewModel() {
        return viewModel;
    }

    private void initSeries() {
        planeCurrentAltData = new EnhancedLineChart.Series<Double, Double>();
        planePredictedAltData = new EnhancedLineChart.Series<Double, Double>();

        recommendedAltData = new EnhancedLineChart.Series<Double, Double>();

        maxAllowedAltData = new EnhancedLineChart.Series<Double, Double>();

        planeCurrentAltData.setName("planeAlt");
        recommendedAltData.setName("recommendedAlt");
        recommendedAltData.setName("maxAlt");

        altitudeChart.getData().add(planeCurrentAltData);
        altitudeChart.getData().add(planePredictedAltData);
        altitudeChart.getData().add(recommendedAltData);
        altitudeChart.getData().add(maxAllowedAltData);
        altitudeChart.setAnimated(false);
    }

    private void reInitSeries() {
        altitudeChart.getData().remove(planeCurrentAltData);
        altitudeChart.getData().remove(recommendedAltData);
        altitudeChart.getData().remove(maxAllowedAltData);
        altitudeChart.getData().remove(planePredictedAltData);
        initSeries();
    }

    private void drawPredictionAltitude(
            AltitudeWidgetViewModel.AltitudeEvent previousEvent, AltitudeWidgetViewModel.AltitudeEvent event) {
        ObservableList<XYChart.Data<Double, Double>> alts = planePredictedAltData.getData();
        if (previousEvent != null) {
            double k =
                (1000 * (event.getAltitude() - previousEvent.getAltitude()))
                    / (event.getTimeStamp().getTime() - previousEvent.getTimeStamp().getTime());
            alts.forEach(data -> data.setYValue(data.getXValue() * k + event.getAltitude()));
            if (predictedDeltaAltitude != null) {
                altitudeChart.getAnnotations().remove(predictedDeltaAltitude, XYAnnotations.Layer.BACKGROUND);
            }

            int maxValue = RIGHT_TIME_BOUND_SECONDS;
            predictedDeltaAltitude =
                new XYTextAnnotation(
                    signedAltitudeCaption(k * maxValue),
                    maxValue,
                    k * maxValue + event.getAltitude(),
                    k < 0 ? Pos.BOTTOM_LEFT : Pos.TOP_LEFT);
            predictedDeltaAltitude
                .getNode()
                .getStyleClass()
                .add(k >= 0 ? CSS_PREDICTED_ALTITUDE_UP : CSS_PREDICTED_ALTITUDE_DOWN);
            altitudeChart.getAnnotations().add(predictedDeltaAltitude, XYAnnotations.Layer.BACKGROUND);
        } else {
            alts.add(new XYChart.Data<>(5D, 0D));
            alts.add(new XYChart.Data<>(RIGHT_TIME_BOUND_SECONDS.doubleValue(), 0D));
        }
    }

    private void drawAltitudeOnChart(double altitude, Date timestamp, XYChart.Series<Double, Double> data) {
        Date lastTimeMeasure = lastTimeMeasures.computeIfAbsent(data.getName(), (name) -> timestamp);
        double secondsPassed = (timestamp.getTime() - lastTimeMeasure.getTime()) / 1.0E3;
        ObservableList<XYChart.Data<Double, Double>> alts = data.getData();
        shiftPoints(secondsPassed, alts);

        double secondsFromNow = (new Date().getTime() - timestamp.getTime()) / 1.0E3;
        alts.add(new XYChart.Data<>(secondsFromNow, altitude));
        lastTimeMeasures.replace(data.getName(), timestamp);
    }

    private void shiftPoints(double secondsPassed, ObservableList<XYChart.Data<Double, Double>> alts) {
        alts.removeIf(alt -> alt.getXValue() < LEFT_TIME_BOUND_SECONDS * 2);
        for (XYChart.Data<Double, Double> alt : alts) {
            alt.setXValue(alt.getXValue() - secondsPassed);
        }
    }

    private XYTextAnnotation rebuildAltitudeAnnotation(
            double altitude, XYTextAnnotation altAnnotation, String cssClass, Pos pos) {
        if (altAnnotation != null) {
            altitudeChart.getAnnotations().remove(altAnnotation, XYAnnotations.Layer.FOREGROUND);
        }

        XYTextAnnotation altitudeAnnotation = new XYTextAnnotation(altitudeCaption(altitude), 0.2D, altitude, pos);
        altitudeChart.getAnnotations().add(altitudeAnnotation, XYAnnotations.Layer.FOREGROUND);
        altitudeAnnotation.getNode().getStyleClass().add(cssClass);
        return altitudeAnnotation;
    }

    private String altitudeCaption(double altitude) {
        return quantityFormat.format(Quantity.of(altitude, Unit.METER), UnitInfo.LOCALIZED_LENGTH);
    }

    private String signedAltitudeCaption(double altitude) {
        return (altitude > 0 ? "+" : "") + " " + altitudeCaption(altitude);
    }
}
