/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.concurrent.Future;

/** Listens for incoming connections (if accepting connections) and presents them as available IConnectionItems. */
public interface IConnectionListener {
    AsyncBooleanProperty acceptIncomingConnectionsProperty();

    AsyncIntegerProperty listeningPortProperty();

    ReadOnlyAsyncListProperty<IConnectionItem> onlineDroneConnectionItemsProperty();

    ReadOnlyAsyncListProperty<IConnectionItem> onlineCameraConnectionItemsProperty();

    /** The current persistent listener error possibly preventing any connections, or null if no error. */
    ReadOnlyAsyncObjectProperty<Throwable> listenerErrorProperty();

    /** restart listener if in a failed state */
    Future<Void> restartAsync();
}
