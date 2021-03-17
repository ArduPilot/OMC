/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.controls.ActivityToggleSwitch;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class ObstacleAvoidanceTelemetryView extends DialogView<ObstacleAvoidanceTelemetryViewModel> {

    private final StringProperty title = new SimpleStringProperty();
    private final ILanguageHelper languageHelper;

    @FXML
    public VBox rootNote;

    @FXML
    private ActivityToggleSwitch obstacleAvoidanceToggleSwitch;

    @InjectViewModel
    private ObstacleAvoidanceTelemetryViewModel viewModel;

    @Inject
    public ObstacleAvoidanceTelemetryView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    @Override
    protected Parent getRootNode() {
        return rootNote;
    }

    @Override
    public ViewModel getViewModel() {
        return viewModel;
    }

    public void initializeView() {
        super.initializeView();

        obstacleAvoidanceToggleSwitch
            .selectedProperty()
            .bindBidirectional(viewModel.obstacleAvoidanceEnabledProperty());
        title.set(languageHelper.getString(ObstacleAvoidanceTelemetryView.class, "title"));
    }
}
