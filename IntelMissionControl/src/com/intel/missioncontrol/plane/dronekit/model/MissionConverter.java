/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.measure.Unit;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.CameraTrigger;
import com.o3dr.services.android.lib.drone.mission.item.command.ReturnToLaunch;
import com.o3dr.services.android.lib.drone.mission.item.command.StartImageCapture;
import com.o3dr.services.android.lib.drone.mission.item.command.Takeoff;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Land;
import com.o3dr.services.android.lib.drone.mission.item.spatial.MountControl;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPhoto;
import eu.mavinci.core.flightplan.CStartProcedure;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IReentryPoint;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.core.flightplan.ReentryPointID;
import eu.mavinci.core.flightplan.visitors.AFlightplanVisitor;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.LandingPoint;
import java.util.logging.Level;
import java.util.logging.Logger;

/** convert to DroneKit missions
 *
 * {@see ACPModelExtractor}
 */
public class MissionConverter {
    private static final Logger DEBUG_LOG = Debug.getLog();
    private static final Logger LOG = Logger.getGlobal();
    private static final String TAG = "MissionConverter";

    final Flightplan flightplan;
    private final int entrypoint;

    double defaultSpeed = 5.0;
    double exposureDelayMilliSec;

    double currentSpeed = 0.0;
    double rtlAltitude;

    private final Mission mission;

    private Takeoff takeoff = null;


    public static Mission convert(Flightplan flightplan, Integer entrypoint) {
        MissionConverter converter = new MissionConverter(flightplan);
        converter.convert();
        return converter.mission;
    }

    private MissionConverter(Flightplan flightplan) {
        this.flightplan = flightplan;
        this.mission = new Mission();
        this.entrypoint = 0;
    }

    public void convert() {
        beforeVisit();
        visitor.startVisit(flightplan);
        afterVisit();
    }

    private void beforeVisit() {
        defaultSpeed = flightplan.getHardwareConfiguration()
                .getPlatformDescription()
                .getPlaneSpeed()
                .convertTo(Unit.METER_PER_SECOND)
                .getValue()
                .doubleValue();
        exposureDelayMilliSec =
                flightplan.getHardwareConfiguration()
                        .getPrimaryPayload(IGenericCameraConfiguration.class)
                        .getDescription()
                        .getCameraDelay()
                        .convertTo(Unit.MILLISECOND)
                        .getValue()
                        .doubleValue();

        // todo: get this
        rtlAltitude = 150;
    }

    private void afterVisit() {
        if (takeoff != null) mission.addMissionItem(0, takeoff);
    }

    private final AFlightplanVisitor visitor = new AFlightplanVisitor() {
        private boolean isReachedEntryPoint = false;

        @Override
        public boolean visit(IFlightplanRelatedObject point) {
            if (point instanceof IReentryPoint) {
                if (ReentryPointID.equalIDexceptCell(entrypoint, ((IReentryPoint)point).getId())
                        || entrypoint == 0
                        || entrypoint == -1) {
                    isReachedEntryPoint = true;
                }
            }
            if (!isReachedEntryPoint) return false;

            System.out.println("Got mission item: " + point.getClass().getSimpleName() + " : " +point);
            if (point instanceof CWaypoint) {
                CWaypoint new_name = (CWaypoint)point;
                onWaypoint(new_name);
            } else if (point instanceof CPhoto) {
                CPhoto new_name = (CPhoto)point;
                onPhoto(new_name);
            } else if (point instanceof LandingPoint) {
                LandingPoint new_name = (LandingPoint)point;
                onLandingPoint(new_name);
            } else if (point instanceof CStartProcedure) {
                CStartProcedure new_name = (CStartProcedure)point;
                onStartProcedure(new_name);
            } else {
                // System.out.println(point.toString() + " not necessary");
            }

            return false;
        }
    };

    private static double getHeight(IFlightplanPositionReferenced point, CFlightplan flightPlan) {
        return point.getAltInMAboveFPRefPoint()
                + flightPlan.getRefPointAltWgs84WithElevation()
                - flightPlan.getTakeofftAltWgs84WithElevation();
    }

    private void onLandingPoint(LandingPoint point) {
        double lat, lon;

        MissionItem item;
        switch (point.getMode()) {
            case DESC_STAYAIRBORNE: // =3 //copters will stay airborne on last waypoint, fixedwing will go to startprocedure
                // location==same as landing but stay on alt
                return;
            case DESC_CIRCLE: // =0 //copters will use this for custom auto landing location
                lat = point.getLat();
                lon = point.getLon();
                double height = getHeight(point, flightplan);
                Land land = new Land();
                land.setCoordinate(new LatLongAlt(lat, lon, height));
                break;

            case DESC_HOLDYAW: // =1 //copters will use this for auto landing on Same as actual takeoff location
                ReturnToLaunch rtl = new ReturnToLaunch();
                rtl.setReturnAltitude(rtlAltitude);
                break;

            default:
                DEBUG_LOG.log(Level.SEVERE, "unsuported landing mode detected:" + point.getMode());
                return;
        }


        //dkMission.addMissionItem(MissionItem);
    }

    private void onWaypoint(CWaypoint point) {
        Orientation o = point.getOrientation();
        double roll = o.isRollDefined() ? o.getRoll() : 0;
        double pitch = o.isPitchDefined() ? o.getPitch() : 0;
        double yaw = o.isYawDefined() ? o.getYaw() : 0;
        // todo something with this
        double speed = defaultSpeed;
        if (point.getSpeedMpSec() > 0) {
            speed = point.getSpeedMpSec();
        } else if (flightplan != null) {
            double s = flightplan.getPhotoSettings().getMaxGroundSpeedMPSec();
            if (s > 0) speed = s;
        }

        double timerMoveMuting = point.getStopHereTimeCopter();
        double timerRotationMuting = point.isTriggerImageHereCopterMode() ? exposureDelayMilliSec : 0;

        Waypoint wp = new Waypoint();

        double alt = point.getAlt() * 0.01;
        double lat = point.getLat();
        double lng = point.getLon();

        LatLongAlt coord = new LatLongAlt(lat, lng, alt);
        wp.setCoordinate(coord);

        if (o.isYawDefined()) wp.setYawAngle(o.getYaw());

        wp.setAcceptanceRadius(point.getRadiusWithinM());

        mission.addMissionItem(wp);

        if(point.isTriggerImageHereCopterMode()){
            //mission.addMissionItem(onTrigger());

            mission.addMissionItem(onCameraMount(pitch, roll, yaw));
            mission.addMissionItem(onStartImageCapture());
        }

       // dkMission.addMissionItem(wp);
    }

    private void onPhoto(CPhoto photo) {

    }

    private MissionItem onTrigger(){
        MissionItem triggerItem = new CameraTrigger();
        ((CameraTrigger) triggerItem).setTriggerDistance(5.0);
        return triggerItem;
    }

    private MissionItem onCameraMount(double pitch, double roll, double yaw){
        MissionItem triggerItem = new MountControl();
        ((MountControl) triggerItem).setCoordinate(new LatLongAlt(0.0, 0.0, 0.0));
        // mapping pitch and yaw to Mavinci pro drone.
        ((MountControl) triggerItem).setPitchAngle(pitch - 90);
        ((MountControl) triggerItem).setRollAngle(roll);
        if (yaw > 180){
            yaw = yaw -360;
        }
        ((MountControl) triggerItem).setYawAngle(yaw);
        return triggerItem;
    }

    private MissionItem onStartImageCapture(){
        MissionItem triggerItem = new StartImageCapture();
        ((StartImageCapture) triggerItem).setNumberOfImagesToCapture(1);
        return triggerItem;
    }

    private void onStartProcedure(CStartProcedure startProcedure) {
        DEBUG_LOG.fine(TAG + ": handleStartProcedure("  + startProcedure + ")");
        if (takeoff != null) LOG.warning("handleStartProcedure, start procedure set again!");

        double maxSpeed = flightplan.getPhotoSettings().getMaxGroundSpeedMPSec();
        double speed = maxSpeed > 0 ? maxSpeed : defaultSpeed;
        double height = getHeight(startProcedure, flightplan);

        takeoff = new Takeoff();
        takeoff.setTakeoffAltitude(Math.max(15.0, height));
        takeoff.setTakeoffPitch(0.0);
    }

}
