/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.intel.missioncontrol.plane.dronekit.ConnectionManager;
import com.intel.missioncontrol.plane.dronekit.model.DroneKitConnector;
import com.intel.missioncontrol.plane.dronekit.model.discovery.BroadcastDiscovery;
import com.intel.missioncontrol.plane.dronekit.model.discovery.DiscoveryCallback;
import com.intel.missioncontrol.plane.dronekit.model.discovery.PortScanDiscovery;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;

/**
 * View controller for {@link MavLinkConnectionWidget}. Doesn't inherit from ViewModel, to allow use independent use.
 * <p>
 * Can run standalone, see AutoConnectControllerTests
 */
public class MavLinkConnectWidgetController {
    final MavLinkConnectionWidget view;
    private ConnectionManager connectionManager = null;

    private ExecutorService service;
    private BroadcastDiscovery broadcastDiscovery;
    Future<?> scanFuture = null;

    private final InvalidationListener connectionListener = (observable) -> {
        ConnectionManager.ConnectionState newState = connectionManager != null ?
                connectionManager.connectionStateProperty().getValue() : ConnectionManager.ConnectionState.DISCONNECTED;
        Platform.runLater(() -> onConnectorStateChange(newState));
    };

    private final MavLinkConnectionWidget.ActionHandler actionHandler = new MavLinkConnectionWidget.ActionHandler() {
        @Override
        public void onStartScan() {
            statScan();
        }

        @Override
        public void onStopScan() {
            stopScan();
        }

        @Override
        public void onConnect() {
            MavLinkConnectionWidget.ConnectionSettings cs = view.currentViewConnectionSetting.get();
            if (cs == null) return;

            ConnectionParameter connParam = toParam(cs);

            if (connParam != null && connectionManager != null) {
                connectionManager.connect(connParam);
            }
        }

        @Override
        public void onDisconnect() {
            if (connectionManager != null) {
                connectionManager.disconnect();
            } else {
                onConnectorStateChange(ConnectionManager.ConnectionState.DISCONNECTED);
            }
        }
    };

    public MavLinkConnectWidgetController(MavLinkConnectionWidget control) {
        this.view = control;
        broadcastDiscovery = new BroadcastDiscovery();

        view.currentViewConnectionSetting.addListener((observable, oldValue, newValue) -> {
            view.connectEnabled.setValue(newValue != null);
        });
        view.setActionHandler(actionHandler);
        view.manualConnectionInput.setText("tcp:localhost:5760");
    }

    public static MavLinkConnectionWidget.ConnectionSettings toSettings(ConnectionParameter param) {
        if (param == null) return null;

        MavLinkConnectionWidget.ConnectionSettings settings = new MavLinkConnectionWidget.ConnectionSettings();
        switch (param.getConnectionType()) {
            case 2: // tcp
                settings.proto = "tcp";
                settings.port = param.getParamsBundle().getInt("extra_tcp_server_port", 0);
                settings.host = param.getParamsBundle().getString("extra_tcp_server_ip", "");
                break;
            case 1: // udp
                settings.proto = "udp";
                settings.port = param.getParamsBundle().getInt("extra_udp_server_port", 0);
                break;
            default:
                settings.proto = "??";
        }
        return settings;
    }

    public static ConnectionParameter toParam(MavLinkConnectionWidget.ConnectionSettings cs) {
        ConnectionParameter connParam = null;
        if ("tcp".equals(cs.proto.toLowerCase())) {
            connParam = ConnectionParameter.newTcpConnection(cs.host, cs.port, null);
        } else if ("udp".equals(cs.proto.toLowerCase())) {
            connParam = ConnectionParameter.newUdpConnection(cs.port, null);
        }
        return connParam;
    }

    // on UI thread
    private void onConnectorStateChange(ConnectionManager.ConnectionState newState) {
        MavLinkConnectionWidget.ConnectionState viewState = MavLinkConnectionWidget.ConnectionState.DISCONNECTED;
        ConnectionParameter param = null;
        ConnectionManager.ConnectionError error = null;

        if (connectionManager != null) {
            param = connectionManager.connectionParamsProperty().getValue();
            error = connectionManager.errorProperty().getValue();
        }

        String status = "";

        switch (newState) {
            case CONNECTING:
                viewState = MavLinkConnectionWidget.ConnectionState.CONNECTING;
                status = "connecting...\n" + toSettings(param);
                break;
            case CONNECTED:
                viewState = MavLinkConnectionWidget.ConnectionState.CONNECTED;
                status = "connected\n" + toSettings(param);
                break;

            case ERROR: /* fall through */
                status = "error";
                if (error == null) break;
                status = "error " + error.getCode() + ": " + error.getMessage();
            case DISCONNECTED:
            default:
                // return MavLinkConnectionWidget.ConnectionState.DISCONNECTED;
        }

        view.connectLabel.setText(status);
        view.connectionState.setValue(viewState);
    }

    private void stopScan() {
        if (scanFuture != null) scanFuture.cancel(true);
        scanFuture = null;
    }

    private void statScan() {
        stopScan();
        view.scanning.setValue(true);
        view.items.clear();
        if (service == null) {
            service = Executors.newSingleThreadExecutor();
        }
        scanFuture = service.submit(this::doScan);
    }

    private void doScan() {
        try {
            // broadcast scan
            final Semaphore semaphore = new Semaphore(0);
            broadcastDiscovery.start(new DiscoveryCallback<>() {
                @Override
                public void onStopped(Exception e) {
                    semaphore.release();
                }

                @Override
                public void onStarted() {

                }

                @Override
                public void onDeviceDiscovered(BroadcastDiscovery.DiscoveredDevice device) {
                    System.err.println("Device discovered" + device);
                    MavLinkConnectionWidget.ConnectionSettings cs = new MavLinkConnectionWidget.ConnectionSettings();
                    cs.proto = device.advertisement.connectionType;
                    cs.host = device.address.getHostString();
                    cs.port = device.advertisement.connectionPort;
                    cs.autoDiscovered = true;
                    Platform.runLater(() -> {
                        view.items.add(cs);
                    });
                }
            }, 15, TimeUnit.SECONDS);


            // port range scan
            List<InetSocketAddress> addresses = PortScanDiscovery.generatePortRange(InetAddress.getLoopbackAddress(), 5760, 5770);
            try {
                // blocking
                final List<InetSocketAddress> socketAddresses = PortScanDiscovery.checkPorts(addresses, 1500);
                Platform.runLater(() -> {
                    for (InetSocketAddress inet : socketAddresses) {
                        MavLinkConnectionWidget.ConnectionSettings cs = new MavLinkConnectionWidget.ConnectionSettings();
                        cs.host = inet.getHostString();
                        cs.proto = "tcp";
                        cs.autoDiscovered = true;
                        cs.port = inet.getPort();
                        view.items.add(cs);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            semaphore.acquire();
        } catch (InterruptedException ignore) {
        } finally {
            broadcastDiscovery.stop();
            Platform.runLater(() -> {
                view.scanning.setValue(false);
            });
        }
    }

    public void setup(DroneKitConnector connector) {
        teardown();
        this.connectionManager = connector.getConnectionManager();
        connectionManager.connectionParamsProperty().addListener(connectionListener);
        connectionManager.connectionStateProperty().addListener(connectionListener);
    }

    public void teardown() {
        if (connectionManager != null) {
            connectionManager.connectionParamsProperty().removeListener(connectionListener);
            connectionManager.connectionStateProperty().removeListener(connectionListener);
            connectionManager = null;
        }
        // reset connection state
        onConnectorStateChange(ConnectionManager.ConnectionState.DISCONNECTED);
    }
}
