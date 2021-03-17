/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.ShortUavInfoViewModel;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.UavDataKey;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ShortUavInfoView extends ViewBase<ShortUavInfoViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    private Label longitudeValue;

    @FXML
    private Label latitudeValue;

    @FXML
    private Label altitudeValue;

    @FXML
    private Label numberOfSatellitesValue;

    @FXML
    private Label connectorBatteryValue;

    @FXML
    private ProgressBar updateProgress;

    @InjectViewModel
    private ShortUavInfoViewModel viewModel;

    @Override
    public void initializeView() {
        super.initializeView();

        longitudeValue.textProperty().bind(viewModel.uavDataProperty().valueAt(UavDataKey.LONGITUDE));
        latitudeValue.textProperty().bind(viewModel.uavDataProperty().valueAt(UavDataKey.LATITUDE));
        altitudeValue.textProperty().bind(viewModel.uavDataProperty().valueAt(UavDataKey.ALTITUDE));
        numberOfSatellitesValue
            .textProperty()
            .bind(viewModel.uavDataProperty().valueAt(UavDataKey.NUMBER_OF_SATELLITES));
        connectorBatteryValue.textProperty().bind(viewModel.uavDataProperty().valueAt(UavDataKey.CONNECTOR_BATTERY));
        updateProgress.progressProperty().bind(viewModel.getUpdateParametersProgress());
        /*        viewModel.selectedUavProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null){
                    connectorBatteryLabelBox.setVisible(!newValue.model.isFalcon());
                    connectorBatteryTextBox.setVisible(!newValue.model.isFalcon());
        }
            });*/
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public ShortUavInfoViewModel getViewModel() {
        return viewModel;
    }

}
