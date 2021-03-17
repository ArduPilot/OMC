/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.KmlLayerWrapper;
import com.intel.missioncontrol.settings.KmlsSettings;

public class KmlLayerViewModel extends SimpleLayerViewModel {

    private final KmlsSettings kmlsSettings;
    private final KmlLayerWrapper kmlLayerWrapper;

    public KmlLayerViewModel(KmlsSettings settings, KmlLayerWrapper layer, ILanguageHelper languageHelper) {
        super(layer, languageHelper);
        this.kmlsSettings = settings;
        this.kmlLayerWrapper = layer;
        setCanDelete(true);
        tooltip.bind(layer.getSettings().resourceProperty());
    }

    @Override
    protected void onDelete() {
        kmlsSettings.kmlsProperty().remove(kmlLayerWrapper.getSettings());
    }

}
