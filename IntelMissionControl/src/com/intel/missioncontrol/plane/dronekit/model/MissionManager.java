/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model;

import android.os.Bundle;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.mission.Mission;
import eu.mavinci.flightplan.Flightplan;

public class MissionManager {

    private final Drone drone;
    Mission currentMission;

    public MissionManager(Drone drone) {
        this.drone = drone;
        this.drone.registerDroneListener(listener);
    }

    private final DroneListener listener = new DroneListener() {
        @Override
        public void onDroneEvent(String event, Bundle extras) {
            handleDroneEvent(event, extras);
        }

        @Override
        public void onDroneServiceInterrupted(String errorMsg) {

        }
    };

    private void handleDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.MISSION_RECEIVED:
                break;
            case AttributeEvent.MISSION_SENT:
                break;
            case AttributeEvent.MISSION_UPDATED:
                break;
            case AttributeEvent.MISSION_ITEM_REACHED:
                break;
            case AttributeEvent.MISSION_ITEM_UPDATED:
                System.out.println("MissionManager: DroneEvent = "+event);
                break;
        }
    }


    public void uploadMission() {
        if (currentMission == null) {
            return;
        }
        System.out.println(">>>>>>>>>>>>>>> set upload mission");

        MissionApi api = MissionApi.getApi(drone);
        api.setMission(currentMission, true);
    }

    public Mission getCurrentMission() {
        return currentMission;
    }

    /** @see eu.mavinci.flightplan.asctec.ACPModelExtractor#extractModel */
    public void setFlightPlan(Flightplan plan, Integer entrypoint) {
        System.out.println(">>>>>>>>>>>>>>> set flightplan entrypoint" +entrypoint);

        currentMission = MissionConverter.convert(plan, entrypoint);

        System.out.println(">>>>>>>>>>>>>>> set flightplan");
        // do something
    }


}
