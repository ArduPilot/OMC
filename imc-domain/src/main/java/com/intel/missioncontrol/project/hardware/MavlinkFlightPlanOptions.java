/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.serialization.BinarySerializationContext;
import com.intel.missioncontrol.serialization.CompositeDeserializationContext;
import com.intel.missioncontrol.serialization.CompositeSerializable;
import com.intel.missioncontrol.serialization.CompositeSerializationContext;

public class MavlinkFlightPlanOptions implements IMavlinkFlightPlanOptions, CompositeSerializable {
    /**
     * Options for mavlink flight plan generation.
     *
     * @param prependMissionItem The type of mission item to insert at the beginning of the flight plan.
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
     */
    private PrependMissionItem prependMissionItem;

    private boolean setSpeedAtEachWaypoint;
    private GimbalAndAttitudeCommand gimbalAndAttitudeCommand;
    private CameraTriggerCommand cameraTriggerCommand;
    private double acceptanceRadiusMeters;
    private double acceptanceAngleDegrees;
    private double defaultRoiDistanceMeters;

    public MavlinkFlightPlanOptions(CompositeDeserializationContext context) {
        this.prependMissionItem = PrependMissionItem.valueOf(context.readString("prependMissionItem"));
        this.setSpeedAtEachWaypoint = context.readBoolean("");
        this.gimbalAndAttitudeCommand =
            GimbalAndAttitudeCommand.valueOf(context.readString("gimbalAndAttitudeCommand"));
        this.cameraTriggerCommand = CameraTriggerCommand.valueOf(context.readString("cameraTriggerCommand"));
        this.acceptanceRadiusMeters = context.readDouble("acceptanceRadiusMeters");
        this.acceptanceAngleDegrees = context.readDouble("acceptanceAngleDegrees");
        this.defaultRoiDistanceMeters = context.readDouble("defaultRoiDistanceMeters");
    }

    public static void verify(IMavlinkFlightPlanOptions options) throws HardwareConfigurationException {
        if (options == null) {
            throw new HardwareConfigurationException("Missing MavlinkFlightPlanOptions");
        }

        if (options.getPrependMissionItem() == null) {
            throw new HardwareConfigurationException("Invalid PrependMissionItem setting");
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
    public PrependMissionItem getPrependMissionItem() {
        return prependMissionItem;
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
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!MavlinkFlightPlanOptions.class.isAssignableFrom(o.getClass())) {
            return false;
        }

        final MavlinkFlightPlanOptions other = (MavlinkFlightPlanOptions)o;

        return this.prependMissionItem == other.prependMissionItem
            && this.setSpeedAtEachWaypoint == other.setSpeedAtEachWaypoint
            && this.gimbalAndAttitudeCommand == other.gimbalAndAttitudeCommand
            && this.cameraTriggerCommand == other.cameraTriggerCommand
            && this.acceptanceRadiusMeters == other.acceptanceRadiusMeters
            && this.acceptanceAngleDegrees == other.acceptanceAngleDegrees
            && this.defaultRoiDistanceMeters == other.defaultRoiDistanceMeters;
    }

    @Override
    public void serialize(CompositeSerializationContext context) {
        throw new NotImplementedException();
    }


    @Override
    public void serialize(BinarySerializationContext context) {

    }
}
