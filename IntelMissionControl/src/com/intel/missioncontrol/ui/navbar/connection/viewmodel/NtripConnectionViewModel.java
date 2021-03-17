/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.connector.IConnectorService;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.NtripConnections;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.RtkConnectionSetupState;
import com.intel.missioncontrol.ui.navbar.connection.RtkType;
import com.intel.missioncontrol.ui.navbar.connection.scope.RtkConnectionScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkStatisticListener;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionSettings;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtkClient;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NtripConnectionViewModel extends ViewModelBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(NtripConnectionViewModel.class);

    private final BooleanProperty connectionPaneExpanded = new SimpleBooleanProperty(true);
    private final BooleanProperty connectionPaneAnimation = new SimpleBooleanProperty(false);
    private final ILanguageHelper languageHelper;
    private final NtripConnections ntripConnections;
    private final IDialogService dialogService;
    private final IConnectorService connectorService;
    private final IApplicationContext applicationContext;

    @InjectScope
    protected MainScope mainScope;

    @InjectScope
    protected RtkConnectionScope rtkConnectionScope;

    private IRtkStatisticListener statisticsListener;
    private UUID connectorId;

    @Inject
    public NtripConnectionViewModel(
            IApplicationContext applicationContext,
            ILanguageHelper languageHelper,
            ISettingsManager settingsManager,
            IDialogService dialogService,
            IConnectorService connectorService) {
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        ntripConnections = settingsManager.getSection(NtripConnections.class);
        this.dialogService = dialogService;
        this.connectorService = connectorService;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        rtkConnectionScope.registerConnectionCommand(RtkType.NTRIP, getConnectionCommand(true));
        rtkConnectionScope.registerDisconnectionCommand(RtkType.NTRIP, getDisconnectCommand(false));

        applicationContext.currentMissionProperty().addListener((o, old, current) -> disconnect());
        rtkConnectionScope
            .rtkSourceProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == RtkType.NTRIP) {
                        statisticsListener = rtkConnectionScope.getStatisticsListener();
                        RtkClient currentClient = rtkConnectionScope.getCurrentClient();
                        if (currentClient != null) {
                            currentClient.removeListener(statisticsListener);
                        }
                    } else if (newValue != null) {
                        if (rtkConnectionScope.getCurrentClient() != null && statisticsListener != null) {
                            rtkConnectionScope.getCurrentClient().addListener(statisticsListener);
                        }
                    }
                });
    }

    protected Command getConnectionCommand(boolean asynchronous) {
        DelegateCommand command =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            connect();
                        }
                    },
                rtkConnectionScope.isConnectedBinding().not(),
                asynchronous);
        command.setOnFailed(
            e -> {
                e.getSource().getException().printStackTrace();
            });
        command.setOnSucceeded(
            e -> {
                rtkConnectionScope.detailedStatisticsItemsProperty().clear();
                rtkConnectionScope.connectedStateProperty().set(ConnectionState.CONNECTED);
                connectionPaneAnimationProperty().set(true);
            });
        return command;
    }

    Command getDisconnectCommand(boolean asynchronous) {
        DelegateCommand delegateCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            disconnect();
                        }
                    },
                rtkConnectionScope.isConnectedBinding(),
                asynchronous);
        delegateCommand.setOnSucceeded(
            e -> rtkConnectionScope.connectedStateProperty().set(ConnectionState.NOT_CONNECTED));
        return delegateCommand;
    }

    private void connect() {
        connectorId = connectorService.createNtripConnector(rtkConnectionScope, connectionSettingProperty().get());
        // wait 5 sec for stream of rtk packages on background thread
        Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
        connectorService
            .getPackageSourceFor(connectorId)
            .subscribe(len -> LOGGER.debug("Read package with length {}", len));
    }

    private void disconnect() {
        connectorService.closeConnector(connectorId);
        connectionPaneAnimationProperty().set(false);
        connectionPaneExpandedProperty().set(true);
    }

    public ObjectProperty<NtripConnectionSettings> connectionSettingProperty() {
        return rtkConnectionScope.selectedNtripConnectionProperty();
    }

    public BooleanBinding isConnectedProperty() {
        return rtkConnectionScope.isConnectedBinding();
    }

    public void openConnectionCreation() {
        rtkConnectionScope.rtkNtripConnectionSetupStateProperty().set(RtkConnectionSetupState.NEW);
        dialogService.requestDialogAndWait(this, SetupNtripConnectionViewModel.class);
    }

    public ListProperty<NtripConnectionSettings> connectionListProperty() {
        return ntripConnections.connections();
    }

    public void openConnectionEdit() {
        rtkConnectionScope.rtkNtripConnectionSetupStateProperty().set(RtkConnectionSetupState.EDIT);
        dialogService.requestDialogAndWait(this, SetupNtripConnectionViewModel.class);
    }

    public BooleanProperty connectionPaneExpandedProperty() {
        return connectionPaneExpanded;
    }

    public BooleanProperty connectionPaneAnimationProperty() {
        return connectionPaneAnimation;
    }

    public ObjectProperty<RtkStatisticData> statisticDataProperty() {
        return rtkConnectionScope.dataProperty();
    }
}
