/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.start;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import com.intel.missioncontrol.utils.IntegerValidator;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import org.asyncfx.beans.binding.ConversionBindings;
import org.asyncfx.beans.binding.Converters;

public class StartPlanDialogView extends DialogView<StartPlanDialogViewModel> {
    @FXML
    private Pane root;

    @FXML
    private Label selectedFlightplanLabel;

    @FXML
    private ToggleGroup startPlanToggleGroup;

    @FXML
    private RadioButton resumePlanRadio;

    @FXML
    private RadioButton startPlanFromBeginningRadio;

    @FXML
    private RadioButton startPlanFromWaypointRadio;

    @FXML
    private Spinner<Integer> startingWaypointSpinner;

    @FXML
    private Button confirmButton;

    @InjectViewModel
    private StartPlanDialogViewModel viewModel;

    private ILanguageHelper languageHelper;

    @Inject
    public StartPlanDialogView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.sidepane.flight.fly.startPlan.StartPlanDialogView.title"));
    }

    @Override
    protected Parent getRootNode() {
        return root;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        selectedFlightplanLabel.textProperty().set(viewModel.selectedFlightplanProperty().getValue().getName());

        resumePlanRadio.setUserData(StartPlanType.RESUME_PLAN);
        resumePlanRadio
            .disableProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () ->
                        viewModel.activeFlightplanProperty().getValue() == null
                            || !viewModel
                                .activeFlightplanProperty()
                                .getValue()
                                .equals(viewModel.selectedFlightplanProperty().getValue()),
                    viewModel.activeFlightplanProperty(),
                    viewModel.selectedFlightplanProperty()));

        startPlanFromBeginningRadio.setUserData(StartPlanType.START_PLAN_FROM_BEGINNING);
        startPlanFromWaypointRadio.setUserData(StartPlanType.START_PLAN_FROM_WAYPOINT);

        setStartPlanToggle(viewModel.startPlanTypeProperty().getValue());
        viewModel.startPlanTypeProperty().addListener((observable, oldValue, newValue) -> setStartPlanToggle(newValue));

        startPlanToggleGroup
            .selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) ->
                    viewModel.startPlanTypeProperty().setValue((StartPlanType)newValue.getUserData()));

        // TODO: this causes a stack overflow error if selectedFlightPlanWaypointCountProperty is 0.
        IntegerValidator wpSpinnerValueFactory =
            new IntegerValidator(
                1, 1, viewModel.selectedFlightPlanWaypointCountProperty().getValue().intValue(), 1, Integer.MAX_VALUE);

        startingWaypointSpinner.setValueFactory(wpSpinnerValueFactory.getValueFactory());
        startingWaypointSpinner.editableProperty().bind(startPlanFromWaypointRadio.selectedProperty());
        startingWaypointSpinner
            .focusedProperty()
            .addListener(
                (observable, oldValue, newValue) -> setStartPlanToggle(StartPlanType.START_PLAN_FROM_WAYPOINT));
        ConversionBindings.bindBidirectional(
            startingWaypointSpinner.getValueFactory().valueProperty(),
            viewModel.startingWaypointProperty(),
            Converters.numberToInt());

        if (!resumePlanRadio.isDisabled()) {
            resumePlanRadio.setSelected(true);
        }
        //        confirmButton.disableProperty().bind(viewModel.formValidation().validProperty().not());
    }

    private void setStartPlanToggle(StartPlanType startPlanType) {
        startPlanToggleGroup
            .getToggles()
            .stream()
            .filter(t -> t.getUserData().equals(startPlanType))
            .findAny()
            .ifPresent(t -> t.setSelected(true));
    }

    public void OnConfirmTakeoffButtonClicked(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getConfirmCommand().execute();
    }

    public void OnCancelButtonClicked(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getCloseCommand().execute();
    }
}
