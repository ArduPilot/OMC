/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.connection.InternalStationType;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.InternalBaseStationViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/** @author Vladimir Iordanov */
public class InternalBaseStationView extends ViewBase<InternalBaseStationViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    private VBox intelViewContainer;

    @FXML
    private VBox mavinciViewContainer;

    @FXML
    private ComboBox<InternalStationType> internalStationTypeComboBox;

    @InjectViewModel
    private InternalBaseStationViewModel viewModel;

    @Override
    public void initializeView() {
        super.initializeView();

        initInternalStationCombo();

        mavinciViewContainer
            .visibleProperty()
            .bind(
                internalStationTypeComboBox
                    .getSelectionModel()
                    .selectedItemProperty()
                    .isEqualTo(InternalStationType.MAVINCI));
        intelViewContainer
            .visibleProperty()
            .bind(
                internalStationTypeComboBox
                    .getSelectionModel()
                    .selectedItemProperty()
                    .isEqualTo(InternalStationType.INTEL));
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public InternalBaseStationViewModel getViewModel() {
        return viewModel;
    }

    private void initInternalStationCombo() {
        internalStationTypeComboBox.setCellFactory(
            view ->
                new ListCell<InternalStationType>() {

                    @Override
                    protected void updateItem(InternalStationType item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setId(item.name());
                            setText(item.toString());
                            setDisable(item == InternalStationType.INTEL);
                        }
                    }
                });

        internalStationTypeComboBox.itemsProperty().set(viewModel.itemsProperty());
        internalStationTypeComboBox.getSelectionModel().select(viewModel.getSelectedView());
        viewModel.selectedViewProperty().bind(internalStationTypeComboBox.getSelectionModel().selectedItemProperty());

        internalStationTypeComboBox.disableProperty().bind(viewModel.isConnectedBinding());
    }

}
