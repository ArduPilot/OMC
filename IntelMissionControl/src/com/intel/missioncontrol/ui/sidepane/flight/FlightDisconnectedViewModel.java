/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.connection.ConnectionDialogViewModel;
import com.intel.missioncontrol.ui.navigation.ConnectionPage;
import com.intel.missioncontrol.ui.navigation.SettingsPage;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

@ScopeProvider(scopes = FlightConnectionScope.class)
public class FlightDisconnectedViewModel extends ViewModelBase {

    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final ILanguageHelper languageHelper;
    private final ICommand renameMissionCommand;

    private final ReadOnlyStringProperty missionName;
    private final StringProperty connectionStatus = new SimpleStringProperty();
    private final StringProperty discoveringMessage = new SimpleStringProperty();
    private final BooleanProperty isConnected = new SimpleBooleanProperty(false);
    private final SimpleListProperty<String> listOfUav = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final StringProperty currentUav = new SimpleStringProperty();
    private final SimpleListProperty<String> mavlinkProtocolType =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final StringProperty mavlinkProtocolSelected = new SimpleStringProperty();
    private final StringProperty mavlinkConnectionName = new SimpleStringProperty();
    private final StringProperty mavlinkIpAddress = new SimpleStringProperty("127.0.0.1");
    private final IntegerProperty mavlinkPort = new SimpleIntegerProperty(911);

    @InjectScope
    private FlightConnectionScope flightConnectionScope;

    private IDialogService dialogService;

    @Inject
    public FlightDisconnectedViewModel(
            IApplicationContext applicationContext,
            INavigationService navigationService,
            IDialogService dialogService,
            ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.dialogService = dialogService;

        missionName =
            PropertyPath.from(applicationContext.currentMissionProperty()).selectReadOnlyString(Mission::nameProperty);

        renameMissionCommand = new DelegateCommand(applicationContext::renameCurrentMission);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        flightConnectionScope.currentUavConnectedProperty().bind(currentUav);
        connectionStatus.setValue(
            languageHelper.getString(
                (isConnected.getValue()
                    ? "com.intel.missioncontrol.ui.sidepane.flight.FlightDisconnectedView.status.connected"
                    : "com.intel.missioncontrol.ui.sidepane.flight.FlightDisconnectedView.status.notConnected")));
        discoveringMessage.setValue(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.sidepane.flight.FlightDisconnectedView.waitingForIncomingConnections"));
        listOfUav
            .get()
            .addAll(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.sidepane.flight.FlightDisconnectedView.typeUAV.mavlink"),
                "--",
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.sidepane.flight.FlightDisconnectedView.typeUAV.mavlink"));
        currentUav.setValue(listOfUav.get(0));
        mavlinkProtocolType
            .get()
            .addAll(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.sidepane.flight.FlightDisconnectedView.protocol.udp"),
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.sidepane.flight.FlightDisconnectedView.protocol.tcp"));
        mavlinkProtocolSelected.set(mavlinkProtocolType.get(0));
    }

    public ReadOnlyStringProperty missionNameProperty() {
        return missionName;
    }

    public ReadOnlyStringProperty getConnectionStatusProperty() {
        return connectionStatus;
    }

    public ReadOnlyStringProperty getDiscoveringMessageProperty() {
        return discoveringMessage;
    }

    public ListProperty<String> listOfUavProperty() {
        return listOfUav;
    }

    public ListProperty<String> mavlinkProtocolTypeProperty() {
        return mavlinkProtocolType;
    }

    public StringProperty mavlinkProtocolSelectedProperty() {
        return mavlinkProtocolSelected;
    }

    public BooleanProperty isConnectedProperty() {
        return isConnected;
    }

    public StringProperty currentUavProperty() {
        return currentUav;
    }

    public StringProperty mavlinkConnectionNameProperty() {
        return mavlinkConnectionName;
    }

    public StringProperty mavlinkIpAddressProperty() {
        return mavlinkIpAddress;
    }

    public IntegerProperty mavlinkPortProperty() {
        return mavlinkPort;
    }

    public ICommand getConnectViaIDLCommand() {
        return connectViaIDLCommand;
    }

    public ICommand getSimulateCommand() {
        return simulateCommand;
    }

    private final ICommand connectViaIDLCommand = new DelegateCommand(() -> connectViaIDL());
    private final ICommand simulateCommand = new DelegateCommand(() -> simulate());

    private void connectViaIDL() {
        // TODO: to call backend to connect to idl uav, once it is connected then navigate to connected page
        navigationService.navigateTo(SidePanePage.FLIGHT_CONNECTED);
    }

    private void simulate() {
        // temp to show toast
        // TODO: move toast to after dronekit library initialized and haven't discovered any device yet
        showToast();

        // TODO: simulate
    }

    public void showToast() {
        applicationContext.addToast(
            Toast.of(ToastType.INFO)
                .setText(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.sidepane.flight.FlightDisconnectedView.toast.pluginDeviceAndGamepadNow"))
                .create());
    }

    public void addMavlinkUAV() {
        // TODO: add current instance of mavlink connection to history

        listOfUavProperty().add(0, mavlinkConnectionName.get());
        currentUavProperty().set(mavlinkConnectionName.get());
    }

    public BooleanExpression isMavlinkTypeProperty() {
        return Bindings.createBooleanBinding(
            () -> {
                String currentuavname = currentUav.get();
                return (currentuavname == null)
                    ? false
                    : currentuavname.equals(
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.sidepane.flight.FlightDisconnectedView.typeUAV.mavlink"));
            },
            currentUavProperty());
    }

    void goToSettings() {
        navigationService.navigateTo(ConnectionPage.UAV_CONNECT);
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

    public ICommand getAddConnectionCommand() {
        return addConnectionCommand;
    }

    public ICommand getRenameMissionCommand() {
        return renameMissionCommand;
    }
}
