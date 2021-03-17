/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers;

import com.intel.missioncontrol.settings.ISettings;
import com.intel.missioncontrol.settings.SettingsMetadata;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;

@SettingsMetadata(section = "generalLayerVisibility")
public class GeneralLayerVisibility implements ISettings {

    private final AsyncBooleanProperty mapBoxHybridLayerVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty mapBoxSatLayerVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty mapBoxStreetsLayerVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty landsatBaseLayerVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty contourLinesLayerVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty latLonGraticuleLayerVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty mgrsGraticuleLayerVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty utmGraticuleLayerVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty terrainProfileLayerVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty compassLayerVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty scalebarLayerVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty tooltipLayerVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty airtrafficVisible =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    public AsyncBooleanProperty mapBoxHybridLayerVisibleProperty() {
        return mapBoxHybridLayerVisible;
    }

    public AsyncBooleanProperty mapBoxSatLayerVisibleProperty() {
        return mapBoxSatLayerVisible;
    }

    public AsyncBooleanProperty mapBoxStreetsLayerVisibleProperty() {
        return mapBoxStreetsLayerVisible;
    }

    public AsyncBooleanProperty contourLinesLayerVisibleProperty() {
        return contourLinesLayerVisible;
    }

    public AsyncBooleanProperty latLonGraticuleLayerVisibleProperty() {
        return latLonGraticuleLayerVisible;
    }

    public AsyncBooleanProperty mgrsGraticuleLayerVisibleProperty() {
        return mgrsGraticuleLayerVisible;
    }

    public AsyncBooleanProperty utmGraticuleLayerVisibleProperty() {
        return utmGraticuleLayerVisible;
    }

    public AsyncBooleanProperty terrainProfileLayerVisibleProperty() {
        return terrainProfileLayerVisible;
    }

    public AsyncBooleanProperty compassLayerVisibleProperty() {
        return compassLayerVisible;
    }

    public AsyncBooleanProperty scalebarLayerVisibleProperty() {
        return scalebarLayerVisible;
    }

    public AsyncBooleanProperty tooltipLayerVisibleProperty() {
        return tooltipLayerVisible;
    }

    public AsyncBooleanProperty airtrafficVisibleProperty() {
        return airtrafficVisible;
    }
}
