/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit;

import android.util.Log;
import com.intel.missioncontrol.plane.dronekit.model.DroneKitConnector;
import com.intel.missioncontrol.plane.dronekit.ui.ConnectionWindow;
import com.intel.missioncontrol.ui.navbar.connection.UavConnection;
import eu.mavinci.plane.IAirplane;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;

/**
 * Temporary glue class that bridges {@link com.intel.missioncontrol.ui.navbar.connection.UavConnectionActionImpl} with DroneKitContext
 * and has its own DroneKitContext UI
 *
 */
public class DroneKitConnectorBridge {
    private static final Logger LOG = Logger.getLogger(DroneKitConnector.class.getSimpleName());
    private static DroneKitConnectorBridge bridge = null;

    private final DroneKitConnector connector;
    private ConnectionWindow connectionWindow;
    private UavConnection connection;

    private static final Object connectLock = new Object();

    public static DroneKitConnectorBridge connect(IAirplane plane, Runnable connectedCallback) {
        synchronized (connectLock) {
            if (plane == null) throw new IllegalArgumentException("plane must be set");

            if (bridge != null) {
                LOG.warning("Existing connection open, attempting to close");
                bridge.close();
                bridge = null;
            }

            try {
                bridge = new DroneKitConnectorBridge(plane, connectedCallback);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            //Platform.runLater(bridge::start);
            return bridge;
        }
    }

    public DroneKitConnector getAirplaneConnector() {
        return connector;
    }

    public UavConnection getConnection() {
        return connection;
    }

    private DroneKitConnectorBridge(IAirplane plane, Runnable connected) throws Exception {
        connector = new DroneKitConnector(plane, () -> FileUtils.getTempDirectory());
        connection = new UavConnection() {
            @Override
            public void close() throws Exception {
                Platform.runLater(bridge::close);
            }
        };
    }

    private void close() {
        DroneKitConnector airplaneConnector = getAirplaneConnector();
        if (airplaneConnector == null) return;
        ConnectionManager connectionManager = airplaneConnector.getConnectionManager();
        if (connectionManager == null) return;
        LOG.warning("Closing connection");
        try {
            connectionManager.disconnect();
        } catch (Exception e) {
            LOG.severe("error shutting down drone: " + e);
        }
        // tell someone to shut it down
    }

    private void start() {
        connectionWindow = new ConnectionWindow(connector.getConnectionManager());
    }


}
