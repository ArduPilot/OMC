/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class MavlinkFlightPlanOptions implements IMavlinkFlightPlanOptions {
    static class Deserializer implements JsonDeserializer<IMavlinkFlightPlanOptions> {
        @Override
        public IMavlinkFlightPlanOptions deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            return context.deserialize(json, MavlinkFlightPlanOptions.class);
        }
    }

    private TakeoffCommand takeoffCommand;
    private LandCommand landCommand;
    private boolean autoDisarmBeforeTakeoff;
    private boolean setSpeedAtEachWaypoint;
    private boolean sendAlsoNonChangedValues;
    private GimbalAndAttitudeCommand gimbalAndAttitudeCommand;
    private CameraTriggerCommand cameraTriggerCommand;
    private double acceptanceRadiusMeters;
    private double acceptanceAngleDegrees;
    private double defaultRoiDistanceMeters;

    @SuppressWarnings("unused")
    private MavlinkFlightPlanOptions() {}

    /**
     * Options for mavlink mission generation.
     *
     * @param takeoffCommand The type of mission item to insert at the beginning of the mission.
     * @param landCommand The type of mission item to insert at the end of the flight plan if auto landing is enabled.
     * @param autoDisarmBeforeTakeoff True if a disarm command should be sent if the drone is already armed before
     *     takeoff.
     * @param setSpeedAtEachWaypoint True if a change speed mission item should be inserted for each IMC waypoint.
     * @param gimbalAndAttitudeCommand The mavlink command to be used for gimbal and/or attitude control to be inserted
     *     for each IMC waypoint.
     * @param cameraTriggerCommand The type of mission item that should be inserted for waypoints that require taking a
     *     photo.
     * @param acceptanceRadiusMeters Distance to waypoint (in meters) that determines when it counts as reached.
     * @param acceptanceAngleDegrees Angular accuracy required for heading at waypoints. Set param MIS_YAW_ERR (PX4
     *     only).
     * @param defaultRoiDistanceMeters Default distance from waypoint to camera target (region of interest) in meters,
     *     if not given by waypoint.
     * @param sendAlsoNonChangedValues If this parameter is true, sent for each IMC waypoints all values as MAVlink
     *     commands, even if the values haven't changed from the previous waypoint
     */
    public MavlinkFlightPlanOptions(
            TakeoffCommand takeoffCommand,
            LandCommand landCommand,
            boolean autoDisarmBeforeTakeoff,
            boolean setSpeedAtEachWaypoint,
            GimbalAndAttitudeCommand gimbalAndAttitudeCommand,
            CameraTriggerCommand cameraTriggerCommand,
            double acceptanceRadiusMeters,
            double acceptanceAngleDegrees,
            double defaultRoiDistanceMeters,
            boolean sendAlsoNonChangedValues) {
        this.takeoffCommand = takeoffCommand;
        this.landCommand = landCommand;
        this.autoDisarmBeforeTakeoff = autoDisarmBeforeTakeoff;
        this.setSpeedAtEachWaypoint = setSpeedAtEachWaypoint;
        this.gimbalAndAttitudeCommand = gimbalAndAttitudeCommand;
        this.cameraTriggerCommand = cameraTriggerCommand;
        this.acceptanceRadiusMeters = acceptanceRadiusMeters;
        this.acceptanceAngleDegrees = acceptanceAngleDegrees;
        this.defaultRoiDistanceMeters = defaultRoiDistanceMeters;
        this.sendAlsoNonChangedValues = sendAlsoNonChangedValues;
    }

    public static void verify(IMavlinkFlightPlanOptions options) throws HardwareConfigurationException {
        if (options == null) {
            throw new HardwareConfigurationException("Missing MavlinkFlightPlanOptions");
        }

        if (options.getTakeoffCommand() == null) {
            throw new HardwareConfigurationException("Invalid TakeoffCommand setting");
        }

        if (options.getLandCommand() == null) {
            throw new HardwareConfigurationException("Invalid LandCommand setting");
        }

        if (options.getGimbalAndAttitudeCommand() == null) {
            throw new HardwareConfigurationException("Invalid GimbalAndAttitudeCommand setting");
        }

        if (options.getCameraTriggerCommand() == null) {
            throw new HardwareConfigurationException("Invalid CameraTriggerCommand setting");
        }

        if (options.getAcceptanceRadiusMeters() < 0.0) {
            throw new HardwareConfigurationException("Invalid AcceptanceRadiusMeters setting");
        }

        if (options.getAcceptanceAngleDegrees() < 0.0) {
            throw new HardwareConfigurationException("Invalid AcceptanceAngleDegrees setting");
        }

        if (options.getDefaultRoiDistanceMeters() < 0.0) {
            throw new HardwareConfigurationException("Invalid DefaultRoiDistanceMeters setting");
        }
    }

    @Override
    public double getAcceptanceAngleDegrees() {
        return acceptanceAngleDegrees;
    }

    @Override
    public TakeoffCommand getTakeoffCommand() {
        return takeoffCommand;
    }

    @Override
    public LandCommand getLandCommand() {
        return landCommand;
    }

    @Override
    public boolean getAutoDisarmBeforeTakeoff() {
        return autoDisarmBeforeTakeoff;
    }

    @Override
    public boolean getSetSpeedAtEachWaypoint() {
        return setSpeedAtEachWaypoint;
    }

    @Override
    public GimbalAndAttitudeCommand getGimbalAndAttitudeCommand() {
        return gimbalAndAttitudeCommand;
    }

    @Override
    public CameraTriggerCommand getCameraTriggerCommand() {
        return cameraTriggerCommand;
    }

    @Override
    public double getAcceptanceRadiusMeters() {
        return acceptanceRadiusMeters;
    }

    @Override
    public double getDefaultRoiDistanceMeters() {
        return defaultRoiDistanceMeters;
    }

    @Override
    public boolean getSendAlsoNonChangedValues() {
        return sendAlsoNonChangedValues;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!MavlinkFlightPlanOptions.class.isAssignableFrom(o.getClass())) {
            return false;
        }

        final MavlinkFlightPlanOptions other = (MavlinkFlightPlanOptions)o;

        return this.takeoffCommand == other.takeoffCommand
            && this.landCommand == other.landCommand
            && this.autoDisarmBeforeTakeoff == other.autoDisarmBeforeTakeoff
            && this.setSpeedAtEachWaypoint == other.setSpeedAtEachWaypoint
            && this.gimbalAndAttitudeCommand == other.gimbalAndAttitudeCommand
            && this.cameraTriggerCommand == other.cameraTriggerCommand
            && this.acceptanceRadiusMeters == other.acceptanceRadiusMeters
            && this.acceptanceAngleDegrees == other.acceptanceAngleDegrees
            && this.defaultRoiDistanceMeters == other.defaultRoiDistanceMeters
            && this.sendAlsoNonChangedValues == other.sendAlsoNonChangedValues;
    }
}
