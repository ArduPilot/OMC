/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.launch.confirm;

import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.plane.AirplaneType;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.net.URL;
import java.util.ResourceBundle;

public class LaunchConfirmView extends ViewBase<LaunchConfirmationViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    public Region falconInstructions;

    @FXML
    public Region siriusInstructions;

    @InjectViewModel
    private LaunchConfirmationViewModel viewModel;

    private ChangeListener<AirplaneType> airplaneTypeListener =
        (observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            if (newValue.isSirius()) {
                falconInstructions.setVisible(false);
                siriusInstructions.setVisible(true);
            } else {
                falconInstructions.setVisible(true);
                siriusInstructions.setVisible(false);
            }
        };

    public void onLaunchConfirm() {
        viewModel.confirmLaunch();
    }

    @Override
    public void initializeView() {
        super.initializeView();

        if (falconInstructions != null && siriusInstructions != null) {
            viewModel.airplaneTypeProperty().addListener(airplaneTypeListener);
        }
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public LaunchConfirmationViewModel getViewModel() {
        return viewModel;
    }

}
