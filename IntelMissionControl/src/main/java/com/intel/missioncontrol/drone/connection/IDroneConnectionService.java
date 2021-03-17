/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import com.intel.missioncontrol.drone.IDrone;
import org.asyncfx.concurrent.Future;

/** Provides and manages available drone connection items and connected IDrone instances. */
public interface IDroneConnectionService {
    /** List of available drone connection items from all sources (i.e. saved settings or connection listener). */
    ReadOnlyAsyncListProperty<IReadOnlyConnectionItem> availableDroneConnectionItemsProperty();

    /** Global connection state, indicating if any connection is currently established. */
    ReadOnlyAsyncObjectProperty<ConnectionState> connectionStateProperty();

    /** List of currently connected IConnectionItems. */
    ReadOnlyAsyncListProperty<IReadOnlyConnectionItem> connectedDroneConnectionItemsProperty();

    /** Get the corresponding connectionItem for an IDrone object. */
    IConnectionItem getConnectionItemForDrone(IDrone drone);

    /**
     * Get the corresponding drone for a connectionItem if it is connected.
     *
     * @return The IDrone object if connectionItem is connected; null otherwise.
     */
    IDrone getConnectedDrone(IReadOnlyConnectionItem connectionItem);

    /**
     * Connect to the given connectionItem.
     *
     * @return The connected drone.
     */
    Future<? extends IDrone> connectAsync(IReadOnlyConnectionItem connectionItem);

    /** Disconnect the given drone. */
    Future<Void> disconnectAsync(IDrone drone);
}
