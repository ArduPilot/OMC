/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.common.BindingUtils;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.RtkConfigurationViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.widget.ProgressButton;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.helper.Pair;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

import java.io.File;

/** @author Vladimir Iordanov */
public class RtkConfigurationView extends ViewBase<RtkConfigurationViewModel> {

    @FXML
    private Pane layoutRoot;

    @FXML
    private Hyperlink rtkConfigCancel;

    @FXML
    private Button rtkConfigAdvancedButton;

    @FXML
    private ProgressButton rtkConfigSendButton;

    @FXML
    private Button rtkConfigBrowseButton;

    @FXML
    private ComboBox<Pair<String, File>> rtkConfigs;

    @InjectViewModel
    private RtkConfigurationViewModel viewModel;

    @Override
    protected void initializeView() {
        super.initializeView();
        initCombobox();

        rtkConfigBrowseButton.setOnAction(
            event -> {
                viewModel.getBrowseCommand().execute();
            });

        rtkConfigSendButton.stateProperty().bind(viewModel.sendStateProperty());
        rtkConfigSendButton.setOnMouseClickedHandler(
            event -> {
                viewModel.getSendCommand().execute();
            });

        rtkConfigSendButton.setPrimary(false);

        BindingUtils.bindVisibility(
            rtkConfigCancel, viewModel.sendStateProperty().isEqualTo(ProgressButton.State.IN_PROGRESS));
        rtkConfigCancel.setOnAction(
            event -> {
                viewModel.getSendCancelCommand().execute();
            });

        // Disabled until INMAV-2467 is ready
        rtkConfigAdvancedButton.setVisible(false);
        rtkConfigAdvancedButton.setManaged(false);
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected RtkConfigurationViewModel getViewModel() {
        return viewModel;
    }

    private void initCombobox() {
        rtkConfigs.itemsProperty().bindBidirectional(viewModel.rtkConfigsProperty());
        rtkConfigs.valueProperty().bindBidirectional(viewModel.selectedConfigProperty());
        rtkConfigs.setCellFactory(
            param ->
                new ListCell<Pair<String, File>>() {
                    @Override
                    protected void updateItem(Pair<String, File> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.first);
                        }
                    }
                });
        rtkConfigs.setConverter(
            new StringConverter<Pair<String, File>>() {
                @Override
                public String toString(Pair<String, File> object) {
                    return object.first;
                }

                @Override
                public Pair<String, File> fromString(String string) {
                    return null;
                }
            });
    }
}
