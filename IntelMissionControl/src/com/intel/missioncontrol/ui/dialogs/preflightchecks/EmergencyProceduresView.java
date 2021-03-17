/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.preflightchecks;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class EmergencyProceduresView extends DialogView<EmergencyProceduresViewModel> {

    private final LanguageHelper languageHelper;

    @FXML
    private VBox rootlayout;

    @InjectViewModel
    private EmergencyProceduresViewModel viewModel;

    @Inject
    public EmergencyProceduresView(LanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.EmergencyProceduresView.title"));
    }

    @Override
    protected Parent getRootNode() {
        return rootlayout;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    public void onLandNowButtonClicked(ActionEvent actionEvent) {
        viewModel.getLandNowCommand().execute();
    }

    public void onTakeSafeAltitudeButtonClicked(ActionEvent actionEvent) {
        viewModel.getTakeSafeAltitude50MCommand().execute();
    }

    public void onReturnHomeButtonClicked(ActionEvent actionEvent) {
        viewModel.getReturnToHomeCommand().execute();
    }
}
