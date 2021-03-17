/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;

public interface IReadOnlyConnectionItem {
    /** A user-friendly name of the corresponding device. */
    ReadOnlyAsyncStringProperty nameProperty();

    /**
     * A string matching a known device type identifier (e.g. platformId of a PlatformDescription). null if
     * unknown/undefined.
     */
    ReadOnlyAsyncStringProperty descriptionIdProperty();

    /** True if this connection item is online and advertising itself on the network */
    ReadOnlyAsyncBooleanProperty isOnlineProperty();

    /** True if this connection item is known to the application and might include user settings. */
    ReadOnlyAsyncBooleanProperty isKnownProperty();

    IConnectionItem createMutableCopy();

    boolean isSameConnection(IReadOnlyConnectionItem other);

    default String getName() {
        return nameProperty().getValue();
    }

    default String getDescriptionId() {
        return descriptionIdProperty().getValue();
    }

    default boolean isOnline() {
        return isOnlineProperty().getValue();
    }

    default boolean isKnown() {
        return isKnownProperty().getValue();
    }
}
