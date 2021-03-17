/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.ui;

import static com.intel.missioncontrol.plane.dronekit.ConnectionManager.ConnectionState.*;

import com.intel.missioncontrol.Bootstrapper;
import com.intel.missioncontrol.plane.dronekit.ConnectionManager;
import com.intel.missioncontrol.plane.dronekit.ConnectionManager.ConnectionState;
import com.intel.missioncontrol.plane.dronekit.model.DroneKitContext;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ConnectionWindow {
    private static final Logger LOG = Logger.getLogger(ConnectionWindow.class.getSimpleName());

    private final Stage stage;
    private final ConnectionManager connectionManager;
    private ConnectionView view;

    private ObjectProperty<ConnectionParameter> connectionProperty = new SimpleObjectProperty<>();


    public ConnectionWindow(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.stage = new Stage();
        stage.setOnCloseRequest(event -> {
            LOG.warning("connection window closed");
        });

        showWindow();
    }

    public void showWindow() {
        Parent parent;
        try {
            FXMLLoader loader = new FXMLLoader(ConnectionWindow.class.getResource("ConnectionView.fxml"));
            parent = loader.load();
            view = loader.getController();
            initView();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Scene scene = new Scene(parent);

        // setup stage;
        stage.getIcons().add(new Image(
                Bootstrapper.class.getResourceAsStream("/com/intel/missioncontrol/app-icon/mission-control-icon.png")));
        stage.setResizable(false);
        stage.setTitle("Connect");
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);

        scene.getWindow().setOnCloseRequest((event) -> {
//            stage = null;
        });
        stage.show();
    }


    private static ConnectionParameter getConnectionParam(String address) {
        Matcher matcher = Pattern.compile("\\s*(tcp)\\s*:\\s*([^:]+)\\s*:\\s*(\\d+)\\s*", Pattern.CASE_INSENSITIVE)
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
//                return ConnectionParameter.
            default:
                return null;
        }
    }

    private void initView() {
        connectionProperty.addListener(
                observable -> view.addressTextField.setText(connectionProperty.get().getUniqueId()));

        // post to UI thread
        connectionManager.connectionStateProperty().addListener((event) -> {
            final ConnectionState state = connectionManager.connectionStateProperty().getValue();
            Platform.runLater(() -> view.statusProperty.set(state));
        });

        view.statusProperty.addListener(observable -> {
            ConnectionState status = view.statusProperty.get();
            switch (status) {
                case CONNECTING:
                    view.statusLabel.setText("Connecting...");
                    break;
                case CONNECTED:
                    view.statusLabel.setText("Connected");
                    break;
                case DISCONNECTED:
                    view.statusLabel.setText("");
                    break;
                case ERROR: // handled elsewhere
                    ConnectionManager.ConnectionError err = connectionManager.errorProperty().getValue();
                    if (err != null) {
                        view.statusLabel.setText("error " + err.getCode() + ": " + err.getMessage());
                    }
            }
        });

        ConnectionParameter param;
        if (connectionManager.isConnected()) {
            param = connectionManager.connectionParamsProperty().getValue();
            view.statusProperty.set(CONNECTED);
        } else {
            param = ConnectionParameter.newTcpConnection("localhost", 5760, null);
        }
        connectionProperty.set(param);

        view.connectButton.setOnAction((event -> {
            if (CONNECTED.equals(view.statusProperty.get())) {
                //LOG.warning("disconnect not hooked up");
                disconnect();
            } else {
                connect();
            }
        }));

        view.statusProperty.setValue(connectionManager.connectionStateProperty().getValue());
    }

    private void disconnect() {
        connectionManager.disconnect();
    }

    private void connect() {
        if (connectionManager.isConnected()) return;
        final ConnectionParameter param = getConnectionParam(view.addressTextField.getText());
        if (param == null) {
            view.statusProperty.set(ERROR);
            view.statusLabel.setText("bad format");
            return;
        }
        view.statusProperty.set(CONNECTING);
        connectionManager.connect(param);
    }

}
