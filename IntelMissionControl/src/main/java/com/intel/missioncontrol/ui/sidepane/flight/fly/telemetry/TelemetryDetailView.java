/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class TelemetryDetailView extends DialogView<TelemetryDetailViewModel> {

    @FXML
    public VBox rootNote;

    @FXML
    private Button closeButton;

    @InjectViewModel
    private TelemetryDetailViewModel viewModel;

    @Inject
    private ILanguageHelper languageHelper;

    private final StringProperty title = new SimpleStringProperty();

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

        closeButton.setOnAction(event -> viewModel.getCloseCommand().execute());
        closeButton.disableProperty().bind(viewModel.getCloseCommand().notExecutableProperty());
        title.bind(
            Bindings.createStringBinding(
                () ->
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry.TelemetryDetailView.title")));
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    public void configureRTK(ActionEvent actionEvent) {
        // TODO
    }
}
