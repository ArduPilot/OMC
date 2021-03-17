/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;

@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class MapLayerWW extends MapLayer implements IMapLayerWW {

    protected Layer wwLayer;

    public MapLayerWW(Layer wwLayer, boolean isVisible, IMapLayer parent) {
        super(isVisible, parent);
        this.wwLayer = wwLayer;
    }

    public MapLayerWW(Layer wwLayer, boolean isVisible) {
        super(isVisible);
        this.wwLayer = wwLayer;
    }

    public MapLayerWW(Layer wwLayer) {
        super(true);
        this.wwLayer = wwLayer;
    }

    public double getOpacity() {
        if (wwLayer == null) {
            return lastOpacitySet;
        }

        return wwLayer.getOpacity();
    }

    protected double lastOpacitySet = 1;

    public void setOpacity(double opacity) {
        lastOpacitySet = opacity;

        if (wwLayer == null) {
            return;
        }

        if (opacity == getOpacity()) {
            return;
        }

        wwLayer.setOpacity(opacity);
        wwLayer.firePropertyChange(AVKey.LAYER, null, wwLayer);
        mapLayerValuesChanged(this);
    }

    @Override
    public Layer getWWLayer() {
        return wwLayer;
    }

}
