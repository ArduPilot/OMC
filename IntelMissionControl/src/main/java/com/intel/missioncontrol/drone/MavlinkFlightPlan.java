/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkMissionItem;
import com.intel.missioncontrol.hardware.IMavlinkFlightPlanOptions;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.WayPoint;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.visitors.AFlightplanVisitor;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.Photo;
import eu.mavinci.flightplan.Waypoint;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import io.dronefleet.mavlink.common.VtolTransitionHeading;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MavlinkFlightPlan {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavlinkFlightPlan.class);
    public static final Duration HOLD_AT_WAYPOINTS_DURATION = Duration.millis(500);

    private static class MissionItemInfo {
        final MavlinkMissionItem missionItem;
        final WayPoint wayPoint;
        final int wayPointIndex;

        MissionItemInfo(MavlinkMissionItem missionItem, WayPoint wayPoint, int wayPointIndex) {
            this.missionItem = missionItem;
            this.wayPoint = wayPoint;
            this.wayPointIndex = wayPointIndex;
        }

        @Override
        public String toString() {
            return "IMC waypoint #" + wayPointIndex + ": " + missionItem.toString();
        }
    }

    private final List<MissionItemInfo> missionItemInfos;
    private final IMavlinkFlightPlanOptions options;
    private final Position startAltitudePosition;
    private final FlightPlan flightPlan;

    private boolean landAutomatically;

    private MavlinkFlightPlan(
            IMavlinkFlightPlanOptions options, Position startAltitudePosition, FlightPlan flightPlan) {
        this.missionItemInfos = new ArrayList<>();
        this.options = options;
        this.startAltitudePosition = startAltitudePosition;
        this.flightPlan = flightPlan;
    }

    /** Convert an IMC FlightPlan to MavlinkMissionItems. */
    static MavlinkFlightPlan fromFlightPlanWithWayPointIndex(
            FlightPlanWithWayPointIndex flightPlanWithWayPointIndex,
            IMavlinkFlightPlanOptions options,
            Position startAltitudePosition) {
        if (flightPlanWithWayPointIndex != null
                && flightPlanWithWayPointIndex.getFlightPlan() != null
                && startAltitudePosition != null) {
            MavlinkFlightPlan mavlinkFlightPlan =
                new MavlinkFlightPlan(options, startAltitudePosition, flightPlanWithWayPointIndex.getFlightPlan());
            mavlinkFlightPlan.addFlightPlanWayPoints(flightPlanWithWayPointIndex);
            return mavlinkFlightPlan;
        } else {
            return null;
        }
    }

    private void addFlightPlanWayPoints(FlightPlanWithWayPointIndex flightPlanWithWayPointIndex) {
        FlightPlan flightPlan = flightPlanWithWayPointIndex.getFlightPlan();

        List<WayPoint> wayPoints = flightPlan.waypointsProperty().get();

        if (wayPoints == null || wayPoints.size() == 0) {
            return;
        }

        IElevationModel elevationModel = StaticInjector.getInstance(IElevationModel.class);

        int i0 = flightPlanWithWayPointIndex.getWayPointIndex();
        if (i0 < 0) {
            i0 = 0;
        } else if (i0 >= wayPoints.size()) {
            i0 = wayPoints.size() - 1;
        }

        Map<Waypoint, Double> triggerDistanceMap =
            (options.getCameraTriggerCommand()
                    == IMavlinkFlightPlanOptions.CameraTriggerCommand.SET_CAMERA_TRIGGER_DISTANCE)
                ? createTriggerDistanceMap(flightPlan)
                : new HashMap<>();

        Double lastSpeed = null;
        Angle lastRoll = null;
        Angle lastPitch = null;

        for (int i = i0; i < wayPoints.size(); i++) {
            WayPoint wp = wayPoints.get(i);

            double lat = wp.latProperty().get().convertTo(Unit.DEGREE).getValue().doubleValue();
            double lon = wp.lonProperty().get().convertTo(Unit.DEGREE).getValue().doubleValue();
            double altitude = wp.altAboveTakeoffProperty().get().convertTo(Unit.METER).getValue().doubleValue();

            Angle pitch = Angle.fromDegrees(wp.pitchProperty().get().convertTo(Unit.DEGREE).getValue().floatValue());
            Angle roll = Angle.fromDegrees(wp.rollProperty().get().convertTo(Unit.DEGREE).getValue().floatValue());
            Angle yaw = Angle.fromDegrees(wp.yawProperty().get().convertTo(Unit.DEGREE).getValue().floatValue());

            Position position = Position.fromDegrees(lat, lon, altitude);
            Duration holdDuration = flightPlan.stopAtWaypointsProperty().get() ? HOLD_AT_WAYPOINTS_DURATION : Duration.ZERO;

            // Speed
            if (options.getSetSpeedAtEachWaypoint()) {
                double groundSpeedMetersPerSecond =
                    wp.speedProperty().get().convertTo(Unit.METER_PER_SECOND).getValue().doubleValue();
                if (options.getSendAlsoNonChangedValues()
                        || lastSpeed == null
                        || lastSpeed != groundSpeedMetersPerSecond) {
                    lastSpeed = groundSpeedMetersPerSecond;
                    addMissionItemInfo(
                        MavlinkMissionItem.createChangeSpeedMissionItem(groundSpeedMetersPerSecond, true), wp, i);
                }
            }

            // Start waypoint / takeoff
            if (i == i0) {
                // Add takeoff waypoint:
                switch (options.getTakeoffCommand()) {
                case NONE:
                    break;
                case TAKEOFF:
                    addMissionItemInfo(MavlinkMissionItem.createTakeoffMissionItem(startAltitudePosition, true), wp, i);
                    break;
                case WAYPOINT_AND_TAKEOFF:
                    addMissionItemInfo(
                        MavlinkMissionItem.createWaypointMissionItem(
                            startAltitudePosition, yaw, holdDuration, (float)options.getAcceptanceRadiusMeters(), true),
                        wp,
                        i);
                    addMissionItemInfo(MavlinkMissionItem.createTakeoffMissionItem(startAltitudePosition, true), wp, i);
                    break;
                case VTOL_TAKEOFF:
                    addMissionItemInfo(
                        MavlinkMissionItem.createVtolTakeoffMissionItem(
                            startAltitudePosition,
                            VtolTransitionHeading.VTOL_TRANSITION_HEADING_NEXT_WAYPOINT,
                            Double.NaN,
                            true),
                        wp,
                        i);
                    break;
                case WAYPOINT_AND_VTOL_TAKEOFF:
                    addMissionItemInfo(
                        MavlinkMissionItem.createWaypointMissionItem(
                            startAltitudePosition, yaw, holdDuration, (float)options.getAcceptanceRadiusMeters(), true),
                        wp,
                        i);
                    addMissionItemInfo(
                        MavlinkMissionItem.createVtolTakeoffMissionItem(
                            startAltitudePosition,
                            VtolTransitionHeading.VTOL_TRANSITION_HEADING_NEXT_WAYPOINT,
                            Double.NaN,
                            true),
                        wp,
                        i);
                    break;
                default:
                    throw new IllegalArgumentException();
                }
            }

            // Camera target (ROI)
            if (options.getSendAlsoNonChangedValues()
                    || options.getGimbalAndAttitudeCommand()
                        == IMavlinkFlightPlanOptions.GimbalAndAttitudeCommand.SET_ROI) {
                Position roi = wp.getLegacyWaypoint().getTargetPosition(options.getDefaultRoiDistanceMeters());
                addMissionItemInfo(MavlinkMissionItem.createSetRoiLocationMissionItem(roi, true), wp, i);
            }

            // Waypoint
            addMissionItemInfo(
                MavlinkMissionItem.createWaypointMissionItem(
                    position, yaw, holdDuration, (float)options.getAcceptanceRadiusMeters(), true),
                wp,
                i);

            // Gimbal (pitch & roll only, yaw controlled by waypoint
            if (options.getGimbalAndAttitudeCommand()
                    == IMavlinkFlightPlanOptions.GimbalAndAttitudeCommand.MOUNT_CONTROL) {
                if (lastRoll == null || lastRoll != roll || lastPitch == null || lastPitch != pitch) {
                    lastRoll = roll;
                    lastPitch = pitch;
                    addMissionItemInfo(
                        MavlinkMissionItem.createMountControlMissionItem(pitch, roll, Angle.ZERO, true), wp, i);
                }
            }

            // Camera trigger
            Double triggerDistanceM = triggerDistanceMap.get(wp.getLegacyWaypoint());
            if (triggerDistanceM != null
                    || (wp.triggerImageHereCopterModeProperty().get()
                        && flightPlan
                            .getLegacyFlightplan()
                            .getHardwareConfiguration()
                            .getPlatformDescription()
                            .isInCopterMode())) {
                switch (options.getCameraTriggerCommand()) {
                case IMAGE_START_CAPTURE:
                    addMissionItemInfo(
                        MavlinkMissionItem.createImageStartCaptureMissionItem(Duration.INDEFINITE, 1, i, true), wp, i);
                    break;
                case DO_DIGICAM_CONTROL:
                    addMissionItemInfo(MavlinkMissionItem.createDoDigicamControlMissionItem(true), wp, i);
                    break;
                case SET_CAMERA_TRIGGER_DISTANCE:
                    {
                        if (triggerDistanceM == null
                                || !(triggerDistanceM < Float.MAX_VALUE)) { // Float.MAX_VALUE seems to crash ArduPilot
                            addMissionItemInfo(
                                MavlinkMissionItem.createSetCamTriggerDistMissionItem(
                                    0.0f, Duration.UNKNOWN, true, true),
                                wp,
                                i);
                        } else {
                            addMissionItemInfo(
                                MavlinkMissionItem.createSetCamTriggerDistMissionItem(
                                    triggerDistanceM.floatValue(), Duration.UNKNOWN, false, true),
                                wp,
                                i);
                        }
                    }

                    break;
                default:
                    break;
                }
            }

            // Landing or hover
            if (i == wayPoints.size() - 1) // last waypoint
            {
                Position fpLandingPos = flightPlan.landingPositionProperty().get();
                if (fpLandingPos == null
                        || (fpLandingPos.getLatitude().getDegrees() == 0.0
                            && fpLandingPos.getLongitude().getDegrees() == 0.0)) {
                    LOGGER.warn("No landing Position available");
                    continue;
                }

                LatLon landingLatLon = new LatLon(fpLandingPos.getLatitude(), fpLandingPos.getLongitude());

                landAutomatically = flightPlan.landAutomaticallyProperty().get();
                if (landAutomatically) {
                    // get landing position altitude:
                    Position takeoffPosition = flightPlan.takeoffPositionProperty().get();
                    double groundElevationRelativeToTakeoff;
                    if (takeoffPosition == null
                            || (takeoffPosition.getLatitude().getDegrees() == 0.0
                                && takeoffPosition.getLongitude().getDegrees() == 0.0)) {
                        groundElevationRelativeToTakeoff = 0.0;
                    } else {
                        double groundElevationWgs84 = elevationModel.getElevationAsGoodAsPossible(landingLatLon);
                        double takeoffElevationWgs84 = elevationModel.getElevationAsGoodAsPossible(takeoffPosition);
                        groundElevationRelativeToTakeoff = groundElevationWgs84 - takeoffElevationWgs84;
                    }

                    // land:
                    Position landingPosition = new Position(landingLatLon, groundElevationRelativeToTakeoff);

                    switch (options.getLandCommand()) {
                    case LAND:
                        addMissionItemInfo(MavlinkMissionItem.createLandMissionItem(landingPosition, true), wp, i);
                        break;
                    case VTOL_LAND:
                        addMissionItemInfo(MavlinkMissionItem.createVtolLandMissionItem(landingPosition, true), wp, i);
                        break;
                    case NONE:
                    default:
                        break;
                    }
                } else {
                    // hover at altitude
                    double landingHoverAltitudeRelativeToTakeoff =
                        flightPlan.landingHoverElevationProperty().get() != null
                            ? flightPlan
                                .landingHoverElevationProperty()
                                .get()
                                .convertTo(Unit.METER)
                                .getValue()
                                .doubleValue()
                            : 0.0;

                    if (landingHoverAltitudeRelativeToTakeoff > 0.0) {
                        Position landingPosition = new Position(landingLatLon, landingHoverAltitudeRelativeToTakeoff);
                        addMissionItemInfo(
                            MavlinkMissionItem.createWaypointMissionItem(
                                landingPosition, yaw, Duration.ZERO, (float)options.getAcceptanceRadiusMeters(), true),
                            wp,
                            i);
                    } else {
                        LOGGER.warn("Hover altitude for landing not set");
                    }
                }
            }
        }
    }

    /** Associates trigger distance values (in meters) with waypoints. Used for distance-based triggering. */
    private Map<Waypoint, Double> createTriggerDistanceMap(FlightPlan flightPlan) {
        Map<Waypoint, Double> res = new HashMap<>();

        Flightplan legacyFlightplan = flightPlan.getLegacyFlightplan();

        // skip for copter
        if (legacyFlightplan.getHardwareConfiguration().getPlatformDescription().isInCopterMode()) {
            return res;
        }

        double[] nextDistanceM = {Double.NaN};

        var visitor =
            new AFlightplanVisitor() {
                @Override
                public boolean visit(IFlightplanRelatedObject fpObj) {
                    if (fpObj instanceof Waypoint) {
                        if (!Double.isNaN(nextDistanceM[0])) {
                            res.put((Waypoint)fpObj, nextDistanceM[0]);
                            nextDistanceM[0] = Double.NaN;
                        }
                    } else if (fpObj instanceof Photo) {
                        double distM = ((Photo)fpObj).getDistanceInCm() * 0.01;

                        if (distM > 0) {
                            nextDistanceM[0] = distM;
                        }
                    }

                    return false;
                }
            };

        visitor.startVisit(legacyFlightplan);

        return res;
    }

    public String getDebugDescription() {
        if (missionItemInfos.isEmpty()) {
            return "Empty MAVLink mission";
        }

        StringBuilder res =
            new StringBuilder(
                "MAVLink mission with " + missionItemInfos.size() + " mission items" + System.lineSeparator());
        for (MissionItemInfo i : missionItemInfos) {
            res.append(i.toString()).append(System.lineSeparator());
        }

        return res.toString();
    }

    private void addMissionItemInfo(MavlinkMissionItem missionItem, WayPoint wayPoint, int wayPointIndex) {
        int seq = missionItemInfos.size();
        missionItemInfos.add(new MissionItemInfo(missionItem.atSequenceIndex(seq), wayPoint, wayPointIndex));
    }

    List<MavlinkMissionItem> getMissionItems() {
        return missionItemInfos.stream().map(x -> x.missionItem).collect(Collectors.toList());
    }

    int getMissionItemCount() {
        return missionItemInfos.size();
    }

    Position getStartAltitudePosition() {
        return startAltitudePosition;
    }

    boolean getLandAutomatically() {
        return landAutomatically;
    }

    int getWayPointIndexForMissionItemIndex(int missionItemIndex) throws IllegalArgumentException {
        if (missionItemIndex < 0 || missionItemIndex >= missionItemInfos.size()) {
            throw new IllegalArgumentException("Mission item index out of bounds");
        }

        MissionItemInfo missionItemInfo = missionItemInfos.get(missionItemIndex);
        return missionItemInfo.wayPointIndex;
    }

    IMavlinkFlightPlanOptions getOptions() {
        return options;
    }

    public FlightPlan getFlightPlan() {
        return flightPlan;
    }
}
