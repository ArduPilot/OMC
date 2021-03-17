/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit;

import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.plane.dronekit.model.DroneKitConnector;
import com.intel.missioncontrol.plane.dronekit.model.DroneKitContext;
import eu.mavinci.plane.IAirplane;
import org.apache.commons.io.FileUtils;

public class SingletonDroneContextManager {

//    private final static Object lock = new Object();
//
//    private static DroneKitConnector connector;
//
//    volatile static IAirplane airplane = null;
//
//    // set by
//    public static void setCurrentMission(Mission mission) {
//        synchronized (lock) {
//            System.out.println("currentMissionProperty CHANGED: " + mission);
//
//            IAirplane airplane = mission.getPlane();
//
//            if (connector == null) {
//                try {
//                    connector = new DroneKitConnector( () -> FileUtils.getTempDirectory());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            // todo: make this reconfiguration work
//            connector.setAirplane(airplane);
//        }
//        // todo: give this to dronekit
//    }
//
//    public static DroneKitConnector getDroneKitConnector() {
//        synchronized (lock) {
//            return connector;
//        }
//    }
}
