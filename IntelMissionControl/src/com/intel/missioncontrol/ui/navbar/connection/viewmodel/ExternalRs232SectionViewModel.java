/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.settings.rtk.RtkSerial;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.model.IRtkStatisticFactory;
import com.intel.missioncontrol.ui.navbar.connection.scope.ExternalConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.scope.RtkConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.view.ExternalBaseStationView;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkClient;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkStatisticListener;
import eu.mavinci.desktop.rs232.MSerialPort;
import eu.mavinci.desktop.rs232.Rs232Params;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** @author Vladimir Iordanov */
public class ExternalRs232SectionViewModel extends ViewModelBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalRs232SectionViewModel.class);

    @InjectScope
    private RtkConnectionScope rtkConnectionScope;

    @InjectScope
    private ExternalConnectionScope externalConnectionScope;

    @Inject
    private IRtkStatisticFactory rtkStatisticFactory;

    private final StringProperty port = new SimpleStringProperty();
    private final ObjectProperty<Integer> bitRate = new SimpleObjectProperty<>(Rs232Params.DEFAULT_BIT_RATE);
    private final ObjectProperty<Rs232Params.DataBits> dataBits =
        new SimpleObjectProperty<>(Rs232Params.DataBits.getDefault());
    private final ObjectProperty<Rs232Params.StopBit> stopBits =
        new SimpleObjectProperty<>(Rs232Params.StopBit.getDefault());
    private final ObjectProperty<Rs232Params.Parity> parity =
        new SimpleObjectProperty<>(Rs232Params.Parity.getDefault());

    private final ListProperty<String> portsList = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<Rs232Params.DataBits> dataBitsList =
        new SimpleListProperty<>(FXCollections.observableArrayList(Rs232Params.DataBits.values()));
    private final ListProperty<Rs232Params.StopBit> stopBitsList =
        new SimpleListProperty<>(FXCollections.observableArrayList(Rs232Params.StopBit.values()));
    private final ListProperty<Rs232Params.Parity> parityList =
        new SimpleListProperty<>(FXCollections.observableArrayList(Rs232Params.Parity.values()));
    private final BooleanProperty isRefreshingPorts = new SimpleBooleanProperty();

    private Command refreshPortsCommand;

    private final ObjectProperty<RtkSerial> currentProperties = new SimpleObjectProperty<>();

    private IRtkStatisticListener listener;

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        rtkConnectionScope
            .currentSettingsProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (oldValue != null && oldValue.getExternalSettings() != null) {
                        unconnectSettings(oldValue.getExternalSettings().getSerialSettings());
                    }

                    if (newValue != null && newValue.getExternalSettings() != null) {
                        connectToSettings(newValue.getExternalSettings().getSerialSettings());
                    }
                });

        refreshPortsCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            refreshPorts(true);
                        }
                    },
                isRefreshingPorts.not());

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
            ExternalBaseStationView.ExternalConnetionType.RS232, connectCommand);
        externalConnectionScope.registerDisconnectCommand(
            ExternalBaseStationView.ExternalConnetionType.RS232, disconnectCommand);

        refreshPorts(false);
    }

    private void refreshPorts(boolean useCache) {
        try {
            isRefreshingPorts.set(true);
            String[] portList = MSerialPort.getPorts(useCache);
            if (portList != null) {
                portsList.setAll(portList);
            }
        } catch (IOException e) {
            LOGGER.error("Exception during loading of Rs232 ports list", e);
        } finally {
            isRefreshingPorts.set(false);
        }

        validatePort();
    }

    private void validatePort() {
        if (portsList.isEmpty()) {
            port.set(null);
        } else if (!portsList.contains(getPort())) {
            port.setValue(portsList.get(0));
        }
    }

    private void connect() {
        if (currentProperties.get() == null) {
            throw new IllegalStateException("No properties to connect to COM port");
        }

        Rs232Params rs232Params = currentProperties.get().toRs232Params();
        IRtkClient client = rtkConnectionScope.getCurrentClient();

        client.disconnect();
        client.connect(rs232Params);
    }

    private void disconnect() {
        IRtkClient client = rtkConnectionScope.getCurrentClient();
        client.disconnect();
    }

    private void connectToSettings(RtkSerial rtkSerial) {
        if (rtkSerial == null) {
            return;
        }

        port.bindBidirectional(rtkSerial.portProperty());
        bitRate.bindBidirectional(rtkSerial.bitRateProperty());
        dataBits.bindBidirectional(rtkSerial.dataBitsProperty());
        stopBits.bindBidirectional(rtkSerial.stopBitsProperty());
        parity.bindBidirectional(rtkSerial.parityProperty());

        currentProperties.setValue(rtkSerial);

        validatePort();
    }

    private void unconnectSettings(RtkSerial rtkSerial) {
        if (rtkSerial == null) {
            return;
        }

        port.unbindBidirectional(rtkSerial.portProperty());
        bitRate.unbindBidirectional(rtkSerial.bitRateProperty());
        dataBits.unbindBidirectional(rtkSerial.dataBitsProperty());
        stopBits.unbindBidirectional(rtkSerial.stopBitsProperty());
        parity.unbindBidirectional(rtkSerial.parityProperty());

        currentProperties.setValue(null);
    }

    public boolean getIsConnected() {
        return rtkConnectionScope.getIsConnected();
    }

    public BooleanBinding isConnectedProperty() {
        return rtkConnectionScope.isConnectedBinding();
    }

    public String getPort() {
        return port.get();
    }

    public StringProperty portProperty() {
        return port;
    }

    public Integer getBitRate() {
        return bitRate.get();
    }

    public ObjectProperty<Integer> bitRateProperty() {
        return bitRate;
    }

    public Rs232Params.DataBits getDataBits() {
        return dataBits.get();
    }

    public ObjectProperty<Rs232Params.DataBits> dataBitsProperty() {
        return dataBits;
    }

    public Rs232Params.StopBit getStopBits() {
        return stopBits.get();
    }

    public ObjectProperty<Rs232Params.StopBit> stopBitsProperty() {
        return stopBits;
    }

    public Rs232Params.Parity getParity() {
        return parity.get();
    }

    public ObjectProperty<Rs232Params.Parity> parityProperty() {
        return parity;
    }

    public ObservableList<String> getPortsList() {
        return portsList.get();
    }

    public ListProperty<String> portsListProperty() {
        return portsList;
    }

    public Command getRefreshPortsCommand() {
        return refreshPortsCommand;
    }

    public ObservableList<Rs232Params.DataBits> getDataBitsList() {
        return dataBitsList.get();
    }

    public ListProperty<Rs232Params.DataBits> dataBitsListProperty() {
        return dataBitsList;
    }

    public ObservableList<Rs232Params.StopBit> getStopBitsList() {
        return stopBitsList.get();
    }

    public ListProperty<Rs232Params.StopBit> stopBitsListProperty() {
        return stopBitsList;
    }

    public ObservableList<Rs232Params.Parity> getParityList() {
        return parityList.get();
    }

    public ListProperty<Rs232Params.Parity> parityListProperty() {
        return parityList;
    }
}
