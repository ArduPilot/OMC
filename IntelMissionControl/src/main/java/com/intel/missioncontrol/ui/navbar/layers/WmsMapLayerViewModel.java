package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ILayer;

public class WmsMapLayerViewModel extends SimpleLayerViewModel {

    public WmsMapLayerViewModel(ILayer layer, ILanguageHelper languageHelper) {
        super(layer, languageHelper);
        tooltip.bind(layer.nameProperty(), layerName -> layerName.toString(languageHelper));
    }

}
