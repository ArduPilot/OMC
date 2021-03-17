/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import io.dronefleet.mavlink.common.MavCmd;
import io.dronefleet.mavlink.common.MavFrame;
import io.dronefleet.mavlink.common.MavMissionType;
import io.dronefleet.mavlink.common.MavMountMode;
import io.dronefleet.mavlink.common.MissionItem;
import io.dronefleet.mavlink.common.MissionItemInt;
import io.dronefleet.mavlink.util.EnumValue;
import javafx.util.Duration;

public class MavlinkMissionItem {
    private final int seq;
    private final MavFrame frame;
    private final MavCmd command;
    private final int current;
    private final int autocontinue;
    private final float param1;
    private final float param2;
    private final float param3;
    private final float param4;
    private final double x;
    private final double y;
    private final double z;
    private final MavMissionType missionType;

    @Override
    public String toString() {
        return "MavlinkMissionItem{"
            + "seq="
            + seq
            + ", command="
            + command
            + ", x="
            + x
            + ", y="
            + y
            + ", z="
            + z
            + '}';
    }

    private MavlinkMissionItem(
            MavFrame frame,
            MavCmd command,
            int autocontinue,
            float param1,
            float param2,
            float param3,
            float param4,
            double x,
            double y,
            double z,
            MavMissionType missionType) {
        this(-1, frame, command, 0, autocontinue, param1, param2, param3, param4, x, y, z, missionType);
    }

    private MavlinkMissionItem(
            int seq,
            MavFrame frame,
            MavCmd command,
            int current,
            int autocontinue,
            float param1,
            float param2,
            float param3,
            float param4,
            double x,
            double y,
            double z,
            MavMissionType missionType) {
        this.seq = seq;
        this.frame = frame;
        this.command = command;
        this.current = current;
        this.autocontinue = autocontinue;
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.param4 = param4;
        this.x = x;
        this.y = y;
        this.z = z;
        this.missionType = missionType;
    }

    public MavlinkMissionItem atSequenceIndex(int newSeq) {
        int newCurrent = newSeq == 0 ? 1 : 0;
        return new MavlinkMissionItem(
            newSeq, frame, command, newCurrent, autocontinue, param1, param2, param3, param4, x, y, z, missionType);
    }

    static MavlinkMissionItem fromMissionItemInt(MissionItemInt m) {
        return new MavlinkMissionItem(
            m.seq(),
            m.frame().entry(),
            m.command().entry(),
            m.current(),
            m.autocontinue(),
            m.param1(),
            m.param2(),
            m.param3(),
            m.param4(),
            (double)m.x() * 1e-7,
            (double)m.y() * 1e-7,
            m.z(),
            m.missionType().entry());
    }

    public int getSeq() {
        return seq;
    }

    public MavFrame getFrame() {
        return frame;
    }

    public MavCmd getCommand() {
        return command;
    }

    public int getCurrent() {
        return current;
    }

    public int getAutocontinue() {
        return autocontinue;
    }

    public float getParam1() {
        return param1;
    }

    public float getParam2() {
        return param2;
    }

    public float getParam3() {
        return param3;
    }

    public float getParam4() {
        return param4;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public MavMissionType getMissionType() {
        return missionType;
    }

    MissionItem asMissionItemForRecipient(MavlinkEndpoint recipient) {
        if (seq < 0) {
            throw new IllegalArgumentException("MissionItem sequence number not set or invalid");
        }

        return MissionItem.builder()
            .targetSystem(recipient.getSystemId())
            .targetComponent(recipient.getComponentId())
            .seq(seq)
            .frame(frame)
            .command(command)
            .current(current)
            .autocontinue(autocontinue)
            .param1(param1)
            .param2(param2)
            .param3(param3)
            .param4(param4)
            .x((float)x)
            .y((float)y)
            .z((float)z)
            .missionType(missionType)
            .build();
    }

    MissionItemInt asMissionItemIntForRecipient(MavlinkEndpoint recipient) {
        if (seq < 0) {
            throw new IllegalArgumentException("MissionItem sequence number not set or invalid");
        }

        return MissionItemInt.builder()
            .targetSystem(recipient.getSystemId())
            .targetComponent(recipient.getComponentId())
            .seq(seq)
            .frame(frame)
            .command(command)
            .current(current)
            .autocontinue(autocontinue)
            .param1(param1)
            .param2(param2)
            .param3(param3)
            .param4(param4)
            .x((int)Math.round(x * 1e7))
            .y((int)Math.round(y * 1e7))
            .z((float)z)
            .missionType(missionType)
            .build();
    }

    public static MavlinkMissionItem createTakeoffMissionItem(Position position, boolean autocontinue) {
        return new MavlinkMissionItem(
            MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT,
            MavCmd.MAV_CMD_NAV_TAKEOFF,
            autocontinue ? 1 : 0,
            30.0f, // Minimum pitch, this is what QGroundControl sends.
            0.0f,
            0.0f,
            Float.NaN,
            position.latitude.degrees,
            position.longitude.degrees,
            position.elevation,
            MavMissionType.MAV_MISSION_TYPE_MISSION);
    }

    public static MavlinkMissionItem createChangeSpeedMissionItem(
            double groundSpeedMetersPerSecond, boolean autocontinue) {
        return new MavlinkMissionItem(
            MavFrame.MAV_FRAME_MISSION,
            MavCmd.MAV_CMD_DO_CHANGE_SPEED,
            autocontinue ? 1 : 0,
            1, // speed is ground speed
            (float)groundSpeedMetersPerSecond,
            -1, // no throttle change
            0, // absolute
            0,
            0,
            0,
            MavMissionType.MAV_MISSION_TYPE_MISSION);
    }

    public static MavlinkMissionItem createWaypointMissionItem(
            Position position, Angle yaw, Duration holdDuration, float acceptanceRadiusMeters, boolean autocontinue) {
        return new MavlinkMissionItem(
            MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT,
            MavCmd.MAV_CMD_NAV_WAYPOINT,
            autocontinue ? 1 : 0,
            (float)holdDuration.toSeconds(),
            acceptanceRadiusMeters,
            0.0f,
            (float)yaw.getDegrees(),
            position.latitude.degrees,
            position.longitude.degrees,
            position.elevation,
            MavMissionType.MAV_MISSION_TYPE_MISSION);
    }

    public static MavlinkMissionItem createMountControlMissionItem(
            Angle pitch, Angle roll, Angle yaw, boolean autocontinue) {
        return new MavlinkMissionItem(
            MavFrame.MAV_FRAME_MISSION,
            MavCmd.MAV_CMD_DO_MOUNT_CONTROL,
            autocontinue ? 1 : 0,
            (float)pitch.getDegrees(),
            (float)roll.getDegrees(),
            (float)yaw.getDegrees(),
            0.0f,
            0.0,
            0.0,
            EnumValue.of(MavMountMode.MAV_MOUNT_MODE_MAVLINK_TARGETING).value(),
            MavMissionType.MAV_MISSION_TYPE_MISSION);
    }

    public static MavlinkMissionItem createSetRoiLocationMissionItem(Position roi, boolean autocontinue) {
        return new MavlinkMissionItem(
            MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT,
            MavCmd.MAV_CMD_DO_SET_ROI_LOCATION,
            autocontinue ? 1 : 0,
            0.0f,
            0.0f,
            0.0f,
            0.0f,
            roi.latitude.degrees,
            roi.longitude.degrees,
            roi.elevation,
            MavMissionType.MAV_MISSION_TYPE_MISSION);
    }

    public static MavlinkMissionItem createImageStartCaptureMissionItem(
            Duration interval, int captureCount, int captureSequenceNumber, boolean autocontinue) {
        return new MavlinkMissionItem(
            MavFrame.MAV_FRAME_MISSION,
            MavCmd.MAV_CMD_IMAGE_START_CAPTURE,
            autocontinue ? 1 : 0,
            0.0f,
            (float)(interval.isIndefinite() ? 0 : interval.toSeconds()),
            captureCount,
            captureSequenceNumber,
            0.0,
            0.0,
            0.0,
            MavMissionType.MAV_MISSION_TYPE_MISSION);
    }

    public static MavlinkMissionItem createDoDigicamControlMissionItem(boolean autocontinue) {
        return new MavlinkMissionItem(
            MavFrame.MAV_FRAME_MISSION,
            MavCmd.MAV_CMD_DO_DIGICAM_CONTROL,
            autocontinue ? 1 : 0,
            0.0f,
            0.0f,
            0.0f,
            0.0f,
            1.0f, // "Shooting Command"
            0.0,
            0.0,
            MavMissionType.MAV_MISSION_TYPE_MISSION);
    }

    public static MavlinkMissionItem createSetCamTriggerDistMissionItem(
            float triggerDistanceMeters,
            Duration shutterIntegrationTime,
            boolean triggerOnceImmediately,
            boolean autocontinue) {
        return new MavlinkMissionItem(
            MavFrame.MAV_FRAME_MISSION,
            MavCmd.MAV_CMD_DO_SET_CAM_TRIGG_DIST,
            autocontinue ? 1 : 0,
            Float.isFinite(triggerDistanceMeters) ? triggerDistanceMeters : 0.0f,
            (shutterIntegrationTime.isIndefinite() || shutterIntegrationTime.isUnknown()
                ? 0.0f
                : (float)shutterIntegrationTime.toMillis()),
            triggerOnceImmediately ? 1.0f : 0.0f,
            0.0f,
            0.0,
            0.0,
            0.0,
            MavMissionType.MAV_MISSION_TYPE_MISSION);
    }

    public static MavlinkMissionItem createLandMissionItem(Position position, boolean autocontinue) {
        return new MavlinkMissionItem(
            MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT,
            MavCmd.MAV_CMD_NAV_LAND,
            autocontinue ? 1 : 0,
            0, // default minimum target altitude if landing is aborted
            0, // PRECISION_LAND_MODE_DISABLED
            Float.NaN, // Empty
            Float.NaN, // Desired yaw angle
            position.latitude.degrees,
            position.longitude.degrees,
            position.elevation, // altitude of landing point relative to takeoff altitude. 0 if same elevation as
                                // takeoff.
            MavMissionType.MAV_MISSION_TYPE_MISSION);
    }
}
