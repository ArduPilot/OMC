/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.LayerName;
import com.intel.missioncontrol.map.worldwind.property.WWAsyncBooleanProperty;
import com.intel.missioncontrol.map.worldwind.property.WWAsyncDoubleProperty;
import eu.mavinci.desktop.gui.wwext.WWFactory;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.SynchronizationRoot;

/** Wraps an existing WorldWind layer class and exposes basic properties that are common to all layer classes. */
public class WWLayerWrapper implements ILayer {

    private final gov.nasa.worldwind.layers.Layer wwLayer;
    private final AsyncObjectProperty<LayerName> name = new SimpleAsyncObjectProperty<>(this);
    private final AsyncBooleanProperty internal = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty enabled;
    private final AsyncBooleanProperty pickable;
    private final AsyncDoubleProperty opacity;

    public WWLayerWrapper(gov.nasa.worldwind.layers.Layer wwLayer, SynchronizationRoot syncRoot) {
        this.wwLayer = wwLayer;

        String defaultName = null;
        boolean defaultEnabled = true;
        boolean defaultPickable = true;
        boolean defaultInternal = false;
        LayerDefaults defaults = getClass().getAnnotation(LayerDefaults.class);
        if (defaults != null) {
            defaultName = defaults.name();
            defaultEnabled = defaults.enabled();
            defaultPickable = defaults.pickable();
            defaultInternal = defaults.internal();
        }

        if (defaultName != null && !defaultName.isEmpty()) {
            name.set(new LayerName(defaultName));
        } else {
            name.set(new LayerName(wwLayer.getName()));
        }

        wwLayer.setEnabled(defaultEnabled);
        wwLayer.setPickEnabled(defaultPickable);
        wwLayer.setOpacity(1.0);

        internal.set(defaultInternal);

        enabled =
            new WWAsyncBooleanProperty(
                this, "enabled", syncRoot, "Enabled", wwLayer, wwLayer::setEnabled, defaultEnabled);

        pickable =
            new WWAsyncBooleanProperty(
                this, "pickable", syncRoot, AVKey.PICK_ENABLED, wwLayer, wwLayer::setPickEnabled, defaultPickable);

        opacity =
            new WWAsyncDoubleProperty(this, "opacity", syncRoot, AVKey.OPACITY, wwLayer, wwLayer::setOpacity, 1.0);
    }

    protected Layer getWrappedLayer() {
        return wwLayer;
    }

    @Override
    public AsyncObjectProperty<LayerName> nameProperty() {
        return name;
    }

    @Override
    public AsyncBooleanProperty internalProperty() {
        return internal;
    }

    @Override
    public AsyncBooleanProperty enabledProperty() {
        return enabled;
    }

    @Override
    public AsyncBooleanProperty pickableProperty() {
        return pickable;
    }

    @Override
    public AsyncDoubleProperty opacityProperty() {
        return opacity;
    }

    public void dropCache() {
        WWFactory.dropDataCache(wwLayer);
    }

}
