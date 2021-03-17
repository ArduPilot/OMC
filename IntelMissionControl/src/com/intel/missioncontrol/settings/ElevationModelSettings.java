/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;

@SettingsMetadata(section = "elevationModel")
public class ElevationModelSettings implements ISettings {

    private final AsyncBooleanProperty useSurfaceDataForPlanning =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty useDefaultElevationModel =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty useGeoTIFF =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty useAirspaceDataForPlanning =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    public boolean isUseSurfaceDataForPlanning() {
        return useSurfaceDataForPlanning.get();
    }

    public AsyncBooleanProperty useSurfaceDataForPlanningProperty() {
        return useSurfaceDataForPlanning;
    }

    public boolean getUseDefaultElevationModel() {
        return useDefaultElevationModel.get();
    }

    public AsyncBooleanProperty useDefaultElevationModelProperty() {
        return useDefaultElevationModel;
    }

    public boolean isUseGeoTIFF() {
        return useGeoTIFF.get();
    }

    public AsyncBooleanProperty useGeoTIFFProperty() {
        return useGeoTIFF;
    }

}
