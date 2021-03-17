/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.wms;

import com.intel.missioncontrol.settings.WmsServerSettings;
import org.asyncfx.collections.AsyncObservableList;

public interface IWmsManager {
    AsyncObservableList<WmsServer> wmsServersProperty();

    AsyncObservableList<WmsServerLayer> wmsServerLayersProperty();

    boolean containsWmsServer(String wmsServerUrl);

    WmsServer getWmsServer(String wmsServerUrl);

    void addWmsServer(String wmsServerUrl);

    void addWmsServer(WmsServerSettings wmsServerSettings);

    void loadWmsServer(WmsServer wmsServer);

    void deleteWmsServer(WmsServer wmsServer);

    boolean isDefaultServer(WmsServer wmsServer);
}
