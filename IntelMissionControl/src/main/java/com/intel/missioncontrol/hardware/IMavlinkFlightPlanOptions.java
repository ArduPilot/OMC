/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

public interface IMavlinkFlightPlanOptions {
    enum CameraTriggerCommand {
        NONE,
        IMAGE_START_CAPTURE, // use MAV_CMD_IMAGE_START_CAPTURE
        DO_DIGICAM_CONTROL, // use MAV_CMD_DO_DIGICAM_CONTROL
        SET_CAMERA_TRIGGER_DISTANCE // use MAV_CMD_DO_SET_CAM_TRIGG_DIST
    }

    enum PrependMissionItem {
        NONE,
        TAKEOFF,
        WAYPOINT_AND_TAKEOFF
    }

    enum GimbalAndAttitudeCommand {
        NONE,
        MOUNT_CONTROL, // use MAV_CMD_DO_MOUNT_CONTROL mavlink command
        SET_ROI // use MAV_CMD_DO_SET_ROI mavlink command
    }

    double getAcceptanceAngleDegrees();

    PrependMissionItem getPrependMissionItem();

    boolean getSetSpeedAtEachWaypoint();

    GimbalAndAttitudeCommand getGimbalAndAttitudeCommand();

    CameraTriggerCommand getCameraTriggerCommand();

    double getAcceptanceRadiusMeters();

    double getDefaultRoiDistanceMeters();
}
