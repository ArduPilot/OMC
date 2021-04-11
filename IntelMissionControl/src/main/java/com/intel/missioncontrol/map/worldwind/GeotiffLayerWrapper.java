package com.intel.missioncontrol.map.worldwind;

import com.intel.missioncontrol.settings.GeoTiffSettings;
import gov.nasa.worldwind.layers.Layer;
import org.asyncfx.concurrent.Dispatcher;

public class GeotiffLayerWrapper extends WWLayerWrapper {

    private final GeoTiffSettings geoTiffSettings;

    public GeotiffLayerWrapper(Layer wwLayer, Dispatcher dispatcher, GeoTiffSettings settings) {
        super(wwLayer, dispatcher);
        this.geoTiffSettings = settings;
    }

    public GeoTiffSettings getGeoTiffSettings() {
        return geoTiffSettings;
    }
}
