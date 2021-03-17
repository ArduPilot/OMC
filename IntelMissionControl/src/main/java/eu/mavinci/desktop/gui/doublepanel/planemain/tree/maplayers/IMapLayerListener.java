/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers;

public interface IMapLayerListener {

    void mapLayerValuesChanged(IMapLayer layer);

    void mapLayerVisibilityChanged(IMapLayer layer, boolean newVisibility);

    void childMapLayerInserted(int i, IMapLayer layer);

    void childMapLayerRemoved(int i, IMapLayer layer);

    void mapLayerStructureChanged(IMapLayer layer);

}
