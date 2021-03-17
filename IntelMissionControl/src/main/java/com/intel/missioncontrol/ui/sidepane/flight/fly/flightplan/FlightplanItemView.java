/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan;

import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.controls.RadioButton;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public class FlightplanItemView extends RadioButton implements FxmlView<FlightplanItemViewModel> {

    @InjectViewModel
    private FlightplanItemViewModel viewModel;

    @FXML
    private RadioButton layoutRoot;

    @FXML
    private Button editButton;

    @FXML
    private Label flightPlanNameLabel;

    @FXML
    private Tooltip flightPlanNameTooltip;

    @FXML
    private Label activeFlightPlanString;

    public void initialize() {
        layoutRoot.getStyleClass().remove("radio-button");
        layoutRoot.getStyleClass().add("table-row-cell");

        flightPlanNameLabel.textProperty().bind(viewModel.getFlightplanName());
        flightPlanNameTooltip.textProperty().bind(viewModel.getFlightplanName());
        activeFlightPlanString.visibleProperty().bind(viewModel.isActiveFlightplan());
        activeFlightPlanString.managedProperty().bind(activeFlightPlanString.visibleProperty());
        layoutRoot.selectedProperty().set(viewModel.isSelectedFlightplan().get());
        editButton.visibleProperty().bind(layoutRoot.selectedProperty());
        editButton.managedProperty().bind(editButton.visibleProperty());

        viewModel
            .isSelectedFlightplan()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (oldValue != newValue) {
                        layoutRoot.selectedProperty().set(newValue);
                    }
                });
        layoutRoot
            .selectedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (oldValue != newValue) {
                        if (newValue) {
                            viewModel.getSelectedFlightPlan().set(viewModel.getFlightPlan());
                        } else if (!newValue && viewModel.isSelectedFlightplan().get()) {
                            layoutRoot.selectedProperty().set(true);
                        }
                    }
                });
    }

    @FXML
    public void onEditClicked() {
        viewModel.getEditCommand().executeAsync();
    }
}
