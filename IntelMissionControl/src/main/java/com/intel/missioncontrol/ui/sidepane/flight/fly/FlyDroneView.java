/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.controls.ActivityButton;
import com.intel.missioncontrol.ui.controls.MenuButton;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.sidepane.FancyTabView;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.SafetyChecksView;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.SafetyChecksViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan.FlightplanOptionView;
import com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan.FlightplanOptionViewModel;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javax.inject.Inject;

public class FlyDroneView extends FancyTabView<FlyDroneViewModel> {

    @InjectViewModel
    private FlyDroneViewModel viewModel;

    @FXML
    private Button showOnMapButton;

    @FXML
    private Label projectNameLabel;

    @FXML
    private MenuButton availableDronesMenuButton;

    @FXML
    private VBox formsContainer;

    @FXML
    private ActivityButton takeoffButton;

    @FXML
    private ActivityButton abortTakeoffButton;

    @FXML
    private ActivityButton landButton;

    @FXML
    private ActivityButton abortLandingButton;

    @FXML
    private ActivityButton runPlanButton;

    @FXML
    private ActivityButton pausePlanButton;

    @FXML
    private ActivityButton returnHomeButton;

    @FXML
    private Region landfill;

    @FXML
    private Label flightDisallowedReasonLabel;

    @InjectContext
    private Context context;

    private ILanguageHelper languageHelper;
    private IDialogContextProvider dialogContextProvider;

    @Inject
    public FlyDroneView(ILanguageHelper languageHelper, IDialogContextProvider dialogContextProvider) {
        this.languageHelper = languageHelper;
        this.dialogContextProvider = dialogContextProvider;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        showOnMapButton.setOnAction(event -> viewModel.getShowOnMapCommand().execute());
        showOnMapButton.disableProperty().bind(viewModel.getShowOnMapCommand().notExecutableProperty());

        dialogContextProvider.setContext(viewModel, context);

        projectNameLabel.textProperty().bind(viewModel.missionNameProperty());

        availableDronesMenuButton.setModel(viewModel.getFlyDroneMenuModel());

        takeoffButton
            .disableProperty()
            .bind(
                viewModel
                    .getTakeoffCommand()
                    .notExecutableProperty()
                    .or(viewModel.availableFlightPlansListProperty().emptyProperty()));

        takeoffButton
            .isBusyProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () ->
                        viewModel.getTakeoffCommand().runningProperty().getValue()
                            && !viewModel.startPlanDialogShowingProperty().getValue(),
                    viewModel.getTakeoffCommand().runningProperty(),
                    viewModel.startPlanDialogShowingProperty()));
        takeoffButton
            .visibleProperty()
            .bind(
                viewModel.getTakeoffCommand().executableProperty().or(viewModel.getTakeoffCommand().runningProperty()));
        takeoffButton
            .managedProperty()
            .bind(
                viewModel.getTakeoffCommand().executableProperty().or(viewModel.getTakeoffCommand().runningProperty()));

        abortTakeoffButton.disableProperty().bind(viewModel.getAbortTakeoffCommand().notExecutableProperty());
        abortTakeoffButton.isBusyProperty().bind(viewModel.getAbortTakeoffCommand().runningProperty());
        abortTakeoffButton
            .visibleProperty()
            .bind(
                viewModel
                    .getAbortTakeoffCommand()
                    .executableProperty()
                    .or(viewModel.getAbortTakeoffCommand().runningProperty()));
        abortTakeoffButton
            .managedProperty()
            .bind(
                viewModel
                    .getAbortTakeoffCommand()
                    .executableProperty()
                    .or(viewModel.getAbortTakeoffCommand().runningProperty()));

        landButton.disableProperty().bind(viewModel.getLandCommand().notExecutableProperty());
        landButton.isBusyProperty().bind(viewModel.getLandCommand().runningProperty());
        landButton.visibleProperty().bind(viewModel.landButtonVisibilityProperty());
        landButton.managedProperty().bind(viewModel.landButtonVisibilityProperty());

        abortLandingButton.disableProperty().bind(viewModel.getAbortLandingCommand().notExecutableProperty());
        abortLandingButton.isBusyProperty().bind(viewModel.getAbortLandingCommand().runningProperty());
        abortLandingButton
            .visibleProperty()
            .bind(
                viewModel
                    .getAbortLandingCommand()
                    .executableProperty()
                    .or(viewModel.getAbortLandingCommand().runningProperty()));
        abortLandingButton
            .managedProperty()
            .bind(
                viewModel
                    .getAbortLandingCommand()
                    .executableProperty()
                    .or(viewModel.getAbortLandingCommand().runningProperty()));

        runPlanButton.disableProperty().bind(viewModel.getRunFlightplanCommand().notExecutableProperty());
        runPlanButton
            .isBusyProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () ->
                        viewModel.getRunFlightplanCommand().runningProperty().getValue()
                            && !viewModel.startPlanDialogShowingProperty().getValue(),
                    viewModel.getRunFlightplanCommand().runningProperty(),
                    viewModel.startPlanDialogShowingProperty()));

        runPlanButton
            .visibleProperty()
            .bind(
                viewModel
                    .getRunFlightplanCommand()
                    .executableProperty()
                    .or(viewModel.getRunFlightplanCommand().runningProperty()));
        runPlanButton
            .managedProperty()
            .bind(
                viewModel
                    .getRunFlightplanCommand()
                    .executableProperty()
                    .or(viewModel.getRunFlightplanCommand().runningProperty()));

        pausePlanButton.disableProperty().bind(viewModel.getPauseFlightPlanCommand().notExecutableProperty());
        pausePlanButton.isBusyProperty().bind(viewModel.getPauseFlightPlanCommand().runningProperty());
        pausePlanButton.visibleProperty().bind(viewModel.pausePlanButtonVisibilityProperty());
        pausePlanButton.managedProperty().bind(viewModel.pausePlanButtonVisibilityProperty());

        returnHomeButton.visibleProperty().bind(returnHomeButton.disabledProperty().not());
        returnHomeButton.managedProperty().bind(returnHomeButton.visibleProperty());

        landButton.visibleProperty().bind(landButton.disabledProperty().not());
        landButton.managedProperty().bind(landButton.visibleProperty());

        pausePlanButton.visibleProperty().bind(pausePlanButton.disabledProperty().not());
        pausePlanButton.managedProperty().bind(pausePlanButton.visibleProperty());

        returnHomeButton.disableProperty().bind(viewModel.getReturnToHomeCommand().notExecutableProperty());
        returnHomeButton.isBusyProperty().bind(viewModel.getReturnToHomeCommand().runningProperty());

        landfill.visibleProperty().bind(returnHomeButton.visibleProperty().not());
        landfill.managedProperty().bind(landfill.visibleProperty());

        flightDisallowedReasonLabel.textProperty().bind(viewModel.flightDisallowedReasonProperty());
        flightDisallowedReasonLabel
            .visibleProperty()
            .bind(viewModel.disallowFlightProperty());
        flightDisallowedReasonLabel.managedProperty().bind(viewModel.disallowFlightProperty());

        formsContainer.getChildren().clear();

        formsContainer.getChildren().add(loadFlightplanOptionView());
        formsContainer.getChildren().add(loadSafetyChecksView());
    }

    private TitledPane loadSafetyChecksView() {
        ViewTuple<SafetyChecksView, SafetyChecksViewModel> viewTuple =
            FluentViewLoader.fxmlView(SafetyChecksView.class).context(context).load();
        return collapsed(
            new TitledPane(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.sidepane.flight.fly.FlyDroneView.safetyChecksTitle"),
                viewTuple.getView()));
    }

    private TitledPane loadFlightplanOptionView() {
        ViewTuple<FlightplanOptionView, FlightplanOptionViewModel> viewTuple =
            FluentViewLoader.fxmlView(FlightplanOptionView.class).context(context).load();
        return collapsed(
            new TitledPane(
                languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.fly.FlyDroneView.flightplan"),
                viewTuple.getView()));
    }

    private TitledPane collapsed(TitledPane titledPane) {
        titledPane.expandedProperty().set(true);
        return titledPane;
    }

    @FXML
    public void onTakeoffClicked() {
        viewModel.getTakeoffCommand().executeAsync();
    }

    @FXML
    public void onAbortTakeoffClicked() {
        viewModel.getAbortTakeoffCommand().executeAsync();
    }

    @FXML
    public void onLandClicked() {
        viewModel.getLandCommand().executeAsync();
    }

    @FXML
    public void onAbortLandingClicked() {
        viewModel.getAbortLandingCommand().executeAsync();
    }

    @FXML
    public void onRunFlightPlanClicked() {
        viewModel.getRunFlightplanCommand().executeAsync();
    }

    @FXML
    public void onPauseClicked() {
        viewModel.getPauseFlightPlanCommand().executeAsync();
    }

    @FXML
    public void onReturnHomeClicked() {
        viewModel.getReturnToHomeCommand().executeAsync();
    }

    @FXML
    public void renameClicked() {
        viewModel.getRenameMissionCommand().execute();
    }
}
