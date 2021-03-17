/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.hardware.IPayloadConfiguration;
import com.intel.missioncontrol.hardware.IPayloadMountConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.flightplan.Flightplan;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import javafx.beans.property.ListProperty;
import org.asyncfx.concurrent.Dispatcher;

public class FlightPlanValidation {
    private static final Globe globe = StaticInjector.getInstance(IWWGlobes.class).getDefaultGlobe();

    public static boolean validateGimbalPitch(FlightPlan flightplan) {
        Flightplan fp = flightplan.getLegacyFlightplan();

        boolean ok = true;

        // Check pitch angle range for all payload mounts
        for (IPayloadMountConfiguration payloadMountConfig : fp.getHardwareConfiguration().getPayloadMounts()) {
            for (IPayloadConfiguration payloadConfig : payloadMountConfig.getPayloads()) {
                var payloadDescription = payloadConfig.getDescription();

                // TODO: clean up hardware description so roll/pitch/yaw angles are consistently defined. Use whole
                // kinematic chain.
                double minPitch = payloadDescription.getMinPitch().convertTo(Unit.DEGREE).getValue().doubleValue();
                double maxPitch = payloadDescription.getMaxPitch().convertTo(Unit.DEGREE).getValue().doubleValue();

                for (WayPoint wp : flightplan.waypointsProperty()) {
                    double wpPitch = wp.pitchProperty().get().convertTo(Unit.DEGREE).getValue().doubleValue();
                    if (wpPitch < minPitch || wpPitch > maxPitch) {
                        Dispatcher.platform().runLater(() -> wp.airspaceWarningProperty().setValue(true));
                        ok = false;
                    }
                }
            }
        }

        return ok;
    }

    public static boolean validateWaypointSeparation(FlightPlan flightplan) {
        Flightplan fp = flightplan.getLegacyFlightplan();

        boolean ok = true;

        IPlatformDescription platformDesc = fp.getHardwareConfiguration().getPlatformDescription();

        double minWpSeparation = platformDesc.getMinWaypointSeparation().convertTo(Unit.METER).getValue().doubleValue();
        double maxWpSeparation = platformDesc.getMaxWaypointSeparation().convertTo(Unit.METER).getValue().doubleValue();

        if (minWpSeparation == 0 && maxWpSeparation == 0) {
            return true;
        }

        Vec4 v;
        ListProperty<WayPoint> wps = flightplan.waypointsProperty();
        // in order to add warning to the first waypoint as well
        Vec4 vPrevious =
            wps.size() > 1 ? globe.computePointFromPosition(wps.get(1).getLegacyWaypoint().getPosition()) : null;

        for (WayPoint wp : flightplan.waypointsProperty()) {
            v = globe.computePointFromPosition(wp.getLegacyWaypoint().getPosition());
            if (vPrevious != null) {
                double distance = vPrevious.distanceTo3(v);

                if ((minWpSeparation > 0 && distance < minWpSeparation)
                        || (maxWpSeparation > 0 && distance > maxWpSeparation)) {
                    Dispatcher.platform().runLater(() -> wp.airspaceWarningProperty().setValue(true));
                    ok = false;
                }
            }

            vPrevious = v;
        }

        return ok;
    }

    public static boolean validateNumberOfWaypoints(FlightPlan flightplan) {
        Flightplan fp = flightplan.getLegacyFlightplan();

        IPlatformDescription platformDesc = fp.getHardwareConfiguration().getPlatformDescription();
        int maxNumberOfWaypoints = platformDesc.getMaxNumberOfWaypoints();

        return maxNumberOfWaypoints <= 0 || flightplan.waypointsProperty().size() <= maxNumberOfWaypoints;
    }

    public static double estimateFlightTime(FlightPlan flightplan) {
        if (flightplan.waypointsProperty().isEmpty()) {
            return 0.0;
        }

        return estimateFlightTime(flightplan, 0);
    }

    public static double estimateFlightTime(FlightPlan flightplan, int fromWaypointIndex) {
        double timeInSeconds = 0.0;

        double speedMs = flightplan.maxGroundSpeedProperty().get().convertTo(Unit.METER_PER_SECOND).getValue().doubleValue();
        Vec4 vPrevious;
        if(fromWaypointIndex == 0) {
            timeInSeconds += 20; // static base take-off duration
            vPrevious = globe.computePointFromPosition(flightplan.takeoffPositionProperty().get());
        } else {
            vPrevious = null;
        }

        for(int i = fromWaypointIndex; i < flightplan.waypointsProperty().getSize(); i++) {
            WayPoint wp = flightplan.waypointsProperty().get(i);

            Vec4 v = globe.computePointFromPosition(wp.getLegacyWaypoint().getPosition());
            if (vPrevious != null) {
                double distance = vPrevious.distanceTo3(v);

                timeInSeconds += distance / speedMs;
            }

            vPrevious = v;
        }


        return timeInSeconds;
    }

}
