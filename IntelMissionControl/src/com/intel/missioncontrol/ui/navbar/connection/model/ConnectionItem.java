/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.model;

import com.intel.missioncontrol.settings.Serializable;
import com.intel.missioncontrol.ui.navbar.connection.UnmannedAerialVehicle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Serializable
public class ConnectionItem {
    private StringProperty name = new SimpleStringProperty("");
    private ObjectProperty<ConnectionDeviceType> connectionDeviceType =
        new SimpleObjectProperty<>(ConnectionDeviceType.COPTER);
    private ObjectProperty<ConnectionTransportType> connectionTransportType = new SimpleObjectProperty<>();
    private StringProperty host = new SimpleStringProperty("127.0.0.1");
    private IntegerProperty port = new SimpleIntegerProperty(0);
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private ObjectProperty<UnmannedAerialVehicle> unmannedAerialVehicle = new SimpleObjectProperty<>();

    public ConnectionItem() {}

    public StringProperty nameProperty() {
        return name;
    }

    public ObjectProperty<ConnectionDeviceType> connectionDeviceTypeProperty() {
        return connectionDeviceType;
    }

    public ObjectProperty<ConnectionTransportType> connectionTransportTypeProperty() {
        return connectionTransportType;
    }

    public StringProperty hostProperty() {
        return host;
    }

    public IntegerProperty portProperty() {
        return port;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public String getName() {
        return name.get();
    }

    public ConnectionDeviceType getConnectionDeviceType() {
        return connectionDeviceType.get();
    }

    public ConnectionTransportType getConnectionTransportType() {
        return connectionTransportType.get();
    }

    public String getHost() {
        return host.get();
    }

    public int getPort() {
        return port.get();
    }

    public boolean isSelected() {
        return selected.get();
    }

    public ObjectProperty<UnmannedAerialVehicle> unmannedAerialVehicleProperty() {
        return unmannedAerialVehicle;
    }
}
