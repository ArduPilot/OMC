/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.view;

import com.intel.missioncontrol.mission.Drone;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.navbar.tools.model.SimulatedSystemFailuresViewModel;
import com.intel.missioncontrol.ui.navbar.tools.model.UavDataParameter;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.plane.IAirplane;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/** Created by eivanchenko on 8/28/2017. */
public class SimulatedSystemFailuresView extends ViewBase<SimulatedSystemFailuresViewModel> {

    public static final int FAIL_TIME_VALUE_MIN = 0;
    public static final int FAIL_TIME_VALUE_MAX = 600;
    public static final int FAIL_TIME_VALUE_STEP = 10;
    public static final int FAIL_TIME_VALUE_INITIAL = 30;
    public static final int BATTERY_LEVEL_VALUE_MIN = 0;
    public static final int BATTERY_LEVEL_VALUE_MAX = 10;
    public static final int BATTERY_LEVEL_VALUE_STEP = 2;
    public static final int BATTERY_LEVEL_VALUE_INITIAL = 2;
    public static final int DEBUG_3_VALUE_MIN = Integer.MIN_VALUE;
    public static final int DEBUG_3_VALUE_MAX = Integer.MAX_VALUE;
    public static final int DEBUG_3_VALUE_STEP = 1;
    public static final int DEBUG_3_VALUE_INITIAL = 0;

    @InjectViewModel
    private SimulatedSystemFailuresViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private ToggleSwitch enginOffSwitch;

    @FXML
    private Label enginOffLabel;

    @FXML
    private ToggleSwitch gpsLossSwitch;

    @FXML
    private Label gpsLossLabel;

    @FXML
    private ToggleSwitch noRcLinkSwitch;

    @FXML
    private Label noRcLinkLabel;

    @FXML
    private ToggleSwitch noDataLinkSwitch;

    @FXML
    private Label noDataLinkLabel;

    @FXML
    private ToggleSwitch batteryLowSwitch;

    @FXML
    private Label batteryLowLabel;

    @FXML
    private Spinner<Integer> failTimeSpinner;

    @FXML
    private Region failTimePane;

    @FXML
    private Label failTimeValue;

    @FXML
    private Label failTimeLabel;

    @FXML
    private Spinner<Integer> batteryLevelSpinner;

    @FXML
    private Region batteryLevelPane;

    @FXML
    private Label batteryLevelValue;

    @FXML
    private Label batteryLevelLabel;

    @FXML
    private Spinner<Integer> debug3Spinner;

    @FXML
    private Region debug3Pane;

    @FXML
    private Label debug3Value;

    @FXML
    private Label debug3Label;

    @FXML
    private Button btnResetAndSend;

    @FXML
    private Button btnSend;

    @Override
    public void initializeView() {
        super.initializeView();

        configureToggleSwitch(SimulatedSystemFailuresViewModel.ENGINE_OFF_PARAM, enginOffLabel, enginOffSwitch);

        configureToggleSwitch(SimulatedSystemFailuresViewModel.GPS_LOSS_PARAM, gpsLossLabel, gpsLossSwitch);

        configureToggleSwitch(SimulatedSystemFailuresViewModel.NO_RC_LINK_PARAM, noRcLinkLabel, noRcLinkSwitch);

        configureToggleSwitch(SimulatedSystemFailuresViewModel.NO_DATA_LINK_PARAM, noDataLinkLabel, noDataLinkSwitch);

        configureToggleSwitch(SimulatedSystemFailuresViewModel.BATTERY_LOW_PARAM, batteryLowLabel, batteryLowSwitch);

        configureToggleSwitch(
            failTimeSpinner,
            FAIL_TIME_VALUE_MIN,
            FAIL_TIME_VALUE_MAX,
            FAIL_TIME_VALUE_INITIAL,
            FAIL_TIME_VALUE_STEP,
            SimulatedSystemFailuresViewModel.FAIL_TIME_PARAM,
            failTimeLabel,
            failTimeValue,
            failTimePane);

        configureToggleSwitch(
            batteryLevelSpinner,
            BATTERY_LEVEL_VALUE_MIN,
            BATTERY_LEVEL_VALUE_MAX,
            BATTERY_LEVEL_VALUE_INITIAL,
            BATTERY_LEVEL_VALUE_STEP,
            SimulatedSystemFailuresViewModel.BATTERY_LEVEL_PARAM,
            batteryLevelLabel,
            batteryLevelValue,
            batteryLevelPane);

        configureToggleSwitch(
            debug3Spinner,
            DEBUG_3_VALUE_MIN,
            DEBUG_3_VALUE_MAX,
            DEBUG_3_VALUE_INITIAL,
            DEBUG_3_VALUE_STEP,
            SimulatedSystemFailuresViewModel.DEBUG_3_PARAM,
            debug3Label,
            debug3Value,
            debug3Pane);
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public SimulatedSystemFailuresViewModel getViewModel() {
        return viewModel;
    }

    private void configureToggleSwitch(String paramName, Label label, final ToggleSwitch ts) {
        UavDataParameter<Drone.ExpertSimulatedFailures> param = viewModel.getUavParameter(paramName);
        label.textProperty().bind(param.displayedNameProperty());
        StringProperty valueProp = param.valueProperty();
        UavDataChangeListener listener =
            new UavDataChangeListener(valueProp, label) {
                @Override
                public String getControlValue() {
                    return String.valueOf(ts.isSelected());
                }
            };
        valueProp.addListener(listener);
        ts.selectedProperty().addListener(listener);
    }

    private void configureToggleSwitch(
            final Spinner<Integer> spinner,
            int minValue,
            int maxValue,
            int initValue,
            int step,
            String paramName,
            Label label,
            Label currentValueLabel,
            Region changeIndicator) {
        UavDataParameter<Drone.ExpertSimulatedFailures> param = viewModel.getUavParameter(paramName);
        StringProperty valueProp = param.valueProperty();
        currentValueLabel.textProperty().bind(valueProp);
        label.textProperty().bind(param.displayedNameProperty());
        spinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, initValue, step));
        UavDataChangeListener listener =
            new UavDataChangeListener(valueProp, changeIndicator) {
                @Override
                public String getControlValue() {
                    return String.valueOf(spinner.getValue());
                }
            };
        valueProp.addListener(listener);
        spinner.valueProperty().addListener(listener);
    }

    private void resetToggleSwitch(ToggleSwitch ts, String paramName) {
        UavDataParameter<Drone.ExpertSimulatedFailures> param = viewModel.getUavParameter(paramName);
        String value = param.getValue();
        if (UavDataParameter.NOT_A_VALUE.equals(value)) {
            ts.setSelected(false);
        } else {
            ts.setSelected(Boolean.TRUE.toString().equals(param.getValue()));
        }
    }

    private void resetSpinner(Spinner<Integer> spinner, String paramName, int initValue) {
        UavDataParameter<Drone.ExpertSimulatedFailures> param = viewModel.getUavParameter(paramName);
        String value = param.getValue();
        if (UavDataParameter.NOT_A_VALUE.equals(value)) {
            spinner.getValueFactory().setValue(initValue);
        } else {
            spinner.getValueFactory().setValue(Integer.valueOf(value));
        }
    }

    @FXML
    private void resetAndSendAction() {
        resetToggleSwitch(enginOffSwitch, SimulatedSystemFailuresViewModel.ENGINE_OFF_PARAM);
        resetToggleSwitch(gpsLossSwitch, SimulatedSystemFailuresViewModel.GPS_LOSS_PARAM);
        resetToggleSwitch(noRcLinkSwitch, SimulatedSystemFailuresViewModel.NO_RC_LINK_PARAM);
        resetToggleSwitch(noDataLinkSwitch, SimulatedSystemFailuresViewModel.NO_DATA_LINK_PARAM);
        resetToggleSwitch(batteryLowSwitch, SimulatedSystemFailuresViewModel.BATTERY_LOW_PARAM);
        resetSpinner(failTimeSpinner, SimulatedSystemFailuresViewModel.FAIL_TIME_PARAM, FAIL_TIME_VALUE_INITIAL);
        resetSpinner(
            batteryLevelSpinner, SimulatedSystemFailuresViewModel.BATTERY_LEVEL_PARAM, BATTERY_LEVEL_VALUE_INITIAL);
        resetSpinner(debug3Spinner, SimulatedSystemFailuresViewModel.DEBUG_3_PARAM, DEBUG_3_VALUE_INITIAL);
    }

    @FXML
    private void sendAction() {
        IAirplane plane = viewModel.getUav().getLegacyPlane();
        if (plane != null) {
            int bitmask = 0;
            if (enginOffSwitch.isSelected()) {
                bitmask += 1;
            }

            if (gpsLossSwitch.isSelected()) {
                bitmask += 2;
            }

            if (noRcLinkSwitch.isSelected()) {
                bitmask += 4;
            }

            if (noDataLinkSwitch.isSelected()) {
                bitmask += 8;
            }

            if (batteryLowSwitch.isSelected()) {
                bitmask += 16;
            }

            plane.expertSendSimulatedFails(
                bitmask, failTimeSpinner.getValue(), batteryLevelSpinner.getValue(), debug3Spinner.getValue());
        }
    }

    private abstract class UavDataChangeListener implements ChangeListener<Object> {

        private StringProperty property;
        private Region region;
        private Background bgChanged;
        private Background bgTransparent;

        public UavDataChangeListener(StringProperty property, Region region) {
            this.property = property;
            this.region = region;
            bgChanged = new Background(new BackgroundFill(Color.YELLOW, CornerRadii.EMPTY, Insets.EMPTY));
            bgTransparent = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
        }

        public abstract String getControlValue();

        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            String value = property.getValue();
            Background background;
            if (UavDataParameter.NOT_A_VALUE.equals(value) || value.equals(getControlValue())) {
                background = bgTransparent;
            } else {
                background = bgChanged;
            }

            region.setBackground(background);
        }
    }

}
