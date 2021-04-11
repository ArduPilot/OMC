package com.intel.missioncontrol.map.worldwind;

import com.intel.missioncontrol.settings.KmlSettings;
import gov.nasa.worldwind.layers.Layer;
import org.asyncfx.concurrent.Dispatcher;

public class KmlLayerWrapper extends WWLayerWrapper {

    private final KmlSettings settings;

    public KmlLayerWrapper(Layer wwLayer, Dispatcher dispatcher, KmlSettings settings) {
        super(wwLayer, dispatcher);
        this.settings = settings;
    }

    public KmlSettings getSettings() {
        return settings;
    }
}
