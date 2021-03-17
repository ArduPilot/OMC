/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.NtripConnectionViewModel;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionSettings;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListCell;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class NtripConnectionView extends ViewBase<NtripConnectionViewModel> {

    @InjectViewModel
    private NtripConnectionViewModel viewModel;

    @InjectContext
    private Context context;

    @Inject
    private IDialogContextProvider dialogContextProvider;

    @FXML
    private Pane layoutRoot;

    @FXML
    private TitledPane connectionPane;

    @FXML
    private VBox connectionContent;

    @FXML
    private ComboBox<NtripConnectionSettings> connection;

    @FXML
    private Hyperlink editConnection;

    @Override
    protected void initializeView() {
        super.initializeView();

        dialogContextProvider.setContext(viewModel, context);

        connectionPane.collapsibleProperty().bind(viewModel.isConnectedProperty());
        connectionPane.expandedProperty().bindBidirectional(viewModel.connectionPaneExpandedProperty());
        connectionPane.animatedProperty().bind(viewModel.connectionPaneAnimationProperty());
        connectionContent.disableProperty().bind(viewModel.isConnectedProperty());

        connection.itemsProperty().bind(viewModel.connectionListProperty());
        connection.setCellFactory(
            view ->
                new ListCell<NtripConnectionSettings>() {
                    @Override
                    protected void updateItem(NtripConnectionSettings item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.toString());
                            setId(item.getStreamAsString());
                        } else {
                            setId("prompt");
                        }
                    }
                });
        connection.valueProperty().bindBidirectional(viewModel.connectionSettingProperty());
        editConnection.disableProperty().bind(connection.valueProperty().isNull());
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected NtripConnectionViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    public void createNewNtripConnection() {
        viewModel.openConnectionCreation();
    }

    @FXML
    public void editNtripConnection() {
        viewModel.openConnectionEdit();
    }
}
