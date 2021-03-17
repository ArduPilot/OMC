/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly;

import com.intel.missioncontrol.Localizable;
import com.intel.missioncontrol.drone.FlightSegment;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.connection.ConnectionState;
import com.intel.missioncontrol.drone.connection.DroneConnectionService;
import com.intel.missioncontrol.drone.connection.IDroneConnectionService;
import com.intel.missioncontrol.drone.connection.IReadOnlyConnectionItem;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.layers.aircraft.AircraftLayerVisibilitySettings;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.menu.MenuModel;
import com.intel.missioncontrol.ui.navbar.layers.IMapClearingCenter;
import com.intel.missioncontrol.ui.navbar.settings.connection.dialog.ConnectionDialogViewModel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SettingsPage;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import com.intel.missioncontrol.ui.sidepane.flight.fly.disconnect.DisconnectDialogResult;
import com.intel.missioncontrol.ui.sidepane.flight.fly.disconnect.DisconnectDialogViewModel;
import de.saxsys.mvvmfx.utils.commands.FutureCommand;
import eu.mavinci.core.obfuscation.IKeepAll;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;
import org.asyncfx.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the menu model for the FlyDrone drop-down menu located in the sidepane header on the flight tab. This class
 * also contains business logic for the menu commands.
 */
public class FlyDroneMenuModel extends MenuModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroneConnectionService.class);

    @Localizable
    public enum MenuIds implements IKeepAll {
        CURRENT_CONNECTION,
        EDIT_CONNECTION,
        DISCONNECT
    }

    private final IDialogService dialogService;
    private final IDroneConnectionService droneConnectionService;
    private final INavigationService navigationService;
    private final IMapClearingCenter mapClearingCenter;
    private final AircraftLayerVisibilitySettings aircraftLayerVisibilitySettings;

    private final UIAsyncObjectProperty<FlightSegment> flightSegment = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<IDrone> drone = new UIAsyncObjectProperty<>(this);
    private final UIAsyncStringProperty currentConnectionName = new UIAsyncStringProperty(this);

    private final FlyDroneViewModel flyDroneViewModel;
    private final FlightScope flightScope;

    private final FutureCommand disconnectCommand;

    public FlyDroneMenuModel(
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            IDroneConnectionService droneConnectionService,
            INavigationService navigationService,
            IMapClearingCenter mapClearingCenter,
            AircraftLayerVisibilitySettings aircraftLayerVisibilitySettings,
            FlightScope flightScope,
            FlyDroneViewModel flyDroneViewModel) {
        super(null, false, null, null, null, null);

        this.dialogService = dialogService;
        this.droneConnectionService = droneConnectionService;
        this.navigationService = navigationService;
        this.mapClearingCenter = mapClearingCenter;
        this.aircraftLayerVisibilitySettings = aircraftLayerVisibilitySettings;
        this.flightScope = flightScope;

        this.flyDroneViewModel = flyDroneViewModel;

        setMnemonicParsing(false);

        getChildren()
            .addAll(
                MenuModel.checkGroup(MenuIds.CURRENT_CONNECTION),
                MenuModel.group(
                    MenuModel.item(MenuIds.EDIT_CONNECTION, languageHelper.toFriendlyName(MenuIds.EDIT_CONNECTION)),
                    MenuModel.item(MenuIds.DISCONNECT, languageHelper.toFriendlyName(MenuIds.DISCONNECT))));

        drone.bind(flightScope.currentDroneProperty());
        ObjectProperty<IReadOnlyConnectionItem> currentConnectionMenuItem =
            find(FlyDroneMenuModel.MenuIds.CURRENT_CONNECTION).checkedItemProperty();
        currentConnectionMenuItem.bind(
            Bindings.createObjectBinding(
                () -> drone.get() != null ? droneConnectionService.getConnectionItemForDrone(drone.get()) : null,
                drone));

        currentConnectionName.bind(
            PropertyPath.from(currentConnectionMenuItem)
                .selectReadOnlyAsyncString(IReadOnlyConnectionItem::nameProperty));
        textProperty().bind(currentConnectionName);

        disconnectCommand =
            new FutureCommand(
                () ->
                    dialogService
                        .requestDialogAsync(flyDroneViewModel, DisconnectDialogViewModel.class, true)
                        .whenDone(
                            (v) -> {
                                DisconnectDialogViewModel viewModel = v.getUnchecked();
                                DisconnectDialogResult dialogResult = viewModel.getDialogResult();
                                if (dialogResult != null) {
                                    if (dialogResult.getConfirmed()) {
                                        disconnectAsync();
                                    }
                                }
                            }),
                droneConnectionService.connectionStateProperty().isEqualTo(ConnectionState.CONNECTED));

        find(FlyDroneMenuModel.MenuIds.EDIT_CONNECTION).setActionHandler(this::editCurrentConnection);

        find(MenuIds.DISCONNECT)
            .setActionHandler(
                () -> {
                    if (disconnectCommand.isExecutable()) {
                        disconnectCommand.execute();
                    }
                });
    }

    private void editCurrentConnection() {
        navigationService.navigateTo(SettingsPage.CONNECTION);
        dialogService.requestDialogAsync(
            flyDroneViewModel,
            ConnectionDialogViewModel.class,
            () ->
                new ConnectionDialogViewModel.Payload(
                    ConnectionDialogViewModel.DialogType.Edit,
                    droneConnectionService.getConnectionItemForDrone(drone.get())),
            true);
    }

    private Future<Void> disconnectAsync() {
        return droneConnectionService
            .disconnectAsync(drone.get())
            .whenSucceeded(
                () -> {
                    // TODO: UI response to disconnecting should be handled in only one place
                    flightScope.currentDroneProperty().set(null);
                    mapClearingCenter.clearTrackLog();
                    aircraftLayerVisibilitySettings.model3DProperty().set(false);
                    navigationService.navigateTo(SidePanePage.CONNECT_DRONE);
                },
                Platform::runLater)
            .whenFailed(ex -> LOGGER.warn("Disconnect failed", ex));
    }
}
