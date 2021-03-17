/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view.widget;

import com.intel.missioncontrol.ui.ViewModelBase;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;

/** @author Vladimir Iordanov */
public class AirplaneSpeedWidgetViewModel extends ViewModelBase {
    private final ObjectProperty<Number> simulationSpeed = new SimpleObjectProperty<>();
    private final ObjectProperty<Float> actualSimulationSpeed = new SimpleObjectProperty<>();
    private final BooleanProperty isMaximumSpeed = new SimpleBooleanProperty();
    private final StringProperty title = new SimpleStringProperty();

    private final FloatProperty maxValue = new SimpleFloatProperty();
    private final ListProperty<Float> sliderValues = new SimpleListProperty<>();
    private final FloatProperty initValue = new SimpleFloatProperty();

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        initBindings();
    }

    private void initBindings() {
        simulationSpeed.addListener(
            (observable, oldValue, newValue) -> {
                updateActualSpeed();
            });
        isMaximumSpeed.addListener(
            (observable, oldValue, newValue) -> {
                updateActualSpeed();
            });
        actualSimulationSpeed.addListener(
            (observable, oldValue, newValue) -> {
                if (currentSimulationSpeed() != newValue) {
                    setActualSpeed(newValue);
                }
            });
    }

    private void updateActualSpeed() {
        actualSimulationSpeed.setValue(currentSimulationSpeed());
    }

    private float currentSimulationSpeed() {
        if (isMaximumSpeed.getValue()) {
            return getMaxValue();
        }

        return sliderValues.get(simulationSpeed.get().intValue());
    }

    private void setActualSpeed(float speed) {
        boolean isMaximum = speed == getMaxValue();
        isMaximumSpeed.setValue(isMaximum);
        if (!isMaximum) {
            simulationSpeed.setValue(sliderValues.indexOf(speed));
        }
    }

    public List<Float> getSliderValues() {
        return sliderValues;
    }

    public float getMaxValue() {
        return maxValue.get();
    }

    public FloatProperty maxValueProperty() {
        return maxValue;
    }

    public ListProperty<Float> sliderValuesProperty() {
        return sliderValues;
    }

    public float getInitValue() {
        return initValue.get();
    }

    public FloatProperty initValueProperty() {
        return initValue;
    }

    public Number getSimulationSpeed() {
        return simulationSpeed.get();
    }

    public ObjectProperty<Number> simulationSpeedProperty() {
        return simulationSpeed;
    }

    public boolean getIsMaximumSpeed() {
        return isMaximumSpeed.get();
    }

    public BooleanProperty isMaximumSpeedProperty() {
        return isMaximumSpeed;
    }

    public float getActualSimulationSpeed() {
        return actualSimulationSpeed.get();
    }

    public ObjectProperty<Float> actualSimulationSpeedProperty() {
        return actualSimulationSpeed;
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }
}
