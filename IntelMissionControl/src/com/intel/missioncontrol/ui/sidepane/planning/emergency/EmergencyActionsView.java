/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.emergency;

import static com.intel.missioncontrol.ui.common.BindingUtils.bindVisibility;
import static com.intel.missioncontrol.ui.common.BindingUtils.unbindVisibility;
import static eu.mavinci.core.plane.AirplaneEventActions.ignore;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Time;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.CEventList;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.core.plane.AirplaneEventActions;
import java.util.HashMap;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class EmergencyActionsView extends ViewBase<EmergencyActionsViewModel> {

    private static final double ALT_MIN = CEventList.minSafetyAlt_CM / 100.0;
    private static final double ALT_MAX = CWaypoint.ALTITUDE_MAX_WITHIN_CM / 100.0;

    private static final double MIN_DELAY_IN_SECONDS = 0.0;
    private static final double MAX_DELAY_IN_SECONDS = 300.0;
    private static final double DELAY_STEP = 1.0;

    @InjectViewModel
    private EmergencyActionsViewModel viewModel;

    @FXML
    private Pane rootNode;

    @Inject
    private ILanguageHelper languageHelper;

    @FXML
    private ComboBox<AirplaneEventActions> gnssLostActionBox;

    @FXML
    private Spinner<Quantity<Time>> gnssLostActionDelaySpinner;

    @FXML
    private CheckBox gnssLostRecoverable;

    @FXML
    private Label gnssLostRecoverableLabel;

    @FXML
    private ComboBox<AirplaneEventActions> rcAndDataLostActionBox;

    @FXML
    private Spinner<Quantity<Time>> rcAndDataLostActionDelaySpinner;

    @FXML
    private CheckBox rcAndDataLostRecoverable;

    @FXML
    private Label rcAndDataLostRecoverableLabel;

    @FXML
    private ComboBox<AirplaneEventActions> dataLostActionBox;

    @FXML
    private Spinner<Quantity<Time>> dataLostActionDelaySpinner;

    @FXML
    private CheckBox dataLostRecoverable;

    @FXML
    private Label dataLostRecoverableLabel;

    @FXML
    private ComboBox<AirplaneEventActions> rcLostActionBox;

    @FXML
    private Spinner<Quantity<Time>> rcLostActionDelaySpinner;

    @FXML
    private CheckBox rcLostRecoverable;

    @FXML
    private Label rcLostRecoverableLabel;

    @FXML
    private ToggleSwitch autoSafetyAltitudeSwitch;

    @FXML
    private Spinner<Quantity<Length>> safetyAltitudeSpinner;

    @FXML
    private GridPane fixedWingEventsPane;

    @FXML
    private VBox copterEventsPane;

    @FXML
    private ComboBox<AirplaneEventActions> gnssLostCopterActionBox;

    @FXML
    private CheckBox gnssLostCopterRecoverable;

    @FXML
    private Label gnssLostCopterRecoverableLabel;

    @FXML
    private ComboBox<AirplaneEventActions> rcAndDataLostCopterActionBox;

    @FXML
    private CheckBox rcAndDataLostCopterRecoverable;

    @FXML
    private Label rcAndDataLostCopterRecoverableLabel;

    @FXML
    private Spinner<Quantity<Length>> safetyAltitudeCopterSpinner;

    private ChangeListener<LandingModes> autoLandingModePropertyListener =
        (observable, oldValue, newValue) -> muteAndRepaintComboBoxes();

    @Inject
    private ISettingsManager settingsManager;

    private final HashMap<String, ChangeListener<AirplaneEventActions>> comboBoxlisteners = new HashMap<>();
    private EnumConverter<AirplaneEventActions> actionsPlaneConverter;

    private ChangeListener<FlightPlan> flightPlanPropChangeListener =
        new ChangeListener<>() {
            @Override
            public void changed(
                    ObservableValue<? extends FlightPlan> observable, FlightPlan oldValue, FlightPlan newValue) {
                viewModel.currentFlightPlanProperty().removeListener(flightPlanPropChangeListener);
                unbindVisibility(fixedWingEventsPane);
                rcAndDataLostActionDelaySpinner.disableProperty().unbind();
                dataLostActionDelaySpinner.disableProperty().unbind();
                rcLostActionDelaySpinner.disableProperty().unbind();
                safetyAltitudeSpinner.disableProperty().unbind();
                viewModel.autoLandingModeProperty().removeListener(autoLandingModePropertyListener);
                unbindVisibility(copterEventsPane);
                unbindVisibility(safetyAltitudeCopterSpinner);
            }
        };

    @Override
    public void initializeView() {
        super.initializeView();

        viewModel.currentFlightPlanProperty().addListener(flightPlanPropChangeListener);
        actionsPlaneConverter = new EnumConverter<>(languageHelper, AirplaneEventActions.class);
        BooleanBinding isFixedWingUsed = viewModel.isSelectedUavFixedWing();
        bindVisibility(fixedWingEventsPane, isFixedWingUsed);

        initDelaySpinner(gnssLostActionDelaySpinner, viewModel.gnssLostDelayQuantityProperty());
        gnssLostActionBox.getItems().setAll(viewModel.getActiveActions(CEventList.NAME_GPSLOSS));
        gnssLostActionBox.setConverter(actionsPlaneConverter);
        gnssLostActionBox.valueProperty().bindBidirectional(viewModel.initEventActionProperty(CEventList.NAME_GPSLOSS));
        gnssLostRecoverable.selectedProperty().bindBidirectional(viewModel.gnssLostRecoverableProperty());

        initDelaySpinner(rcAndDataLostActionDelaySpinner, viewModel.rcAndDataLostDelayQuantityProperty());
        listenSignalActionBox(rcAndDataLostActionBox, CEventList.NAME_RCDATALOSS);
        rcAndDataLostRecoverable.selectedProperty().bindBidirectional(viewModel.rcAndDataLostRecoverableProperty());
        rcAndDataLostActionDelaySpinner
            .disableProperty()
            .bind(rcAndDataLostActionBox.valueProperty().isEqualTo(ignore));

        initDelaySpinner(dataLostActionDelaySpinner, viewModel.dataLostDelayQuantityProperty());
        listenSignalActionBox(dataLostActionBox, CEventList.NAME_DATALOSS);
        dataLostRecoverable.selectedProperty().bindBidirectional(viewModel.dataLostRecoverableProperty());
        dataLostActionDelaySpinner.disableProperty().bind(dataLostActionBox.valueProperty().isEqualTo(ignore));

        initDelaySpinner(rcLostActionDelaySpinner, viewModel.rcLostDelayQuantityProperty());
        listenSignalActionBox(rcLostActionBox, CEventList.NAME_RCLOSS);
        rcLostRecoverable.selectedProperty().bindBidirectional(viewModel.rcLostRecoverableProperty());
        rcLostActionDelaySpinner.disableProperty().bind(rcLostActionBox.valueProperty().isEqualTo(ignore));

        bindRecoverable();

        safetyAltitudeSpinner.disableProperty().bind(autoSafetyAltitudeSwitch.selectedProperty());
        initAltitudeSpinner(safetyAltitudeSpinner, viewModel.safetyAltitudeQuantityProperty());
        autoSafetyAltitudeSwitch.selectedProperty().bindBidirectional(viewModel.autoSafetyAltitudeProperty());
        viewModel.autoLandingModeProperty().addListener(autoLandingModePropertyListener);

        // COPTER
        bindVisibility(copterEventsPane, isFixedWingUsed.not());

        CopterEventActionsConverter actionsCopterConverter = new CopterEventActionsConverter(languageHelper);

        gnssLostCopterActionBox.getItems().setAll(viewModel.getGpsLostCopterActions());
        gnssLostCopterActionBox.setConverter(actionsCopterConverter);
        gnssLostCopterActionBox
            .valueProperty()
            .bindBidirectional(viewModel.initEventActionProperty(CEventList.NAME_GPSLOSS));
        gnssLostCopterRecoverable.selectedProperty().bindBidirectional(viewModel.gnssLostCopterRecoverableProperty());

        rcAndDataLostCopterActionBox.getItems().setAll(viewModel.getLinkLostCopterActions());
        rcAndDataLostCopterActionBox.setConverter(actionsCopterConverter);
        rcAndDataLostCopterActionBox
            .valueProperty()
            .bindBidirectional(viewModel.initEventActionProperty(CEventList.NAME_RCDATALOSS));
        rcAndDataLostCopterRecoverable
            .selectedProperty()
            .bindBidirectional(viewModel.rcAndDataLostCopterRecoverableProperty());

        initAltitudeSpinner(safetyAltitudeCopterSpinner, viewModel.safetyAltitudeQuantityProperty());
        BooleanBinding useAltitudeSpinnner =
            rcAndDataLostCopterActionBox.valueProperty().isEqualTo(AirplaneEventActions.returnToStartOnSafetyAltitude);
        bindVisibility(safetyAltitudeCopterSpinner, useAltitudeSpinnner);
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public EmergencyActionsViewModel getViewModel() {
        return viewModel;
    }

    private void listenSignalActionBox(ComboBox<AirplaneEventActions> actionBox, String eventName) {
        actionBox.getItems().setAll(viewModel.getPossibleActions(eventName));
        actionBox.setConverter(actionsPlaneConverter);

        SimpleObjectProperty<AirplaneEventActions> actionProperty = viewModel.initEventActionProperty(eventName);
        actionBox.setValue(actionProperty.get());
        actionBox.setCellFactory(
            lv ->
                new ListCell<AirplaneEventActions>() {
                    @Override
                    public void updateItem(AirplaneEventActions item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(actionsPlaneConverter.toString(item));
                            setDisable(!viewModel.getActiveActions(eventName).contains(item));
                        }
                    }
                });

        ChangeListener<AirplaneEventActions> listener =
            (observable, oldValue, newValue) -> onSignalEventComboBoxChange(actionProperty, newValue);
        actionBox.valueProperty().addListener(listener);
        comboBoxlisteners.put(eventName, listener);
    }

    private void onSignalEventComboBoxChange(
            SimpleObjectProperty<AirplaneEventActions> actionProperty, AirplaneEventActions newAction) {
        actionProperty.set(newAction);
        muteAndRepaintComboBoxes();
    }

    private void muteAndRepaintComboBoxes() {
        rcAndDataLostActionBox.valueProperty().removeListener(comboBoxlisteners.get(CEventList.NAME_RCDATALOSS));
        dataLostActionBox.valueProperty().removeListener(comboBoxlisteners.get(CEventList.NAME_DATALOSS));
        rcLostActionBox.valueProperty().removeListener(comboBoxlisteners.get(CEventList.NAME_RCLOSS));

        // update all three checkboxes together
        updateActionComboBoxValues(rcAndDataLostActionBox, CEventList.NAME_RCDATALOSS);
        updateActionComboBoxValues(dataLostActionBox, CEventList.NAME_DATALOSS);
        updateActionComboBoxValues(rcLostActionBox, CEventList.NAME_RCLOSS);

        rcAndDataLostActionBox.valueProperty().addListener(comboBoxlisteners.get(CEventList.NAME_RCDATALOSS));
        dataLostActionBox.valueProperty().addListener(comboBoxlisteners.get(CEventList.NAME_DATALOSS));
        rcLostActionBox.valueProperty().addListener(comboBoxlisteners.get(CEventList.NAME_RCLOSS));
    }

    private void updateActionComboBoxValues(ComboBox<AirplaneEventActions> comboBox, String eventName) {
        comboBox.setValue(null);
        comboBox.getItems().setAll(viewModel.getPossibleActions(eventName));
        comboBox.setValue(viewModel.getAction(eventName));
    }

    private void initDelaySpinner(Spinner<Quantity<Time>> spinner, QuantityProperty<Time> property) {
        ViewHelper.initAutoCommitSpinner(
            spinner,
            property,
            Unit.SECOND,
            settingsManager.getSection(GeneralSettings.class),
            0,
            MIN_DELAY_IN_SECONDS,
            MAX_DELAY_IN_SECONDS,
            DELAY_STEP,
            false);
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

    private void bindRecoverable() {
        BooleanBinding operationLevelBinding = viewModel.isAdvancedOperationLevelBinding().not();
        gnssLostRecoverable.disableProperty().bind(operationLevelBinding);
        rcAndDataLostRecoverable.disableProperty().bind(operationLevelBinding);
        dataLostRecoverable.disableProperty().bind(operationLevelBinding);
        rcLostRecoverable.disableProperty().bind(operationLevelBinding);
        gnssLostRecoverableLabel.disableProperty().bind(operationLevelBinding);
        rcAndDataLostRecoverableLabel.disableProperty().bind(operationLevelBinding);
        dataLostRecoverableLabel.disableProperty().bind(operationLevelBinding);
        rcLostRecoverableLabel.disableProperty().bind(operationLevelBinding);

        gnssLostCopterRecoverable.disableProperty().bind(operationLevelBinding);
        rcAndDataLostCopterRecoverable.disableProperty().bind(operationLevelBinding);
        gnssLostCopterRecoverableLabel.disableProperty().bind(operationLevelBinding);
        rcAndDataLostCopterRecoverableLabel.disableProperty().bind(operationLevelBinding);
    }

}
