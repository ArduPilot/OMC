/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import java.io.IOException;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

public class MavLinkConnectionWidget extends VBox {

    public final BooleanProperty scanning = new SimpleBooleanProperty(false);

    public final ObservableList<ConnectionSettings> items = FXCollections.observableArrayList();

    public final ObjectProperty<ConnectionSettings> selectedItem = new SimpleObjectProperty<>();
    public final ObjectProperty<ConnectionState> connectionState = new SimpleObjectProperty<>(ConnectionState.DISCONNECTED);
    public final ObjectProperty<ConnectionSettings> currentViewConnectionSetting = new SimpleObjectProperty<>(null);
    public final BooleanProperty connectEnabled = new SimpleBooleanProperty();
    private final ObjectProperty<ConnectionMode> selectedMode = new SimpleObjectProperty<>();
    private final ObjectProperty<ConnectionSettings> manualConnectionSetting = new SimpleObjectProperty<>(null);
    @FXML
    TabPane connectionTabs;
    @FXML
    ComboBox<ConnectionSettings> discoveredDevices;
    @FXML
    TextField manualConnectionInput;
    @FXML
    Label manualConnectionLabel;
    @FXML
    Button connectButton;
    @FXML
    ProgressIndicator connectProgress;
    @FXML
    Label connectLabel;

    @FXML
    private Label scanLabel;
    @FXML
    private ToggleButton scanButton;
    @FXML
    private ProgressIndicator scanProgress;

    private ActionHandler handler;

    public MavLinkConnectionWidget() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "MavLinkConnectionWidget.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        setupView();
    }

    private static ConnectionSettings getConnectionParam(String address) {
        try {
            String[] split = address.split("[:]");

            if (split.length < 2) {
                return null;
            }

            String proto = split[0].trim();
            String host = null;
            int port = 0;
            if (split.length == 2) {
                port = Integer.parseInt(split[1].trim());
            } else {
                host = split[1].trim();
                port = Integer.parseInt(split[2].trim());
            }

            ConnectionSettings settings = new ConnectionSettings();
            settings.host = host;
            settings.port = port;
            settings.proto = proto;
            return settings;
        } catch (Exception e) {
        }
        return null;
    }

    public StringProperty connectStatusTextProperty() {
        return connectLabel.textProperty();
    }

    public ReadOnlyObjectProperty<ConnectionMode> connectionModeProperty() {
        return selectedMode;
    }

    public void setActionHandler(ActionHandler handler) {
        this.handler = handler != null ? handler : new ActionHandler();
    }

    private void setupView() {
        //scanButton.disableProperty().bind(scanning);
        scanProgress.visibleProperty().bind(scanning);
        scanning.addListener((observable, oldValue, newValue) -> {
            scanButton.setSelected(newValue);
        });

        scanLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            String found = "found "+(items.size()>0 ? items.size() : "no") +" items";
            if (scanning.get()) {
                return "scanning... "+found;
            } else {
                return found;
            }
        }, scanning, items));


        scanButton.setOnAction((ignored) -> {
            if (scanning.get()) {
                handler.onStopScan();
            } else {
                handler.onStartScan();
            }
        });

        discoveredDevices.setItems(items);
        selectedItem.bind(discoveredDevices.valueProperty());

        discoveredDevices.promptTextProperty().bind(Bindings.createStringBinding(() -> {
            return "found " + items.size() + " items";
        }, items));

        manualConnectionInput.textProperty().addListener(event -> {
            CharSequence characters = manualConnectionInput.getCharacters();
            ConnectionSettings connectionParam = getConnectionParam(characters.toString());
            manualConnectionSetting.set(connectionParam);

            if (connectionParam == null) {
                manualConnectionLabel.setText("format: '(tcp|udp):[<host>]:<port>'");
                manualConnectionLabel.setTextFill(Paint.valueOf("red"));
                manualConnectionInput.setStyle("-fx-focus-color: red;");
            } else {
                manualConnectionLabel.setText("");
                manualConnectionInput.setStyle("");
                manualConnectionLabel.setTextFill(null);
            }
        });


        selectedMode.bind(Bindings.createObjectBinding(() -> {
            String tabId = connectionTabs.getSelectionModel().getSelectedItem().getId();
            return "autoTab".equals(tabId) ? ConnectionMode.AUTO : ConnectionMode.MANUAL;
        }, connectionTabs.getSelectionModel().selectedItemProperty()));

        connectButton.textProperty().bind(Bindings.createStringBinding(() -> {
            ConnectionState state = connectionState.getValue();
            return (state == ConnectionState.CONNECTED || state == ConnectionState.CONNECTING) ?
                    "Disconnect" : "Connect";
        }, connectionState));

        connectProgress.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> (connectionState.get() == ConnectionState.CONNECTING), connectionState));

        connectButton.setOnAction(event -> {
            ConnectionState state = connectionState.getValue();

            if (state == ConnectionState.CONNECTED || state == ConnectionState.CONNECTING) {
                handler.onDisconnect();
            } else {
                handler.onConnect();
            }
        });

        connectButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            boolean enabled = true;
            if (connectionState.get() == ConnectionState.DISCONNECTED) {
                enabled = connectEnabled.getValue();
            }
            return !enabled;
        }, connectEnabled, connectionState));

        currentViewConnectionSetting.bind(Bindings.createObjectBinding(() -> {
            return selectedMode.get() == ConnectionMode.MANUAL ? manualConnectionSetting.get() : selectedItem.get();
        }, selectedMode, manualConnectionSetting, selectedItem));

//        connectEnabled.setValue();
    }


    public void onUavConnectViewInit(UavConnectView uavConnectView, Object object) {
        System.out.println("onUavConnectViewInit !" + uavConnectView + " obj " + object);
    }

    public enum ConnectionMode {
        MANUAL,
        AUTO
    }

    public enum ConnectionState {
        CONNECTED,
        CONNECTING,
        DISCONNECTED
    }

    public static class ConnectionSettings {
        public String host;
        public String proto;
        public int port;
        public boolean autoDiscovered = false;

        @Override
        public String toString() {
            String h = (host != null) ? host + ":" : "";
            return "" + proto + ':'
                    + h +
                    +port;
        }
    }

    public static class ActionHandler {
        public void onConnect() {
        }

        public void onDisconnect() {
        }

        public void onStartScan() {
        }

        public void onStopScan() {

        }

    }

}