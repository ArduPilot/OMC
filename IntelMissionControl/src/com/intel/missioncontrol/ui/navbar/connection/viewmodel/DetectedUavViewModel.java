/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.backend.UavConnectionListener;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.UavConnection;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionAction;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.UavSource;
import com.intel.missioncontrol.ui.navbar.connection.UnmannedAerialVehicle;
import com.intel.missioncontrol.ui.navigation.ConnectionPage;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.desktop.rs232.Rs232Params;
import eu.mavinci.plane.IAirplane;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetectedUavViewModel extends ViewModelBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DetectedUavViewModel.class);

    private final ListProperty<UnmannedAerialVehicle> availableUavs;
    private final BooleanProperty refreshInProgress;
    private final BooleanProperty connectionInProgress;

    private final UavSource uavSource;
    private final UavConnectionAction connectionAction;
    private volatile UavConnection uavConnection;
    private volatile UavConnectionListener uavConnectionListener;

    private final IApplicationContext applicationContext;

    // for unit tests
    @InjectScope
    protected UavConnectionScope scope;

    @InjectScope
    protected MainScope mainScope;

    @Inject
    public DetectedUavViewModel(
            IApplicationContext applicationContext, UavSource uavSource, UavConnectionAction connectionAction) {
        this.applicationContext = applicationContext;
        this.uavSource = uavSource;
        this.connectionAction = connectionAction;
        availableUavs = new SimpleListProperty<>(initialValue());

        refreshInProgress = new SimpleBooleanProperty();
        connectionInProgress = new SimpleBooleanProperty();
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        uavConnectionListener = new UavConnectionListener(scope);
    }

    private ObservableList<UnmannedAerialVehicle> initialValue() {
        return FXCollections.observableList(new CopyOnWriteArrayList<>());
    }

    public BooleanProperty uavPinLabelVisibleProperty() {
        return scope.uavPinLabelVisibleProperty();
    }

    public BooleanProperty uavPinsListVisibleProperty() {
        return scope.uavPinsListVisibleProperty();
    }

    public ObjectProperty<UnmannedAerialVehicle> selectedUavProperty() {
        return scope.selectedUavProperty();
    }

    public BooleanProperty uavPinsListDisableProperty() {
        return scope.uavPinsListDisableProperty();
    }

    public BooleanProperty selectedUavDisableProperty() {
        return scope.detectedUavListDisableProperty();
    }

    public ListProperty<UnmannedAerialVehicle> availableUavsProperty() {
        return availableUavs;
    }

    public BooleanProperty connectButtonDisableProperty() {
        return scope.connectButtonDisableProperty();
    }

    public BooleanProperty disconnectButtonDisableProperty() {
        return scope.disconnectButtonDisableProperty();
    }

    public BooleanProperty userDisconnectCheckVisibleProperty() {
        return scope.userDisconnectCheckVisibleProperty();
    }

    public BooleanProperty userDisconnectCheckMarkedProperty() {
        return scope.userDisconnectCheckMarkedProperty();
    }

    public StringProperty uavPinValueProperty() {
        return scope.uavPinValueProperty();
    }

    public SetProperty<String> uavPinsHistoricalValuesProperty() {
        return scope.uavPinsHistoricalValuesProperty();
    }

    public ObjectProperty<ConnectionState> connectionStateProperty() {
        return scope.connectionStateProperty();
    }

    public BooleanProperty refreshInProgressProperty() {
        return refreshInProgress;
    }

    public BooleanProperty connectionInProgressProperty() {
        return connectionInProgress;
    }

    public DelegateCommand getRefreshCommand(boolean asynchronous) {
        DelegateCommand refreshCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            availableUavs.clear();
                            availableUavs.addAll(uavSource.listUavs());
                        }
                    },
                asynchronous);
        refreshCommand.setOnScheduled(this::onRefreshScheduled);
        refreshCommand.setOnSucceeded(this::onRefreshSucceeded);
        refreshCommand.setOnFailed(this::onRefreshFailed);
        return refreshCommand;
    }

    public DelegateCommand getConnectCommand(boolean asynchronous) {
        DelegateCommand connectCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            UnmannedAerialVehicle uav = scope.selectedUavProperty().get();
                            String pin = scope.uavPinValueProperty().get();
                            if (pin != null) {
                                Rs232Params connectionParams = uav.connectionParams;
                                if (connectionParams != null) {
                                    connectionParams.setPlanePin(Short.parseShort(pin));
                                }

                                scope.getLruPinsHistory().add(pin);
                                scope.uavPinsHistoricalValuesProperty()
                                    .set(FXCollections.observableSet(scope.getLruPinsHistory().keySet()));
                                scope.getPlaneSettings()
                                    .setPinsHistory(
                                        FXAsyncCollections.observableList(
                                            new ArrayList<>(scope.getLruPinsHistory().keySet())));
                            }

                            IAirplane plane = applicationContext.getCurrentMission().getLegacyPlane();
                            uavConnectionListener.setPlane(plane);
                            plane.addListener(uavConnectionListener);
                            uavConnection = connectionAction.connectTo(uav, plane);
                        }
                    },
                asynchronous);
        connectCommand.setOnScheduled(this::onConnectionProcedureScheduled);
        connectCommand.setOnSucceeded(this::onConnectionProcedureSucceeded);
        connectCommand.setOnFailed(this::onConnectionProcedureHasFailed);
        return connectCommand;
    }

    public void disconnectButtonOnMouseClick(MouseEvent event) {
        if (uavConnection != null) {
            try {
                uavConnection.close();
            } catch (Exception e) {
                LOGGER.error("Can't close connection to uav", e);
            }
        }

        uavConnectionListener.setPlane(null);

        scope.selectedUavProperty().set(null);

        scope.detectedUavListDisableProperty().set(false);

        scope.uavPinLabelVisibleProperty().set(false);
        scope.uavPinsListVisibleProperty().set(false);

        scope.usbConnectorInfoManagedProperty().set(true);
        scope.usbConnectorInfoVisibleProperty().set(false);

        userDisconnectCheckVisibleProperty().set(false);
        userDisconnectCheckMarkedProperty().set(false);

        scope.connectedPageProperty().set(null);
        scope.connectionStateProperty().set(ConnectionState.NOT_CONNECTED);
    }

    private void onRefreshScheduled(WorkerStateEvent e) {
        selectedUavDisableProperty().set(true);
        selectedUavProperty().set(null);
        scope.usbConnectorInfoManagedProperty().set(false);
        refreshInProgressProperty().set(true);
    }

    private void onRefreshSucceeded(WorkerStateEvent e) {
        selectedUavDisableProperty().set(false);
        refreshInProgressProperty().set(false);
    }

    private void onRefreshFailed(WorkerStateEvent e) {
        selectedUavDisableProperty().set(false);
        refreshInProgressProperty().set(false);
    }

    private void onConnectionProcedureScheduled(WorkerStateEvent e) {
        LOGGER.info("Start Connection procedure");
        scope.detectedUavListDisableProperty().set(true);

        scope.uavPinLabelVisibleProperty().set(true);

        scope.uavPinsListDisableProperty().set(true);
        scope.uavPinsListVisibleProperty().set(true);

        scope.connectionStateProperty().set(ConnectionState.CONNECTING);

        connectionInProgressProperty().set(true);
    }

    private void onConnectionProcedureSucceeded(WorkerStateEvent e) {
        LOGGER.info("Connection procedure succeeded");
        UnmannedAerialVehicle uav = scope.selectedUavProperty().get();
        if (uav != null) {
            switch (uav.model) {
            case SIRIUS_BASIC:
                break;
            case SIRIUS_PRO:
                break;
            case FALCON8:
                scope.usbConnectorInfoManagedProperty().set(true);
                scope.usbConnectorInfoVisibleProperty().set(true);
                scope.usbConnectorInfoExpandedProperty().set(false);

                scope.shortUavInfoManagedProperty().set(true);
                scope.shortUavInfoVisibleProperty().set(true);

                userDisconnectCheckVisibleProperty().set(true);
                userDisconnectCheckMarkedProperty().set(false);
                break;

            case FALCON8PLUS:
                scope.usbConnectorInfoManagedProperty().set(true);
                scope.usbConnectorInfoVisibleProperty().set(true);
                scope.usbConnectorInfoExpandedProperty().set(false);

                scope.shortUavInfoManagedProperty().set(true);
                scope.shortUavInfoVisibleProperty().set(true);

                userDisconnectCheckVisibleProperty().set(true);
                userDisconnectCheckMarkedProperty().set(false);

                scope.getLruPinsHistory().add(uavPinValueProperty().get());
                scope.uavPinsHistoricalValuesProperty()
                    .set(FXCollections.observableSet(scope.getLruPinsHistory().keySet()));
                break;

            default:
                LOGGER.error("Unknown UAV: {}", uav.model);
                break;
            }
        }

        scope.uavPinLabelVisibleProperty().set(false);

        scope.uavPinsListVisibleProperty().set(false);

        scope.connectedPageProperty().set(ConnectionPage.UAV_CONNECT);
        scope.connectionStateProperty().set(ConnectionState.CONNECTED);

        connectionInProgressProperty().set(false);
    }

    private void onConnectionProcedureHasFailed(WorkerStateEvent e) {
        LOGGER.error("Connection procedure has failed", e.getSource().getException());
        uavConnectionListener.setPlane(null);
        selectedUavDisableProperty().set(false);
        selectedUavProperty().set(null);

        uavPinLabelVisibleProperty().set(false);

        uavPinsListVisibleProperty().set(false);
        uavPinsListDisableProperty().set(false);
        uavPinValueProperty().set(null);

        scope.usbConnectorInfoManagedProperty().set(true);
        scope.usbConnectorInfoVisibleProperty().set(false);

        scope.shortUavInfoManagedProperty().set(false);
        scope.shortUavInfoVisibleProperty().set(false);

        connectionStateProperty().set(ConnectionState.NOT_CONNECTED);

        connectButtonDisableProperty().set(true);

        userDisconnectCheckVisibleProperty().set(false);
        userDisconnectCheckMarkedProperty().set(false);
    }
}
