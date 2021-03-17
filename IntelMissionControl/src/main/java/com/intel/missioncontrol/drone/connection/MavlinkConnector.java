/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolReceiver;
import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkEndpoint;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkHandler;
import com.intel.missioncontrol.drone.connection.mavlink.TcpClient;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import io.dronefleet.mavlink.common.Heartbeat;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;
import org.asyncfx.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MavlinkConnector<T, TConnectionItem extends IMavlinkConnectionItem> implements IConnector<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavlinkConnector.class);

    private static final Duration tcpConnectTimeout = Duration.ofSeconds(5);
    protected final TConnectionItem connectionItem;
    protected final MavlinkEndpoint targetEndpoint;
    protected final IConnectionListenerService connectionListenerService;
    protected final IHardwareConfigurationManager hardwareConfigurationManager;
    private CancellationSource cancellationSource;
    private MavlinkHandler mavlinkHandler;
    private ConnectionProtocolSender connectionProtocolSender;
    private Future<Void> heartbeatSenderFuture;

    MavlinkConnector(
            TConnectionItem connectionItem,
            IConnectionListenerService connectionListenerService,
            IHardwareConfigurationManager hardwareConfigurationManager,
            int componentId) {
        this.connectionItem = connectionItem;
        this.connectionListenerService = connectionListenerService;
        this.hardwareConfigurationManager = hardwareConfigurationManager;

        int systemId = connectionItem.systemIdProperty().get();

        targetEndpoint =
            new MavlinkEndpoint(
                connectionItem.getTransportType(),
                new InetSocketAddress(connectionItem.getHost(), connectionItem.getPort()),
                systemId,
                componentId);
    }

    @Override
    public TConnectionItem getConnectionItem() {
        return connectionItem;
    }

    protected abstract T create(
            Heartbeat heartbeat,
            TConnectionItem connectionItem,
            MavlinkHandler mavlinkHandler,
            MavlinkEndpoint targetEndpoint,
            CancellationSource cancellationSource,
            ConnectionProtocolSender connectionProtocolSender,
            Future<Void> heartbeatSenderFuture);

    @Override
    public Future<T> connectAsync() {
        if (!(connectionListenerService.getConnectionListener() instanceof MavlinkConnectionListener)) {
            return Futures.failed(new NotImplementedException("Only mavlink listeners are currently supported"));
        }

        cancellationSource = new CancellationSource();

        // connect & get handler:
        return connectAndGetHandlerAsync(cancellationSource)
            .thenApplyAsync(
                handler -> {
                    this.mavlinkHandler = handler;

                    // Start sending own heartbeat to specific target endpoint:
                    connectionProtocolSender =
                        new ConnectionProtocolSender(targetEndpoint, mavlinkHandler, cancellationSource);

                    heartbeatSenderFuture =
                        connectionProtocolSender
                            .startSendingHeartbeatsAsync()
                            .whenFailed(e -> cancellationSource.cancel());

                    // Start waiting for heartbeat:
                    return expectHeartbeatAsync();
                })
            .thenApply(
                heartbeat ->
                    create(
                        heartbeat,
                        connectionItem,
                        mavlinkHandler,
                        targetEndpoint,
                        cancellationSource,
                        connectionProtocolSender,
                        heartbeatSenderFuture))
            .whenFailed(e -> cancellationSource.cancel());
    }

    private Future<MavlinkHandler> connectAndGetHandlerAsync(CancellationSource cancellationSource) {
        switch (targetEndpoint.getTcpIpTransportType()) {
        case TCP:
            {
                TcpClient tcpClient = new TcpClient();
                return tcpClient
                    .connectAsync(targetEndpoint.getAddress(), tcpConnectTimeout, cancellationSource)
                    .thenGet(tcpClient::getHandler);
            }
        case UDP:
            {
                MavlinkConnectionListener listener =
                    (MavlinkConnectionListener)connectionListenerService.getConnectionListener();

                return Futures.successful(listener.getHandler());
            }
        default:
            throw new IllegalArgumentException("Unsupported transport type");
        }
    }

    private Future<Heartbeat> expectHeartbeatAsync() {
        FutureCompletionSource<Heartbeat> futureCompletionSource = new FutureCompletionSource<>(cancellationSource);

        CancellationSource cts = new CancellationSource();
        cancellationSource.addListener(b -> cts.cancel());

        futureCompletionSource.getFuture().whenDone((Runnable)cts::cancel);

        ConnectionProtocolReceiver connectionProtocolReceiver =
            new ConnectionProtocolReceiver(targetEndpoint, mavlinkHandler, cts);

        // Listen for first heartbeat to find autopilot type before indicating success:
        connectionProtocolReceiver
            .registerHeartbeatHandlerAsync(
                receivedPayload -> {
                    Heartbeat heartbeat = receivedPayload.getPayload();
                    futureCompletionSource.setResult(heartbeat);
                },
                () -> {
                    // timeout
                    futureCompletionSource.setException(new TimeoutException());
                })
            .whenFailed(
                throwable -> {
                    LOGGER.error("failed registering heartbeat handler", throwable);
                    futureCompletionSource.setException(throwable);
                });

        return futureCompletionSource.getFuture();
    }

    @Override
    public Future<Void> disconnectAsync() {
        if (cancellationSource != null) {
            cancellationSource.cancel();
        }

        return Futures.successful();
    }

}
