/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan;

import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.property.QuantityBindings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javax.inject.Inject;

public class SelectedFlightplanItemView extends ViewBase<SelectedFlightplanItemViewModel> {

    private final ISettingsManager settingsManager;

    @InjectViewModel
    private SelectedFlightplanItemViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private Label statusLabel;

    @FXML
    private Label flightplanName;

    @FXML
    private Label savedOnLabel;

    @FXML
    private Label flightTimeLabel;

    @FXML
    private Label images;

    @FXML
    private Label waypointsCountLabel;

    @Inject
    public SelectedFlightplanItemView(ISettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    private QuantityFormat quantityFormat;

    @Override
    protected void initializeView() {
        super.initializeView();

        quantityFormat = new AdaptiveQuantityFormat(settingsManager.getSection(GeneralSettings.class));
        quantityFormat.setSignificantDigits(3);
        flightplanName.textProperty().bind(viewModel.getFlightplanName());

        statusLabel.textProperty().bind(viewModel.getStatus());
        savedOnLabel.textProperty().bind(viewModel.getSavedOn());
        flightTimeLabel
            .textProperty()
            .bind(QuantityBindings.createStringBinding(viewModel.flightTimeProperty(), quantityFormat));

        images.textProperty().bind(viewModel.getImages());
        waypointsCountLabel.textProperty().bind(viewModel.getWaypointsCount());
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

}
