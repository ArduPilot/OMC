/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.view;

import org.asyncfx.beans.binding.ConversionBindings;
import org.asyncfx.beans.binding.Converters;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.navbar.settings.viewmodel.InternetConnectivityViewModel;
import com.intel.missioncontrol.utils.IntegerValidator;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class InternetConnectivitySettingsView extends ViewBase<InternetConnectivityViewModel> {

    private static final Integer SPINNER_MIN = 0;
    private static final Integer SPINNER_MAX = 0xFFFF;
    private static final Integer SPINNER_DEFAULT = 0;
    private static final Integer SPINNER_MIN_DIGITS = 1;
    private static final Integer SPINNER_MAX_DIGITS = 5;

    @InjectViewModel
    private InternetConnectivityViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private Label httpLabel;

    @FXML
    private Label ftpLabel;

    @FXML
    private Label socksLabel;

    @FXML
    private Spinner<Integer> httpPortSpinner;

    @FXML
    private TextField httpTextField;

    @FXML
    private Label httpsLabel;

    @FXML
    private Spinner<Integer> httpsPortSpinner;

    @FXML
    private TextField httpsTextField;

    @FXML
    private Spinner<Integer> ftpPortSpinner;

    @FXML
    private TextField ftpTextField;

    @FXML
    private Spinner<Integer> socksPortSpinner;

    @FXML
    private TextField socksTextField;

    @FXML
    private ToggleSwitch useProxyOption;

    @FXML
    private ToggleSwitch autoProxyToggle;

    @FXML
    private GridPane servers;

    @Override
    public void initializeView() {
        super.initializeView();

        initSpinner();

        httpTextField.textProperty().bindBidirectional(viewModel.httpHostProperty());
        httpsTextField.textProperty().bindBidirectional(viewModel.httpsHostProperty());
        ftpTextField.textProperty().bindBidirectional(viewModel.ftpHostProperty());
        socksTextField.textProperty().bindBidirectional(viewModel.socksHostProperty());

        ConversionBindings.bindBidirectional(
            httpPortSpinner.getValueFactory().valueProperty(), viewModel.httpPortProperty(), Converters.numberToInt());

        ConversionBindings.bindBidirectional(
            httpsPortSpinner.getValueFactory().valueProperty(),
            viewModel.httpsPortProperty(),
            Converters.numberToInt());

        ConversionBindings.bindBidirectional(
            ftpPortSpinner.getValueFactory().valueProperty(), viewModel.ftpPortProperty(), Converters.numberToInt());

        ConversionBindings.bindBidirectional(
            socksPortSpinner.getValueFactory().valueProperty(),
            viewModel.socksPortProperty(),
            Converters.numberToInt());

        useProxyOption.selectedProperty().bindBidirectional(viewModel.useProxyProperty());
        autoProxyToggle.selectedProperty().bindBidirectional(viewModel.useAutoProxyProperty());

        useProxyOption.disableProperty().bind(autoProxyToggle.selectedProperty());
        bindAll(
            useProxyOption.selectedProperty().not().or(autoProxyToggle.selectedProperty()),
            httpLabel.disableProperty(),
            httpTextField.disableProperty(),
            httpPortSpinner.disableProperty(),
            httpsLabel.disableProperty(),
            httpsTextField.disableProperty(),
            httpsPortSpinner.disableProperty(),
            ftpLabel.disableProperty(),
            ftpTextField.disableProperty(),
            ftpPortSpinner.disableProperty(),
            socksLabel.disableProperty(),
            socksTextField.disableProperty(),
            socksPortSpinner.disableProperty());

        servers.visibleProperty().bind(useProxyOption.selectedProperty());
        servers.managedProperty().bind(useProxyOption.selectedProperty());
    }


    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public InternetConnectivityViewModel getViewModel() {
        return viewModel;
    }

    private void initSpinner() {
        IntegerValidator httpPortSpinnerValueFactory =
            new IntegerValidator(SPINNER_DEFAULT, SPINNER_MIN, SPINNER_MAX, SPINNER_MIN_DIGITS, SPINNER_MAX_DIGITS);
        IntegerValidator httpsPortSpinnerValueFactory =
            new IntegerValidator(SPINNER_DEFAULT, SPINNER_MIN, SPINNER_MAX, SPINNER_MIN_DIGITS, SPINNER_MAX_DIGITS);
        IntegerValidator ftpPortSpinnerValueFactory =
            new IntegerValidator(SPINNER_DEFAULT, SPINNER_MIN, SPINNER_MAX, SPINNER_MIN_DIGITS, SPINNER_MAX_DIGITS);
        IntegerValidator socksPortSpinnerValueFactory =
            new IntegerValidator(SPINNER_DEFAULT, SPINNER_MIN, SPINNER_MAX, SPINNER_MIN_DIGITS, SPINNER_MAX_DIGITS);

        httpPortSpinner.setValueFactory(httpPortSpinnerValueFactory.getValueFactory());
        httpPortSpinner.setEditable(true);
        httpPortSpinner.getEditor().setTextFormatter(httpPortSpinnerValueFactory.getTextFormatter());

        httpsPortSpinner.setValueFactory(httpsPortSpinnerValueFactory.getValueFactory());
        httpsPortSpinner.setEditable(true);
        httpsPortSpinner.getEditor().setTextFormatter(httpsPortSpinnerValueFactory.getTextFormatter());

        ftpPortSpinner.setValueFactory(ftpPortSpinnerValueFactory.getValueFactory());
        ftpPortSpinner.setEditable(true);
        ftpPortSpinner.getEditor().setTextFormatter(ftpPortSpinnerValueFactory.getTextFormatter());

        socksPortSpinner.setValueFactory(socksPortSpinnerValueFactory.getValueFactory());
        socksPortSpinner.setEditable(true);
        socksPortSpinner.getEditor().setTextFormatter(socksPortSpinnerValueFactory.getTextFormatter());
    }

    private void bindAll(BooleanBinding masterProperty, BooleanProperty... dependentProperties) {
        for (BooleanProperty dependentProperty : dependentProperties) {
            dependentProperty.bind(masterProperty);
        }
    }

}
