/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.connect;

import com.google.inject.Inject;
import com.intel.missioncontrol.drone.connection.IConnectionItem;
import com.intel.missioncontrol.drone.connection.IReadOnlyConnectionItem;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.Animations;
import com.intel.missioncontrol.ui.controls.ActivityButton;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.sidepane.FancyTabView;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;

public class ConnectDroneView extends FancyTabView<ConnectDroneViewModel> {

    @InjectContext
    private Context context;

    @InjectViewModel
    private ConnectDroneViewModel viewModel;

    @FXML
    private Label projectNameLabel;

    @FXML
    private ImageView waitingForIncomingConnectionsBusyIndicator;

    @FXML
    private Button showHelpButton;

    @FXML
    private Button settingsButton;

    @FXML
    private ComboBox<IReadOnlyConnectionItem> availableConnectionsComboBox;

    @FXML
    private ActivityButton connectToDroneButton;

    @FXML
    private ActivityButton simulateButton;

    private final ILanguageHelper languageHelper;
    private final IDialogContextProvider dialogContextProvider;

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Inject
    public ConnectDroneView(ILanguageHelper languageHelper, IDialogContextProvider dialogContextProvider) {
        this.dialogContextProvider = dialogContextProvider;
        this.languageHelper = languageHelper;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        dialogContextProvider.setContext(viewModel, context);

        projectNameLabel.textProperty().bind(viewModel.missionNameProperty());
        Animations.spinForever(waitingForIncomingConnectionsBusyIndicator);

        availableConnectionsComboBox.setConverter(
            new StringConverter<>() {
                @Override
                public String toString(IReadOnlyConnectionItem connItem) {
                    if (connItem == null) {
                        return languageHelper.getString(ConnectDroneView.class, "pleaseSelect");
                    } else {
                        String name = connItem.getName();
                        if (connItem.getDescriptionId() == null) {
                            return languageHelper.getString(ConnectDroneView.class, "connectionName.undefinedType", name);
                        } else if (connItem.isOnline()) {
                            return languageHelper.getString(ConnectDroneView.class, "connectionName.online", name);
                        }

                        return languageHelper.getString(ConnectDroneView.class, "connectionName.offline", name);
                    }
                }

                @Override
                public IConnectionItem fromString(String string) {
                    throw new UnsupportedOperationException();
                }
            });

        availableConnectionsComboBox.itemsProperty().bind(viewModel.availableDroneConnectionItemsProperty());
        availableConnectionsComboBox.valueProperty().bindBidirectional(viewModel.selectedConnectionItemProperty());

        showHelpButton.disableProperty().bind(viewModel.getShowHelpCommand().notExecutableProperty());
        showHelpButton.setOnAction(event -> viewModel.getShowHelpCommand().execute());

        settingsButton.setOnAction(event -> viewModel.getGoToDroneConnectionSettingsCommand().execute());

        connectToDroneButton.disableProperty().bind(viewModel.getConnectToDroneCommand().notExecutableProperty());
        connectToDroneButton.isBusyProperty().bind(viewModel.getConnectToDroneCommand().runningProperty());

        simulateButton.disableProperty().bind(viewModel.getSimulateCommand().notExecutableProperty());
        simulateButton.isBusyProperty().bind(viewModel.getSimulateCommand().runningProperty());
        simulateButton.managedProperty().set(false);
        simulateButton.visibleProperty().set(false);
    }

    public void OnSimulate() {
        viewModel.getSimulateCommand().execute();
    }

    public void OnConnectToDrone() {
        viewModel.getConnectToDroneCommand().executeAsync();
    }

    @FXML
    public void renameClicked() {
        viewModel.getRenameMissionCommand().execute();
    }
}
