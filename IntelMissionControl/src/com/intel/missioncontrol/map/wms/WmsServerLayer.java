/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.wms;

import com.intel.missioncontrol.beans.AsyncObservable;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.collections.LockedList;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.LayerGroup;
import com.intel.missioncontrol.map.LayerGroupType;
import com.intel.missioncontrol.map.elevation.IElevationLayer;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;

/** WmsServerLayer is a layer which is in turn a group of WMS map layers... */
public class WmsServerLayer extends LayerGroup {
    private final AsyncObjectProperty<WmsServer> wmsServer = new SimpleAsyncObjectProperty<>(this);

    private final SimpleAsyncListProperty<IElevationLayer> elevationsLayers =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<IElevationLayer>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(layer -> new AsyncObservable[] {layer.enabledProperty()}))
                .create());

    public WmsServerLayer(SynchronizationRoot syncRoot, WmsServer wmsServer) {
        super(LayerGroupType.WMS_SERVER_GROUP, ToggleHint.ONE_OR_NONE);
        this.wmsServer.set(wmsServer);
        nameProperty().bind(wmsServer.serverNameProperty());
        subLayersProperty().bindContent(wmsServer.wmsMapsProperty(), new WmsMapConverter(syncRoot, wmsServer.getUrl()));
    }

    public WmsServer getWmsServer() {
        return wmsServer.get();
    }

    // TODO fill in elevations
    public SimpleAsyncListProperty<IElevationLayer> elevationsLayersProperty() {
        return elevationsLayers;
    }

    public void dropCache() {
        try (LockedList<IElevationLayer> list = elevationsLayers.lock()) {
            for (IElevationLayer elevationLayer : list) {
                elevationLayer.dropCache();
            }
        }

        try (LockedList<ILayer> list = subLayersProperty().lock()) {
            for (ILayer imageryLayer : list) {
                if (imageryLayer instanceof WWLayerWrapper) {
                    WWLayerWrapper tmp = (WWLayerWrapper)imageryLayer;
                    tmp.dropCache();
                }
            }
        }
    }

}
