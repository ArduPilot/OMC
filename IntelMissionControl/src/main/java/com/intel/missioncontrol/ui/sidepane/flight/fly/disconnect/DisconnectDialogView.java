/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.disconnect;

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
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class DisconnectDialogView extends DialogView<DisconnectDialogViewModel> {
    @FXML
    private Pane root;

    @FXML
    private Button confirmButton;

    @FXML
    private Label currentDroneName;

    @InjectViewModel
    private DisconnectDialogViewModel viewModel;

    private ILanguageHelper languageHelper;

    @Inject
    public DisconnectDialogView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.sidepane.flight.fly.disconnect.DisconnectDialogView.title"));
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
        currentDroneName.textProperty().set(viewModel.currentConnectionNameProperty().getValue());
    }

    public void OnConfirmDisconnectButtonClicked(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getConfirmCommand().execute();
    }

    public void OnCancelButtonClicked(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getCloseCommand().execute();
    }
}
