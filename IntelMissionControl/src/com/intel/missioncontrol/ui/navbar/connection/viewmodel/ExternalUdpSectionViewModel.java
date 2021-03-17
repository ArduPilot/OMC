/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.intel.missioncontrol.settings.rtk.RtkUdp;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.scope.ExternalConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.scope.RtkConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.view.ExternalBaseStationView;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkClient;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/** @author Vladimir Iordanov */
public class ExternalUdpSectionViewModel extends ViewModelBase {

    @InjectScope
    private RtkConnectionScope rtkConnectionScope;

    @InjectScope
    private ExternalConnectionScope externalConnectionScope;

    private final ObjectProperty<Integer> port = new SimpleObjectProperty<>(RtkUdp.UDP_DEFAULT_VALUE);
    private final ObjectProperty<RtkUdp> currentProperties = new SimpleObjectProperty<>();

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        rtkConnectionScope
            .currentSettingsProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (oldValue != null && oldValue.getExternalSettings() != null) {
                        unconnectSettings(oldValue.getExternalSettings().getUdpSettings());
                    }

                    if (newValue != null && newValue.getExternalSettings() != null) {
                        connectToSettings(newValue.getExternalSettings().getUdpSettings());
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
            ExternalBaseStationView.ExternalConnetionType.UDP, connectCommand);
        externalConnectionScope.registerDisconnectCommand(
            ExternalBaseStationView.ExternalConnetionType.UDP, disconnectCommand);
    }

    private void connect() {
        if (currentProperties.get() == null) {
            throw new IllegalStateException("No properties to connect to UDP port");
        }

        IRtkClient client = rtkConnectionScope.getCurrentClient();

        client.disconnect();
        client.connect(currentProperties.get().getPort());
    }

    private void disconnect() {
        IRtkClient client = rtkConnectionScope.getCurrentClient();
        client.disconnect();
    }

    private void connectToSettings(RtkUdp rtkUdp) {
        if (rtkUdp == null) {
            return;
        }

        port.bindBidirectional(rtkUdp.portProperty());

        currentProperties.setValue(rtkUdp);
    }

    private void unconnectSettings(RtkUdp rtkUdp) {
        if (rtkUdp == null) {
            return;
        }

        port.unbindBidirectional(rtkUdp.portProperty());

        currentProperties.setValue(null);
    }

    public Integer getPort() {
        return port.get();
    }

    public ObjectProperty<Integer> portProperty() {
        return port;
    }

    public BooleanBinding isConnected() {
        return rtkConnectionScope.isConnectedBinding();
    }
}
