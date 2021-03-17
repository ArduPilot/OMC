/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.droidplanner.services.android.impl.core.mission.commands;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import java.util.List;
import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;

public class ImageStartCaptureImpl extends MissionCMD{

    private int numberOfImagesToCapture = (1);
    private int durationBetweenConsecutivePictures = 0;

    public ImageStartCaptureImpl(MissionItemImpl item) {
        super(item);
    }

    public ImageStartCaptureImpl(msg_mission_item msg, MissionImpl missionImpl) {
        super(missionImpl);
        unpackMAVMessage(msg);
    }

    public ImageStartCaptureImpl(MissionImpl missionImpl, int numberOfImagesToCapture) {
        super(missionImpl);
        this.numberOfImagesToCapture = numberOfImagesToCapture;
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_IMAGE_START_CAPTURE;
        mavMsg.param3 = numberOfImagesToCapture;
        mavMsg.param2 = durationBetweenConsecutivePictures;
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        numberOfImagesToCapture = Math.round(mavMsg.param3);
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.START_IMAGE_CAPTURE;
    }

    public int getNumberOfImagesToCapture() {
        return numberOfImagesToCapture;
    }

    public void setNumberOfImagesToCapture(int numberOfImagesToCapture) {
        this.numberOfImagesToCapture = numberOfImagesToCapture;
    }
}
