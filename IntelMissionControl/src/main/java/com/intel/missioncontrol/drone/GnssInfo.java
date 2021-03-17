/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncDoubleProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;

public class GnssInfo implements IGnssInfo {
    private final AsyncObjectProperty<GnssState> gnssState =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<GnssState>().initialValue(GnssState.UNKNOWN).create());
    private final AsyncDoubleProperty qualityPercentage =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(Double.NaN).create());

    private final AsyncIntegerProperty numberOfSatellites =
        new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().initialValue(-1).create());

    private final AsyncBooleanProperty telemetryOld =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    public GnssInfo() {}

    @Override
    public AsyncObjectProperty<GnssState> gnssStateProperty() {
        return gnssState;
    }

    @Override
    public AsyncDoubleProperty qualityPercentageProperty() {
        return qualityPercentage;
    }

    @Override
    public AsyncIntegerProperty numberOfSatellitesProperty() {
        return numberOfSatellites;
    }

    @Override
    public AsyncBooleanProperty telemetryOldProperty() {
        return telemetryOld;
    }
}
