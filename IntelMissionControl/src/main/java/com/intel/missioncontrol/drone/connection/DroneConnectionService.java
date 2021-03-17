/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.settings.ConnectionSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.NotImplementedException;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.collections.AsyncListChangeListener;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneConnectionService implements IDroneConnectionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DroneConnectionService.class);

    private final MockDroneConnector.Factory mockDroneConnectorFactory;
    private final MavlinkDroneConnector.Factory mavlinkDroneConnectorFactory;

    private final AsyncListProperty<IReadOnlyConnectionItem> availableDroneConnectionItems =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<IReadOnlyConnectionItem>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final AsyncListProperty<IConnectionItem> droneConnectionItems =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<IConnectionItem>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final AsyncListProperty<IReadOnlyConnectionItem> connectedDroneConnectionItems =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<IReadOnlyConnectionItem>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final ConcurrentHashMap<IDrone, IConnector<? extends IDrone>> connectorMap = new ConcurrentHashMap<>();

    private final AsyncObjectProperty<ConnectionState> connectionState = new SimpleAsyncObjectProperty<>(this);

    @Inject
    public DroneConnectionService(
            ISettingsManager settingsManager,
            IConnectionListenerService droneConnectionListenerService,
            MockDroneConnector.Factory mockDroneConnectorFactory,
            MavlinkDroneConnector.Factory mavlinkDroneConnectorFactory) {
        this.mockDroneConnectorFactory = mockDroneConnectorFactory;
        this.mavlinkDroneConnectorFactory = mavlinkDroneConnectorFactory;

        ConnectionSettings connectionSettings = settingsManager.getSection(ConnectionSettings.class);

        // Known Drones from connection settings:
        availableDroneConnectionItems.bindContent(droneConnectionItems);
        droneConnectionItems.add(new MockConnectionItem());
        droneConnectionItems.addAll(connectionSettings.connectionItemsListProperty().get());
        connectionSettings.connectionItemsListProperty().addListener(this::onKnownConnectionItemsChanged);

        // Online / auto detected drones via udp listener:
        droneConnectionListenerService
            .getConnectionListener()
            .onlineDroneConnectionItemsProperty()
            .addListener(this::onOnlineConnectionItemsChanged);

        connectedDroneConnectionItems.addListener((observable, oldValue, newValue) -> updateConnectionState());

        updateConnectionState();
    }

    private void onOnlineConnectionItemsChanged(AsyncListChangeListener.Change<? extends IConnectionItem> change) {
        try (LockedList<IConnectionItem> lockedList = droneConnectionItems.lock()) {
            while (change.next()) {
                for (var item : change.getRemoved()) {
                    Optional<IConnectionItem> foundItem = findItemInList(item, lockedList);
                    if (foundItem.isPresent()) {
                        foundItem.get().isOnlineProperty().set(false);
                        if (!foundItem.get().isKnown()) {
                            lockedList.remove(foundItem.get());
                        }
                    } else {
                        LOGGER.error("onOnlineConnectionItemsChanged: List element not found");
                    }
                }

                for (var item : change.getAddedSubList()) {
                    Optional<IConnectionItem> foundItem = findItemInList(item, lockedList);
                    if (foundItem.isPresent()) {
                        foundItem.get().isOnlineProperty().set(true);
                    } else {
                        lockedList.add(item);
                    }
                }
            }
        }
    }

    private void onKnownConnectionItemsChanged(AsyncListChangeListener.Change<? extends IConnectionItem> change) {
        try (LockedList<IConnectionItem> lockedList = droneConnectionItems.lock()) {
            while (change.next()) {
                if (!change.wasUpdated()) {
                    for (var item : change.getRemoved()) {
                        Optional<IConnectionItem> foundItem = findItemInList(item, lockedList);
                        if (foundItem.isPresent()) {
                            foundItem.get().unbind();
                            foundItem.get().isKnownProperty().set(false);
                            if (!foundItem.get().isOnline()) {
                                lockedList.remove(foundItem.get());
                            }
                        } else {
                            LOGGER.error("onKnownConnectionItemsChanged: List element not found");
                        }
                    }

                    for (var item : change.getAddedSubList()) {
                        Optional<IConnectionItem> foundItem = findItemInList(item, lockedList);
                        if (foundItem.isPresent()) {
                            // TODO: lock section
                            foundItem.get().bindContent(item);
                            foundItem.get().isOnlineProperty().set(item.isOnline());
                            foundItem.get().isKnownProperty().set(item.isKnown());
                        } else {
                            lockedList.add(change.getFrom(), item);
                        }
                    }
                }
                // bindings handle "updated" case
            }
        }
    }

    private Optional<IConnectionItem> findItemInList(IReadOnlyConnectionItem item, List<IConnectionItem> list) {
        return list.stream().filter(i -> i.isSameConnection(item)).findFirst();
    }

    private void updateConnectionState() {
        connectionState.set(
            connectedDroneConnectionItems.isEmpty() ? ConnectionState.NOT_CONNECTED : ConnectionState.CONNECTED);
    }

    @Override
    public ReadOnlyAsyncListProperty<IReadOnlyConnectionItem> availableDroneConnectionItemsProperty() {
        return availableDroneConnectionItems;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<ConnectionState> connectionStateProperty() {
        return connectionState;
    }

    @Override
    public ReadOnlyAsyncListProperty<IReadOnlyConnectionItem> connectedDroneConnectionItemsProperty() {
        return connectedDroneConnectionItems;
    }

    @Override
    public Future<? extends IDrone> connectAsync(IReadOnlyConnectionItem connectionItem) {
        IConnector<? extends IDrone> connector;
        if (connectionItem instanceof MockConnectionItem) {
            connector = mockDroneConnectorFactory.create((MockConnectionItem)connectionItem);
        } else if (connectionItem instanceof LegacyLocalSimulationConnectionItem) {
            connector = new LegacyLocalSimulationConnector((LegacyLocalSimulationConnectionItem)connectionItem);
        } else if (connectionItem instanceof MavlinkDroneConnectionItem) {
            connector = mavlinkDroneConnectorFactory.create((MavlinkDroneConnectionItem)connectionItem);
        } else {
            throw new NotImplementedException("No suitable connector found for connection item type");
        }

        connectionState.set(ConnectionState.CONNECTING);
        try {
            return connector
                .connectAsync()
                .whenSucceeded(
                    (drone) -> {
                        connectorMap.put(drone, connector);
                        connectedDroneConnectionItems.add(connectionItem);
                    })
                .whenFailed(e -> LOGGER.warn("Connect failed", e))
                .whenDone((v) -> updateConnectionState());
        } catch (Exception e) {
            // Hardware configuration not available
            SettableFuture<IDrone> future = SettableFuture.create();
            future.setException(e);
            return Futures.fromListenableFuture(future);
        }
    }

    @Override
    public Future<Void> disconnectAsync(IDrone drone) {
        if (drone == null) {
            return Futures.successful(null);
        }

        IConnector<? extends IDrone> droneConnector = connectorMap.get(drone);
        return droneConnector
            .disconnectAsync()
            .whenSucceeded(
                (v) -> {
                    connectorMap.remove(drone);
                    connectedDroneConnectionItems.remove(droneConnector.getConnectionItem());
                })
            .whenDone((v) -> updateConnectionState());
    }

    @Override
    public IConnectionItem getConnectionItemForDrone(IDrone drone) {
        if (drone == null) {
            return null;
        }

        IConnector<? extends IDrone> droneConnector = connectorMap.get(drone);
        return droneConnector != null ? droneConnector.getConnectionItem() : null;
    }

    @Override
    public IDrone getConnectedDrone(IReadOnlyConnectionItem connectionItem) {
        return connectorMap
            .entrySet()
            .stream()
            .filter(x -> x.getValue().getConnectionItem().isSameConnection(connectionItem))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }
}
