/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.connect;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.drone.connection.IDroneConnectionService;
import com.intel.missioncontrol.drone.connection.IReadOnlyConnectionItem;
import com.intel.missioncontrol.drone.connection.LegacyLocalSimulationConnectionItem;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navbar.settings.connection.dialog.ConnectionDialogViewModel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SettingsPage;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.AsyncCommand;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.commands.FutureCommand;
import java.util.concurrent.TimeoutException;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ListChangeListener;
import org.asyncfx.beans.AsyncObservable;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.UIAsyncListProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;

public class ConnectDroneViewModel extends ViewModelBase {
    @InjectScope
    private FlightScope flightScope;

    private final UIAsyncStringProperty missionName = new UIAsyncStringProperty(this);

    private final UIAsyncListProperty<IReadOnlyConnectionItem> availableConnectionItems =
        new UIAsyncListProperty<>(
            this,
            new UIPropertyMetadata.Builder<AsyncObservableList<IReadOnlyConnectionItem>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(
                        e -> new AsyncObservable[] {e.nameProperty(), e.descriptionIdProperty(), e.isOnlineProperty()}))
                .create());
    private final UIAsyncObjectProperty<IReadOnlyConnectionItem> selectedConnectionItem =
        new UIAsyncObjectProperty<>(this);

    private final AsyncCommand connectToDroneCommand;
    private final Command simulateCommand;

    private final Command renameMissionCommand;
    private final Command showHelpCommand;
    private final Command goToConnectionSettingsCommand;

    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final IDroneConnectionService droneConnectionService;
    private final ILanguageHelper languageHelper;

    @Inject
    public ConnectDroneViewModel(
            IApplicationContext applicationContext,
            INavigationService navigationService,
            IDroneConnectionService droneConnectionService,
            ILanguageHelper languageHelper,
            IDialogService dialogService) {
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.droneConnectionService = droneConnectionService;
        this.languageHelper = languageHelper;

        missionName.bind(
            PropertyPath.from(applicationContext.currentLegacyMissionProperty()).selectReadOnlyString(Mission::nameProperty));

        availableConnectionItems.bindContent(droneConnectionService.availableDroneConnectionItemsProperty());

        availableConnectionItems.addListener(
            (Observable o) -> {
                var items = availableConnectionItems.get();
                if (items.size() > 0) {
                    selectedConnectionItem.set(items.get(0));
                }
            },
            Dispatcher.platform());

        renameMissionCommand = new DelegateCommand(applicationContext::renameCurrentMission);

        showHelpCommand = new DelegateCommand(() -> navigationService.navigateTo(SidePanePage.CONNECT_DRONE_HELP));

        goToConnectionSettingsCommand =
            new DelegateCommand(() -> navigationService.navigateTo(SettingsPage.CONNECTION));

        connectToDroneCommand =
            new FutureCommand(
                () -> {
                    IReadOnlyConnectionItem connItem = selectedConnectionItem.get();
                    if (connItem.getDescriptionId() != null) {
                        return connectToDroneAsync(connItem);
                    }

                    // Undefined platform, force user to choose in ConnectionDialog:
                    navigationService.navigateTo(SettingsPage.CONNECTION);
                    return dialogService
                        .requestDialogAsync(
                            this,
                            ConnectionDialogViewModel.class,
                            () ->
                                new ConnectionDialogViewModel.Payload(
                                    ConnectionDialogViewModel.DialogType.Edit, connItem),
                            true)
                        .thenApply(
                            vm -> {
                                ConnectionDialogViewModel.Result dialogResult = vm.getDialogResult();
                                if (dialogResult == null) {
                                    return Futures.successful();
                                }

                                return connectToDroneAsync(dialogResult.getConnectionItem());
                            });
                },
                e -> {},
                selectedConnectionItem.isNotNull());

        simulateCommand = new DelegateCommand(this::simulate);

        // Toasts for connected / disconnected events
        droneConnectionService
            .connectedDroneConnectionItemsProperty()
            .addListener(
                (ListChangeListener.Change<? extends IReadOnlyConnectionItem> change) -> {
                    while (change.next()) {
                        change.getAddedSubList().forEach(this::onConnectionItemConnected);
                        change.getRemoved().forEach(this::onConnectionItemDisconnected);
                    }
                },
                Dispatcher.background());
    }

    private void onConnectionItemConnected(IReadOnlyConnectionItem connectionItem) {
        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.run(
            () -> {
                if (navigationService.getSidePanePage() == SidePanePage.CONNECT_DRONE) {
                    navigationService.navigateTo(SidePanePage.FLY_DRONE);
                }

                Toast.ToastBuilder toastBuilder =
                    Toast.of(ToastType.INFO)
                        .setText(
                            languageHelper.getString(
                                ConnectDroneViewModel.class, "connectedTo", connectionItem.getName()))
                        ;

                if (navigationService.getSidePanePage() != SidePanePage.FLY_DRONE) {
                    // TODO if eG dataset pane => dont switch automatically
                    toastBuilder
                        .setTimeout(Toast.LONG_TIMEOUT)
                        .setAction(
                        languageHelper.getString(ConnectDroneViewModel.class, "gotoFlightTab"),
                        false,
                        true,
                        () -> {
                            // TODO: select newly connected drone first.
                            if (selectedConnectionItem.get() == null) {
                                selectedConnectionItem.setValue(connectionItem);
                            }

                            if (navigationService.getSidePanePage() == SidePanePage.START_PLANNING) {
                                navigationService.navigateTo(SidePanePage.FLY_DRONE);
                            }
                        },
                        Dispatcher.platform());
                }

                Toast toast = toastBuilder.create();
                applicationContext.addToast(toast);
            });
    }

    private void onConnectionItemDisconnected(IReadOnlyConnectionItem connectionItem) {
        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.run(
            () -> {
                Toast toast =
                    Toast.of(ToastType.INFO)
                        .setText(
                            languageHelper.getString(
                                ConnectDroneViewModel.class, "disconnectedFrom", connectionItem.getName()))
                        .create();
                applicationContext.addToast(toast);
            });
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();
    }

    ReadOnlyProperty<String> missionNameProperty() {
        return missionName;
    }

    ReadOnlyListProperty<IReadOnlyConnectionItem> availableDroneConnectionItemsProperty() {
        return availableConnectionItems.getReadOnlyProperty();
    }

    Property<IReadOnlyConnectionItem> selectedConnectionItemProperty() {
        return selectedConnectionItem;
    }

    AsyncCommand getConnectToDroneCommand() {
        return connectToDroneCommand;
    }

    Command getSimulateCommand() {
        return simulateCommand;
    }

    private Future<Void> connectToDroneAsync(IReadOnlyConnectionItem connectionItem) {
        return droneConnectionService
            .connectAsync(connectionItem)
            .whenSucceeded(
                (drone) -> {
                    flightScope.currentDroneProperty().set(drone);
                    navigationService.navigateTo(SidePanePage.FLY_DRONE);
                })
            .whenFailed(
                e -> {
                    Dispatcher dispatcher = Dispatcher.platform();
                    dispatcher.run(
                        () -> {
                            Throwable ex = e.getCause() != null ? e.getCause() : e;
                            String errorMessage =
                                (ex instanceof TimeoutException)
                                    ? languageHelper.getString(ConnectDroneViewModel.class, "timeout")
                                    : ex.getMessage();

                            Toast toast =
                                Toast.of(ToastType.ALERT)
                                    .setShowIcon(true)
                                    .setText(
                                        languageHelper.getString(
                                            ConnectDroneViewModel.class, "connectFailed", errorMessage))
                                    .create();
                            applicationContext.addToast(toast);
                        });
                })
            .thenGet(() -> null);
    }

    private void simulate() {
        Mission mission = applicationContext.getCurrentLegacyMission();
        if (mission == null) {
            throw new IllegalStateException();
        }

        FlightPlan fp = mission.getCurrentFlightPlan();

        droneConnectionService
            .connectAsync(new LegacyLocalSimulationConnectionItem(mission, fp))
            .whenSucceeded(
                (drone) ->
                    PropertyHelper.setValueAsync(flightScope.currentDroneProperty(), drone)
                        .whenSucceeded(v -> navigationService.navigateTo(SidePanePage.FLY_DRONE)));
    }

    Command getGoToDroneConnectionSettingsCommand() {
        return goToConnectionSettingsCommand;
    }

    Command getRenameMissionCommand() {
        return renameMissionCommand;
    }

    Command getShowHelpCommand() {
        return showHelpCommand;
    }
}
