/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.modules.MapModule;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.SearchResultLayer;
import eu.mavinci.desktop.gui.wwext.search.SearchManager;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.concurrent.SynchronizationRoot;

@LayerDefaults(internal = true)
public class SearchResultsLayer extends WWLayerWrapper {

    @Inject
    SearchResultsLayer(
            @Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot,
            SearchManager searchManager,
            ISelectionManager selectionManager) {
        super(new SearchResultLayer(searchManager, syncRoot, selectionManager), syncRoot);
    }

    public ReadOnlyAsyncIntegerProperty resultCountProperty() {
        return ((SearchResultLayer)getWrappedLayer()).resultCountProperty();
    }
}
