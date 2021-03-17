/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.map;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.layout.Pane;

/** @author Vladimir Iordanov */
public class GpsLostEmergencyInstructionsView extends ViewBase<GpsLostEmergencyInstructionsViewModel> {

    public static final double ALERT_ICON_WIDTH_EM = 4;

    @FXML
    private Pane rootNode;

    @FXML
    public Label messageText;

    @FXML
    public ImageView alertImage;

    @FXML
    private ImageView closeButton;

    @InjectViewModel
    private GpsLostEmergencyInstructionsViewModel viewModel;

    @Override
    public void initializeView() {
        super.initializeView();
        alertImage.setFitWidth(ScaleHelper.emsToPixels(ALERT_ICON_WIDTH_EM));
        closeButton.setOnMouseClicked((event) -> viewModel.closeDelegateProperty().getValue().execute());
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public GpsLostEmergencyInstructionsViewModel getViewModel() {
        return viewModel;
    }

    public Label getMessageText() {
        return messageText;
    }

    public ImageView getAlertImage() {
        return alertImage;
    }

    public ImageView getCloseButton() {
        return closeButton;
    }
}
