/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncDoubleProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import com.intel.missioncontrol.map.elevation.ElevationModelShiftWrapper;

@Serializable
public class GeoTiffSettings {

    private final AsyncStringProperty name = new SimpleAsyncStringProperty(this);

    private final AsyncStringProperty path = new SimpleAsyncStringProperty(this);

    private final AsyncStringProperty cacheName = new SimpleAsyncStringProperty(this);

    private final AsyncBooleanProperty enabled =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncObjectProperty<ElevationModelShiftWrapper.ShiftType> elevationModelShiftType =
        new SimpleAsyncObjectProperty<>(
            this,
            new PropertyMetadata.Builder<ElevationModelShiftWrapper.ShiftType>()
                .initialValue(ElevationModelShiftWrapper.ShiftType.MANUAL)
                .create());

    private final AsyncDoubleProperty manualElevationShift = new SimpleAsyncDoubleProperty(this);

    public AsyncStringProperty nameProperty() {
        return name;
    }

    public AsyncStringProperty pathProperty() {
        return path;
    }

    public AsyncStringProperty cacheNameProperty() {
        return cacheName;
    }

    public AsyncBooleanProperty enabledProperty() {
        return enabled;
    }

    public AsyncObjectProperty<ElevationModelShiftWrapper.ShiftType> elevationModelShiftTypeProperty() {
        return elevationModelShiftType;
    }

    public AsyncDoubleProperty manualElevationShiftProperty() {
        return manualElevationShift;
    }

}
