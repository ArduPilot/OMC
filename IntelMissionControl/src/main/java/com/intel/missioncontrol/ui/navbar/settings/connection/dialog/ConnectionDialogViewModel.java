/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.connection.dialog;

import com.google.inject.Inject;
import com.intel.missioncontrol.drone.connection.DroneConnectionType;
import com.intel.missioncontrol.drone.connection.IDroneConnectionService;
import com.intel.missioncontrol.drone.connection.IReadOnlyConnectionItem;
import com.intel.missioncontrol.drone.connection.MavlinkDroneConnectionItem;
import com.intel.missioncontrol.drone.connection.TcpIpTransportType;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.ConnectionSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.common.hardware.PlatformItem;
import com.intel.missioncontrol.ui.common.hardware.PlatformItemType;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.validation.CompositeValidator;
import de.saxsys.mvvmfx.utils.validation.ObservableRuleBasedValidator;
import de.saxsys.mvvmfx.utils.validation.ValidationMessage;
import de.saxsys.mvvmfx.utils.validation.ValidationStatus;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import org.apache.commons.lang3.NotImplementedException;
import org.asyncfx.beans.binding.BidirectionalValueConverter;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncListProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionDialogViewModel
        extends DialogViewModel<ConnectionDialogViewModel.Result, ConnectionDialogViewModel.Payload> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionDialogViewModel.class);

    public enum DialogType {
        Create,
        Edit
    }

    public static class Payload {
        private final DialogType dialogType;
        private final MavlinkDroneConnectionItem connectionItem;

        public Payload(DialogType dialogType, IReadOnlyConnectionItem connectionItem) {
            this.dialogType = dialogType;
            if (connectionItem == null || connectionItem instanceof MavlinkDroneConnectionItem) {
                this.connectionItem = (MavlinkDroneConnectionItem)connectionItem;
            } else {
                throw new NotImplementedException(
                    "Connection editing is currently only implemented for mavlink connections");
            }
        }

        public DialogType getDialogType() {
            return dialogType;
        }

        public MavlinkDroneConnectionItem getConnectionItem() {
            return connectionItem;
        }
    }

    public static class Result {
        private final MavlinkDroneConnectionItem connectionItem;

        Result(MavlinkDroneConnectionItem connectionItem) {
            this.connectionItem = connectionItem;
        }

        public MavlinkDroneConnectionItem getConnectionItem() {
            return connectionItem;
        }
    }

    // TODO default host & port could be moved to generic mavlink drone hardware description
    private static final String DefaultHost = "127.0.0.1";
    private static final int TcpDefaultPort = 5760;
    private static final int UdpDefaultPort = 14570;
    private static final int DefaultSystemId = 1;

    private final StringProperty dialogTitle = new SimpleStringProperty();
    private final UIAsyncStringProperty connectionName = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty commitText = new UIAsyncStringProperty(this);
    private final ListProperty<PlatformItem> availablePlatformItems =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private final UIAsyncObjectProperty<TcpIpTransportType> transportType =
        new UIAsyncObjectProperty<>(
            this, new UIPropertyMetadata.Builder<TcpIpTransportType>().initialValue(TcpIpTransportType.TCP).create());
    private final UIAsyncStringProperty host =
        new UIAsyncStringProperty(this, new UIPropertyMetadata.Builder<String>().initialValue(DefaultHost).create());
    private final UIAsyncIntegerProperty port =
        new UIAsyncIntegerProperty(
            this, new UIPropertyMetadata.Builder<Number>().initialValue(TcpDefaultPort).create());
    private final UIAsyncIntegerProperty systemId =
        new UIAsyncIntegerProperty(
            this, new UIPropertyMetadata.Builder<Number>().initialValue(DefaultSystemId).create());

    private final UIAsyncObjectProperty<DialogType> dialogType = new UIAsyncObjectProperty<>(this);
    private final UIAsyncBooleanProperty fixedConnection = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty fixedPlatformItem = new UIAsyncBooleanProperty(this);

    private final UIAsyncObjectProperty<PlatformItem> selectedPlatformItem = new UIAsyncObjectProperty<>(this);
    private final IHardwareConfigurationManager hardwareConfigurationManager;
    private final IDroneConnectionService droneConnectionService;
    private final ILanguageHelper languageHelper;
    private final ConnectionSettings connectionSettings;

    private final UIAsyncListProperty<IReadOnlyConnectionItem> availableDroneConnections =
        new UIAsyncListProperty<>(
            this,
            new UIPropertyMetadata.Builder<AsyncObservableList<IReadOnlyConnectionItem>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private Result result;
    private CompositeValidator formValidator;

    private Command commitCommand;

    @Inject
    public ConnectionDialogViewModel(
            ILanguageHelper languageHelper,
            IHardwareConfigurationManager hardwareConfigurationManager,
            IDroneConnectionService droneConnectionService,
            ISettingsManager settingsManager) {
        this.languageHelper = languageHelper;
        this.hardwareConfigurationManager = hardwareConfigurationManager;
        this.droneConnectionService = droneConnectionService;

        availableDroneConnections.bind(droneConnectionService.availableDroneConnectionItemsProperty());

        connectionSettings = settingsManager.getSection(ConnectionSettings.class);

        updatePlatformItems();
    }

    @Override
    protected void initializeViewModel(ConnectionDialogViewModel.Payload payload) {
        super.initializeViewModel(payload);

        IPlatformDescription defaultPlatformDesc =
            hardwareConfigurationManager.getImmutableDefault().getPlatformDescription();
        String defaultPlatformId = defaultPlatformDesc.getId();

        dialogType.set(payload.getDialogType());
        switch (payload.getDialogType()) {
        case Create:
            String name = ""; // TODO default name //defaultPlatformDesc.getName();
            this.result =
                new Result(
                    new MavlinkDroneConnectionItem(
                        false,
                        true,
                        name,
                        defaultPlatformId,
                        TcpIpTransportType.TCP,
                        DefaultHost,
                        TcpDefaultPort,
                        DefaultSystemId));
            break;
        case Edit:
            var connItem = new MavlinkDroneConnectionItem(payload.connectionItem);
            connItem.isKnownProperty().set(true);
            if (connItem.getPlatformId() == null) {
                connItem.platformIdProperty().set(defaultPlatformId);
            }

            this.result = new Result(connItem);
            break;
        default:
            throw new IllegalArgumentException("invalid DialogType");
        }

        // bind UI elements

        fixedConnection.bind(result.getConnectionItem().isOnlineProperty());
        fixedPlatformItem.bind(
            Bindings.createBooleanBinding(
                () -> droneConnectionService.getConnectedDrone(result.getConnectionItem()) != null,
                droneConnectionService.connectedDroneConnectionItemsProperty()));

        dialogTitle.bind(
            Bindings.createStringBinding(
                () -> {
                    if (dialogType.get() == DialogType.Edit) {
                        return languageHelper.getString(ConnectionDialogViewModel.class, "title.edit");
                    } else {
                        return languageHelper.getString(ConnectionDialogViewModel.class, "title.create");
                    }
                },
                dialogType));

        commitText.bind(
            Bindings.createStringBinding(
                () ->
                    dialogType.getValue() == ConnectionDialogViewModel.DialogType.Edit
                        ? languageHelper.getString(ConnectionDialogViewModel.class, "commit.edit")
                        : languageHelper.getString(ConnectionDialogViewModel.class, "commit.create"),
                dialogType));

        connectionName.bindBidirectional(result.getConnectionItem().nameProperty());
        transportType.bindBidirectional(result.getConnectionItem().transportTypeProperty());
        host.bindBidirectional(result.getConnectionItem().hostProperty());
        port.bindBidirectional(result.getConnectionItem().portProperty());
        systemId.bindBidirectional(result.getConnectionItem().systemIdProperty());

        transportType.addListener(
            (observable, oldValue, newValue) -> {
                if (host.get().equals(DefaultHost)
                        && ((oldValue == TcpIpTransportType.TCP && port.get() == TcpDefaultPort)
                            || (oldValue == TcpIpTransportType.UDP && port.get() == UdpDefaultPort))) {
                    switch (newValue) {
                    case TCP:
                        port.set(TcpDefaultPort);
                        break;
                    case UDP:
                        port.set(UdpDefaultPort);
                        break;
                    }
                }
            });

        BidirectionalValueConverter<String, PlatformItem> converter =
            new BidirectionalValueConverter<>() {
                @Override
                public PlatformItem convert(String value) {
                    Optional<PlatformItem> platformItem =
                        value != null
                            ? availablePlatformItems
                                .stream()
                                .filter(
                                    item ->
                                        item.getDescription() != null && item.getDescription().getId().equals(value))
                                .findFirst()
                            : Optional.empty();
                    if (platformItem.isPresent()) {
                        return platformItem.get();
                    } else {
                        if (result.getConnectionItem().getPlatformId() != null) {
                            LOGGER.error("Connection item model " + value + " not found");
                        }

                        IPlatformDescription defaultPlatformDesc =
                            hardwareConfigurationManager.getImmutableDefault().getPlatformDescription();
                        return new PlatformItem(
                            defaultPlatformDesc.isInFixedWingEditionMode()
                                ? PlatformItemType.FIXED_WING
                                : PlatformItemType.MULTICOPTER,
                            defaultPlatformDesc);
                    }
                }

                @Override
                public String convertBack(PlatformItem value) {
                    return value.getDescription() != null ? value.getDescription().getId() : null;
                }
            };

        selectedPlatformItem.bindBidirectional(result.getConnectionItem().platformIdProperty(), converter);

        // Form validators:
        ObservableRuleBasedValidator connectionNameValidator = new ObservableRuleBasedValidator();
        connectionNameValidator.addRule(connectionName.isNotEmpty(), ValidationMessage.error("Name may not be empty"));
        ObservableRuleBasedValidator hostValidator = new ObservableRuleBasedValidator();
        hostValidator.addRule(host.isNotEmpty(), ValidationMessage.error("Host may not be empty"));
        ObservableRuleBasedValidator portValidator = new ObservableRuleBasedValidator();
        portValidator.addRule(port.isNotEqualTo(0), ValidationMessage.error("Port may not be 0"));
        ObservableRuleBasedValidator systemIdValidator = new ObservableRuleBasedValidator();
        systemIdValidator.addRule(systemId.isNotEqualTo(0), ValidationMessage.error("System ID may not be 0"));
        ObservableRuleBasedValidator droneValidator = new ObservableRuleBasedValidator();
        droneValidator.addRule(selectedPlatformItem.isNotNull(), ValidationMessage.error("Drone must be selected"));
        ObservableRuleBasedValidator duplicateConnectionValidator = new ObservableRuleBasedValidator();
        BooleanProperty duplicateConnection = new SimpleBooleanProperty();
        duplicateConnectionValidator.addRule(
            duplicateConnection.not(), ValidationMessage.error("Duplicate connection"));

        formValidator = new CompositeValidator();
        formValidator.addValidators(
            connectionNameValidator,
            hostValidator,
            portValidator,
            systemIdValidator,
            droneValidator,
            duplicateConnectionValidator);

        duplicateConnection.bind(
            Bindings.createBooleanBinding(
                () -> {
                    // Check if connection settings correspond to any other previous one:
                    try (var lockedList = availableDroneConnections.lock()) {
                        return lockedList
                            .stream()
                            .anyMatch(
                                i ->
                                    !i.isSameConnection(payload.connectionItem)
                                        && i.isSameConnection(result.getConnectionItem()));
                    }
                },
                availableDroneConnections,
                result.getConnectionItem().transportTypeProperty(),
                result.getConnectionItem().hostProperty(),
                result.getConnectionItem().portProperty(),
                result.getConnectionItem().systemIdProperty()));

        commitCommand =
            new DelegateCommand(
                () -> {
                    // Known ConnectionItems originate in the settings manager, so the dialog result is
                    // applied there:
                    switch (payload.getDialogType()) {
                    case Create:
                        connectionSettings.connectionItemsListProperty().add(result.getConnectionItem());
                        break;
                    case Edit:
                        try (LockedList<MavlinkDroneConnectionItem> knownConnections =
                            connectionSettings.connectionItemsListProperty().lock()) {
                            Optional<MavlinkDroneConnectionItem> conn =
                                knownConnections
                                    .stream()
                                    .filter(c -> c.isSameConnection(payload.getConnectionItem()))
                                    .findFirst();
                            if (conn.isPresent()) {
                                conn.get().set(result.getConnectionItem());
                            } else {
                                connectionSettings.connectionItemsListProperty().add(result.getConnectionItem());
                            }
                        }

                        break;
                    default:
                        throw new IllegalArgumentException("invalid DialogType");
                    }

                    setDialogResult(result);
                    getCloseCommand().execute();
                });
    }

    private void updatePlatformItems() {
        List<IPlatformDescription> availablePlatforms = Arrays.asList(hardwareConfigurationManager.getPlatforms());

        List<IPlatformDescription> platforms =
            availablePlatforms
                .stream()
                .filter(desc -> desc.getConnectionType().equals(DroneConnectionType.MAVLINK) || desc.getConnectionType().equals(DroneConnectionType.MOCK))
                .collect(Collectors.toList());

        List<IPlatformDescription> fixedWings =
            platforms
                .stream()
                .filter(IPlatformDescription::isInFixedWingEditionMode)
                .collect(Collectors.toList());
        List<IPlatformDescription> multicopters =
            platforms.stream().filter(desc -> !desc.isInFixedWingEditionMode()).collect(Collectors.toList());

        availablePlatformItems.clear();

        if (!fixedWings.isEmpty()) {
            availablePlatformItems.add(new PlatformItem(PlatformItemType.FIXED_WING));
            for (IPlatformDescription desc : fixedWings) {
                availablePlatformItems.add(new PlatformItem(PlatformItemType.FIXED_WING, desc));
            }
        }

        if (!multicopters.isEmpty()) {
            availablePlatformItems.add(new PlatformItem(PlatformItemType.MULTICOPTER));
            for (IPlatformDescription desc : multicopters) {
                availablePlatformItems.add(new PlatformItem(PlatformItemType.MULTICOPTER, desc));
            }
        }
    }

    ReadOnlyListProperty<PlatformItem> availablePlatformItemsProperty() {
        return availablePlatformItems;
    }

    Property<PlatformItem> selectedPlatformItemProperty() {
        return selectedPlatformItem;
    }

    Property<String> connectionNameProperty() {
        return connectionName;
    }

    Property<TcpIpTransportType> transportTypeProperty() {
        return transportType;
    }

    Property<String> hostProperty() {
        return host;
    }

    Property<Number> portProperty() {
        return port;
    }

    Property<Number> systemIdProperty() {
        return systemId;
    }

    ValidationStatus getFormValidationStatus() {
        return formValidator.getValidationStatus();
    }

    Command getCommitCommand() {
        return commitCommand;
    }

    ReadOnlyProperty<Boolean> fixedConnectionProperty() {
        return fixedConnection;
    }

    ReadOnlyProperty<Boolean> fixedPlatformItemProperty() {
        return fixedPlatformItem;
    }

    ReadOnlyProperty<String> commitTextProperty() {
        return commitText;
    }

    ReadOnlyStringProperty dialogTitleProperty() {
        return dialogTitle;
    }
}
