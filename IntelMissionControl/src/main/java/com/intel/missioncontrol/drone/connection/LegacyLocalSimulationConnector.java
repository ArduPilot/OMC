/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.MavinciObjectFactory;
import com.intel.missioncontrol.mission.Drone;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.plane.simjava.AirplaneSim;
import gov.nasa.worldwind.geom.Position;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;

public class LegacyLocalSimulationConnector implements IConnector<IDrone> {
    private final LegacyLocalSimulationConnectionItem connectionItem;

    private AirplaneSim airplaneSimulator;

    private final Mission mission;
    private final FlightPlan flightPlan;

    private final MavinciObjectFactory mavinciObjectFactory = StaticInjector.getInstance(MavinciObjectFactory.class);

    LegacyLocalSimulationConnector(LegacyLocalSimulationConnectionItem connectionItem) {
        this.connectionItem = connectionItem;
        this.mission = connectionItem.getMission();
        this.flightPlan = connectionItem.getFlightPlan();
    }

    @Override
    public LegacyLocalSimulationConnectionItem getConnectionItem() {
        return connectionItem;
    }

    @Override
    public Future<IDrone> connectAsync() {
        // start simulation
        Dispatcher dispatcher = Dispatcher.platform();
        return dispatcher.getLaterAsync(
            () -> {
                Flightplan legacyFp = flightPlan.getLegacyFlightplan();
                Drone drone = mission.droneProperty().get();
                IAirplane airplane = drone.getLegacyPlane();

                IHardwareConfiguration hardwareConfiguration = legacyFp.getHardwareConfiguration();
                IPlatformDescription platformDescription = hardwareConfiguration.getPlatformDescription();

                airplane.setNativeHardwareConfiguration(hardwareConfiguration);
                drone.setSimulatedPlatformDescription(platformDescription);

                airplaneSimulator = mavinciObjectFactory.createAirplaneSimulator(platformDescription, airplane);
                airplane.setSimulationSpeed(1.0f);

                Position takeOffPosition = flightPlan.takeoffPositionProperty().get();
                airplane.setStartpos(takeOffPosition.getLongitude().degrees, takeOffPosition.getLatitude().degrees);

                return drone;
            });
    }

    @Override
    public Future<Void> disconnectAsync() {
        // stop simulation
        Dispatcher dispatcher = Dispatcher.platform();
        return dispatcher.runLaterAsync(
            () -> {
                if (airplaneSimulator != null) {
                    airplaneSimulator.close();
                    airplaneSimulator = null;
                    Drone drone = mission.droneProperty().get();
                    IAirplane airplane = drone.getLegacyPlane();
                    airplane.disconnectSilently();
                }
            });
    }
}
