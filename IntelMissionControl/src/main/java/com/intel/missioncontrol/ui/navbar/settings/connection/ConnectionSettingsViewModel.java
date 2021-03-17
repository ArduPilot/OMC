/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.connection;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.drone.connection.ConnectionState;
import com.intel.missioncontrol.drone.connection.IConnectionListener;
import com.intel.missioncontrol.drone.connection.IConnectionListenerService;
import com.intel.missioncontrol.drone.connection.IDroneConnectionService;
import com.intel.missioncontrol.drone.connection.IReadOnlyConnectionItem;
import com.intel.missioncontrol.drone.connection.MavlinkDroneConnectionItem;
import com.intel.missioncontrol.drone.connection.MockConnectionItem;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.ConnectionSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navbar.settings.connection.dialog.ConnectionDialogViewModel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.AsyncCommand;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.commands.FutureCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedDelegateCommand;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.NotImplementedException;
import org.asyncfx.beans.binding.ValueConverter;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncListProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;

public class ConnectionSettingsViewModel extends ViewModelBase {

    @InjectScope
    private FlightScope flightScope;

    private final UIAsyncBooleanProperty acceptIncomingConnections = new UIAsyncBooleanProperty(this);
    private final UIAsyncIntegerProperty listeningPort = new UIAsyncIntegerProperty(this);
    private final UIAsyncBooleanProperty isNotConnected = new UIAsyncBooleanProperty(this);
    private final UIAsyncObjectProperty<ConnectionSettingsTableItem> selectedTableItem =
        new UIAsyncObjectProperty<>(this);
    private final AsyncCommand connectToDroneCommand;
    private final Command addConnectionCommand;
    private final Command editConnectionCommand;
    private final ParameterizedCommand<ConnectionSettingsTableItem> removeConnectionCommand;
    private Toast listenerErrorToast;
    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final INavigationService navigationService;
    private final IDroneConnectionService droneConnectionService;
    private final IConnectionListenerService droneConnectionListenerService;
    private final UIAsyncListProperty<ConnectionSettingsTableItem> tableItems =
        new UIAsyncListProperty<>(
            this,
            new UIPropertyMetadata.Builder<AsyncObservableList<ConnectionSettingsTableItem>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());
    private ObjectProperty<AlertType> listenerImageType = new SimpleObjectProperty<>();
    private StringProperty listenerStatus = new SimpleStringProperty();

    @Inject
    public ConnectionSettingsViewModel(
            IApplicationContext applicationContext,
            INavigationService navigationService,
            IDroneConnectionService droneConnectionService,
            ILanguageHelper languageHelper,
            IDialogService dialogService,
            ISettingsManager settingsManager,
            IDroneConnectionService uavConnectionService,
            IConnectionListenerService droneConnectionListenerService,
            IHardwareConfigurationManager hardwareConfigurationManager) {
        this.navigationService = navigationService;
        this.droneConnectionService = droneConnectionService;
        this.droneConnectionListenerService = droneConnectionListenerService;
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;

        droneConnectionService
            .availableDroneConnectionItemsProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (selectedTableItem.get() == null && newValue.size() > 0) {
                        selectedTableItem.set(null); // TODO set to first item in list
                    }
                },
                Dispatcher.platform());

        IConnectionListener connectionListener = droneConnectionListenerService.getConnectionListener();

        // Toasts for listener errors:
        droneConnectionListenerService
            .getConnectionListener()
            .listenerErrorProperty()
            .addListener((observable, oldValue, newValue) -> showListenerErrorToast(newValue), Dispatcher.platform());

        navigationService
            .workflowStepProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    Throwable listenerError =
                        droneConnectionListenerService.getConnectionListener().listenerErrorProperty().get();
                    if (newValue == WorkflowStep.FLIGHT && listenerError != null) {
                        showListenerErrorToast(listenerError);
                    } else if (listenerErrorToast != null) {
                        listenerErrorToast.dismiss();
                        listenerErrorToast = null;
                    }
                });

        var connectionItemConverter =
            new ValueConverter<IReadOnlyConnectionItem, ConnectionSettingsTableItem>() {
                @Override
                public ConnectionSettingsTableItem convert(IReadOnlyConnectionItem value) {
                    if (value instanceof MockConnectionItem) {
                        return new ConnectionSettingsTableItem(value);
                    } else if (value instanceof MavlinkDroneConnectionItem) {
                        return new ConnectionSettingsTableItem(
                            hardwareConfigurationManager, (MavlinkDroneConnectionItem)value);
                    }

                    throw new NotImplementedException("Unsupported connection item type");
                }
            };

        tableItems.bindContent(uavConnectionService.availableDroneConnectionItemsProperty(), connectionItemConverter);

        acceptIncomingConnections.bindBidirectional(connectionListener.acceptIncomingConnectionsProperty());
        listeningPort.bindBidirectional(connectionListener.listeningPortProperty());

        isNotConnected.bind(droneConnectionService.connectionStateProperty().isEqualTo(ConnectionState.NOT_CONNECTED));

        ConnectionSettings connectionSettings = settingsManager.getSection(ConnectionSettings.class);

        if (!tableItems.isEmpty()) {
            selectedTableItem.set(tableItems.get(0));
        }

        connectToDroneCommand =
            new FutureCommand(
                this::connectToDroneAsync,
                e -> {},
                selectedTableItem
                    .isNotNull()
                    .and(applicationContext.currentMissionProperty().isNotNull())
                    .and(
                        PropertyPath.from(selectedTableItem)
                            .selectReadOnlyString(ConnectionSettingsTableItem::modelNameProperty)
                            .isNotNull())
                    .and(isNotConnected));

        addConnectionCommand =
            new DelegateCommand(
                () ->
                    dialogService
                        .requestDialogAsync(
                            this,
                            ConnectionDialogViewModel.class,
                            () ->
                                new ConnectionDialogViewModel.Payload(
                                    ConnectionDialogViewModel.DialogType.Create, null),
                            true)
                        .whenSucceeded(
                            (viewModel) -> {
                                ConnectionDialogViewModel.Result dialogResult = viewModel.getDialogResult();
                                if (dialogResult != null) {
                                    selectedTableItem.set(findTableItem(dialogResult.getConnectionItem()));
                                }
                            }));

        editConnectionCommand =
            new DelegateCommand(
                () -> {
                    dialogService
                        .requestDialogAsync(
                            this,
                            ConnectionDialogViewModel.class,
                            () ->
                                new ConnectionDialogViewModel.Payload(
                                    ConnectionDialogViewModel.DialogType.Edit,
                                    selectedTableItem.get().getConnectionItem()),
                            true)
                        .whenSucceeded(
                            (viewModel) -> {
                                ConnectionDialogViewModel.Result dialogResult = viewModel.getDialogResult();
                                if (dialogResult != null) {
                                    selectedTableItem.set(findTableItem(dialogResult.getConnectionItem()));
                                }
                            });
                },
                selectedTableItem.isNotNull());

        removeConnectionCommand =
            new ParameterizedDelegateCommand<>(
                (item) -> {
                    try (var list = connectionSettings.connectionItemsListProperty().lock()) {
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isSameConnection(item.getConnectionItem())) {
                                list.remove(i);
                                return;
                            }
                        }
                    }
                });
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();
        listenerImageType.bind(
            Bindings.createObjectBinding(
                () -> {
                    if (acceptIncomingConnections.get()) {
                        return AlertType.LOADING;
                    } else {
                        return AlertType.WARNING;
                    }
                },
                acceptIncomingConnections));

        listenerStatus.bind(
            Bindings.createStringBinding(
                () -> {
                    if (acceptIncomingConnections.get()) {
                        return languageHelper.getString(ConnectionSettingsViewModel.class, "autodiscoverOn");
                    } else {
                        return languageHelper.getString(ConnectionSettingsViewModel.class, "autodiscoverOff");
                    }
                },
                acceptIncomingConnections));
    }

    AsyncCommand getConnectToDroneCommand() {
        return connectToDroneCommand;
    }

    private Future<Void> connectToDroneAsync() {
        return droneConnectionService
            .connectAsync(selectedTableItem.get().getConnectionItem())
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
                                    ? languageHelper.getString(
                                        "com.intel.missioncontrol.ui.sidepane.flight.connect.ConnectDroneViewModel.timeout")
                                    : ex.getMessage();

                            Toast toast =
                                Toast.of(ToastType.ALERT)
                                    .setShowIcon(true)
                                    .setText(
                                        languageHelper.getString(
                                            "com.intel.missioncontrol.ui.sidepane.flight.connect.ConnectDroneViewModel.connectFailed",
                                            errorMessage))
                                    .create();
                            applicationContext.addToast(toast);
                        });
                })
            .thenGet(() -> null);
    }

    private ConnectionSettingsTableItem findTableItem(MavlinkDroneConnectionItem connectionItem) {
        try (var lockedList = tableItems.lock()) {
            return lockedList
                .stream()
                .filter(tableItem -> tableItem.getConnectionItem().isSameConnection(connectionItem))
                .findFirst()
                .orElse(null);
        }
    }

    private void showListenerErrorToast(Throwable listenerError) {
        String text =
            listenerError == null
                ? null
                : languageHelper.getString(
                    "com.intel.missioncontrol.ui.navbar.settings.connection.ConnectionSettingsViewModel.listenerError",
                    listenerError.getMessage());

        if (listenerErrorToast != null && listenerErrorToast.isShowingProperty().get()) {
            if (Objects.equals(listenerErrorToast.getText(), text)) {
                return;
            }

            listenerErrorToast.dismiss();
            listenerErrorToast = null;
        }

        if (text != null) {
            listenerErrorToast =
                Toast.of(ToastType.ALERT)
                    .setShowIcon(true)
                    .setTimeout(Toast.LONG_TIMEOUT)
                    .setText(text)
                    .setCloseable(true)
                    .setAction(
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.navbar.settings.connection.ConnectionSettingsViewModel.retry"),
                        false,
                        true,
                        () -> droneConnectionListenerService.getConnectionListener().restartAsync(),
                        MoreExecutors.directExecutor())
                    .create();
            applicationContext.addToast(listenerErrorToast);
        }
    }

    public ReadOnlyObjectProperty<AlertType> listenerImageTypeProperty() {
        return listenerImageType;
    }

    public ReadOnlyStringProperty listenerStatusProperty() {
        return listenerStatus;
    }

    ReadOnlyListProperty<ConnectionSettingsTableItem> tableItemsProperty() {
        return tableItems.getReadOnlyProperty();
    }

    ReadOnlyObjectProperty currentMissionProperty() {
        return applicationContext.currentMissionProperty();
    }

    Property<Boolean> acceptIncomingConnectionsProperty() {
        return acceptIncomingConnections;
    }

    Property<Number> listeningPortProperty() {
        return listeningPort;
    }

    Property<ConnectionSettingsTableItem> selectedTableItemProperty() {
        return selectedTableItem;
    }

    Command getAddConnectionCommand() {
        return addConnectionCommand;
    }

    Command getEditConnectionCommand() {
        return editConnectionCommand;
    }

    ParameterizedCommand<ConnectionSettingsTableItem> getRemoveConnectionCommand() {
        return removeConnectionCommand;
    }
}
