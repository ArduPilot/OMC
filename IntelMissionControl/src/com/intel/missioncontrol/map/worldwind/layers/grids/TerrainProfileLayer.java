/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.grids;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.map.worldwind.WorldWindowProvider;
import com.intel.missioncontrol.measure.SystemOfMeasurement;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.settings.GeneralSettings;
import eu.mavinci.core.obfuscation.IKeepAll;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;
import java.awt.Font;

@LayerDefaults(name = "%com.intel.missioncontrol.map.worldwind.layers.grids.TerrainProfileLayer", enabled = false)
public class TerrainProfileLayer extends WWLayerWrapper implements IKeepAll {
    gov.nasa.worldwind.layers.TerrainProfileLayer tpl;

    @Inject
    TerrainProfileLayer(
            @Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot,
            WorldWindowProvider worldWindowProvider,
            GeneralSettings generalSettings) {
        super(new gov.nasa.worldwind.layers.TerrainProfileLayer(), syncRoot);
        tpl = (gov.nasa.worldwind.layers.TerrainProfileLayer)getWrappedLayer();
        tpl.setStartLatLon(LatLon.fromDegrees(0, -10));
        tpl.setEndLatLon(LatLon.fromDegrees(0, 65));
        ScaleHelper.scalePropProperty().addListener((observable, oldVal, newVal) -> updateScale());

        worldWindowProvider.whenAvailable(
            wwd -> {
                tpl.setEventSource(wwd);
            });
        generalSettings
            .systemOfMeasurementProperty()
            .addListener((observable, oldValue, newValue) -> setUnit(newValue));
        updateScale();
        setUnit(generalSettings.getSystemOfMeasurement());
    }

    void updateScale() {
        tpl.setLocationOffset(new Vec4(ScaleHelper.emsToPixels(-3.68), 0));
        tpl.setBorderWidth((int)ScaleHelper.emsToPixels(5));
        tpl.setFont(new Font("Arial", Font.PLAIN, (int)ScaleHelper.emsToPixels(1)));
    }

    void setUnit(SystemOfMeasurement newValue) {
        tpl.setUnit(
            newValue == SystemOfMeasurement.METRIC
                ? gov.nasa.worldwind.layers.TerrainProfileLayer.UNIT_METRIC
                : gov.nasa.worldwind.layers.TerrainProfileLayer.UNIT_IMPERIAL);
    }

}
