package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.wms.IWmsManager;
import com.intel.missioncontrol.map.wms.WmsServer;
import com.intel.missioncontrol.map.wms.WmsServerLayer;

public class WmsServerLayerViewModel extends LayerGroupViewModel {

    private final IWmsManager wmsManager;
    private final WmsServer wmsServer;

    public WmsServerLayerViewModel(
            WmsServerLayer wmsServerLayer, ILanguageHelper languageHelper, IWmsManager wmsManager) {
        super(wmsServerLayer, languageHelper, false, false);
        subLayerItems.bindContent(
            wmsServerLayer.subLayersProperty(), layer -> new WmsMapLayerViewModel(layer, languageHelper));
        this.wmsManager = wmsManager;
        wmsServer = wmsServerLayer.getWmsServer();
        if (!wmsManager.isDefaultServer(wmsServer)) {
            setCanDelete(true);
        }

        if (!wmsManager.isDefaultServer(wmsServer)) {
            tooltip.set(wmsServer.getUrl());
        }

        hasWarningProperty().bind(wmsServer.hasWarningProperty());
        warningProperty().bind(wmsServer.warningProperty());
    }

    @Override
    protected void onDelete() {
        wmsManager.deleteWmsServer(wmsServer);
    }

    @Override
    void onResolveWarning() {
        wmsManager.loadWmsServer(wmsServer);
    }
}
