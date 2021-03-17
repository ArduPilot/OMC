/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.asctec;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPhoto;
import eu.mavinci.core.flightplan.CPhotoSettings;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IReentryPoint;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.core.flightplan.ReentryPointID;
import eu.mavinci.core.flightplan.visitors.AFlightplanVisitor;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.LandingPoint;
import gov.nasa.worldwind.geom.LatLon;
import java.util.logging.Level;

public class ACPModelExtractor {

    double defaultSpeed = 1;
    ACPTaskModel model;

    AFlightplanVisitor vis =
        new AFlightplanVisitor() {

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

                if (isReachedEntryPoint) {
                    if (point instanceof CWaypoint) {
                        CWaypoint new_name = (CWaypoint)point;
                        add(new_name);
                    } else if (point instanceof CPhoto) {
                        CPhoto new_name = (CPhoto)point;
                        add(new_name);
                    } else if (point instanceof LandingPoint) {
                        LandingPoint new_name = (LandingPoint)point;
                        add(new_name);
                    } else {
                        // System.out.println(point.toString() + " not necessary");
                    }
                }

                return false;
            }
        };
    private Integer entrypoint;
    LatLon takeoffLocation;
    double exposureDelayMilliSec;

    public ACPTaskModel extractModel(CFlightplan plan, Integer entrypoint, LatLon takeoffLocation) {
        this.entrypoint = entrypoint;
        defaultSpeed =
            plan.getHardwareConfiguration()
                .getPlatformDescription()
                .getPlaneSpeed()
                .convertTo(Unit.METER_PER_SECOND)
                .getValue()
                .doubleValue();
        model = new ACPTaskModel(plan.getHardwareConfiguration());
        exposureDelayMilliSec =
            plan.getHardwareConfiguration()
                .getPrimaryPayload(IGenericCameraConfiguration.class)
                .getDescription()
                .getCameraDelay()
                .convertTo(Unit.MILLISECOND)
                .getValue()
                .doubleValue();

        this.takeoffLocation = takeoffLocation;
        // elementsSwitch(plan.getEventList());

        add(plan.getPhotoSettings());
        vis.startVisit(plan);

        return model;
    }

    private void add(CPhotoSettings photoSettings) {
        // TODO in future configure camera
    }

    private void add(CPhoto new_name) {
        // TODO as soon as we have waypoint which are NOT triggering any images, we have to track camera on/off from
        // this photo statements
    }

    private double getHeight(IFlightplanPositionReferenced point, CFlightplan flightPlan) {
        return point.getAltInMAboveFPRefPoint()
            + flightPlan.getRefPointAltWgs84WithElevation()
            - flightPlan.getTakeofftAltWgs84WithElevation();
    }

    private void add(LandingPoint point) {
        if (takeoffLocation == null) {
            return; // we are in cockpit export mode
        }

        double lat;
        double lon;

        switch (point.getMode()) {
        case LAST_WAYPOINT: // =3 //copters will stay airborne on last waypoint, fixedwing will go to startprocedure
            // location==same as landing but stay on alt
            return;
        case CUSTOM_LOCATION: // =0 //copters will use this for custom auto landing location
            lat = point.getLat();
            lon = point.getLon();
            break;

        case LAND_AT_TAKEOFF: // =1 //copters will use this for auto landing on Same as actual takeoff location
            lat = takeoffLocation.latitude.degrees;
            lon = takeoffLocation.longitude.degrees;
            break;

        default:
            Debug.getLog().log(Level.SEVERE, "unsuported landing mode detected:" + point.getMode());
            return;
        }

        double roll = 0;
        double pitch = 0;
        double yaw = 0;

        CFlightplan cFlightplan = point.getFlightplan();
        Ensure.notNull(cFlightplan, "cFlightplan");
        double speed = cFlightplan.getPhotoSettings().getMaxGroundSpeedMPSec();
        double height = point.getAltInMAboveFPRefPoint();
        int reentryId = point.getId();

        model.addPoint(roll, pitch, yaw, speed, height, lat, lon, false, reentryId, 0, 0);
    }

    private void add(CWaypoint point) {
        Orientation o = point.getOrientation();
        double roll = o.isRollDefined() ? o.getRoll() : 0;
        double pitch = o.isPitchDefined() ? o.getPitch() : 0;
        double yaw = o.isYawDefined() ? o.getYaw() : 0;

        CFlightplan flightPlan = point.getFlightplan();
        double speed = defaultSpeed;
        if (point.getSpeedMpSec() > 0) {
            speed = point.getSpeedMpSec();
        } else if (flightPlan != null) {
            double s = flightPlan.getPhotoSettings().getMaxGroundSpeedMPSec();
            if (s > 0) speed = s;
        }

        double height = getHeight(point, flightPlan);
        double lat = point.getLat();
        double lon = point.getLon();
        int reentryId = point.getId();

        double timerMoveMuting = point.getStopHereTimeCopter();
        double timerRotationMuting = point.isTriggerImageHereCopterMode() ? exposureDelayMilliSec : 0;

        model.addPoint(
            roll,
            pitch,
            yaw,
            speed,
            height,
            lat,
            lon,
            point.isTriggerImageHereCopterMode(),
            reentryId,
            timerMoveMuting,
            timerRotationMuting);
    }

}
