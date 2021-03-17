/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.connection;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import com.intel.missioncontrol.ui.navbar.connection.model.ConnectionDeviceType;
import com.intel.missioncontrol.ui.navbar.connection.model.ConnectionTransportType;
import com.intel.missioncontrol.utils.IntegerValidator;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

public class ConnectionDialogView extends DialogView<ConnectionDialogViewModel> {

    @FXML
    private Pane root;

    @FXML
    private TextField connectionName;

    @FXML
    private ComboBox<ConnectionDeviceType> typeOfDeviceComboBox;

    @FXML
    private ToggleGroup transportToggleGroup;

    @FXML
    private RadioButton udpRadio;

    @FXML
    private RadioButton tcpRadio;

    @FXML
    private Spinner<Integer> portSpinner;

    @FXML
    private TextField hostTextField;

    @FXML
    private CheckBox connectNowBox;

    @InjectViewModel
    private ConnectionDialogViewModel viewModel;

    private ILanguageHelper languageHelper;
    private Property<Integer> portIntegerProperty;

    @Inject
    public ConnectionDialogView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }
    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString("com.intel.missioncontrol.ui.navbar.connection.view.uavConnectView.addConnection"));
    }

    @Override
    protected Parent getRootNode() {
        return root;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        connectionName.textProperty().bindBidirectional(viewModel.connectionNameProperty());
        typeOfDeviceComboBox.itemsProperty().bind(viewModel.connectionDeviceTypesListProperty());
        typeOfDeviceComboBox.valueProperty().bindBidirectional(viewModel.connectionDeviceTypeObjectProperty());

        initTransportTypesToggle();
        hostTextField.textProperty().bindBidirectional(viewModel.hostProperty());
        IntegerValidator portSpinnerValueFactory =
                new IntegerValidator(0, 0, 99999, 1, 5);
        portSpinner.setValueFactory(portSpinnerValueFactory.getValueFactory());
        portSpinner.getValueFactory().valueProperty().bindBidirectional(viewModel.portProperty());
        connectNowBox.selectedProperty().bindBidirectional(viewModel.connectNowProperty());
    }

    public void OnAddButtonClicked(ActionEvent actionEvent) {
        viewModel.onAdd();
    }

    public void OnCancelButtonClicked(ActionEvent actionEvent) {
        viewModel.getCloseCommand().execute();
    }

    private void initTransportTypesToggle() {
        udpRadio.setUserData(ConnectionTransportType.UDP);
        tcpRadio.setUserData(ConnectionTransportType.TCP);
        transportToggleGroup
                .selectedToggleProperty()
                .addListener(
                        (observable, oldValue, newValue) -> viewModel.transportTypeObjectProperty().set((ConnectionTransportType)newValue.getUserData()));

        viewModel
                .transportTypeObjectProperty()
                .addListener(
                        (observable, oldValue, newValue) -> selectTransportToggle(newValue));

        selectTransportToggle(viewModel.transportTypeObjectProperty().get());
    }

    private void selectTransportToggle(ConnectionTransportType connectionTransportType) {
        transportToggleGroup
                .getToggles()
                .stream()
                .filter(t -> t.getUserData().equals(connectionTransportType))
                .findAny()
                .ifPresent(t -> t.setSelected(true));
    }
}
