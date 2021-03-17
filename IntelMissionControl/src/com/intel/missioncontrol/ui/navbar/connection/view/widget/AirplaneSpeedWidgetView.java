/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view.widget;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

/** @author Vladimir Iordanov */
public class AirplaneSpeedWidgetView extends ViewBase<AirplaneSpeedWidgetViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    private Slider simulationSpeedSlider;

    @FXML
    private ToggleSwitch maximumSpeedSwitch;

    @FXML
    private Label simulationSpeedLabel;

    @InjectViewModel
    private AirplaneSpeedWidgetViewModel viewModel;

    private final FloatProperty maxValue = new SimpleFloatProperty();
    private final ListProperty<Float> sliderValues = new SimpleListProperty<>();
    private final FloatProperty initValue = new SimpleFloatProperty();

    @Override
    public void initializeView() {
        super.initializeView();
        simulationSpeedSlider.valueProperty().bindBidirectional(viewModel.simulationSpeedProperty());
        maximumSpeedSwitch.selectedProperty().bindBidirectional(viewModel.isMaximumSpeedProperty());

        simulationSpeedSlider.disableProperty().bind(maximumSpeedSwitch.selectedProperty());

        simulationSpeedLabel.textProperty().bind(viewModel.titleProperty());
        maxValue.bind(viewModel.maxValueProperty());
        sliderValues.bind(viewModel.sliderValuesProperty());
        initValue.bind(viewModel.initValueProperty());

        sliderValues.addListener(
            (observable, oldValue, newValue) -> {
                simulationSpeedSlider.setMax(getMaxSliderItems(newValue));
            });

        initValue.addListener(
            (observable, oldValue, newValue) -> {
                simulationSpeedSlider.setValue(getIndexOfValue(newValue));
            });

        initSlider();
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public AirplaneSpeedWidgetViewModel getViewModel() {
        return viewModel;
    }

    private int getMaxSliderItems(ObservableList<Float> items) {
        return items != null ? items.size() - 1 : 0;
    }

    private int getIndexOfValue(Number value) {
        return Math.max(0, sliderValues.indexOf(value.floatValue()));
    }

    public void initSlider() {
        simulationSpeedSlider.setMin(0);
        simulationSpeedSlider.setMinorTickCount(0);
        simulationSpeedSlider.setMajorTickUnit(1);
        simulationSpeedSlider.setSnapToTicks(true);
        simulationSpeedSlider.setShowTickMarks(true);
        simulationSpeedSlider.setShowTickLabels(true);
        simulationSpeedSlider.setMinHeight(Slider.USE_PREF_SIZE);
        simulationSpeedSlider.setBlockIncrement(1);

        simulationSpeedSlider.setLabelFormatter(
            new StringConverter<Double>() {
                @Override
                public String toString(Double n) {
                    return new DecimalFormat("0.##").format(sliderValues.get((int)Math.round(n)));
                }

                @Override
                public Double fromString(String s) {
                    return null;
                }
            });
    }
}
