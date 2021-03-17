/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.linkbox.ILinkBoxConnectionService;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class UAVLockedView extends DialogView<UAVLockedViewModel> {

    private final StringProperty title = new SimpleStringProperty();

    @FXML
    public VBox rootNote;

    @FXML
    private Button cancelButton;

    @FXML
    private Label uavLockedMessage;

    @InjectViewModel
    private UAVLockedViewModel viewModel;

    @InjectContext
    private Context context;

    private ILanguageHelper languageHelper;

    private IDialogContextProvider dialogContextProvider;

    private ILinkBoxConnectionService linkBoxConnectionService;

    @Inject
    public UAVLockedView(
            IDialogContextProvider dialogContextProvider,
            ILanguageHelper languageHelper,
            ILinkBoxConnectionService linkBoxConnectionService) {
        this.dialogContextProvider = dialogContextProvider;
        this.languageHelper = languageHelper;
        this.linkBoxConnectionService = linkBoxConnectionService;
    }

    @Override
    protected Parent getRootNode() {
        return rootNote;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    public void initializeView() {
        super.initializeView();

        dialogContextProvider.setContext(viewModel, context);

        cancelButton.setOnAction(event -> viewModel.getCloseCommand().execute());
        cancelButton.disableProperty().bind(viewModel.getCloseCommand().notExecutableProperty());
        title.setValue(languageHelper.getString(UAVLockedView.class, "title"));
        uavLockedMessage.setText(
            languageHelper.getString(
                UAVLockedView.class,
                "uavLockedMessage",
                this.linkBoxConnectionService.getLinkBox().get().linkBoxNameProperty().get()));
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    public void requestLinkBoxAuthorization(ActionEvent actionEvent) {
        viewModel.getRequestAuthorizationDialogCommand().execute();
    }

    public void onCancelClicked(ActionEvent actionEvent) {
        viewModel.getCloseCommand().execute();
    }

    public Context getContext() {
        return context;
    }
}
