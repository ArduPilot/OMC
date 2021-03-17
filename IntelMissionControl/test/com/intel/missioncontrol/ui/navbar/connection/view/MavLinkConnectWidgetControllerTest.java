/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.intel.missioncontrol.plane.dronekit.model.DroneKitConnector;
import com.intel.missioncontrol.plane.dronekit.model.TestAirplane;
import com.intel.missioncontrol.ui.controls.Button;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConnectionState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFlightphase;
import eu.mavinci.core.plane.listeners.IAirplaneListenerHealth;
import eu.mavinci.test.rules.GuiceInitializer;
import eu.mavinci.test.rules.MavinciInitializer;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.assertj.core.util.Files;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test App for MavLinkConnectWidgetController, allows testing of aircraft reinjection
 */
@Ignore //Automatic tests on CI server have no access to DISPLAY
public class MavLinkConnectWidgetControllerTest extends Application {

    @ClassRule
    public static final MavinciInitializer MAVINCI_INITIALIZER = new MavinciInitializer();
    @ClassRule
    public static final GuiceInitializer GUICE_INITIALIZER = new GuiceInitializer();

    private MavLinkConnectionWidget autoControl;
    private MavLinkConnectWidgetController controller;

    private DroneKitConnector connector = null;

    @Override
    public void start(Stage stage) throws Exception {
        autoControl = new MavLinkConnectionWidget();
        VBox container = new VBox();
        container.spacingProperty().setValue(10);
        Button button = new Button();
        button.setOnMouseClicked(event -> {
            try {
                System.err.println(">>>>>>>>>>>>>>>>>>> I'M RELOADING IAirplane....");
                setupView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        button.setText("[TEST] Re-inject IAirplane");

        container.getChildren().add(button);
        container.getChildren().add(new Separator(Orientation.HORIZONTAL));
        container.getChildren().add(autoControl);
        Scene scene = new Scene(container);
        stage.setScene(scene);
        stage.setTitle("MavLinkConnectWidgetController");
        stage.setWidth(300);
        stage.setHeight(400);
        stage.show();

        controller = new MavLinkConnectWidgetController(autoControl);
        setupView();
        stage.setHeight(autoControl.getPrefHeight());
        // autoControl.setText("Hello!");
    }

    public void setupView() throws Exception {
        System.out.println("setup view");

        TestAirplane airplane = new TestAirplane();
        airplane.addListener((IAirplaneListenerFlightphase) fp ->
                System.err.println("AutoConnectControllerTest.recv_flightPhase:" + "fp = [" + fp + "]"));
        airplane.addListener((IAirplaneListenerHealth) d ->
                System.err.println("AutoConnectControllerTest.recv_health:" + "d = [" + d + "]"));
        airplane.addListener((IAirplaneListenerConnectionState) newState ->
                System.err.println("AutoConnectControllerTest.connectionStateChange:" + "newState = [" + newState + "]"));

        if (connector != null) {
            connector.getConnectionManager().disconnect();
        }
        connector = new DroneKitConnector(airplane, Files::temporaryFolder);
        controller.setup(connector);
    }

    // don't run here
    private static void main(String[] args) {
        launch(args);
    }

    // run from here so DependencyInjection works
    @Test
    @Ignore //Automatic tests on CI server have no access to DISPLAY
    public void runTestApp() {
        MavLinkConnectWidgetControllerTest.main(new String[]{});
    }

}