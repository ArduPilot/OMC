/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

import com.google.inject.Inject;
import com.intel.missioncontrol.collections.LockedList;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.hardware.IConnectionProperties;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.PlaneSettings;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.UavDataKey;
import com.intel.missioncontrol.ui.navigation.ConnectionPage;
import de.saxsys.mvvmfx.Scope;
import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.core.plane.listeners.IAirplaneParamsUpdateListener;
import eu.mavinci.core.plane.listeners.IBackendBroadcastListener;
import eu.mavinci.core.plane.management.INewConnectionCallback;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.plane.management.Airport;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;

public class UavConnectionScope implements Scope, ConnectionScope {
    private final IHardwareConfigurationManager hardwareConfigurationManager;

    private final ObjectProperty<UnmannedAerialVehicle> selectedUav;

    private final BooleanProperty uavPinLabelVisible;

    private final BooleanProperty uavPinsListVisible;
    private final BooleanProperty uavPinsListDisable;
    private final StringProperty uavPinValue;
    private final LruPinsHistory lruPinsHistory;
    private final SetProperty<String> uavPinsHistoricalValues;

    private final BooleanProperty connectButtonDisable;

    private final BooleanProperty usbConnectorInfoVisible;
    private final BooleanProperty usbConnectorInfoManaged;

    private final BooleanProperty shortUavInfoManaged;
    private final BooleanProperty shortUavInfoVisible;
    private final BooleanProperty detectedUavListDisable;

    private final BooleanProperty disconnectButtonDisable;
    private final BooleanProperty userDisconnectCheckVisible;
    private final BooleanProperty userDisconnectCheckMarked;
    private final BooleanProperty toDisconnectUAVNow;

    private final BooleanProperty uavConnectorInfoExpanded;
    private final ObjectProperty<ConnectionState> connectionState;
    private final ObjectProperty<ConnectionPage> connectedPage;
    private final MapProperty<UavDataKey, String> uavData;
    private final IBackendBroadcastListener backendBroadcastListener;
    private final INewConnectionCallback connectionCallback;
    private IAirplaneParamsUpdateListener paramsUpdateListener;
    private final FloatProperty updateParametersProgress;
    private final PlaneSettings planeSettings;

    private final StringProperty connectionError;
    private final QuantityFormat quantityFormat = new QuantityFormat();

    @Inject
    public UavConnectionScope(
            IHardwareConfigurationManager hardwareConfigurationManager, ISettingsManager settingsManager) {
        this.hardwareConfigurationManager = hardwareConfigurationManager;

        planeSettings = settingsManager.getSection(PlaneSettings.class);
        lruPinsHistory = new LruPinsHistory();

        try (LockedList<String> pinsHistory = planeSettings.pinsHistoryProperty().lock()) {
            pinsHistory.forEach(lruPinsHistory::add);
        }

        detectedUavListDisable = new SimpleBooleanProperty(false);

        uavPinLabelVisible = new SimpleBooleanProperty(false);

        uavPinsListVisible = new SimpleBooleanProperty(false);
        uavPinsListDisable = new SimpleBooleanProperty(false);

        uavPinValue = new SimpleStringProperty(null);
        uavPinsHistoricalValues = new SimpleSetProperty<>(FXCollections.observableSet(lruPinsHistory.keySet()));

        usbConnectorInfoVisible = new SimpleBooleanProperty(false);

        connectButtonDisable = new SimpleBooleanProperty(true);

        usbConnectorInfoManaged = new SimpleBooleanProperty(true);

        shortUavInfoManaged = new SimpleBooleanProperty(false);
        shortUavInfoVisible = new SimpleBooleanProperty(false);

        selectedUav = new SimpleObjectProperty<>(null);
        selectedUav.addListener(selectedUavListener());

        uavConnectorInfoExpanded = new SimpleBooleanProperty(true);
        connectionState = new SimpleObjectProperty<>(ConnectionState.NOT_CONNECTED);
        connectedPage = new SimpleObjectProperty<>();
        uavData = new SimpleMapProperty<>(FXCollections.observableMap(new EnumMap<>(UavDataKey.class)));

        userDisconnectCheckVisible = new SimpleBooleanProperty(false);
        userDisconnectCheckMarked = new SimpleBooleanProperty(false);

        disconnectButtonDisable = new SimpleBooleanProperty();
        disconnectButtonDisable.bind(userDisconnectCheckVisible.and(userDisconnectCheckMarked.not()));

        updateParametersProgress = new SimpleFloatProperty(0.02f);

        toDisconnectUAVNow = new SimpleBooleanProperty(false);

        connectionError = new SimpleStringProperty();

        backendBroadcastListener =
            new IBackendBroadcastListener() {
                @Override
                public void backendListChanged() {
                    // do nothing
                }

                @Override
                public void recv_backend(Backend host, MVector<Port> ports) {
                    UnmannedAerialVehicle vehicle = selectedUav.get();
                    if (vehicle != null) {
                        final String selectedDevice = vehicle.connectionInfo.planeUdpUrl;
                        if (ports.stream().map(port -> port.device).anyMatch(selectedDevice::equals)) {
                            UavInFlightInfo uavInFlightInfo = vehicle.getUavInFlightInfo();
                            uavInFlightInfo.setAltitude(host.alt / 100.0d);
                            uavInFlightInfo.setLatitude(host.lat);
                            uavInFlightInfo.setLongitude(host.lon);
                            Double voltage = host.batteryVoltage;
                            uavInFlightInfo.setBatteryVoltage(voltage);
                            uavInFlightInfo.setBatteryPercent(voltage.intValue());
                            updateInFlightInfoFor(vehicle);
                        }
                    }
                }
            };
        Airport.getInstance().addBackendBroadcastListener(backendBroadcastListener);
        connectionCallback =
            new INewConnectionCallback() {
                @Override
                public void newTcpConnectionArchieved(ICAirplane plane) {
                    paramsUpdateListener =
                        pStatus -> {
                            UnmannedAerialVehicle vehicle = selectedUav.get();
                            if (vehicle != null) {
                                UavInFlightInfo uavInFlightInfo = vehicle.getUavInFlightInfo();
                                uavInFlightInfo.setParametersUpdateProgress(pStatus.getTotalPercent());

                                updateInFlightInfoFor(vehicle);
                            }
                        };
                    plane.addListener(paramsUpdateListener);
                }
            };
        Airport.getInstance().addNewConnectionListener(connectionCallback);
    }

    public BooleanProperty uavPinLabelVisibleProperty() {
        return uavPinLabelVisible;
    }

    public BooleanProperty uavPinsListVisibleProperty() {
        return uavPinsListVisible;
    }

    public BooleanProperty connectButtonDisableProperty() {
        return connectButtonDisable;
    }

    public ObjectProperty<UnmannedAerialVehicle> selectedUavProperty() {
        return selectedUav;
    }

    public BooleanProperty uavPinsListDisableProperty() {
        return uavPinsListDisable;
    }

    public BooleanProperty disconnectButtonDisableProperty() {
        return disconnectButtonDisable;
    }

    public BooleanProperty userDisconnectCheckVisibleProperty() {
        return userDisconnectCheckVisible;
    }

    public BooleanProperty userDisconnectCheckMarkedProperty() {
        return userDisconnectCheckMarked;
    }

    private ChangeListener<? super UnmannedAerialVehicle> selectedUavListener() {
        return (observable, oldValue, newValue) -> {
            if (newValue == null) {
                detectedUavListDisableProperty().set(false);
                uavPinLabelVisibleProperty().set(false);
                uavPinsListVisibleProperty().set(false);
                uavPinsListDisableProperty().set(false);
                uavPinValueProperty().set(null);
                usbConnectorInfoVisibleProperty().set(false);
                usbConnectorInfoManagedProperty().set(true);
                shortUavInfoManagedProperty().set(false);
                shortUavInfoVisibleProperty().set(false);
                connectButtonDisableProperty().set(true);
            } else {
                IPlatformDescription platform =
                    hardwareConfigurationManager.getHardwareConfiguration(newValue.model).getPlatformDescription();
                IConnectionProperties connectionProperties = platform.getConnectionProperties();
                uavPinsListDisableProperty().set(!connectionProperties.isPinRequired());

                switch (newValue.model) {
                case FALCON8:
                    uavDataProperty().put(UavDataKey.SERIAL_NUMBER, newValue.info.serialNumber);
                    uavDataProperty().put(UavDataKey.HARDWARE_REVISION, String.valueOf(newValue.info.hardwareRevision));
                    uavDataProperty().put(UavDataKey.HARDWARE_TYPE, newValue.info.hardwareType);
                    uavDataProperty().put(UavDataKey.SOFTWARE_REVISION, newValue.info.softwareRevision);
                    uavDataProperty().put(UavDataKey.PROTOCOL_VERSION, String.valueOf(newValue.info.protocolVersion));

                    updateInFlightInfoFor(newValue);

                    uavPinLabelVisibleProperty().set(true);

                    uavPinsListVisibleProperty().set(true);

                    uavPinValueProperty().set(null);

                    shortUavInfoManagedProperty().set(false);
                    shortUavInfoVisibleProperty().set(false);

                    usbConnectorInfoManagedProperty().set(true);
                    usbConnectorInfoVisibleProperty().set(true);
                    usbConnectorInfoExpandedProperty().set(true);

                    connectButtonDisableProperty().set(false);

                    break;

                case FALCON8PLUS:
                    uavDataProperty().put(UavDataKey.SERIAL_NUMBER, newValue.info.serialNumber);
                    uavDataProperty().put(UavDataKey.HARDWARE_REVISION, String.valueOf(newValue.info.hardwareRevision));
                    uavDataProperty().put(UavDataKey.HARDWARE_TYPE, newValue.info.hardwareType);
                    uavDataProperty().put(UavDataKey.SOFTWARE_REVISION, newValue.info.softwareRevision);
                    uavDataProperty().put(UavDataKey.PROTOCOL_VERSION, String.valueOf(newValue.info.protocolVersion));

                    updateInFlightInfoFor(newValue);

                    uavPinLabelVisibleProperty().set(true);

                    uavPinsListVisibleProperty().set(true);
                    uavPinValueProperty().set(null);

                    shortUavInfoManagedProperty().set(false);
                    shortUavInfoVisibleProperty().set(false);

                    usbConnectorInfoManagedProperty().set(true);
                    usbConnectorInfoVisibleProperty().set(true);
                    usbConnectorInfoExpandedProperty().set(true);

                    connectButtonDisableProperty().set(false);

                    break;

                case SIRIUS_BASIC:
                case SIRIUS_PRO:
                    updateInFlightInfoFor(newValue);

                    uavPinLabelVisibleProperty().set(true);

                    uavPinsListVisibleProperty().set(true);

                    shortUavInfoManagedProperty().set(true);
                    shortUavInfoVisibleProperty().set(false);

                    usbConnectorInfoVisibleProperty().set(false);

                    connectButtonDisableProperty().set(false);

                    break;

                default:
                    break;
                }
            }
        };
    }

    private void updateInFlightInfoFor(UnmannedAerialVehicle vehicle) {
        Dispatcher.postToUI(
            () -> {
                uavDataProperty().put(UavDataKey.SERIAL_NUMBER, vehicle.info.serialNumber);
                uavDataProperty().put(UavDataKey.HARDWARE_REVISION, Integer.toString(vehicle.info.hardwareRevision));
                uavDataProperty().put(UavDataKey.HARDWARE_TYPE, vehicle.info.hardwareType);
                uavDataProperty().put(UavDataKey.SOFTWARE_REVISION, vehicle.info.softwareRevision);
                uavDataProperty().put(UavDataKey.PROTOCOL_VERSION, Integer.toString(vehicle.info.protocolVersion));
                updateParametersProgress.setValue(vehicle.uavInFlightInfo.getParametersUpdateProgress());

                if (vehicle.uavInFlightInfo.getLongitude() == 0 || vehicle.uavInFlightInfo.getLatitude() == 0) {
                    uavDataProperty().put(UavDataKey.LONGITUDE, "");
                    uavDataProperty().put(UavDataKey.LATITUDE, "");
                    uavDataProperty().put(UavDataKey.ALTITUDE, "");
                    uavDataProperty().put(UavDataKey.NUMBER_OF_SATELLITES, "");
                    uavDataProperty().put(UavDataKey.CONNECTOR_BATTERY, "");
                } else {
                    uavDataProperty()
                        .put(
                            UavDataKey.LONGITUDE,
                            quantityFormat.format(Quantity.of(vehicle.uavInFlightInfo.getLongitude(), Unit.DEGREE)));
                    uavDataProperty()
                        .put(
                            UavDataKey.LATITUDE,
                            quantityFormat.format(Quantity.of(vehicle.uavInFlightInfo.getLatitude(), Unit.DEGREE)));
                    uavDataProperty()
                        .put(
                            UavDataKey.ALTITUDE,
                            quantityFormat.format(Quantity.of(vehicle.uavInFlightInfo.getAltitude(), Unit.DEGREE)));
                    uavDataProperty()
                        .put(
                            UavDataKey.NUMBER_OF_SATELLITES,
                            String.valueOf(vehicle.uavInFlightInfo.getNumberOfSatellites()));
                    uavDataProperty()
                        .put(
                            UavDataKey.CONNECTOR_BATTERY,
                            String.format(
                                "%.2fV/%d%%",
                                vehicle.uavInFlightInfo.getBatteryVoltage(),
                                vehicle.uavInFlightInfo.getBatteryPercent()));
                }
            });
    }

    public BooleanProperty usbConnectorInfoManagedProperty() {
        return usbConnectorInfoManaged;
    }

    public BooleanProperty usbConnectorInfoVisibleProperty() {
        return usbConnectorInfoVisible;
    }

    public BooleanProperty shortUavInfoVisibleProperty() {
        return shortUavInfoVisible;
    }

    public BooleanProperty shortUavInfoManagedProperty() {
        return shortUavInfoManaged;
    }

    public BooleanProperty detectedUavListDisableProperty() {
        return detectedUavListDisable;
    }

    public BooleanProperty usbConnectorInfoExpandedProperty() {
        return uavConnectorInfoExpanded;
    }

    public ObjectProperty<ConnectionState> connectionStateProperty() {
        return connectionState;
    }

    public ObjectProperty<ConnectionPage> connectedPageProperty() {
        return connectedPage;
    }

    public MapProperty<UavDataKey, String> uavDataProperty() {
        return uavData;
    }

    public LruPinsHistory getLruPinsHistory() {
        return lruPinsHistory;
    }

    public StringProperty uavPinValueProperty() {
        return uavPinValue;
    }

    public SetProperty<String> uavPinsHistoricalValuesProperty() {
        return uavPinsHistoricalValues;
    }

    public ObservableValue<? extends Number> getUpdateParametersProgress() {
        return updateParametersProgress;
    }

    public static class LruPinsHistory extends LinkedHashMap<String, Object> {
        private static final int MAX_HISTORY_SIZE = 3;
        private static final Object DUMMY = new Object();

        LruPinsHistory() {
            super(16, 0.75f, false);
        }

        public void add(String pin) {
            put(pin, DUMMY);
            if (pin != null) {
                // TODO store ping
            }
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
            return size() > MAX_HISTORY_SIZE;
        }
    }

    @Override
    public void setDetectedUavListDisabled(boolean disabled) {
        detectedUavListDisableProperty().set(disabled);
    }

    @Override
    public void setSelectedUav(UnmannedAerialVehicle uav) {
        selectedUavProperty().set(uav);
    }

    @Override
    public void setUavPinLabelVisible(boolean visible) {
        uavPinLabelVisibleProperty().set(visible);
    }

    @Override
    public void setUavPinsListVisible(boolean visible) {
        uavPinsListVisibleProperty().set(visible);
    }

    @Override
    public void setUavPinsListDisabled(boolean disabled) {
        uavPinsListDisableProperty().set(disabled);
    }

    @Override
    public void setUavPinValue(String uavPin) {
        uavPinValueProperty().set(uavPin);
    }

    @Override
    public void setUsbConnectorInfoManaged(boolean managed) {
        usbConnectorInfoManagedProperty().set(managed);
    }

    @Override
    public void setUsbConnectorInfoVisible(boolean visible) {
        usbConnectorInfoVisibleProperty().set(visible);
    }

    @Override
    public void setShortUavInfoManaged(boolean managed) {
        shortUavInfoManagedProperty().set(managed);
    }

    @Override
    public void setShortUavInfoVisible(boolean visible) {
        shortUavInfoVisibleProperty().set(visible);
    }

    @Override
    public void setConnectionState(ConnectionState connectionState) {
        connectionStateProperty().set(connectionState);
    }

    @Override
    public void setConnectButtonDisabled(boolean disabled) {
        connectButtonDisableProperty().set(disabled);
    }

    @Override
    public void setUserDisconnectCheckVisible(boolean visible) {
        userDisconnectCheckVisibleProperty().set(visible);
    }

    @Override
    public void setUserDisconnectCheckMarked(boolean marked) {
        userDisconnectCheckMarkedProperty().set(marked);
    }

    public PlaneSettings getPlaneSettings() {
        return planeSettings;
    }

    @Override
    public void setDisconnectUAVNow(boolean disconnectUAVNow) {
        toDisconnectUAVNow.set(disconnectUAVNow);
    }

    public BooleanProperty getDisconnectUAVNowProperty() { return toDisconnectUAVNow; }

    public StringProperty getConnectionError() { return connectionError; }
}
