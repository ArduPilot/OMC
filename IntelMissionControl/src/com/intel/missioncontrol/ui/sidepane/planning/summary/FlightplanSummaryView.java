/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.summary;

import com.google.inject.Inject;
import com.intel.missioncontrol.beans.binding.QuantityBindings;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.TimeStyle;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.controls.Button;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.core.helper.StringHelper;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public class FlightplanSummaryView extends ViewBase<FlightplanSummaryViewModel> {

    @InjectViewModel
    private FlightplanSummaryViewModel viewModel;

    @FXML
    private GridPane coverageBox;

    @FXML
    private Label flightTimeLabel;

    @FXML
    private Label distanceLabel;

    @FXML
    private Button imageCountLabel;

    @FXML
    private Label trueOrthoAreaLabel;

    @FXML
    private Label pseudoOrthoAreaLabel;

    @FXML
    private ProgressBar trueOrthoProgressBar;

    @FXML
    private ProgressBar pseudoOrthoProgressBar;

    @FXML
    private Label trueOrthoProgressBarLabel;

    @FXML
    private Label pseudoOrthoProgressBarLabel;

    @FXML
    private StackPane pseudoOrthoProgress;

    @FXML
    private StackPane trueOrthoProgress;

    @FXML
    private Pane layoutRoot;

    private final QuantityFormat quantityFormat;

    private final IDialogContextProvider dialogContextProvider;

    @InjectContext @MonotonicNonNull
    private Context context;

    @Inject
    public FlightplanSummaryView(ISettingsManager settingsManager, IDialogContextProvider dialogContextProvider) {
        quantityFormat = new AdaptiveQuantityFormat(settingsManager.getSection(GeneralSettings.class));
        quantityFormat.setSignificantDigits(3);
        this.dialogContextProvider = dialogContextProvider;
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void initializeView() {
        super.initializeView();
        Expect.notNull(
            viewModel, "viewModel",
            flightTimeLabel, "flightTimeLabel",
            distanceLabel, "distanceLabel",
            imageCountLabel, "imageCountLabel");

        dialogContextProvider.setContext(viewModel, context);

        imageCountLabel.disableProperty().bind(viewModel.getShowEditWayointsDialogCommand().notExecutableProperty());
        imageCountLabel.setOnAction((actionEvent) -> viewModel.getShowEditWayointsDialogCommand().execute());
        imageCountLabel.textProperty().bind(viewModel.imageCountProperty().asString());
        trueOrthoProgressBar.progressProperty().bind(viewModel.trueOrthoCoverageRatioProperty());
        pseudoOrthoProgressBar.progressProperty().bind(viewModel.pseudoOrthoCoverageRatioProperty());

        coverageBox
            .visibleProperty()
            .bind(
                viewModel
                    .pseudoOrthoCoverageRatioEnabledProperty()
                    .or(viewModel.trueOrthoCoverageRatioEnabledProperty()));
        coverageBox.managedProperty().bind(coverageBox.visibleProperty());

        pseudoOrthoProgress.visibleProperty().bind(viewModel.pseudoOrthoCoverageRatioEnabledProperty());
        trueOrthoProgress.visibleProperty().bind(viewModel.trueOrthoCoverageRatioEnabledProperty());

        trueOrthoProgressBarLabel
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () ->
                        StringHelper.ratioToPercent(viewModel.trueOrthoCoverageRatioProperty().doubleValue(), 1, true),
                    viewModel.trueOrthoCoverageRatioProperty()));

        pseudoOrthoProgressBarLabel
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () ->
                        StringHelper.ratioToPercent(
                            viewModel.pseudoOrthoCoverageRatioProperty().doubleValue(), 1, true),
                    viewModel.pseudoOrthoCoverageRatioProperty()));

        flightTimeLabel
            .textProperty()
            .bind(QuantityBindings.createStringBinding(viewModel.flightTimeProperty(), quantityFormat));

        distanceLabel
            .textProperty()
            .bind(QuantityBindings.createStringBinding(viewModel.distanceProperty(), quantityFormat));

        trueOrthoAreaLabel
            .textProperty()
            .bind(QuantityBindings.createStringBinding(viewModel.trueOrthoAreaProperty(), quantityFormat));

        pseudoOrthoAreaLabel
            .textProperty()
            .bind(QuantityBindings.createStringBinding(viewModel.pseudoOrthoAreaProperty(), quantityFormat));
    }

}
