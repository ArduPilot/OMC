/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.intel.missioncontrol.hardware.IPayloadConfiguration;
import com.intel.missioncontrol.hardware.IPayloadMountConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.measure.Unit;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.flightplan.Flightplan;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import org.asyncfx.concurrent.Dispatcher;

public class FlightPlanValidation {
    private static final Globe globe =
        DependencyInjector.getInstance().getInstanceOf(IWWGlobes.class).getDefaultGlobe();

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

        Vec4 v = null;
        Vec4 vPrevious = null;

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

        if (maxNumberOfWaypoints <= 0)
        {
            return true;
        }

        return flightplan.waypointsProperty().size() <= maxNumberOfWaypoints;
    }

    public static double estimateFlightTime(FlightPlan flightplan) {
        int N = flightplan.waypointsProperty().size();
        if (N == 0) {
            return 0.0;
        }

        WayPoint wp = flightplan.waypointsProperty().get(0);
        return estimateFlightTime(flightplan, wp);
    }

    public static double estimateFlightTime(FlightPlan flightplan, WayPoint currentWaypoint) {
        Flightplan fp = flightplan.getLegacyFlightplan();

        boolean ok = true;

        IPlatformDescription platformDesc = fp.getHardwareConfiguration().getPlatformDescription();

        Vec4 v = null;
        Vec4 vPrevious = null;

        double totalDistance = 0.0;
        boolean startWpReached = false;

        int n = 0;
        for (WayPoint wp : flightplan.waypointsProperty()) {
            if (!startWpReached) {
                if (wp.equals(currentWaypoint)) {
                    startWpReached = true;
                } else {
                    continue;
                }
            }

            v = globe.computePointFromPosition(wp.getLegacyWaypoint().getPosition());
            if (vPrevious != null) {
                double distance = vPrevious.distanceTo3(v);

                totalDistance += distance;
            }

            vPrevious = v;
            n++;
        }

        double speedMs =
            flightplan.maxGroundSpeedProperty().get().convertTo(Unit.METER_PER_SECOND).getValue().doubleValue();

        double flightTime = (totalDistance / speedMs) + n * 5.0;
        return flightTime;
    }

}
