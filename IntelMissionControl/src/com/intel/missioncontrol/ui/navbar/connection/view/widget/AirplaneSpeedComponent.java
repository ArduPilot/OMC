/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view.widget;

import com.google.common.collect.ImmutableList;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import java.util.List;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.layout.VBox;

/**
 * This component allows to specify speed of Airplane. It can be directly included into fxml.
 *
 * @author Vladimir Iordanov
 */
public class AirplaneSpeedComponent extends VBox {
    public static final List<Float> DEFAULT_SLIDER_VALUES = ImmutableList.of(0.25f, 0.50f, 1f, 2f, 4f, 8f, 16f);
    public static final float DEFAULT_INIT_VALUE = 1f;
    public static final float DEFAULT_MAX_VALUE = Float.MAX_VALUE;

    private final ViewTuple<AirplaneSpeedWidgetView, AirplaneSpeedWidgetViewModel> tuple;

    private final FloatProperty maxValue = new SimpleFloatProperty(DEFAULT_MAX_VALUE);
    private final FloatProperty initValue = new SimpleFloatProperty(DEFAULT_INIT_VALUE);
    private final ListProperty<Float> sliderValues =
        new SimpleListProperty<>(FXCollections.observableArrayList(DEFAULT_SLIDER_VALUES));
    private final StringProperty title = new SimpleStringProperty("");

    public AirplaneSpeedComponent() {
        tuple = FluentViewLoader.fxmlView(AirplaneSpeedWidgetView.class).root(this).load();
        initBindings();
    }

    public AirplaneSpeedComponent(Float initValue, Float maxValue, List<Float> sliderValues) {
        this();
        this.initValue.setValue(initValue);
        this.maxValue.setValue(maxValue);
        this.sliderValues.setValue(FXCollections.observableList(sliderValues));
    }

    private void initBindings() {
        title.bindBidirectional(getViewModel().titleProperty());
        maxValue.bindBidirectional(getViewModel().maxValueProperty());
        initValue.bindBidirectional(getViewModel().initValueProperty());
        sliderValues.bindBidirectional(getViewModel().sliderValuesProperty());
    }

    public AirplaneSpeedWidgetViewModel getViewModel() {
        return tuple.getViewModel();
    }

    public AirplaneSpeedWidgetView getView() {
        return tuple.getCodeBehind();
    }

    public ObjectProperty<Float> simulationSpeedProperty() {
        return getViewModel().actualSimulationSpeedProperty();
    }

    public Float getMaxValue() {
        return maxValue.getValue();
    }

    public void setMaxValue(Float maxValue) {
        this.maxValue.setValue(maxValue);
    }

    public Float getInitValue() {
        return initValue.getValue();
    }

    public void setInitValue(Float initValue) {
        this.initValue.setValue(initValue);
    }

    public List<Float> getSliderValues() {
        return sliderValues;
    }

    public void setSliderValues(List<Float> sliderValues) {
        this.sliderValues.setValue(FXCollections.observableList(sliderValues));
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }
}
