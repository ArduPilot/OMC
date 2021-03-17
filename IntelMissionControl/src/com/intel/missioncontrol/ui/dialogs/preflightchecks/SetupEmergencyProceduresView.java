/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.preflightchecks;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.core.flightplan.CEventList;
import eu.mavinci.core.flightplan.CWaypoint;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;

public class SetupEmergencyProceduresView extends DialogView<SetupEmergencyProceduresViewModel> {

    private static final double ALT_MIN = CEventList.minSafetyAlt_CM / 100.0;
    private static final double ALT_MAX = CWaypoint.ALTITUDE_MAX_WITHIN_CM / 100.0;

    @InjectViewModel
    private SetupEmergencyProceduresViewModel viewModel;

    @FXML
    private VBox layoutRoot;

    @FXML
    private ComboBox<String> procedureComboBox;

    @FXML
    private ComboBox<String> controllerComboBox;

    @FXML
    private Label safetyAltitudeLabel;

    @FXML
    private Spinner<Quantity<Dimension.Length>> safetyAltitudeSpinner;

    private LanguageHelper languageHelper;
    private SpinnerValueFactory<Integer> altitudeValueFactory;
    private ISettingsManager settingsManager;

    @Inject
    public SetupEmergencyProceduresView(LanguageHelper languageHelper, ISettingsManager settingsManager) {
        this.languageHelper = languageHelper;
        this.settingsManager = settingsManager;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.preflightchecks.SetupEmergencyProceduresView.title"));
    }

    public void onApplyButtonClicked(ActionEvent actionEvent) {
        // TODO: apply the user input to data layer
        viewModel.getCloseCommand().execute();
    }

    public void onCancelButtonClicked(ActionEvent actionEvent) {
        viewModel.getCloseCommand().execute();
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
    protected void initializeView() {
        super.initializeView();

        procedureComboBox.itemsProperty().bind(viewModel.emergencyProceduresProperty());
        procedureComboBox.setValue(viewModel.emergencyProcedureSelectedProperty().get());

        safetyAltitudeSpinner
            .visibleProperty()
            .bind(
                procedureComboBox
                    .valueProperty()
                    .isEqualTo(
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.dialogs.preflightChecks.SetupEmergencyProceduresView.emergencyAction.returnHomeatFixedSafetyAltitude")));
        safetyAltitudeLabel
            .visibleProperty()
            .bind(
                procedureComboBox
                    .valueProperty()
                    .isEqualTo(
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.dialogs.preflightChecks.SetupEmergencyProceduresView.emergencyAction.returnHomeatFixedSafetyAltitude")));

        altitudeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE);
        initAltitudeSpinner(safetyAltitudeSpinner, viewModel.safetyAltitudeProperty());

        controllerComboBox.itemsProperty().bind(viewModel.backupControllersProperty());
        controllerComboBox.setValue(viewModel.backupControllerSelectedProperty().get());
    }

    private void initAltitudeSpinner(
            Spinner<Quantity<Dimension.Length>> spinner, QuantityProperty<Dimension.Length> property) {
        ViewHelper.initAutoCommitSpinner(
            spinner,
            property,
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            0,
            ALT_MIN,
            ALT_MAX,
            1.0,
            false);
    }
}
