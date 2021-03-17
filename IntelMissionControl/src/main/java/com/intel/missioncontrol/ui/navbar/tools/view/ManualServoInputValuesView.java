/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.view;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.navbar.tools.model.ManualServoInputValuesViewModel;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.desktop.helper.MathHelper;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

public class ManualServoInputValuesView extends ViewBase<ManualServoInputValuesViewModel> {

    private static int DEFAULT_VALUE = 127;

    @InjectViewModel
    private ManualServoInputValuesViewModel viewModel;

    private static final double JOYSTICK_PANE_WIDTH = ScaleHelper.emsToPixels(13.);
    private static final double JOYSTICK_PANE_HEIGHT = ScaleHelper.emsToPixels(13.);

    private static final double JOYSTICK_POINTER_WIDTH = ScaleHelper.emsToPixels(1.56);
    private static final double JOYSTICK_POINTER_HEIGHT = ScaleHelper.emsToPixels(1.56);

    @FXML
    private Pane rootNode;

    @FXML
    private Pane elevatorRudderPane;

    @FXML
    private ToggleSwitch verticalInversionSwitch;

    @FXML
    private Text elevatorLabel;

    @FXML
    private Label elevatorValue;

    @FXML
    private Text rudderLabel;

    @FXML
    private Label rudderValue;

    @FXML
    private Pane throttleAileronPane;

    @FXML
    private ToggleSwitch horizontalInversion;

    @FXML
    private Text throttleLabel;

    @FXML
    private Label throttleValue;

    @FXML
    private Text aileronLabel;

    @FXML
    private Label aileronValue;

    @FXML
    private Label autoPilotIndicator;

    @FXML
    private Text autoPilotLabel;

    @FXML
    private Label landingIndicator;

    @FXML
    private Text landingLabel;

    private Polygon elevatorRudderPointer;
    private Line elevatorRudderGreenLine;
    private Polygon throttleAileronPointer;
    private Line throttleAileronGreenLine;

    @Override
    public void initializeView() {
        super.initializeView();
        elevatorRudderPane.setPrefHeight(JOYSTICK_PANE_HEIGHT);
        elevatorRudderPane.setPrefWidth(JOYSTICK_PANE_WIDTH);
        elevatorRudderPointer = createPointer();
        elevatorRudderGreenLine = new Line();
        prepareCanvas(elevatorRudderPane, elevatorRudderPointer, elevatorRudderGreenLine);
        ManualServoInputValuesViewModel.ManualServoInputValue elevator = viewModel.getElevator();
        elevatorLabel.textProperty().setValue(elevator.getDisplayedName());
        StringProperty elevatorProp = elevator.valueProperty();
        elevatorValue.textProperty().bind(elevatorProp);
        elevatorProp.addListener((observable, oldValue, newValue) -> redrawElevator(viewModel.getElevator().getIntValue()));
        ManualServoInputValuesViewModel.ManualServoInputValue rudder = viewModel.getRudder();
        rudderLabel.textProperty().setValue(rudder.getDisplayedName());
        StringProperty rudderProp = rudder.valueProperty();
        rudderValue.textProperty().bind(rudderProp);
        rudderProp.addListener((observable, oldValue, newValue) -> redrawRudder(viewModel.getRudder().getIntValue()));
        throttleAileronPane.setPrefHeight(JOYSTICK_PANE_HEIGHT);
        throttleAileronPane.setPrefWidth(JOYSTICK_PANE_WIDTH);
        throttleAileronPointer = createPointer();
        throttleAileronGreenLine = new Line();
        prepareCanvas(throttleAileronPane, throttleAileronPointer, throttleAileronGreenLine);
        ManualServoInputValuesViewModel.ManualServoInputValue throttle = viewModel.getThrottle();
        throttleLabel.textProperty().setValue(throttle.getDisplayedName());
        StringProperty throttleProp = throttle.valueProperty();
        throttleValue.textProperty().bind(throttleProp);
        throttleProp.addListener((observable, oldValue, newValue) -> redrawThrottle(viewModel.getThrottle().getIntValue()));
        ManualServoInputValuesViewModel.ManualServoInputValue aileron = viewModel.getAileron();
        aileronLabel.textProperty().setValue(aileron.getDisplayedName());
        StringProperty aileronProp = aileron.valueProperty();
        aileronValue.textProperty().bind(aileronProp);
        redrawAileron(DEFAULT_VALUE);
        redrawElevator(DEFAULT_VALUE);
        redrawRudder(DEFAULT_VALUE);
        redrawThrottle(DEFAULT_VALUE);
        aileronProp.addListener((observable, oldValue, newValue) -> redrawAileron(viewModel.getAileron().getIntValue()));
        ManualServoInputValuesViewModel.ManualServoModeIndicator autopilot = viewModel.getAutoPilot();
        autoPilotLabel.textProperty().set(autopilot.getDisplayedName());
        autoPilotIndicator.textProperty().bind(autopilot.valueProperty());
        ManualServoInputValuesViewModel.ManualServoModeIndicator landing = viewModel.getLanding();
        landingLabel.textProperty().set(landing.getDisplayedName());
        landingIndicator.textProperty().bind(landing.valueProperty());
        verticalInversionSwitch
            .selectedProperty()
            .addListener((observable, oldValue, newValue) -> verticalSwitchAction());
        horizontalInversion
            .selectedProperty()
            .addListener((observable, oldValue, newValue) -> horizantalSwitchAction());
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    protected ManualServoInputValuesViewModel getViewModel() {
        return viewModel;
    }

    private Polygon createPointer() {
        return new Polygon(
            -(JOYSTICK_POINTER_WIDTH / 6),
            JOYSTICK_POINTER_HEIGHT / 2,
            JOYSTICK_POINTER_WIDTH / 6,
            JOYSTICK_POINTER_HEIGHT / 2,
            JOYSTICK_POINTER_WIDTH / 6,
            JOYSTICK_POINTER_HEIGHT / 6,
            JOYSTICK_POINTER_WIDTH / 2,
            JOYSTICK_POINTER_HEIGHT / 6,
            JOYSTICK_POINTER_WIDTH / 2,
            -(JOYSTICK_POINTER_HEIGHT / 6),
            JOYSTICK_POINTER_WIDTH / 6,
            -(JOYSTICK_POINTER_HEIGHT / 6),
            JOYSTICK_POINTER_WIDTH / 6,
            -(JOYSTICK_POINTER_HEIGHT / 2),
            -(JOYSTICK_POINTER_WIDTH / 6),
            -(JOYSTICK_POINTER_HEIGHT / 2),
            -(JOYSTICK_POINTER_WIDTH / 6),
            -(JOYSTICK_POINTER_HEIGHT / 6),
            -(JOYSTICK_POINTER_WIDTH / 2),
            -(JOYSTICK_POINTER_HEIGHT / 6),
            -(JOYSTICK_POINTER_WIDTH / 2),
            JOYSTICK_POINTER_HEIGHT / 6,
            -(JOYSTICK_POINTER_WIDTH / 6),
            JOYSTICK_POINTER_HEIGHT / 6);
    }

    private void prepareCanvas(Pane canvas, Polygon pointer, Line greenLine) {
        canvas.setBorder(Border.EMPTY);
        ObservableList<Node> nodes = canvas.getChildren();
        // border ands background cross
        nodes.addAll(
            new Line(0.0, 0.0, JOYSTICK_PANE_WIDTH, 0.0),
            new Line(JOYSTICK_PANE_WIDTH, 0.0, JOYSTICK_PANE_WIDTH, JOYSTICK_PANE_HEIGHT),
            new Line(0.0, JOYSTICK_PANE_HEIGHT, JOYSTICK_PANE_WIDTH, JOYSTICK_PANE_HEIGHT),
            new Line(0.0, 0.0, 0.0, JOYSTICK_PANE_HEIGHT),
            new Line(JOYSTICK_PANE_WIDTH / 2, 0.0, JOYSTICK_PANE_WIDTH / 2, JOYSTICK_PANE_HEIGHT),
            new Line(0.0, JOYSTICK_PANE_HEIGHT / 2, JOYSTICK_PANE_WIDTH, JOYSTICK_PANE_HEIGHT / 2));
        // green line from center to pointer
        greenLine.setStroke(Color.GREEN);
        greenLine.setStartX(JOYSTICK_PANE_WIDTH / 2);
        greenLine.setStartY(JOYSTICK_PANE_HEIGHT / 2);
        nodes.add(greenLine);
        // pointer
        pointer.setFill(Color.BLACK);
        nodes.add(pointer);
    }

    public boolean isControlVerticallyInverted() {
        return verticalInversionSwitch.isSelected();
    }

    public boolean isControlHorizonlyInverted() {
        return horizontalInversion.isSelected();
    }

    private double normalizeValueX(Integer value, boolean inverted) {
        return normalizeValue(value, inverted, JOYSTICK_PANE_WIDTH);
    }

    private double normalizeValueY(Integer value, boolean inverted) {
        return normalizeValue(value, inverted, JOYSTICK_PANE_HEIGHT);
    }

    private double normalizeValue(Integer value, boolean inverted, double naxSize) {
        int val = value == null ? DEFAULT_VALUE : value;
        val = MathHelper.intoRange(val, 0, 255);
        return ((inverted ? (255 - val) : val) / 255.) * naxSize;
    }

    private void redrawRudder(Integer value) {
        double x = normalizeValueX(value, isControlHorizonlyInverted());
        elevatorRudderGreenLine.endXProperty().set(x);
        elevatorRudderPointer.translateXProperty().set(x);
    }

    private void redrawElevator(Integer value) {
        double y = normalizeValueY(value, isControlVerticallyInverted());
        elevatorRudderGreenLine.endYProperty().set(y);
        elevatorRudderPointer.translateYProperty().set(y);
    }

    private void redrawThrottle(Integer value) {
        double y = normalizeValueY(value, isControlVerticallyInverted());
        throttleAileronGreenLine.endYProperty().set(y);
        throttleAileronPointer.translateYProperty().set(y);
    }

    private void redrawAileron(Integer value) {
        double x = normalizeValueX(value, isControlHorizonlyInverted());
        throttleAileronGreenLine.endXProperty().set(x);
        throttleAileronPointer.translateXProperty().set(x);
    }

    @FXML
    private void verticalSwitchAction() {
        redrawElevator(viewModel.getElevator().getIntValue());
        redrawThrottle(viewModel.getThrottle().getIntValue());
    }

    @FXML
    private void horizantalSwitchAction() {
        redrawRudder(viewModel.getRudder().getIntValue());
        redrawAileron(viewModel.getAileron().getIntValue());
    }

}
