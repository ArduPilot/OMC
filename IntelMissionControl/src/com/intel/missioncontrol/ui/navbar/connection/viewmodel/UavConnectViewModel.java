/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.backend.UavConnectionListener;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.ConnectionSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.connection.ConnectionDialogViewModel;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.UavConnection;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionAction;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionInfo;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.UavInFlightInfo;
import com.intel.missioncontrol.ui.navbar.connection.UavInfo;
import com.intel.missioncontrol.ui.navbar.connection.UavSource;
import com.intel.missioncontrol.ui.navbar.connection.UnmannedAerialVehicle;
import com.intel.missioncontrol.ui.navbar.connection.model.ConnectionDeviceType;
import com.intel.missioncontrol.ui.navbar.connection.model.ConnectionItem;
import com.intel.missioncontrol.ui.navbar.connection.model.ConnectionStatus;
import com.intel.missioncontrol.ui.navbar.connection.model.ConnectionTransportType;
import com.intel.missioncontrol.ui.navigation.ConnectionPage;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import eu.mavinci.core.plane.AirplaneType;
import eu.mavinci.core.plane.management.BackendState;
import eu.mavinci.core.plane.management.CAirport;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.BackendInfo;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.rs232.MSerialPort;
import eu.mavinci.desktop.rs232.Rs232Params;
import eu.mavinci.plane.IAirplane;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UavConnectViewModel extends ViewModelBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UavConnectViewModel.class);

    @InjectScope
    protected UavConnectionScope scope;

    private final ObjectProperty<ConnectionStatus> connectionStatus =
        new SimpleObjectProperty<>(ConnectionStatus.DISCONNECTED);
    private final ObjectProperty<ConnectionItem> selectedConnectionItem = new SimpleObjectProperty<>();
    private final BooleanProperty isSelectedItemConnected = new SimpleBooleanProperty(false);

    private final BooleanProperty refreshInProgress = new SimpleBooleanProperty();
    private final BooleanProperty connectionInProgress = new SimpleBooleanProperty();

    private IDialogService dialogService;
    private IApplicationContext applicationContext;
    private ConnectionSettings connectionSettings;
    private final UavSource uavSource;
    private UavConnectionAction connectionAction;
    private volatile UavConnection uavConnection;
    private volatile UavConnectionListener uavConnectionListener;

    private SimpleObjectProperty<Mission> missionProp = new SimpleObjectProperty<Mission>();

    public ReadOnlyObjectProperty<Mission> currentMissionProperty() {
        return missionProp;
    }

    @Inject
    public UavConnectViewModel(
            IDialogService dialogService,
            IApplicationContext applicationContext,
            ISettingsManager settingsManager,
            UavSource uavSource,
            UavConnectionAction connectionAction) {
        this.dialogService = dialogService;
        this.applicationContext = applicationContext;
        this.connectionSettings = settingsManager.getSection(ConnectionSettings.class);
        this.uavSource = uavSource;
        this.connectionAction = connectionAction;
    }


    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        uavConnectionListener = new UavConnectionListener(scope);

        // todo: unbind?
        missionProp.bind(applicationContext.currentMissionProperty());

        initList();
        if (connectionSettings.connectionItemsListProperty().size() > 0) {
            selectedConnectionItem.set(connectionSettings.connectionItemsListProperty().get(0));
            selectedConnectionItem.addListener(
                (observable, oldValue, newValue) -> {
                if (newValue != null && newValue.unmannedAerialVehicleProperty().get() != null) {
                    scope.selectedUavProperty().set(newValue.unmannedAerialVehicleProperty().get());
                    if (connectionAction != null) {
                        connectionAction.connectionErrorProperty().set(""); //reset connection error
                    }
                }
            });
        }

        acceptIncommingConnectionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    connectionStatus.set(newValue ? ConnectionStatus.LISTENING : ConnectionStatus.DISCONNECTED);
                });

        selectedConnectionItem.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    isSelectedItemConnected.unbind();
                    isSelectedItemConnected.bind(newValue.selectedProperty());
                }
            });

        scope.getDisconnectUAVNowProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
            if (newValue) {
                System.out.println("-----scope.getDisconnectUAVNowProperty()----heard!");
                getDisconnectCommand().execute();
                scope.getDisconnectUAVNowProperty().set(false);
            }
        });
    }

    private final ICommand addConnectionCommand =
        new DelegateCommand(
            () ->
                Futures.addCallback(
                    dialogService.requestDialog(this, ConnectionDialogViewModel.class, true),
                    new FutureCallback<>() {
                        @Override
                        public void onSuccess(ConnectionDialogViewModel connectionDialogViewModel) {}

                        @Override
                        public void onFailure(Throwable throwable) {}

                    }));

    private final ICommand disconnectCommand =
        new DelegateCommand(
            () -> {
                System.out.println("----to disconnect");
                connectionAction.disconnect();

                for (ConnectionItem ci : connectionSettings.connectionItemsListProperty().get()) {
                    if (ci.isSelected()) {
                        ci.selectedProperty().set(false);
                    }
                }

                if (connectionAction.connectionStateObjectProperty().get() != null) {
                    connectionAction.connectionStateObjectProperty().set(ConnectionState.NOT_CONNECTED);
                }
            });

    //    public BooleanProperty usbConnectorInfoManagedProperty() {
    //        return scope.usbConnectorInfoManagedProperty();
    //    }
    //
    //    public BooleanProperty usbConnectorInfoVisibleProperty() {
    //        return scope.usbConnectorInfoVisibleProperty();
    //    }
    //
    //    public BooleanProperty shortUavInfoManagedProperty() {
    //        return scope.shortUavInfoManagedProperty();
    //    }
    //
    //    public BooleanProperty shortUavInfoVisibleProperty() {
    //        return scope.shortUavInfoVisibleProperty();
    //    }
    //
    //    public BooleanProperty usbConnectorInfoExpandedProperty() {
    //        return scope.usbConnectorInfoExpandedProperty();
    //    }

    public ObjectProperty<ConnectionStatus> connectionStatusObjectProperty() {
        return connectionStatus;
    }

    public BooleanProperty acceptIncommingConnectionProperty() {
        return connectionSettings.acceptIncomingConnectionsProperty();
    }

    public IntegerProperty receivingPortProperty() {
        return connectionSettings.receivingPortProperty();
    }

    public ICommand getAddConnectionCommand() {
        return addConnectionCommand;
    }

    public de.saxsys.mvvmfx.utils.commands.DelegateCommand getConnectCommand(boolean asynchronous) {
        de.saxsys.mvvmfx.utils.commands.DelegateCommand connectCommand =
            new de.saxsys.mvvmfx.utils.commands.DelegateCommand(
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
                            scope.getConnectionError().bind(connectionAction.connectionErrorProperty());
                            connectionAction
                                .connectionStateObjectProperty()
                                .addListener(
                                    (observable, oldValue, newValue) -> {
                                        Platform.runLater(
                                            () -> {
                                                selectedConnectionItem
                                                    .get()
                                                    .selectedProperty()
                                                    .set(newValue.equals(ConnectionState.CONNECTED));

                                                connectionStatus.set(
                                                    newValue.equals(ConnectionState.CONNECTED)
                                                        ? ConnectionStatus.CONNECTED
                                                        : ConnectionStatus.DISCONNECTED);
                                                scope.connectionStateProperty()
                                                    .set(
                                                        newValue.equals(ConnectionState.CONNECTED)
                                                        ? ConnectionState.CONNECTED
                                                        : ConnectionState.NOT_CONNECTED);
                                            });
                                    });
                        }
                    },
                asynchronous);
        connectCommand.setOnScheduled(this::onConnectionProcedureScheduled);
        connectCommand.setOnSucceeded(this::onConnectionProcedureSucceeded);
        connectCommand.setOnFailed(this::onConnectionProcedureHasFailed);
        return connectCommand;
    }

    public ICommand getDisconnectCommand() {
        return disconnectCommand;
    }

    public ListProperty<ConnectionItem> connectionItemListProperty() {
        return connectionSettings.connectionItemsListProperty();
    }

    public ObjectProperty<ConnectionItem> selectedConnectionItemProperty() {
        return selectedConnectionItem;
    }

    public void removeConnectionItemFromList(ConnectionItem item) {
        connectionSettings.connectionItemsListProperty().remove(item);
    }

    private void initList() {
        if (connectionSettings.connectionItemsListProperty().size() == 0) {
            List<UnmannedAerialVehicle> uavs = new ArrayList<>();
            Vector<BackendState> backendList = CAirport.getInstance().getBackendList();
            for (BackendState backendState : backendList) {
                backendState
                        .getPorts()
                        .forEach(
                                port -> {
                                    AirplaneType model = AirplaneType.SIRIUS_PRO;
                                    Backend backend = backendState.getBackend();
                                    String name = String.format("%s %s", backend.name, port.name);
                                    UavInFlightInfo position = createUavInFlightInfo(backend);
                                    UavInfo info = createUavInfo(backend.info);
                                    UavConnectionInfo connectionInfo =
                                new UavConnectionInfo(
                                    backendState.getHost().getHostString(), backend.port, port.device);

                                    UnmannedAerialVehicle unmannedAerialVehicle =
                                            new UnmannedAerialVehicle(
                                                    model, name, position, info, connectionInfo, Rs232Params.conAscTec);
                                    uavs.add(unmannedAerialVehicle);

                                    ConnectionItem ci = new ConnectionItem();
                                    ci.selectedProperty().set(false);
                                    ci.hostProperty().set(backendState.getHost().getHostString());
                                    ci.portProperty().set(backend.port);
                                    ci.nameProperty().set(name);
                                    ci.unmannedAerialVehicleProperty().set(unmannedAerialVehicle);
                                    connectionSettings.connectionItemsListProperty().add(ci);
                                });
            }

            List<Rs232Params> rs232Params =
                    Rs232Params.listDetectedPorts(
                            Rs232Params.DEFAULT_PORTS_SOURCE,
                            Rs232Params.DEFAULT_CONFIGURATION_PROPERTIES,
                            params -> {
                                MSerialPort port = null;
                                try {
                                    port = params.openPort();
                                    boolean idlDetect = detectIDL(port);
                                    System.out.println("IDL detected status: " + idlDetect);
                                    params.setIsAscTec(idlDetect);
                                    port.closePort();
                                    return true;
                                } catch (IOException e) {
                                    // TODO if port is not valid -- tell it to the user and ask to replug to the other port
                                    return false;
                                } finally {
                                    if (port != null && port.isOpened())
                                        try {
                                            port.closePort();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                }
                            });
            for (Rs232Params params : rs232Params) {
                if (params.isAscTec()) {
                    AirplaneType model = AirplaneType.FALCON8PLUS;
                    String name = "IDL connected to " + params.getPort();
                    UavInFlightInfo position = new UavInFlightInfo(0, 0, 0, 0, 25, 88);
                    UavInfo info = new UavInfo("SERIAL_NUMBER", 0, "HARDWARE TYPE", "SOFTWARE REVISION", 4);
                    UavConnectionInfo connectionInfo = new UavConnectionInfo(params.getPort(), 1, "");
                    UnmannedAerialVehicle unmannedAerialVehicle =
                            new UnmannedAerialVehicle(model, name, position, info, connectionInfo, params);
                    uavs.add(unmannedAerialVehicle);

                    ConnectionItem ci = new ConnectionItem();
                    ci.selectedProperty().set(false);
                    ci.hostProperty().set(params.getPort());
                    ci.portProperty().set(1);
                    ci.nameProperty().set(name);
                    ci.unmannedAerialVehicleProperty().set(unmannedAerialVehicle);
                    connectionSettings.connectionItemsListProperty().add(ci);
                }
            }

            // no longer F8+ TCP connection
            // detectAndAddFalcon8plus(uavs);
            uavs.add(generateMavlinkUDPStub());
            uavs.add(generateMavlinkTCPStub());
            uavs.addAll(generateStubs());
        }
    }

    private UnmannedAerialVehicle generateMavlinkUDPStub() {
        UnmannedAerialVehicle unmannedAerialVehicle =
                new UnmannedAerialVehicle(
                        AirplaneType.SIRIUS_BASIC,
                        "MAVLink_UDP_default",
                        new UavInFlightInfo(18.6417654, 394.987582, 100.234, 10, 219.85, 99),
                        new UavInfo("serial-2", 6, "hwtype-2", "3.6.7", 3),
                        new UavConnectionInfo("localhost", 14550, "udp"),
                        Rs232Params.conAscTec);

        ConnectionItem ci = new ConnectionItem();
        ci.selectedProperty().set(false);
        ci.hostProperty().set("localhost");
        ci.portProperty().set(14550);
        ci.nameProperty().set("MAVLink_UDP_default");
        ci.connectionTransportTypeProperty().set(ConnectionTransportType.UDP);
        ci.connectionDeviceTypeProperty().set(ConnectionDeviceType.COPTER);
        ci.unmannedAerialVehicleProperty().set(unmannedAerialVehicle);
        connectionSettings.connectionItemsListProperty().add(ci);

        return unmannedAerialVehicle;
    }

    private UnmannedAerialVehicle generateMavlinkTCPStub() {
        UnmannedAerialVehicle unmannedAerialVehicle =
                new UnmannedAerialVehicle(
                        AirplaneType.SIRIUS_BASIC,
                        "MAVLink_TCP_default",
                        new UavInFlightInfo(18.6417654, 394.987582, 100.234, 10, 219.85, 99),
                        new UavInfo("serial-2", 6, "hwtype-2", "3.6.7", 3),
                        new UavConnectionInfo("localhost", 5762, "tcp"),
                        Rs232Params.conAscTec);

        ConnectionItem ci = new ConnectionItem();
        ci.selectedProperty().set(false);
        ci.hostProperty().set("localhost");
        ci.portProperty().set(5762);
        ci.nameProperty().set("MAVLink_TCP_default");
        ci.connectionTransportTypeProperty().set(ConnectionTransportType.TCP);
        ci.connectionDeviceTypeProperty().set(ConnectionDeviceType.COPTER);
        ci.unmannedAerialVehicleProperty().set(unmannedAerialVehicle);
        connectionSettings.connectionItemsListProperty().add(ci);

        return unmannedAerialVehicle;
    }

    private List<UnmannedAerialVehicle> generateStubs() {
        List<UnmannedAerialVehicle> listofUnmannedAerialVehicles =
            Collections.singletonList(
                new UnmannedAerialVehicle(
                    AirplaneType.SIRIUS_BASIC,
                    "SIRIUS_BASIC",
                    new UavInFlightInfo(18.6417654, 394.987582, 100.234, 10, 219.85, 99),
                    new UavInfo("serial-2", 6, "hwtype-2", "3.6.7", 3),
                    new UavConnectionInfo("sim.mavinci.de", 9000, "UDP://localhost:9070"),
                    Rs232Params.conAscTec));

        ConnectionItem ci = new ConnectionItem();
        ci.selectedProperty().set(false);
        ci.hostProperty().set("sim.mavinci.de");
        ci.portProperty().set(9000);
        ci.nameProperty().set("SIRIUS_BASIC");        //SIRIUS_BASIC Simulation Server"
        ci.connectionTransportTypeProperty().set(ConnectionTransportType.UDP);
        ci.connectionDeviceTypeProperty().set(ConnectionDeviceType.SIMULATOR);
        ci.unmannedAerialVehicleProperty().set(listofUnmannedAerialVehicles.get(0));
        connectionSettings.connectionItemsListProperty().add(ci);
        return listofUnmannedAerialVehicles;
    }

    private static final byte[] IDL_DETECT_SEQUENCE = new byte[] {33, 35, 33, -128};

    private boolean detectIDL(MSerialPort port) {
        long startTime = System.currentTimeMillis();
        long timeout = 2000;
        try {
            byte[] bytes = null;
            while ((bytes = port.readBytes()) == null && System.currentTimeMillis() - startTime < timeout) ;
            if (bytes != null) {
                String bytesStr = new String(bytes, StandardCharsets.UTF_8);
                String IDL_DETECT_SEQUENCE_STR = new String(IDL_DETECT_SEQUENCE, StandardCharsets.UTF_8);
                int i = bytesStr.indexOf(IDL_DETECT_SEQUENCE_STR);

                return i != -1;
            } else {
                // TODO notify user
                Debug.getLog().severe("No bytes received on COM port " + port.toString() + " in 2 seconds");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private UavInFlightInfo createUavInFlightInfo(Backend backend) {
        double longitude = backend.lon;
        double latitude = backend.lat;
        double altitude = backend.alt;
        // TODO find where to get number of satellites from old API
        int numberOfSatellites = 0;
        double batteryVoltage = backend.batteryVoltage;
        // TODO find how old API calculate battery percent
        int batteryPercent = 99; // even God does not give you 100%
        return new UavInFlightInfo(longitude, latitude, altitude, numberOfSatellites, batteryVoltage, batteryPercent);
    }

    private UavInfo createUavInfo(BackendInfo info) {
        return new UavInfo(
            info.serialNumber,
            info.revisionHardware,
            info.hardwareType,
            info.getHumanReadableSWversion(),
            info.protocolVersion);
    }

    private void onConnectionProcedureScheduled(WorkerStateEvent e) {
        LOGGER.info("Start Connection procedure");
        scope.detectedUavListDisableProperty().set(true);

        scope.uavPinLabelVisibleProperty().set(true);

        scope.uavPinsListDisableProperty().set(true);
        scope.uavPinsListVisibleProperty().set(true);

        scope.connectionStateProperty().set(ConnectionState.CONNECTING);
        connectionStatus.set(ConnectionStatus.CONNECTING);

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
        connectionStatus.set(ConnectionStatus.DISCONNECTED);

        connectButtonDisableProperty().set(true);

        userDisconnectCheckVisibleProperty().set(false);
        userDisconnectCheckMarkedProperty().set(false);
    }

    public BooleanProperty selectedUavDisableProperty() {
        return scope.detectedUavListDisableProperty();
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

    public BooleanProperty uavPinsListDisableProperty() {
        return scope.uavPinsListDisableProperty();
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

    public BooleanProperty isSelectedItemConnected() {
        return isSelectedItemConnected;
    }

    public BooleanExpression connectButtonDisableCheck() {
        return Bindings.createBooleanBinding(
                () -> {
                    Boolean isAnyOtherConnected = false;
                    for (ConnectionItem ci : connectionSettings.connectionItemsListProperty().get()) {
                        if (ci.isSelected() && !ci.equals(selectedConnectionItem.get())) {
                            isAnyOtherConnected = true;
                        }
                    }

                    return selectedConnectionItemProperty().isNull().get() || isAnyOtherConnected;
                },
            connectionSettings.connectionItemsListProperty(),
            selectedConnectionItemProperty());
    }
}
