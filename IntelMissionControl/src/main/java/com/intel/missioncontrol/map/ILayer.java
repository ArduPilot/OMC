/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;

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

    default LayerName getName() {
        return nameProperty().get();
    }

    default void setName(LayerName name) {
        nameProperty().set(name);
    }

    default boolean isEnabled() {
        return enabledProperty().get();
    }

    default void setEnabled(boolean enabled) {
        enabledProperty().set(enabled);
    }

    default boolean isPickable() {
        return pickableProperty().get();
    }

    default void setPickable(boolean enabled) {
        pickableProperty().set(enabled);
    }

    default double getOpacity() {
        return opacityProperty().get();
    }

    default void setOpacity(double opacity) {
        opacityProperty().set(opacity);
    }

}
