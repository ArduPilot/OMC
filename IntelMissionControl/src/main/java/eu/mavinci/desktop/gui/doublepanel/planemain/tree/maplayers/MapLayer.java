/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers;

import eu.mavinci.core.desktop.listener.WeakListenerList;
import java.util.concurrent.CopyOnWriteArrayList;

public class MapLayer implements IMapLayer {

    protected WeakListenerList<IMapLayerListener> listeners;
    protected IMapLayer parent = null;
    protected CopyOnWriteArrayList<IMapLayer> subLayers = new CopyOnWriteArrayList<IMapLayer>();
    protected boolean isLoaded = false;
    protected boolean isVisible;
    protected boolean mute = false;
    protected boolean isLoading = false;
    private boolean selected;
    private boolean enabled = true;

    public MapLayer(boolean isVisible) {
        this(isVisible, null);
    }

    public MapLayer(boolean isVisible, IMapLayer parent) {
        listeners = new WeakListenerList<IMapLayerListener>("MapLayerListeners:" + hashCode());
        this.isVisible = isVisible;
        setParent(parent);
    }

    @Override
    public void setParent(IMapLayer parent) {
        if (this.parent == parent) {
            return;
        }

        if (this.parent != null) {
            removeMapListener(this.parent);
        }

        this.parent = parent;
        if (parent != null) {
            addMapListener(parent);
        }

        mapLayerStructureChanged(this);
    }

    @Override
    public void addMapListener(IMapLayerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeMapListener(IMapLayerListener listener) {
        listeners.remove(listener);
    }

    @Override
    public IMapLayer getParentLayer() {
        return parent;
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void setVisible(boolean isVisible) {
        if (isVisible == this.isVisible) {
            return;
        }

        this.isVisible = isVisible;
        mapLayerVisibilityChanged(this, isVisible);
    }

    @Override
    public boolean isVisibleIncludingParent() {
        if (parent == null) {
            return isVisible();
        } else {
            return parent.isVisibleIncludingParent() && isVisible();
        }
    }

    public void addMapLayer(IMapLayer layer) {
        addMapLayer(sizeMapLayer(), layer);
    }

    @Override
    public void addMapLayer(int i, IMapLayer layer) {
        getLayers().add(i, layer);
        layer.setParent(this);
        childMapLayerInserted(i, layer);
    }

    @Override
    public IMapLayer getMapLayer(int i) {
        return getLayers().get(i);
    }

    @Override
    public IMapLayer removeMapLayer(int i) {
        IMapLayer layer = getLayers().remove(i);
        if (layer != null) {
            layer.dispose();
            childMapLayerRemoved(i, layer);
            return layer;
        }

        return null;
    }

    @Override
    public void removeMapLayer(IMapLayer layer) {
        int i = getLayers().indexOf(layer);
        if (i != -1) {
            removeMapLayer(i);
        }
    }

    public void removeAllLayers(boolean doDispose) {
        if (sizeMapLayer() == 0) {
            return;
        }

        for (IMapLayer layer : getLayers()) {
            layer.setParent(null);
        }

        if (doDispose) {
            for (IMapLayer layer : getLayers()) {
                layer.dispose();
            }
        }

        getLayers().clear();
        mapLayerStructureChanged(this);
    }

    @Override
    public int sizeMapLayer() {
        return getLayers().size();
    }

    @Override
    public void childMapLayerInserted(int i, IMapLayer layer) {
        if (mute) {
            return;
        }

        for (IMapLayerListener listener : listeners) {
            listener.childMapLayerInserted(i, layer);
        }
    }

    @Override
    public void childMapLayerRemoved(int i, IMapLayer layer) {
        if (mute) {
            return;
        }

        for (IMapLayerListener listener : listeners) {
            listener.childMapLayerRemoved(i, layer);
        }
    }

    @Override
    public void mapLayerValuesChanged(IMapLayer layer) {
        if (mute) {
            return;
        }

        for (IMapLayerListener listener : listeners) {
            listener.mapLayerValuesChanged(layer);
        }
    }

    @Override
    public void mapLayerVisibilityChanged(IMapLayer layer, boolean newVisibility) {
        if (mute) {
            return;
        }

        for (IMapLayerListener listener : listeners) {
            listener.mapLayerVisibilityChanged(layer, newVisibility);
        }
    }

    @Override
    public void mapLayerStructureChanged(IMapLayer layer) {
        if (mute) {
            return;
        }

        for (IMapLayerListener listener : listeners) {
            listener.mapLayerStructureChanged(layer);
        }
    }

    @Override
    public boolean isMute() {
        return mute;
    }

    @Override
    public void setMute(boolean mute) {
        if (this.mute == mute) {
            return;
        }

        this.mute = mute;
    }

    @Override
    public void setSilentUnmute() {
        this.mute = false;
    }

    @Override
    public synchronized CopyOnWriteArrayList<IMapLayer> getLayers() {
        return subLayers;
    }

    @Override
    public void dispose() {
        for (IMapLayer layer : getLayers()) {
            layer.dispose();
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
