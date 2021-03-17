/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.warnings;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class WarningsDialogView extends ViewBase<WarningsDialogViewModel> {
    private static final double WARNINGS_PARAMETERS_STAGE_WIDTH = ScaleHelper.emsToPixels(31);

    @FXML
    private Pane rootNode;

    @FXML
    public VBox warnings;

    @FXML
    public Label warningsLabel;

    @FXML
    public CheckBox confirmationCheckbox;

    @FXML
    public Button proceedBtn;

    @FXML
    public Button cancelBtn;

    @InjectViewModel
    private WarningsDialogViewModel viewModel;

    private static final String BULLET_SYMB = "  \u2022  ";

    @Override
    public void initializeView() {
        super.initializeView();

        proceedBtn.disableProperty().bind(confirmationCheckbox.selectedProperty().not());
        setWarnings(viewModel.warningsProperty());
        viewModel
            .warningsProperty()
            .addListener(
                (ListChangeListener<ResolvableValidationMessage>)
                    l -> {
                        setWarnings(l.getList());
                    });
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public WarningsDialogViewModel getViewModel() {
        return viewModel;
    }

    private void setWarnings(ObservableList<? extends ResolvableValidationMessage> listWarnings) {
        warnings.getChildren().clear();
        listWarnings.forEach(
            message -> {
                Text label = new Text(BULLET_SYMB + message.getMessage());
                label.setWrappingWidth(WARNINGS_PARAMETERS_STAGE_WIDTH);
                warnings.getChildren().add(label);
            });
    }
}
