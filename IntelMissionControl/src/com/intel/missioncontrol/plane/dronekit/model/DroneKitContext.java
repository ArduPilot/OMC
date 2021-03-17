/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model;

import static com.intel.missioncontrol.plane.dronekit.ConnectionManager.ConnectionState.CONNECTED;
import static com.intel.missioncontrol.plane.dronekit.ConnectionManager.ConnectionState.CONNECTING;
import static com.intel.missioncontrol.plane.dronekit.ConnectionManager.ConnectionState.DISCONNECTED;
import static com.intel.missioncontrol.plane.dronekit.ConnectionManager.ConnectionState.ERROR;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidCompat.DesktopHelper;
import com.intel.missioncontrol.plane.dronekit.ConnectionManager;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.LinkListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import java.io.File;
import java.util.concurrent.Callable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Handles connection and holds dronekit object
 */
public class DroneKitContext implements ConnectionManager {
    private final Context context;
    private final Handler handler;
    private final ControlTower tower;

    private final Drone drone;

    private final BooleanProperty connectedProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<ConnectionState> stateProperty = new SimpleObjectProperty<>(DISCONNECTED);
    private final ObjectProperty<ConnectionError> errorProperty = new SimpleObjectProperty<>(null);
    private final ObjectProperty<ConnectionParameter> connectionParameterProperty = new SimpleObjectProperty<>(null);
    private static final int RECONNECT_TIMEOUT = 10000; //millisecond
    private double reconnectTime;

    private final boolean retryEnabled = false;

    public DroneKitContext(Callable<File> fileProvider) throws Exception {
        handler = new Handler(Looper.getMainLooper());
        context = DesktopHelper.setup(Drone.class, fileProvider.call());

        connectedProperty.bind(Bindings.createBooleanBinding(
                () -> stateProperty.getValue() == CONNECTED, stateProperty));

        tower = new ControlTower(context);
        drone = new Drone(context);
        tower.registerDrone(drone, handler);
        drone.registerDroneListener(new DroneListener() {
            @Override
            public void onDroneEvent(String event, Bundle extras) {
                switch (event) {
                    case AttributeEvent.STATE_CONNECTED:
                    case AttributeEvent.STATE_CONNECTING:
                    case AttributeEvent.STATE_DISCONNECTED:
                        debugDumpDroneState("onDroneEvent" + event);

                }
                handleDroneEvent(event, extras);
            }

            @Override
            public void onDroneServiceInterrupted(String errorMsg) {
                debugDumpDroneState("onDroneServiceInterrupted");
                alertUser("/// onDroneServiceInterrupted" + errorMsg);
            }
        });

        stateProperty.addListener((observable, oldValue, newValue) -> {

            debugDumpDroneState("stateProperty  "+oldValue+" -> "+newValue);
        });
    }

    private void debugDumpDroneState(String where) {

        final String D = ", ";
        String str = where + " |  Excep";
        try {
            State state = drone.getAttribute(AttributeType.STATE);
            str = where + " | drone.isConnected(): " + drone.isConnected() + D
                    + "stateProperty: " + stateProperty.get() + D
                    + "DK state: " + state;
        } catch (Exception e) {

        }
        System.out.println(">>> DroneState | "+str);
    }

    @Override
    public ReadOnlyProperty<ConnectionError> errorProperty() {
        return errorProperty;
    }

    @Override
    public ReadOnlyProperty<ConnectionState> connectionStateProperty() {
        return stateProperty;
    }

    @Override
    public ReadOnlyProperty<ConnectionParameter> connectionParamsProperty() {
        return connectionParameterProperty;
    }

    @Override
    public ReadOnlyBooleanProperty connectedProperty() {
        return  connectedProperty;
    }

    @Override
    public boolean isConnected() {
        return stateProperty.get().equals(CONNECTED);
    }

    @Override
    public void connect(ConnectionParameter param) {
        debugDumpDroneState("connect()");
        if (isConnected()) return;
        connectionParameterProperty.set(param);
        drone.connect(param, listener);
    }

    @Override
    public void disconnect() {
        debugDumpDroneState("disconnect()");
        try {
            drone.disconnect();
        } catch (Exception ignore) {}
    }

    private void reconnect() {
        drone.connect(connectionParameterProperty.get(), listener);
    }

    public Drone getDrone() {
        return drone;
    }

    private final LinkListener listener = connectionStatus -> {
        debugDumpDroneState("LinkListener");
        switch (connectionStatus.getStatusCode()) {
            case LinkConnectionStatus.CONNECTING:
                errorProperty.set(null);
                stateProperty.set(CONNECTING);
                break;

            case LinkConnectionStatus.FAILED: {
                Bundle extras = connectionStatus.getExtras();
                String msg = "";
                int code = 0;
                if (extras != null) {
                    msg = extras.getString(LinkConnectionStatus.EXTRA_ERROR_MSG);
                    code = extras.getInt(LinkConnectionStatus.EXTRA_ERROR_CODE);
                }
                alertUser("Connection Failed: code " + code + "  - "  + msg + "\n");

                errorProperty.set(new ConnectionErrorImpl(code, msg));
                stateProperty.set(ERROR);
                try {
                if (retryEnabled) {
                    if (reconnectTime == 0) {
                        // Detected disconnect so start a timer and try to reconnect
                        System.out.println("Detected link failure; initiating reconnect attempt");
                        reconnectTime = System.currentTimeMillis();
                        reconnect();
                        Thread.sleep(5000);
                    } else if (System.currentTimeMillis() - reconnectTime < RECONNECT_TIMEOUT) {
                        // Keep trying to reconnect till the time is expired
                        System.out.println("Link reconnect attempt failed; trying again");
                        reconnect();
                        Thread.sleep(5000);
                    } else {
                        // Reset for next time it fails
                        System.out.println("Link reconnect attempt failed; giving up");
                        reconnectTime = 0;
                    }
                }
                } catch (InterruptedException e){
                    System.out.println("Wait interrupted.");
                }
                break;
            }

            case LinkConnectionStatus.CONNECTED: {
                alertUser("Link connected:");
                errorProperty.set(null);
                stateProperty.set(CONNECTED);
                reconnectTime = 0;
                break;
            }

            case LinkConnectionStatus.DISCONNECTED: {
                alertUser("linkstate updated: " + connectionStatus.getStatusCode());
                stateProperty.set(DISCONNECTED);
                reconnectTime = 0;
                break;
            }
        }
    };


    private void alertUser(String errorMsg) {
        System.out.println("ERROR: "+errorMsg);
    }

    private void handleDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.STATE_DISCONNECTED:
                stateProperty.set(DISCONNECTED);
            case AttributeEvent.STATE_CONNECTED:
            case AttributeEvent.STATE_CONNECTING:
                alertUser("state change: "+event);
                break;
        }
    }

    private static class ConnectionErrorImpl implements ConnectionError {
        final int code;
        final String message;

        ConnectionErrorImpl(int code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

}
