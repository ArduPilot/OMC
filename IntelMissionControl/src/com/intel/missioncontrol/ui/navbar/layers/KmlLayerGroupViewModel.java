/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.LayerGroup;
import com.intel.missioncontrol.map.worldwind.KmlLayerWrapper;
import com.intel.missioncontrol.settings.KmlsSettings;

public class KmlLayerGroupViewModel extends LayerGroupViewModel {

    public KmlLayerGroupViewModel(
            LayerGroup kmlsLayerGroup, ILanguageHelper languageHelper, KmlsSettings kmlsSettings) {
        super(kmlsLayerGroup, languageHelper, false, true);

        subLayerItems.bindContent(
            kmlsLayerGroup.subLayersProperty(),
            layer -> new KmlLayerViewModel(kmlsSettings, (KmlLayerWrapper)layer, languageHelper));
    }

}
