/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.AsyncDoubleProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;

/**
 * Represents a map layer. All map layers expose some basic properties, but may be extended by creating custom layer
 * classes that implement this interface.
 */
public interface ILayer {

    AsyncBooleanProperty internalProperty();

    AsyncObjectProperty<LayerName> nameProperty();

    AsyncBooleanProperty enabledProperty();

    AsyncBooleanProperty pickableProperty();

    AsyncDoubleProperty opacityProperty();

    default boolean isInternal() {
        return internalProperty().get();
    }

    default void setInternal(boolean value) {
        internalProperty().set(value);
    }

    default void setInternalAsync(boolean value) {
        internalProperty().setAsync(value);
    }

    default LayerName getName() {
        return nameProperty().get();
    }

    default void setName(LayerName name) {
        nameProperty().set(name);
    }

    default void setNameAsync(LayerName name) {
        nameProperty().setAsync(name);
    }

    default boolean isEnabled() {
        return enabledProperty().get();
    }

    default void setEnabled(boolean enabled) {
        enabledProperty().set(enabled);
    }

    default void setEnabledAsync(boolean enabled) {
        enabledProperty().setAsync(enabled);
    }

    default boolean isPickable() {
        return pickableProperty().get();
    }

    default void setPickable(boolean enabled) {
        pickableProperty().set(enabled);
    }

    default void setPickableAsync(boolean enabled) {
        pickableProperty().setAsync(enabled);
    }

    default double getOpacity() {
        return opacityProperty().get();
    }

    default void setOpacity(double opacity) {
        opacityProperty().set(opacity);
    }

    default void setOpacityAsync(double opacity) {
        opacityProperty().setAsync(opacity);
    }

}
