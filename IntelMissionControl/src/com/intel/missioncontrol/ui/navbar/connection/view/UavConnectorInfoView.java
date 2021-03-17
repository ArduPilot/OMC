/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.UavConnectorInfoViewModel;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.UavDataKey;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.layout.Pane;

public class UavConnectorInfoView extends ViewBase<UavConnectorInfoViewModel> {

    @FXML
    private Parent rootNode;

    @FXML
    private Label serialNumberValue;

    @FXML
    private Label hardwareRevisionValue;

    @FXML
    private Label hardwareTypeValue;

    @FXML
    private Label softwareRevisionValue;

    @FXML
    private Label protocolVersionValue;

    @InjectViewModel
    private UavConnectorInfoViewModel viewModel;

    @Override
    public void initializeView() {
        super.initializeView();

        serialNumberValue.textProperty().bind(viewModel.uavDataProperty().valueAt(UavDataKey.SERIAL_NUMBER));

        hardwareRevisionValue.textProperty().bind(viewModel.uavDataProperty().valueAt(UavDataKey.HARDWARE_REVISION));

        hardwareTypeValue.textProperty().bind(viewModel.uavDataProperty().valueAt(UavDataKey.HARDWARE_TYPE));

        softwareRevisionValue.textProperty().bind(viewModel.uavDataProperty().valueAt(UavDataKey.SOFTWARE_REVISION));

        protocolVersionValue.textProperty().bind(viewModel.uavDataProperty().valueAt(UavDataKey.PROTOCOL_VERSION));
    }

    @Override
    public Parent getRootNode() {
        return rootNode;
    }

    @Override
    public UavConnectorInfoViewModel getViewModel() {
        return viewModel;
    }

}
