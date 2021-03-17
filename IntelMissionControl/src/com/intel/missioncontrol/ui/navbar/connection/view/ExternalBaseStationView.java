/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.ExternalBaseStationViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import static com.intel.missioncontrol.ui.common.BindingUtils.bindVisibility;

/** @author Vladimir Iordanov */
public class ExternalBaseStationView extends ViewBase<ExternalBaseStationViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    private VBox settingsBluetooth;

    @FXML
    private VBox settingsUdp;

    @FXML
    private VBox settingsRs232;

    @FXML
    private ToggleGroup connectionTypeGroup;

    @FXML
    private HBox connectionContainer;

    @InjectViewModel
    private ExternalBaseStationViewModel viewModel;

    public enum ExternalConnetionType {
        RS232("Rs232"),
        UDP("UDP"),
        BLUETOOTH("Bluetooth");

        private final String label;

        ExternalConnetionType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    @Override
    public void initializeView() {
        super.initializeView();

        initConnectionTypeSection();

        bindVisibility(settingsRs232, viewModel.currentSectionProperty().isEqualTo(ExternalConnetionType.RS232));
        bindVisibility(settingsUdp, viewModel.currentSectionProperty().isEqualTo(ExternalConnetionType.UDP));
        bindVisibility(
            settingsBluetooth, viewModel.currentSectionProperty().isEqualTo(ExternalConnetionType.BLUETOOTH));
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public ExternalBaseStationViewModel getViewModel() {
        return viewModel;
    }

    private void initConnectionTypeSection() {
        connectionContainer.getChildren().clear();

        boolean firstButton = true;

        for (ExternalConnetionType connetionType : ExternalConnetionType.values()) {
            RadioButton radioButton = new RadioButton(connetionType.getLabel());
            radioButton.setId("connectionTypeGroup");
            radioButton.getStyleClass().remove("radio-button");
            radioButton.getStyleClass().addAll("toggle-button");
            if (firstButton) radioButton.getStyleClass().addAll("first");
            radioButton.setUserData(connetionType);
            radioButton.setToggleGroup(connectionTypeGroup);
            connectionContainer.getChildren().add(radioButton);
            radioButton.setSelected(connetionType == viewModel.getCurrentSection());
            firstButton = false;
        }

        connectionTypeGroup
            .selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        viewModel.currentSectionProperty().setValue((ExternalConnetionType)newValue.getUserData());
                    }
                });
    }

}
