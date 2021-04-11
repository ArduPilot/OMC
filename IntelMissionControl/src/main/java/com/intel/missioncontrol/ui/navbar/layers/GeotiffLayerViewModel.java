package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.GeotiffLayerWrapper;
import com.intel.missioncontrol.settings.GeoTiffsSettings;
import javafx.beans.value.ObservableValue;

public class GeotiffLayerViewModel extends SimpleLayerViewModel {

    private final GeoTiffsSettings geoTiffsSettings;
    private final GeotiffLayerWrapper geotiffLayerWrapper;

    public GeotiffLayerViewModel(
            GeoTiffsSettings settings,
            GeotiffLayerWrapper layer,
            ILanguageHelper languageHelper) {
        super(layer, languageHelper);
        this.geoTiffsSettings = settings;
        this.geotiffLayerWrapper = layer;
        setCanDelete(true);
        tooltip.bind(layer.getGeoTiffSettings().pathProperty());
    }

    @Override
    protected void onDelete() {
        if (geotiffLayerWrapper == null) {
            return;
        }

        geoTiffsSettings.geoTiffsProperty().remove(geotiffLayerWrapper.getGeoTiffSettings());
    }

}
