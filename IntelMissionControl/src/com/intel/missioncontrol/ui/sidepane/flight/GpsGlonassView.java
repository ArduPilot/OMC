/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class GpsGlonassView extends AlertAwareView<GpsGlonassViewModel> {

    @FXML
    private VBox gpsGlonassBox;

    @FXML
    private Label labelGps;

    @FXML
    private Label labelGlonass;

    @InjectViewModel
    private GpsGlonassViewModel viewModel;

    @Override
    public void initializeView() {
        super.initializeView();

        labelGps.textProperty().bind(viewModel.gpsProperty().asString());
        labelGlonass.textProperty().bind(viewModel.glonassProperty().asString());
    }

    @Override
    protected Parent getRootNode() {
        return gpsGlonassBox;
    }

    @Override
    protected AlertAwareViewModel getViewModel() {
        return viewModel;
    }

}
