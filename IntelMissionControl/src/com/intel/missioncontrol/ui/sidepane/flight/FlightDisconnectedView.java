/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.ui.Animations;
import com.intel.missioncontrol.ui.controls.ActivityButton;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.sidepane.FancyTabView;
import com.intel.missioncontrol.utils.IntegerValidator;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FlightDisconnectedView extends FancyTabView<FlightDisconnectedViewModel> {

    public static final Integer SPINNER_MIN = 0;

    public static final Integer SPINNER_MAX = 99999;

    public static final Integer SPINNER_DEFAULT = 0;

    public static final Integer SPINNER_MIN_DIGITS = 1;

    public static final Integer SPINNER_MAX_DIGITS = 5;

    @InjectContext
    private Context context;
    
    @InjectViewModel
    private FlightDisconnectedViewModel viewModel;

    @FXML
    private Label projectNameLabel;

    @FXML
    private Label connectionStatus;

    @FXML
    private ImageView searchingInProgress;

    @FXML
    private Label discoveringMessage;

    @FXML
    private HBox discoveringMessageBox;

    @FXML
    private Button settingButton;

    @FXML
    private ComboBox<String> connectToUAVComboBox;

    @FXML
    private Button connectToUAVPlus;

    @FXML
    private ActivityButton connectViaIDL;

    @FXML
    private ActivityButton simulate;

    @FXML
    private VBox mavlinkTypeConfig;

    @FXML
    private TextField mavlinkConnectionName;

    @FXML
    private ComboBox mavlinkProtocolTypeCombo;

    @FXML
    private TextField hostTextField;

    @FXML
    private Spinner<Integer> portSpinner;

    @FXML
    private Button addButton;

    private final LanguageHelper languageHelper;
    private IDialogContextProvider dialogContextProvider;
    private Property<Integer> mavlinkIntegerProperty;

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Inject
    public FlightDisconnectedView(IDialogContextProvider dialogContextProvider, LanguageHelper languageHelper) {
        this.dialogContextProvider = dialogContextProvider;
        this.languageHelper = languageHelper;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        dialogContextProvider.setContext(viewModel, context);
        IntegerValidator portSpinnerValueFactory =
            new IntegerValidator(SPINNER_DEFAULT, SPINNER_MIN, SPINNER_MAX, SPINNER_MIN_DIGITS, SPINNER_MAX_DIGITS);

        projectNameLabel.textProperty().bind(viewModel.missionNameProperty());
        connectionStatus.textProperty().bind(viewModel.getConnectionStatusProperty());
        Animations.spinForever(searchingInProgress);
        discoveringMessage.textProperty().bind(viewModel.getDiscoveringMessageProperty());
        settingButton.setOnAction(event -> viewModel.goToSettings());

        connectToUAVComboBox.itemsProperty().bindBidirectional(viewModel.listOfUavProperty());
        connectToUAVComboBox.valueProperty().bindBidirectional(viewModel.currentUavProperty());
        mavlinkConnectionName.textProperty().bindBidirectional(viewModel.mavlinkConnectionNameProperty());
        mavlinkProtocolTypeCombo.itemsProperty().bind(viewModel.mavlinkProtocolTypeProperty());
        mavlinkProtocolTypeCombo.valueProperty().bind(viewModel.mavlinkProtocolSelectedProperty());
        hostTextField.textProperty().bindBidirectional(viewModel.mavlinkIpAddressProperty());
        mavlinkIntegerProperty = viewModel.mavlinkPortProperty().asObject();
        portSpinner.setValueFactory(portSpinnerValueFactory.getValueFactory());
        portSpinner.getValueFactory().valueProperty().bindBidirectional(mavlinkIntegerProperty);
        addButton.setOnAction(
            event -> {
                mavlinkTypeConfig.setVisible(false);
                viewModel.addMavlinkUAV();
            });
        //connectToUAVPlus.visibleProperty().bind(viewModel.isMavlinkTypeProperty());
        connectToUAVPlus.setOnAction(event -> viewModel.getAddConnectionCommand().execute());
    }

    private void hideConnectionMessageHBox(boolean visible) {
        if (visible) {
            discoveringMessageBox.setMinHeight(25);
            discoveringMessageBox.setPrefHeight(25);
            discoveringMessageBox.setMinHeight(25);
        } else {
            discoveringMessageBox.setMaxHeight(0);
            discoveringMessageBox.setPrefHeight(0);
            discoveringMessageBox.setMinHeight(0);
        }

        searchingInProgress.setVisible(visible);
        discoveringMessage.setVisible(visible);
    }

    public void OnSimulate(ActionEvent actionEvent) {
        viewModel.getSimulateCommand().execute();
    }

    public void OnConnectViaIDL(ActionEvent actionEvent) {
        viewModel.getConnectViaIDLCommand().execute();
    }

    @FXML
    public void renameClicked() {
        viewModel.getRenameMissionCommand().execute();
    }
}