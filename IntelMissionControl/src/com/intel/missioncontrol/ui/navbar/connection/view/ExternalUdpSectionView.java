/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.intel.missioncontrol.settings.rtk.RtkUdp;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.ExternalUdpSectionViewModel;
import com.intel.missioncontrol.utils.IntegerValidator;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/** @author Vladimir Iordanov */
public class ExternalUdpSectionView extends ViewBase<ExternalUdpSectionViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    private Label portLabel;

    @FXML
    private Label title;

    @FXML
    private Spinner<Integer> portSpinner;

    @FXML
    private VBox portSettings;

    @InjectViewModel
    private ExternalUdpSectionViewModel viewModel;

    @Override
    public void initializeView() {
        super.initializeView();

        initPortSpinner();

        portLabel.disableProperty().bind(viewModel.isConnected());
        title.disableProperty().bind(viewModel.isConnected());
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public ExternalUdpSectionViewModel getViewModel() {
        return viewModel;
    }

    private void initPortSpinner() {
        final int minDigits = String.valueOf(RtkUdp.UDP_MIN_VALUE).length();
        final int maxDigits = String.valueOf(RtkUdp.UDP_MAX_VALUE).length();
        final int amountToStepBy = 1;

        IntegerValidator validator =
            new IntegerValidator(
                viewModel.getPort(), RtkUdp.UDP_MIN_VALUE, RtkUdp.UDP_MAX_VALUE, minDigits, maxDigits, amountToStepBy);

        SpinnerValueFactory<Integer> valueFactory = validator.getValueFactory();

        portSpinner.setValueFactory(valueFactory);
        portSpinner.getEditor().setTextFormatter(validator.getTextFormatter());

        valueFactory.valueProperty().bindBidirectional(viewModel.portProperty());

        portSpinner.disableProperty().bind(viewModel.isConnected());
    }
}
