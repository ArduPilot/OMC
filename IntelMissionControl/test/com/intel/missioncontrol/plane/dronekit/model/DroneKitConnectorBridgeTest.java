/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model;

import android.os.Bundle;
import com.intel.missioncontrol.plane.dronekit.DroneKitConnectorBridge;
import com.o3dr.android.client.interfaces.DroneListener;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConnectionState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDelegator;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPositionOrientation;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.test.rules.GuiceInitializer;
import eu.mavinci.test.rules.MavinciInitializer;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;


/** Test App for DroneKitConnectorBridge */
@Ignore //Automatic tests on CI server have no access to DISPLAY
public class DroneKitConnectorBridgeTest {

    @ClassRule
    public static final MavinciInitializer MAVINCI_INITIALIZER = new MavinciInitializer();
    @ClassRule
    public static final GuiceInitializer GUICE_INITIALIZER = new GuiceInitializer();


    private static final Logger LOG = Logger.getLogger(DroneKitConnectorBridgeTest.class.getSimpleName());

    public static class TestApp extends Application {
        private TestAirplane plane;
        private DroneKitConnectorBridge bridge;
        private DroneKitConnector airplaneConnector;

        @Override
        public void start(Stage stage) throws Exception {
            plane = new TestAirplane();

            IAirplaneListenerDelegator rootHandler = plane.getRootHandler();
            rootHandler.addListenerAtSecond(new IAirplaneListenerConnectionState() {
                @Override
                public void connectionStateChange(AirplaneConnectorState newState) {
                    LOG.warning(" connectionStateChange "+newState);
                }

            });
            rootHandler.addListenerAtSecond(new IAirplaneListenerPositionOrientation() {
                @Override
                public void recv_positionOrientation(PositionOrientationData po) {
                    LOG.warning(" recv_positionOrientation "+po);
                }
            });

            bridge = DroneKitConnectorBridge.connect(plane, () -> {
                LOG.info("Connected?");
            });

            final String[] skip = new String[] {
                    "PARAMETERS_RECEIVED",
            };
            airplaneConnector = bridge.getAirplaneConnector();
            final long start = System.currentTimeMillis();

            airplaneConnector.getDrone().registerDroneListener(new DroneListener() {
                @Override
                public void onDroneEvent(String event, Bundle extras) {
                    for (String s : skip) if (event.contains(s)) return;

                    System.out.printf("DRONEEVENT, %03.2f, %s, %s\n",
                            (float)(System.currentTimeMillis() - start)/1000.f,
                            event.replace("com.o3dr.services.android.lib.attribute.event.", ""),
                            extras.toString());
                }

                @Override
                public void onDroneServiceInterrupted(String errorMsg) {

                }
            });

        }

        // Main
        public static void main(String[] args) {
            LOG.info("Starting Test app");
            LOG.entering("Application", "launch");
            launch(args);
        }
    }

    @Test
    @Ignore //Automatic tests on CI server have no access to DISPLAY
    public void runTestApp() {
        TestApp.main(new String[]{});
    }
}