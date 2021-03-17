/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.emergency;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.CEvent;
import eu.mavinci.core.plane.AirplaneEventActions;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;

public class EmergencyActionsView extends ViewBase<EmergencyActionsViewModel> {

    private static final double ALT_MIN = 0;
    private static final double ALT_MAX = 10000;
    private static final double DUR_MIN = 0;
    private static final double DUR_MAX = 300;

    @InjectViewModel
    private EmergencyActionsViewModel viewModel;

    @FXML
    private Control rootNode;

    @Inject
    private ILanguageHelper languageHelper;

    @FXML
    private VBox settableEmergencyActions;

    @FXML
    private ToggleSwitch autoSafetyAltitudeSwitch;

    @FXML
    private Spinner<Quantity<Length>> safetyAltitudeSpinner;

    @FXML
    private Spinner<Quantity<Dimension.Time>> rcLinkLossSpinner;

    @FXML
    private Spinner<Quantity<Dimension.Time>> primaryLinkLossSpinner;

    @FXML
    private Spinner<Quantity<Dimension.Time>> gnssLinkLossSpinner;

    @FXML
    private ComboBox<AirplaneEventActions> rcLinkLossComboBox;

    @FXML
    private ComboBox<AirplaneEventActions> primaryLinkLossComboBox;

    @FXML
    private ComboBox<AirplaneEventActions> gnssLinkLossComboBox;

    @FXML
    private ComboBox<AirplaneEventActions> geofenceBreachComboBox;

    @Inject
    private ISettingsManager settingsManager;

    @Override
    public void initializeView() {
        super.initializeView();

        settableEmergencyActions.visibleProperty().bind(viewModel.emergencyActionsSettableProperty());
        settableEmergencyActions.managedProperty().bind(viewModel.emergencyActionsSettableProperty());

        safetyAltitudeSpinner.disableProperty().bind(autoSafetyAltitudeSwitch.selectedProperty());
        initAltitudeSpinner(safetyAltitudeSpinner, viewModel.safetyAltitudeQuantityProperty());
        initDurationSpinner(rcLinkLossSpinner, viewModel.rcLinkLossDurationQuantityProperty());
        initDurationSpinner(primaryLinkLossSpinner, viewModel.primaryLinkLossDurationQuantityProperty());
        initDurationSpinner(gnssLinkLossSpinner, viewModel.gnssLinkLossDurationQuantityProperty());

        initEmergencyActionCombobox(
            rcLinkLossComboBox,
            viewModel.rcLinkLossComboBoxProperty(),
            Arrays.asList(CEvent.SIGNAL_COPTER_LOSS_POSSIBLE_ACTIONS));
        initEmergencyActionCombobox(
            primaryLinkLossComboBox,
            viewModel.primaryLinkLossComboBoxProperty(),
            Arrays.asList(CEvent.SIGNAL_COPTER_LOSS_POSSIBLE_ACTIONS));
        initEmergencyActionCombobox(
            gnssLinkLossComboBox,
            viewModel.gnssLinkLossComboBoxProperty(),
            Arrays.asList(CEvent.GPS_COPTER_LOSS_POSSIBLE_ACTIONS));
        initEmergencyActionCombobox(
            geofenceBreachComboBox,
            viewModel.geofenceBreachComboBoxProperty(),
            Arrays.asList(CEvent.SIGNAL_COPTER_LOSS_POSSIBLE_ACTIONS));

        autoSafetyAltitudeSwitch.selectedProperty().bindBidirectional(viewModel.autoComputeSafetyHeightProperty());
    }

    @Override
    public Control getRootNode() {
        return rootNode;
    }

    @Override
    public EmergencyActionsViewModel getViewModel() {
        return viewModel;
    }

    private void initAltitudeSpinner(Spinner<Quantity<Length>> spinner, QuantityProperty<Length> property) {
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

    private void initDurationSpinner(
            Spinner<Quantity<Dimension.Time>> spinner, QuantityProperty<Dimension.Time> property) {
        ViewHelper.initAutoCommitSpinner(
            spinner, property, Unit.SECOND, IQuantityStyleProvider.NEUTRAL, 0, DUR_MIN, DUR_MAX, 1.0, false);
    }

    private void initEmergencyActionCombobox(
            ComboBox<AirplaneEventActions> comboBox,
            Property<AirplaneEventActions> property,
            List<AirplaneEventActions> items) {
        EnumConverter<AirplaneEventActions> emergencyActionsEnumConverter =
            new EnumConverter<>(languageHelper, AirplaneEventActions.class);
        comboBox.getItems().addAll(items);
        comboBox.setConverter(emergencyActionsEnumConverter);
        comboBox.valueProperty().bindBidirectional(property);
    }

}
