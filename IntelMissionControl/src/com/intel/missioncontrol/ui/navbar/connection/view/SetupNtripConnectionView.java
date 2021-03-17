/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.common.BindingUtils;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.navbar.connection.RtkConnectionSetupState;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.SetupNtripConnectionViewModel;
import com.intel.missioncontrol.utils.IntegerValidator;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripSourceStr;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class SetupNtripConnectionView extends DialogView<SetupNtripConnectionViewModel> {

    @InjectViewModel
    private SetupNtripConnectionViewModel viewModel;

    @InjectContext
    private Context context;

    @Inject
    private IDialogContextProvider dialogContextProvider;

    @Inject
    private ILanguageHelper languageHelper;

    @FXML
    private Pane layoutRoot;

    @FXML
    private TextField host;

    @FXML
    private Spinner<Integer> port;

    @FXML
    private ToggleSwitch https;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button refreshStreams;

    @FXML
    private TableView<NtripSourceStr> streams;

    @FXML
    private Button create;

    @FXML
    private Button edit;

    @FXML
    private Button delete;

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString("com.intel.missioncontrol.ui.connection.view.SetupNtripConnectionView.title"));
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        dialogContextProvider.setContext(viewModel, context);

        streams.itemsProperty().bind(viewModel.ntripStreamsProperty());
        streams.getSelectionModel()
            .selectedItemProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        viewModel.streamProperty().set(newValue);
                    }
                });
        if (viewModel.rtkNtripConnectionSetupStateProperty().get() == RtkConnectionSetupState.EDIT) {
            NtripSourceStr sourceStr = viewModel.selectedConnectionProperty().get().getStream();
            streams.scrollTo(sourceStr);
            streams.getSelectionModel().select(sourceStr);
        }

        streams.disableProperty().bind(viewModel.streamRefreshInProgressProperty());

        host.textProperty().bindBidirectional(viewModel.hostProperty());

        IntegerValidator spinnerValidator = new IntegerValidator(viewModel.portProperty().get(), 1, 65536, 1, 5);

        port.setValueFactory(spinnerValidator.getValueFactory());
        port.setEditable(true);
        port.getEditor().setTextFormatter(spinnerValidator.getTextFormatter());
        port.getValueFactory().valueProperty().bindBidirectional(viewModel.portProperty());

        https.selectedProperty().bindBidirectional(viewModel.httpsProperty());

        username.textProperty().bindBidirectional(viewModel.usernameProperty());
        password.textProperty().bindBidirectional(viewModel.passwordProperty());

        refreshStreams.setOnMouseClicked(e -> viewModel.getRefreshStreamsCommand().execute());
        refreshStreams.disableProperty().bind(viewModel.refreshStreamEnableProperty().not());

        BindingUtils.bindVisibility(
            create, viewModel.rtkNtripConnectionSetupStateProperty().isEqualTo(RtkConnectionSetupState.NEW));
        create.disableProperty().bind(viewModel.okButtonDisableProperty());
        create.setOnMouseClicked(viewModel::handleCreate);

        BindingUtils.bindVisibility(
            edit, viewModel.rtkNtripConnectionSetupStateProperty().isEqualTo(RtkConnectionSetupState.EDIT));
        edit.disableProperty().bind(viewModel.okButtonDisableProperty());
        edit.setOnMouseClicked(viewModel::handleEdit);

        delete.visibleProperty()
            .bind(viewModel.rtkNtripConnectionSetupStateProperty().isEqualTo(RtkConnectionSetupState.EDIT));
        delete.setOnMouseClicked(viewModel::handleDelete);
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected SetupNtripConnectionViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    public void cancelAction(ActionEvent event) {
        viewModel.getCloseCommand().execute();
    }
}
