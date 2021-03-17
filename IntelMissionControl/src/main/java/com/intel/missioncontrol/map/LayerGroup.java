/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import org.asyncfx.beans.AsyncObservable;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncDoubleProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;

public class LayerGroup implements ILayer {

    public enum ToggleHint {
        /** Any number of sublayers can be enabled. */
        ANY,

        /** One sublayer or no sublayer can be enabled. */
        ONE_OR_NONE,

        /** Exactly one sublayer must be enabled. */
        ONE
    }

    private final AsyncObjectProperty<LayerName> name = new SimpleAsyncObjectProperty<>(this);
    private final AsyncBooleanProperty internal = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty enabled = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty pickEnabled = new SimpleAsyncBooleanProperty(this);
    private final AsyncDoubleProperty opacity = new SimpleAsyncDoubleProperty(this);
    private final AsyncObjectProperty<LayerGroupType> type = new SimpleAsyncObjectProperty<>(this);

    private final AsyncObjectProperty<ToggleHint> selectionBehavior =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<ToggleHint>().initialValue(ToggleHint.ANY).create());

    private final AsyncListProperty<ILayer> subLayers =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<ILayer>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(
                        subLayer -> new AsyncObservable[] {subLayer.enabledProperty()}))
                .create());

    public LayerGroup(LayerGroupType type) {
        this.name.overrideMetadata(
            new PropertyMetadata.Builder<LayerName>().initialValue(new LayerName(type.getName())).create());
        this.type.overrideMetadata(new PropertyMetadata.Builder<LayerGroupType>().initialValue(type).create());
    }

    public LayerGroup(LayerName layerName, LayerGroupType type, ToggleHint toggleHint) {
        this.name.overrideMetadata(new PropertyMetadata.Builder<LayerName>().initialValue(layerName).create());
        this.type.overrideMetadata(new PropertyMetadata.Builder<LayerGroupType>().initialValue(type).create());
        this.selectionBehavior.overrideMetadata(
            new PropertyMetadata.Builder<ToggleHint>().initialValue(toggleHint).create());
    }

    public LayerGroup(LayerGroupType type, ToggleHint toggleHint) {
        this.name.overrideMetadata(
            new PropertyMetadata.Builder<LayerName>().initialValue(new LayerName(type.getName())).create());
        this.type.overrideMetadata(new PropertyMetadata.Builder<LayerGroupType>().initialValue(type).create());
        this.selectionBehavior.overrideMetadata(
            new PropertyMetadata.Builder<ToggleHint>().initialValue(toggleHint).create());
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
        return pickEnabled;
    }

    @Override
    public AsyncDoubleProperty opacityProperty() {
        return opacity;
    }

    public AsyncObjectProperty<LayerGroupType> typeProperty() {
        return type;
    }

    public AsyncObjectProperty<ToggleHint> selectionBehaviorProperty() {
        return selectionBehavior;
    }

    public AsyncListProperty<ILayer> subLayersProperty() {
        return subLayers;
    }

    public AsyncObservableList<ILayer> getSubLayers() {
        return subLayers.get();
    }

    public LayerGroupType getType() {
        return type.get();
    }

    public ToggleHint getSelectionBehavior() {
        return selectionBehavior.get();
    }

}
