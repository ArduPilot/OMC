/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.measure.AngleStyle;
import com.intel.missioncontrol.measure.SystemOfMeasurement;
import com.intel.missioncontrol.measure.TimeStyle;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;

public interface IQuantityStyleProvider {

    IQuantityStyleProvider NEUTRAL =
        new IQuantityStyleProvider() {
            private final ReadOnlyObjectWrapper<SystemOfMeasurement> systemOfMeasurement =
                new ReadOnlyObjectWrapper<>(SystemOfMeasurement.METRIC);

            private final ReadOnlyObjectWrapper<AngleStyle> angleStyle =
                new ReadOnlyObjectWrapper<>(AngleStyle.DECIMAL_DEGREES);

            private final ReadOnlyObjectWrapper<TimeStyle> timeStyle = new ReadOnlyObjectWrapper<>(TimeStyle.DECIMAL);

            @Override
            public ReadOnlyObjectProperty<SystemOfMeasurement> systemOfMeasurementProperty() {
                return systemOfMeasurement;
            }

            @Override
            public ReadOnlyObjectProperty<AngleStyle> angleStyleProperty() {
                return angleStyle;
            }

            @Override
            public ReadOnlyObjectProperty<TimeStyle> timeStyleProperty() {
                return timeStyle;
            }
        };

    ObservableValue<SystemOfMeasurement> systemOfMeasurementProperty();

    ObservableValue<AngleStyle> angleStyleProperty();

    ObservableValue<TimeStyle> timeStyleProperty();

    default SystemOfMeasurement getSystemOfMeasurement() {
        return systemOfMeasurementProperty().getValue();
    }

    default AngleStyle getAngleStyle() {
        return angleStyleProperty().getValue();
    }

    default TimeStyle getTimeStyle() {
        return timeStyleProperty().getValue();
    }

}
