/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.rename;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class RenameConnectionDialogView extends DialogView<RenameConnectionDialogViewModel> {
    @FXML
    private Pane root;

    @FXML
    private Text newString;

    @FXML
    private Button confirmButton;

    @InjectViewModel
    private RenameConnectionDialogViewModel viewModel;

    private ILanguageHelper languageHelper;

    @Inject
    public RenameConnectionDialogView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.sidepane.flight.fly.startPlan.StartPlanDialogView.title"));
    }

    @Override
    protected Parent getRootNode() {
        return root;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        //        confirmButton.disableProperty().bind(viewModel.formValidation().validProperty().not());
    }

    public void OnConfirmTakeoffButtonClicked(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getConfirmCommand().execute();
    }

    public void OnCancelButtonClicked(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getCloseCommand().execute();
    }
}
