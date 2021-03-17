/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers;

import eu.mavinci.core.flightplan.IMuteable;
import eu.mavinci.core.helper.IPropertiesStoreable;
import gov.nasa.worldwind.Disposable;
import java.util.List;

/**
 * Representation of a layer Something to make in/visible in (world wind: WW) WWWinWidget An independent interface to
 * link WW Principle: To link a single layer or multiple layers together into one layer.
 */
public interface IMapLayer extends IMapLayerListener, Disposable, IMuteable {

    public boolean isVisible();

    public void setVisible(boolean isVisible);

    public boolean isVisibleIncludingParent();

    public IMapLayer getParentLayer();

    public void setParent(IMapLayer parent);
    // public IPlaneTreeController getController();

    public void addMapListener(IMapLayerListener listener);

    public void removeMapListener(IMapLayerListener listener);

    // sublayer access
    public int sizeMapLayer();

    public IMapLayer getMapLayer(int i);

    public IMapLayer removeMapLayer(int i);

    public void removeMapLayer(IMapLayer layer);

    public void addMapLayer(IMapLayer layer);

    public void addMapLayer(int i, IMapLayer layer);

    public List<IMapLayer> getLayers();

    default boolean isEnabled() {
        return true;
    }

    default void setEnabled(boolean enabled) {}

}
