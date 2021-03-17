/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.model.IRtkStatisticFactory;
import com.intel.missioncontrol.ui.navbar.connection.scope.ExternalConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.scope.RtkConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.view.ExternalBaseStationView;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.desktop.bluetooth.BTService;
import eu.mavinci.desktop.bluetooth.BTdevice;
import eu.mavinci.desktop.bluetooth.BluetoothManager;
import eu.mavinci.desktop.bluetooth.IBluetoothListener;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkClient;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/** @author Vladimir Iordanov */
public class ExternalBluetoothSectionViewModel extends ViewModelBase {

    @InjectScope
    private RtkConnectionScope rtkConnectionScope;

    @InjectScope
    private ExternalConnectionScope externalConnectionScope;

    @Inject
    private IRtkStatisticFactory rtkStatisticFactory;

    private final ListProperty<BTdevice> devicesList = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<BTdevice> selectedDevice = new SimpleObjectProperty<>();
    private final ListProperty<BTService> servicesList = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<BTService> selectedService = new SimpleObjectProperty<>();
    private final BooleanProperty isRefreshingDevices = new SimpleBooleanProperty();

    private Command refreshDevicesCommand;
    private final IBluetoothListener bluetoothListener =
        new IBluetoothListener() {

            @Override
            public void bluetoothDiscoveryReady() {
                Dispatcher.postToUI(ExternalBluetoothSectionViewModel.this::deviceListChanged);
                isRefreshingDevices.set(false);
            }

            @Override
            public void bluetoothDiscoveryStarted() {
                isRefreshingDevices.set(true);
            }
        };

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        refreshDevicesCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            refreshDevices();
                        }
                    },
                isRefreshingDevices.not());

        selectedDevice.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    deviceChanged(newValue);
                }
            });

        Command connectCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            connect();
                        }
                    });
        Command disconnectCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            disconnect();
                        }
                    });
        externalConnectionScope.registerConnectCommand(
            ExternalBaseStationView.ExternalConnetionType.BLUETOOTH, connectCommand);
        externalConnectionScope.registerDisconnectCommand(
            ExternalBaseStationView.ExternalConnetionType.BLUETOOTH, disconnectCommand);

        BluetoothManager.instance.addListener(bluetoothListener);
        deviceListChanged();
    }

    private void refreshDevices() {
        BluetoothManager.instance.refresh();
    }

    /** Code source is {@link BluetoothConnectWidget#deviceListChanged()} */
    private void deviceListChanged() {
        devicesList.clear();
        List<BTdevice> list = BluetoothManager.instance.getDevices();
        if (!BluetoothManager.instance.isDescovering()) {
            if (list == null || list.size() == 0) {
                return;
            }

            devicesList.setAll(list);
            BTdevice btDevice = list.stream().filter(d -> !d.hasDefaultDevices).findFirst().orElse(list.get(0));

            selectedDevice.setValue(btDevice);
        }
    }

    /**
     * Code source is {@link BluetoothConnectWidget#deviceSelectionChanged()}
     *
     * @param btDevice Current device
     */
    private void deviceChanged(BTdevice btDevice) {
        servicesList.clear();
        if (!btDevice.getServices().isEmpty()) {
            servicesList.setAll(btDevice.getServices());
            selectedService.setValue(servicesList.get(0));
        }
    }

    private void connect() {
        if (selectedService.get() == null) {
            throw new IllegalStateException("Service is not selected");
        }

        IRtkClient client = rtkConnectionScope.getCurrentClient();
        client.disconnect();
        client.connect(selectedService.get());
    }

    private void disconnect() {
        IRtkClient client = rtkConnectionScope.getCurrentClient();
        client.disconnect();
    }

    public boolean getIsConnected() {
        return rtkConnectionScope.getIsConnected();
    }

    public BooleanBinding isConnectedProperty() {
        return rtkConnectionScope.isConnectedBinding();
    }

    public ObservableList<BTdevice> getDevicesList() {
        return devicesList.get();
    }

    public ListProperty<BTdevice> devicesListProperty() {
        return devicesList;
    }

    public BTdevice getSelectedDevice() {
        return selectedDevice.get();
    }

    public ObjectProperty<BTdevice> selectedDeviceProperty() {
        return selectedDevice;
    }

    public ObservableList<BTService> getServicesList() {
        return servicesList.get();
    }

    public ListProperty<BTService> servicesListProperty() {
        return servicesList;
    }

    public BTService getSelectedService() {
        return selectedService.get();
    }

    public ObjectProperty<BTService> selectedServiceProperty() {
        return selectedService;
    }

    public Command getRefreshDevicesCommand() {
        return refreshDevicesCommand;
    }
}
