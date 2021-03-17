/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.plane.dronekit.ConnectionManager;
import com.intel.missioncontrol.plane.dronekit.model.DroneKitConnector;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.plane.AirplaneType;
import eu.mavinci.core.plane.listeners.IAirplaneListenerMsg;
import eu.mavinci.core.plane.tcp.AAirplaneConnector;
import eu.mavinci.core.plane.tcp.CAirplaneTCPconnector;
import eu.mavinci.desktop.rs232.Rs232Params;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.plane.management.Airport;
import eu.mavinci.plane.tcp.AirplaneTCPconnector;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UavConnectionActionImpl implements UavConnectionAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(UavConnectionActionImpl.class);

    private ObjectProperty<ConnectionState> connectionState = new SimpleObjectProperty<>(ConnectionState.NOT_CONNECTED);
    private StringProperty connectionError = new SimpleStringProperty();
    private IHardwareConfigurationManager hardwareConfigurationManager =
        DependencyInjector.getInstance().getInstanceOf(IHardwareConfigurationManager.class);
    private IApplicationContext applicationContext =
        DependencyInjector.getInstance().getInstanceOf(IApplicationContext.class);

    private IAirplaneListenerMsg airplaneListenerMsg =
        (lvl, data) -> applicationContext.addToast(Toast.of(ToastType.INFO).setText(data).create());

    private DroneKitConnector connector;
    private ConnectionManager connectionManager;

    public ObjectProperty<ConnectionState> connectionStateObjectProperty() {
        return connectionState;
    }

    public StringProperty connectionErrorProperty() {
        return connectionError;
    }

    @Override
    public void disconnect() {
        if (connectionManager != null && connectionManager.isConnected()) {
            connectionManager.disconnect();
        }
    }

    @Override
    public UavConnection connectTo(UnmannedAerialVehicle unmannedAerialVehicle, IAirplane plane) throws Exception {
        plane.addListener(airplaneListenerMsg);
        connectionError.set("");
        trySetCorrectPlatformType(plane, unmannedAerialVehicle.model);

        switch (unmannedAerialVehicle.model) {
        case DRONEKIT:
        case SIRIUS_BASIC:
        case SIRIUS_PRO:
            {
                if (unmannedAerialVehicle.name.startsWith("MAVLink")) {
//                    DroneKitConnectorBridge bridge = DroneKitConnectorBridge.connect(plane,
//                            () -> {
//                                try {
//                                    Airport.getInstance().newPlaneConnectionAchieved(plane);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            });
//
//                    connector = bridge.getAirplaneConnector();
                    connectionManager = connector.getConnectionManager();

                    connectionManager.connectionStateProperty().addListener((observable, oldValue, newValue) -> {
                        switch (newValue) {
                            case CONNECTED:
                                connectionState.set(ConnectionState.CONNECTED);
                                break;
                            case ERROR:
                                connectionState.set(ConnectionState.NOT_CONNECTED);
                                break;
                            case DISCONNECTED:
                                connectionState.set(ConnectionState.NOT_CONNECTED);
                                break;
                        }
                    });

                    connectionManager.errorProperty().addListener((observable, oldValue, newValue) -> {
                        Platform.runLater(() -> {
                        if (newValue != null) {
                            connectionError.set("error code: " + newValue.getCode() + " - " + newValue.getMessage());
                        }
                        });
                    });

                    String parameter = unmannedAerialVehicle.connectionInfo.planeUdpUrl
                                        + ":"
                                        + unmannedAerialVehicle.connectionInfo.host
                                        + ":"
                                        + unmannedAerialVehicle.connectionInfo.port;
                    connectToDronekit(parameter);
                    return null;// bridge.getConnection();
                }

                AirplaneTCPconnector connector =
                    new AirplaneTCPconnector(
                        plane,
                        CAirplaneTCPconnector.DUMMY_CONNECTION_HANDLER,
                        CAirplaneTCPconnector.DEFAULT_PORT_WAITER);
                LOGGER.info(
                    "Connecting to host - {} and port - {} connection url to uav - {}",
                    unmannedAerialVehicle.connectionInfo.host,
                    unmannedAerialVehicle.connectionInfo.port,
                    unmannedAerialVehicle.connectionInfo.planeUdpUrl);
                connector.socketConnect(unmannedAerialVehicle.connectionInfo.toLegacyConnection());
                Airport.getInstance().newPlaneConnectionAchieved(plane);
                return new UavConnectionImpl(plane);
            }
        case FALCON8:
            {
                AAirplaneConnector connector = null;
                Exception error = null;
                Rs232Params connectionParams = unmannedAerialVehicle.connectionParams;
                if (connectionParams.isAscTec()) {
                    try {
                        throw new RuntimeException("not supported");
                        //Airport.getInstance().newPlaneConnectionAchieved(plane);
                    } catch (Exception e) {
                        error =
                            new Exception(
                                "Unable to connect to " + unmannedAerialVehicle.model + " model by asc tec", e);
                    }
                }

                if (connector != null) {
                    return new UavConnectionImpl(plane);
                } else {
                    throw error;
                }
            }
        case FALCON8PLUS:
            {
                AAirplaneConnector connector = null;
                Exception error = null;
                Rs232Params connectionParams = unmannedAerialVehicle.connectionParams;

                try {
                    throw new RuntimeException("not supported");
                    //Airport.getInstance().newPlaneConnectionAchieved(plane);

                } catch (Exception e) {
                    error =
                        new Exception("Unable to connect to " + unmannedAerialVehicle.model + " model by asc tec", e);
                }

                if (connector != null) {
                    return new UavConnectionImpl(plane);
                } else {
                    throw error;
                }
            }
        default:
            throw new RuntimeException("Unsupported uav model " + unmannedAerialVehicle.model);
        }
    }

    private void trySetCorrectPlatformType(IAirplane plane, AirplaneType type) {
        // sending empty flight plan causes setting default platform settings to the fpManager.onAirFlightPlan
        // -- so the view on the map also correspond to the default platform
        IHardwareConfiguration hardwareConfiguration = hardwareConfigurationManager.getHardwareConfiguration(type);
        plane.setHardwareConfiguration(hardwareConfiguration);

        Flightplan fp = new Flightplan();
        fp.getHardwareConfiguration().initializeFrom(hardwareConfiguration);

        plane.getRootHandler().recv_setFlightPlanXML(fp.toXML(), false, true);
    }

    private void delayForAnimation() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            LOGGER.error("Connection thread was interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    private static class UavConnectionImpl implements UavConnection {
        private final IAirplane plane;

        UavConnectionImpl(IAirplane plane) {
            this.plane = plane;
        }

        @Override
        public void close() throws Exception {
            plane.getAirplaneConnector().close();
        }
    }

    private void connectToDronekit(String parameter) {

        if (connectionManager.isConnected()) return;
        final ConnectionParameter param = getConnectionParam(parameter);//"tcp:localhost:5762");
        if (param == null) {
            System.out.println("connection parameter error to connect to dronekit!");
            return;
        }
        connectionManager.connect(param);
    }

    private static ConnectionParameter getConnectionParam(String address) {
        Matcher matcher = Pattern.compile("\\s*(udp|tcp)\\s*:\\s*([^:]+)\\s*:\\s*(\\d+)\\s*", Pattern.CASE_INSENSITIVE)
                .matcher(address);

        if (!matcher.matches()) return null;
        String proto = matcher.group(1).toLowerCase();
        String host = matcher.group(2);
        int port = Integer.parseInt(matcher.group(3));

        ConnectionParameter connectionParameter;

        switch (proto) {
            case "tcp":
                return ConnectionParameter.newTcpConnection(host, port, null);
            case "udp":
                return ConnectionParameter.newUdpConnection(port, null);
            default:
                return null;
        }
    }

}
