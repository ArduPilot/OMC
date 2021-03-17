package com.intel.dronekitsample.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import com.intel.dronekitsample.AppController;
import com.intel.dronekitsample.DroneTestApp;
import com.intel.dronekitsample.model.DroneModel;
import com.o3dr.android.client.interfaces.LinkListener;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionPanelController extends ViewController {
    public static final String DEFAULT_ADDRESS_STRING = "tcp:localhost:5760";

    @FXML
    Button connectButton;

    @FXML
    ComboBox<Address> connectionCombo;

    @FXML
    Label label;

    public ObservableList<Address> items = FXCollections.observableArrayList();
    public ObjectProperty<Address> currentAddress = new SimpleObjectProperty<>();

    public ObjectProperty<State> state = new SimpleObjectProperty<>(State.DISCONNECTED);

    /** holds non UI stuff */
    private final ConnectionModel model = new ConnectionModel();

    public void setModel(DroneModel drone) {
        model.droneModel = drone;
    }

    public enum State {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        ERROR
    }

    public static class Address {
        String proto;
        String address;
        int port;

        @Override
        public String toString() {
            return proto + " / " + address + " : " + port;
        }
    }

    public static Address parseAddress(String string) {
        Pattern p = Pattern.compile("(?:(udp|tcp)\\s*:)?\\s*([^:]+)\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(string);
        Address addr = null;
        if (matcher.matches()) {
            addr = new Address();
            addr.proto = matcher.group(1) == null ? "udp" : matcher.group(1).toLowerCase();
            addr.address = matcher.group(2);
            addr.port = Integer.parseInt(matcher.group(3));
        }

        return addr;
    }

    public void setAddress(String addressString, boolean setEditor) {
        Address address = parseAddress(addressString);
        currentAddress.setValue(address);
        if (setEditor) connectionCombo.getEditor().setText(addressString);
    }

    @Override
    public void doInitialize() {
        System.out.println("init "+label);
        connectionCombo.setItems(items);
        connectionCombo.getEditor().textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                setAddress(newValue, false);
            }
        });
        state.addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case CONNECTING:
                    label.setText("Connecting... "+currentAddress.get());
                    label.setTextFill(Color.GRAY);
                    connectionCombo.setDisable(true);
                    connectButton.setText("Disconnect");
                    break;

                case CONNECTED:
                    connectionCombo.setDisable(true);
                    label.setText("Connected "+currentAddress.get().proto.toUpperCase());
                    label.setTextFill(Color.DARKGREEN);
                    connectButton.setText("Disconnect");
                    break;

                case DISCONNECTED:
                    label.setTextFill(null);
                    connectionCombo.setDisable(false);
                    label.setText("Disconnected");
                    connectButton.setText("Connect");
                    break;

            }
        });
        state.set(State.DISCONNECTED);

        currentAddress.addListener((observable, oldValue, newValue) -> {
            connectButton.setDisable(newValue == null);
            label.setText(newValue == null ? "invalid addr." : "");
        });
        
        setAddress(DEFAULT_ADDRESS_STRING, true);
    }

    @FXML
    public void connect(ActionEvent actionEvent) {
        if (state.get() != State.CONNECTED) {
            Address addr = parseAddress(connectionCombo.getEditor().getText());
            if (addr != null) {
                //items.add(addr);
                model.connect(addr);
            }
        } else {
            model.disconnect();
        }
    }

    Alert showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(AppController.primaryWindow());
        alert.getDialogPane().setContentText(message);
        alert.getDialogPane().setHeaderText(null);
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> state.set(State.DISCONNECTED));
        return alert;
    }


    // Non UI stuff

    final class ConnectionModel {
        public DroneModel droneModel;

        public void connect(Address addr) {
            ConnectionParameter params;
            String proto = addr.proto.toLowerCase();
            switch (proto) {
                case "udp":
                    params = ConnectionParameter.newUdpConnection(addr.port, null);
                    break;

                case "tcp":
                    params = ConnectionParameter.newTcpConnection(addr.address, addr.port, null);
                    break;

                default:
                    return;
                    // todo: error message
                    // oops;
            }

            state.set(State.CONNECTING);
            droneModel.drone.connect(params, new LinkListener() {
                @Override
                public void onLinkStateUpdated(@NonNull LinkConnectionStatus connectionStatus) {
                    switch (connectionStatus.getStatusCode()) {
                        case LinkConnectionStatus.FAILED:
                            Bundle extras = connectionStatus.getExtras();
                            String msg = null;
                            int code = 0;
                            if (extras != null) {
                                msg = extras.getString(LinkConnectionStatus.EXTRA_ERROR_MSG);
                                code = extras.getInt(LinkConnectionStatus.EXTRA_ERROR_CODE);
                            }
                            final String message = "connection to "+ addr+ " failed:\n" + "code " + code + ": " + msg;

                            Platform.runLater(() -> {
                                showErrorAlert(message);
                            });

                            alertUser("Connection Failed:" + msg);
                            break;

                        case LinkConnectionStatus.CONNECTED:
                            alertUser("Link connected:");

                            Platform.runLater(() -> {
                                state.set(State.CONNECTED);
                            });
                            break;

                        case LinkConnectionStatus.DISCONNECTED:
                            Platform.runLater(() -> {
                                state.set(State.DISCONNECTED);
                            });

                            alertUser("linkstate updated: " + connectionStatus.getStatusCode());
                            break;
                    }
                }
            });
        }

        public void disconnect() {
            droneModel.drone.disconnect();
            Platform.runLater(() -> {
                state.set(State.DISCONNECTED);
            });
        }
    }

    private void alertUser(String s) {
        System.out.println(s);
    }
}

