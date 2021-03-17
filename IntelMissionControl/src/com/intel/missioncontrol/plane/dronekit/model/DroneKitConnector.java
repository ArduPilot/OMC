/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model;

import com.intel.missioncontrol.plane.dronekit.ConnectionManager;
import com.o3dr.android.client.Drone;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.SingleHealthDescription;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.plane.IAirplane;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/** See {@link eu.mavinci.plane.asctec.AirplaneAscTecConnector} */
public class DroneKitConnector {
    private final Logger LOG = Logger.getLogger(DroneKitConnector.class.getName());

    private final IAirplane plane;

    private final DroneKitContext droneKitContext;
    private final AbstractPlaneConnector connectorDelegate;
    private final DroneEventBridge eventBridge;
    private final MissionManager missionManager;
    private final DroneControl droneControl;
    private AirplaneFlightphase previousPhase = AirplaneFlightphase.ground; //Initialized it to the ground for now.

    public DroneKitConnector(IAirplane plane, Callable<File> droneKitFileProvider) throws Exception {
        this.plane = plane;
        this.droneKitContext = new DroneKitContext(droneKitFileProvider);
        this.eventBridge = new DroneEventBridge();
        this.missionManager = new MissionManager(droneKitContext.getDrone());
        this.connectorDelegate = new PlaneConnectorDelegate();
        this.droneControl = new DroneControl(this.plane, this.droneKitContext.getDrone());
        setup();
    }

    public ConnectionManager getConnectionManager() {
        return droneKitContext;
    }

    public Drone getDrone() {
        return droneKitContext.getDrone();
    }

    private void setup() {
        // setup
        plane.setAirplaneConnector(connectorDelegate);

        connectorDelegate.setRootHandler(plane.getRootHandler());
        connectorDelegate.fireConnectionStateEvent(AirplaneConnectorState.connectingDevice);

        // setup DroneKitContext things
        eventBridge.setup(droneKitContext.getDrone(), plane.getRootHandler(), plane.getAirplaneCache());
        droneKitContext
            .connectionStateProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
            eventBridge.onConnectionStateChanged(newValue);
        });
        PlaneInfo planeInfo = new PlaneInfo();
        setHealthDescriptions(planeInfo);
        plane.getRootHandler().recv_planeInfo(planeInfo);
    }

    private void setHealthDescriptions(PlaneInfo planeInfo) {
        // Battery health description
        SingleHealthDescription sh = new SingleHealthDescription();
        sh.doWarnings = true;
        sh.isImportant = true;

        //TODO: This should be dependent on the plane model. Tentatively placeholder values 8/23/2018.
        sh.maxGreen = 100f;
        sh.minGreen = 79f;
        sh.maxYellow = 70f;
        sh.minYellow = 50f;

        sh.name = PlaneConstants.DEF_BATTERY;
        sh.unit = "%";
        planeInfo.healthDescriptions.add(sh);

        /////////////////////////////////////
        // GPS Health description (GPS Satellite Count)
        sh = new SingleHealthDescription();
        sh.name = PlaneConstants.DEF_GPS;
        sh.doWarnings = true;
        sh.isImportant = true;
        sh.maxGreen = 100f;
        sh.minGreen = 10f;
        sh.maxYellow = 101f;
        sh.minYellow = 5f;
        sh.unit = "sat";
        planeInfo.healthDescriptions.add(sh);

        //////////////////////////////////
        // GPS fix type
        sh = new SingleHealthDescription();
        sh.name = PlaneConstants.DEF_GPS_QUALITY;
        sh.doWarnings = true;
        sh.isImportant = true;
        sh.unit = "mode";
        sh.maxGreen = 10f;
        sh.minGreen = 1f;
        sh.maxYellow = 20f;
        sh.minYellow = 1f;

        planeInfo.healthDescriptions.add(sh);
    }

    /**
     * @param phase
     * @see eu.mavinci.plane.asctec.AirplaneAscTecConnector#setFlightPhase)
     */
    private void doSetFlightPhase(AirplaneFlightphase phase) {
        // System.out.println("Previous Phase: " + this.previousPhase.getDisplayNameKey() + "; Current phase is: " +
        // phase.getDisplayNameKey());
        switch (phase) {
            case takeoff:
                droneControl.armAndTakeoff(true);
                // arm
                // takeoff
                break;
            case startFlight:
                // ???
                if ((this.previousPhase == AirplaneFlightphase.holdPosition)){
                    System.out.println("Resuming mission");
                    droneControl.resumeMission();
                } else {
                    System.out.println("Starting mission");
                    droneControl.startMission();
                }

                break;
            case jumpToLanding:
                // land now
                droneControl.setModeLand();
                break;

            case returnhome:
                droneControl.setModeRTL();
                //
                break;

            case holdPosition:
                droneControl.pauseMission();
                break;

            default:
                LOG.warning("setFlightPhase called with unhandled phase phase=" + phase);
                return;
        }
    }

    private final class PlaneConnectorDelegate extends AbstractPlaneConnector {

        @Override
        public void setFlightPhase(AirplaneFlightphase phase) {
            doSetFlightPhase(phase);
            previousPhase = phase;
        }

        @Override
        public void setFlightPlanXML(String plan, Integer entrypoint) {
            // this pushes flight plan/**/
            Flightplan fp = new Flightplan();
            fp.fromXML(plan);
            missionManager.setFlightPlan(fp, entrypoint);
            missionManager.uploadMission();
        }
        }

}
