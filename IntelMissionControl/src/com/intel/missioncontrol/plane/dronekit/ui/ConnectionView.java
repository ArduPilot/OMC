/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.ui;

import static com.intel.missioncontrol.plane.dronekit.ConnectionManager.ConnectionState.*;

import com.intel.missioncontrol.plane.dronekit.ConnectionManager;
import com.intel.missioncontrol.plane.dronekit.ConnectionManager.ConnectionState;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public class ConnectionView {
    public Label statusLabel;
    public Button connectButton;
    public TextField addressTextField;

    public StringProperty address = new SimpleStringProperty();

    public ObjectProperty<ConnectionState> statusProperty = new SimpleObjectProperty<>(DISCONNECTED);
    public ProgressIndicator connectionProgress;


    public void initialize() {
        BooleanBinding editable = Bindings.createBooleanBinding(
                () -> statusProperty.get().canConnect(), statusProperty);

        addressTextField.disableProperty().bind(editable.not());
        addressTextField.editableProperty().bind(editable);

        connectionProgress.visibleProperty().bind(statusProperty.isEqualTo(CONNECTING));

        connectButton.disableProperty().bind(statusProperty.isEqualTo(CONNECTING));
        connectButton.textProperty().bind(Bindings.when(statusProperty.isEqualTo(CONNECTED))
                .then("Disconnect").otherwise("Connect"));

        statusLabel.styleProperty().bind(Bindings.when(statusProperty.isEqualTo(ERROR))
                .then("-fx-text-fill: red;").otherwise(""));
    }
}
