/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolReceiver;
import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkEndpoint;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkHandler;
import com.intel.missioncontrol.drone.connection.mavlink.ReceivedPayload;
import com.intel.missioncontrol.drone.connection.mavlink.UdpBroadcastListener;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.settings.ConnectionSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import io.dronefleet.mavlink.common.Heartbeat;
import io.dronefleet.mavlink.common.MavComponent;
import io.dronefleet.mavlink.util.EnumValue;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavlinkConnectionListener implements IConnectionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavlinkConnectionListener.class);

    private final AsyncBooleanProperty acceptIncomingConnections = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty broadcastOwnHeartbeat = new SimpleAsyncBooleanProperty(this);
    private final AsyncIntegerProperty listeningPort = new SimpleAsyncIntegerProperty(this);
    private final AsyncListProperty<IConnectionItem> onlineConnectionItems =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<IConnectionItem>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());
    private final AsyncListProperty<IConnectionItem> onlineDroneConnectionItems =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<IConnectionItem>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());
    private final AsyncListProperty<IConnectionItem> onlineCameraConnectionItems =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<IConnectionItem>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());
    private final String defaultPlatformId;

    private final AsyncObjectProperty<Throwable> listenerError = new SimpleAsyncObjectProperty<>(this);

    private CancellationSource listenerCancellationSource;

    private UdpBroadcastListener udpBroadcastListener;

    private final IHardwareConfigurationManager hardwareConfigurationManager;

    MavlinkConnectionListener(
            ISettingsManager settingsManager, IHardwareConfigurationManager hardwareConfigurationManager) {
        this.hardwareConfigurationManager = hardwareConfigurationManager;
        ConnectionSettings connectionSettings = settingsManager.getSection(ConnectionSettings.class);

        acceptIncomingConnections.bindBidirectional(connectionSettings.acceptIncomingConnectionsProperty());
        broadcastOwnHeartbeat.bindBidirectional(connectionSettings.broadcastOwnHeartbeatProperty());
        listeningPort.bindBidirectional(connectionSettings.receivingPortProperty());

        acceptIncomingConnections.addListener(
            (observable, oldValue, newValue) -> {
                if (!oldValue && newValue) {
                    startListeningAsync();
                } else if (oldValue && !newValue) {
                    stopListeningAsync();
                }
            });

        listeningPort.addListener(o -> restartAsync());

        if (acceptIncomingConnections.get()) {
            startListeningAsync();
        }

        defaultPlatformId = hardwareConfigurationManager.getImmutableDefault().getPlatformDescription().getId();
    }

    public MavlinkHandler getHandler() {
        return udpBroadcastListener.getHandler();
    }

    private Future<Void> startListeningAsync() {
        // start listener:
        LOGGER.debug("Starting MavlinkConnectionListener");
        listenerError.set(null);
        listenerCancellationSource = new CancellationSource();

        udpBroadcastListener = new UdpBroadcastListener();
        return udpBroadcastListener
            .bindAsync(listeningPort.get(), listenerCancellationSource)
            .whenSucceeded(
                v -> {
                    LOGGER.debug("MavlinkConnectionListener started");
                    try {
                        if (broadcastOwnHeartbeat.get()) {
                            startBroadcastingOwnHeartbeatsAsync().whenFailed(this::onListenerError);
                        }

                        listenForHeartbeatsAsync().whenFailed(this::onListenerError);
                    } catch (Exception e) {
                        onListenerError(e);
                    }
                })
            .whenFailed(this::onListenerError);
    }

    private Future<Void> startBroadcastingOwnHeartbeatsAsync() {
        MavlinkHandler handler = udpBroadcastListener.getHandler();
        if (handler == null) {
            return Futures.failed(new IllegalStateException("UDP handler not present"));
        }

        ConnectionProtocolSender connectionProtocolSender =
            new ConnectionProtocolSender(
                MavlinkEndpoint.fromUdpBroadcastOnPort(14570), // TODO port
                handler,
                listenerCancellationSource);

        return connectionProtocolSender.startSendingHeartbeatsAsync().whenFailed(this::onListenerError);
    }

    private Future<Void> listenForHeartbeatsAsync() {
        MavlinkHandler handler = udpBroadcastListener.getHandler();
        if (handler == null) {
            return Futures.failed(new IllegalStateException("UDP handler not present"));
        }

        // handler for any heartbeat (known + unknown):
        ConnectionProtocolReceiver receiver =
            new ConnectionProtocolReceiver(MavlinkEndpoint.UnspecifiedUdp, handler, listenerCancellationSource);

        return receiver.registerHeartbeatHandlerAsync(
            receivedPayload -> {
                IConnectionItem connItem = createConnectionItem(receivedPayload);
                if (connItem == null) {
                    return;
                }

                IConnectionItem oldItem = findItem(connItem);
                if (oldItem == null) {
                    addHeartbeatListener(connItem, receivedPayload.getSenderEndpoint(), listenerCancellationSource);

                    LOGGER.info("Connection item added: " + connItem);
                }
            },
            () -> {
                // ignore timeout
            });
    }

    private void addHeartbeatListener(
            IConnectionItem connItem, MavlinkEndpoint mavlinkEndpoint, CancellationSource cancellationSource) {
        // handler for heartbeats for one specific connectionItem:
        CancellationSource cts = new CancellationSource();
        cancellationSource.addListener(cts::cancel);

        ConnectionProtocolReceiver receiver =
            new ConnectionProtocolReceiver(mavlinkEndpoint, udpBroadcastListener.getHandler(), cts);

        AsyncListProperty<IConnectionItem> items;
        if (connItem instanceof MavlinkDroneConnectionItem) {
            items = onlineDroneConnectionItems;
        } else if (connItem instanceof MavlinkCameraConnectionItem) {
            items = onlineCameraConnectionItems;
        } else {
            LOGGER.error("Invalid connection item type: " + connItem);
            return;
        }

        onlineConnectionItems.add(connItem);
        items.add(connItem);

        receiver.registerHeartbeatHandlerAsync(
                receivedPayload -> {
                    // ignore
                },
                () -> {
                    // timeout: stop listening to this item and remove from list:
                    cts.cancel();
                    items.remove(connItem);
                    onlineConnectionItems.remove(connItem);
                    LOGGER.info("Connection item removed: " + connItem);
                })
            .whenFailed(this::onListenerError);
    }

    private void onListenerError(Throwable e) {
        LOGGER.error("MavlinkConnectionListener error: " + e.toString(), e);
        listenerCancellationSource.cancel();
        onlineConnectionItems.clear();
        onlineDroneConnectionItems.clear();
        onlineCameraConnectionItems.clear();
        listenerError.set(e instanceof ExecutionException ? e.getCause() : e);
    }

    private IConnectionItem createConnectionItem(ReceivedPayload<Heartbeat> receivedPayload) {
        int compId = receivedPayload.getSenderEndpoint().getComponentId();
        MavComponent mavComponent = EnumValue.create(MavComponent.class, compId).entry();
        if (mavComponent == null) {
            LOGGER.debug("Received unknown mavlink heartbeat component id " + compId);
            return null;
        }

        switch (mavComponent) {
        case MAV_COMP_ID_ALL:
        case MAV_COMP_ID_AUTOPILOT1:
        case MAV_COMP_ID_SYSTEM_CONTROL:
            return createDroneConnectionItem(receivedPayload);
        case MAV_COMP_ID_CAMERA:
            return createCameraConnectionItem(receivedPayload, 1);
        case MAV_COMP_ID_CAMERA2:
            return createCameraConnectionItem(receivedPayload, 2);
        case MAV_COMP_ID_CAMERA3:
            return createCameraConnectionItem(receivedPayload, 3);
        case MAV_COMP_ID_CAMERA4:
            return createCameraConnectionItem(receivedPayload, 4);
        case MAV_COMP_ID_CAMERA5:
            return createCameraConnectionItem(receivedPayload, 5);
        case MAV_COMP_ID_CAMERA6:
            return createCameraConnectionItem(receivedPayload, 6);
        default:
            LOGGER.debug("Received unsupported mavlink heartbeat component id " + compId);
            return null;
        }
    }

    private MavlinkDroneConnectionItem createDroneConnectionItem(ReceivedPayload<Heartbeat> receivedPayload) {
        Heartbeat heartbeat = receivedPayload.getPayload();
        InetSocketAddress senderAddress = receivedPayload.getSenderEndpoint().getAddress();
        String name =
            heartbeat.autopilot().entry().name()
                + " @ "
                + senderAddress.getHostString()
                + ":"
                + senderAddress.getPort();

        // TODO auto-detection of platformId.

        String platformId = null;

        return new MavlinkDroneConnectionItem(
            true,
            false,
            name,
            platformId,
            receivedPayload.getSenderEndpoint().getTcpIpTransportType(),
            senderAddress.getHostString(),
            senderAddress.getPort(),
            receivedPayload.getSenderEndpoint().getSystemId());
    }

    private MavlinkCameraConnectionItem createCameraConnectionItem(
            ReceivedPayload<Heartbeat> receivedPayload, int cameraNumber) {
        InetSocketAddress senderAddress = receivedPayload.getSenderEndpoint().getAddress();

        return new MavlinkCameraConnectionItem(
            "Camera #" + cameraNumber + " @ " + senderAddress.getHostString() + ":" + senderAddress.getPort(),
            cameraNumber,
            null,
            true,
            false,
            receivedPayload.getSenderEndpoint().getTcpIpTransportType(),
            senderAddress.getHostString(),
            senderAddress.getPort(),
            receivedPayload.getSenderEndpoint().getSystemId(),
            receivedPayload.getSenderEndpoint().getComponentId());
    }

    private IConnectionItem findItem(IConnectionItem connItem) {
        IConnectionItem res = null;
        try (LockedList<IConnectionItem> lockedList = onlineConnectionItems.lock()) {
            for (var item : lockedList) {
                if (item.isSameConnection(connItem)) {
                    res = item;
                    break;
                }
            }
        }

        return res;
    }

    private Future<Void> stopListeningAsync() {
        LOGGER.info("Stopping MavlinkConnectionListener, dropping all connection items");
        listenerError.set(null);
        if (listenerCancellationSource != null) {
            listenerCancellationSource.cancel();
        }

        udpBroadcastListener = null;
        onlineConnectionItems.clear();
        onlineCameraConnectionItems.clear();
        onlineDroneConnectionItems.clear();

        return Futures.successful();
    }

    @Override
    public AsyncBooleanProperty acceptIncomingConnectionsProperty() {
        return acceptIncomingConnections;
    }

    @Override
    public AsyncIntegerProperty listeningPortProperty() {
        return listeningPort;
    }

    @Override
    public ReadOnlyAsyncListProperty<IConnectionItem> onlineDroneConnectionItemsProperty() {
        return onlineDroneConnectionItems;
    }

    @Override
    public ReadOnlyAsyncListProperty<IConnectionItem> onlineCameraConnectionItemsProperty() {
        return onlineCameraConnectionItems;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Throwable> listenerErrorProperty() {
        return listenerError;
    }

    @Override
    public Future<Void> restartAsync() {
        boolean enabled = acceptIncomingConnections.get();
        Future<Void> future = enabled ? stopListeningAsync() : Futures.successful(null);
        return future.thenRunAsync(() -> enabled ? startListeningAsync() : Futures.successful(null));
    }

}
