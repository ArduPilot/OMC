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
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/** @author Vladimir Iordanov */
public class EmergencyActionView extends AlertAwareView<EmergencyActionViewModel> {

    @FXML
    private VBox layoutRoot;

    @FXML
    private ImageView icon;

    @FXML
    private Label titleText;

    @FXML
    private Label titleDetailsPart1;

    @FXML
    private Label titleDetailsPart2;

    @FXML
    private Label message;

    @InjectViewModel
    private EmergencyActionViewModel viewModel;

    @Override
    public void initializeView() {
        super.initializeView();
        titleDetailsPart2.textProperty().bindBidirectional(viewModel.titleDetailsPart2Property());
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected AlertAwareViewModel getViewModel() {
        return viewModel;
    }
}
