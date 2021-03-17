/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.connection;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.ConnectionSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionInfo;
import com.intel.missioncontrol.ui.navbar.connection.UavInFlightInfo;
import com.intel.missioncontrol.ui.navbar.connection.UavInfo;
import com.intel.missioncontrol.ui.navbar.connection.UnmannedAerialVehicle;
import com.intel.missioncontrol.ui.navbar.connection.model.ConnectionDeviceType;
import com.intel.missioncontrol.ui.navbar.connection.model.ConnectionItem;
import com.intel.missioncontrol.ui.navbar.connection.model.ConnectionTransportType;
import eu.mavinci.core.plane.AirplaneType;
import eu.mavinci.desktop.rs232.Rs232Params;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

public class ConnectionDialogViewModel extends DialogViewModel {

    private final StringProperty connectionName = new SimpleStringProperty();
    private final ObjectProperty<ConnectionDeviceType> connectionDeviceTypeSelected = new SimpleObjectProperty<>(ConnectionDeviceType.COPTER);
    private final SimpleListProperty<ConnectionDeviceType> connectionDeviceTypesList =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<ConnectionTransportType> transportTypeSelected = new SimpleObjectProperty<>(ConnectionTransportType.UDP);
    private final StringProperty host = new SimpleStringProperty("127.0.0.1");
    private final ObjectProperty<Integer> port = new SimpleObjectProperty<>(911);
    private final BooleanProperty connectNow = new SimpleBooleanProperty(false);

    private final ILanguageHelper languageHelper;
    private final ConnectionSettings connectionSettings;

    @Inject
    public ConnectionDialogViewModel(ILanguageHelper languageHelper, ISettingsManager settingsManager) {
        this.languageHelper = languageHelper;
        this.connectionSettings = settingsManager.getSection(ConnectionSettings.class);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        connectionDeviceTypesList.addAll(ConnectionDeviceType.COPTER, ConnectionDeviceType.PLANE, ConnectionDeviceType.SIMULATOR);
    }

    public StringProperty connectionNameProperty() {
        return connectionName;
    }

    public SimpleListProperty<ConnectionDeviceType> connectionDeviceTypesListProperty() {
        return connectionDeviceTypesList;
    }

    public ObjectProperty<ConnectionDeviceType> connectionDeviceTypeObjectProperty() {
        return connectionDeviceTypeSelected;
    }

    public ObjectProperty<ConnectionTransportType> transportTypeObjectProperty() {
        return transportTypeSelected;
    }

    public StringProperty hostProperty() {
        return host;
    }

    public ObjectProperty<Integer> portProperty() {
        return port;
    }

    public BooleanProperty connectNowProperty() {
        return connectNow;
    }

    public void onAdd() {

        UnmannedAerialVehicle unmannedAerialVehicle = new UnmannedAerialVehicle(
                AirplaneType.SIRIUS_BASIC,
                getConnectionName(),
                new UavInFlightInfo(18.6417654, 394.987582, 100.234, 10, 219.85, 99),
                new UavInfo("serial-2", 6, "hwtype-2", "3.6.7", 3),
                new UavConnectionInfo(host.get(), port.get(), transportTypeSelected.get()==ConnectionTransportType.TCP?"tcp":"udp"),
                Rs232Params.conAscTec);

        ConnectionItem connectionItem = new ConnectionItem();
        connectionItem.nameProperty().set(getConnectionName());
        connectionItem.connectionDeviceTypeProperty().set(connectionDeviceTypeSelected.get());
        connectionItem.connectionTransportTypeProperty().set(transportTypeSelected.get());
        connectionItem.hostProperty().set(host.get());
        connectionItem.portProperty().set(port.get());
        connectionItem.unmannedAerialVehicleProperty().set(unmannedAerialVehicle);
        //if connect Now is checked, then run to connect to device
        if (connectNow.get()) {
            //TODO:send command to start connection
            clearAllConnectionItemSelections();
            connectionItem.selectedProperty().set(true);
        }
        else {
            connectionItem.selectedProperty().set(false);
        }

        connectionSettings.connectionItemsListProperty().add(connectionItem);
        getCloseCommand().execute();
    }

    private String getConnectionName() {
        return (connectionDeviceTypeSelected.get() == ConnectionDeviceType.SIMULATOR)?connectionName.get(): ("MAVLink" + connectionName.get());
    }

    private void clearAllConnectionItemSelections() {
        for (ConnectionItem connectionItem : connectionSettings.connectionItemsListProperty().get()) {
            connectionItem.selectedProperty().set(false);
        }
    }


}
