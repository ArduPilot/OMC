/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import com.intel.missioncontrol.helper.ScaleHelper;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class IconAndLabelView extends AlertAwareView<IconAndLabelViewModel> {

    @FXML
    private VBox iconAndLabelBox;

    @FXML
    private Label labelCaption;

    @FXML
    private ImageView image;

    @InjectViewModel
    private IconAndLabelViewModel viewModel;

    @Override
    public void initializeView() {
        super.initializeView();

        labelCaption.textProperty().bind(viewModel.textProperty());
        image.imageProperty().bind(viewModel.imageProperty());
        image.rotateProperty().bind(viewModel.imageRotateProperty());
        image.setFitHeight(ScaleHelper.emsToPixels(1.5));
        image.setFitWidth(ScaleHelper.emsToPixels(1.5));
    }

    @Override
    protected Parent getRootNode() {
        return iconAndLabelBox;
    }

    @Override
    protected AlertAwareViewModel getViewModel() {
        return viewModel;
    }

}
