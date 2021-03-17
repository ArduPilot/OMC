/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.droidplanner.services.android.impl.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import java.util.List;
import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;

public class MountControlImpl extends SpatialCoordItem{


    private double pitchAngle;
    private double rollAngle = (0);
    private double yawAngle;
    private int mavMountModeEnum = (2);

    public MountControlImpl(MissionItemImpl item) {
        super(item);
    }

    public MountControlImpl(MissionImpl missionImpl, LatLongAlt coord) {
        super(missionImpl, coord);
    }

    public MountControlImpl(msg_mission_item msg, MissionImpl missionImpl) {
        super(missionImpl, null);
        unpackMAVMessage(msg);
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_DO_MOUNT_CONTROL;
        mavMsg.param1 = (float) getPitchAngle();
        mavMsg.param2 = (float) rollAngle;
        mavMsg.param3 = (float) getYawAngle();
        mavMsg.param4 = (float) 0.0;
        mavMsg.x = (float) 0.0;
        mavMsg.y = (float) 0.0;
        mavMsg.z = (float) mavMountModeEnum;
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        super.unpackMAVMessage(mavMsg);
        setPitchAngle(mavMsg.param1);
        setRollAngle(mavMsg.param2);
        setYawAngle(mavMsg.param3);
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.MOUNT_CONTROL;
    }


    public double getPitchAngle() {
        return pitchAngle;
    }

    public void setPitchAngle(double pitchAngle) {
        this.pitchAngle = pitchAngle;
    }

    public void setRollAngle(double rollAngle){
        this.rollAngle = rollAngle;
    }

    public double getRollAngle() {
        return rollAngle;
    }

    public double getYawAngle() {
        return yawAngle;
    }

    public void setYawAngle(double yawAngle) {
        this.yawAngle = yawAngle;
    }

}
