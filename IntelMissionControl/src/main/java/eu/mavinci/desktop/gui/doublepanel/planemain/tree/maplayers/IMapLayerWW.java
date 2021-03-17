/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers;

import gov.nasa.worldwind.layers.Layer;

public interface IMapLayerWW extends IMapLayer {

    Layer getWWLayer();
}
