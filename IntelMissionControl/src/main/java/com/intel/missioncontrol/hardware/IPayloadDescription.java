/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import java.util.List;

/** IPayloadDescription is a general concept of a plane payload, could have its own rotations. */
public interface IPayloadDescription extends INotificationObject {

    String ID_PROPERTY = "id";
    String ROLL_PROPERTY = "roll";
    String PITCH_PROPERTY = "pitch";
    String YAW_PROPERTY = "yaw";
    String IS_ROLL_FIXED_PROPERTY = "isRollFixed";
    String IS_PITCH_FIXED_PROPERTY = "isPitchFixed";
    String IS_YAW_FIXED_PROPERTY = "isYawFixed";
    String IS_ROLL_STABILIZED_PROPERTY = "isRollStabilized";
    String IS_PITCH_STABILIZED_PROPERTY = "isPitchStabilized";
    String IS_YAW_STABILIZED_PROPERTY = "isYawStabilized";
    String MAX_ROLL_PROPERTY = "maxRoll";
    String MAX_PITCH_PROPERTY = "maxPitch";
    String MAX_YAW_PROPERTY = "maxYaw";
    String MIN_ROLL_PROPERTY = "minRoll";
    String MIN_PITCH_PROPERTY = "minPitch";
    String MIN_YAW_PROPERTY = "minYaw";
    String ENFORCE_PITCH_RANGE_PROPERTY = "enforcePitchRange";
    String OFFSET_TO_TAIL_PROPERTY = "offsetToTail";
    String OFFSET_TO_RIGHT_WING_PROPERTY = "offsetToRightWing";
    String OFFSET_TO_SKY_PROPERTY = "offsetToSky";
    String MAVLINK_PARAMS_PROPERTY = "mavlinkParams";

    String getId();

    Quantity<Angle> getRoll();

    Quantity<Angle> getPitch();

    Quantity<Angle> getYaw();

    boolean isRollFixed();

    boolean isPitchFixed();

    boolean isYawFixed();

    boolean isRollStabilized();

    boolean isPitchStabilized();

    boolean isYawStabilized();

    Quantity<Angle> getMaxRoll();

    Quantity<Angle> getMinRoll();

    Quantity<Angle> getMaxPitch();

    Quantity<Angle> getMinPitch();

    Quantity<Angle> getMaxYaw();

    Quantity<Angle> getMinYaw();

    boolean getEnforcePitchRange();

    Quantity<Length> getOffsetToTail();

    Quantity<Length> getOffsetToRightWing();

    Quantity<Length> getOffsetToSky();

    List<MavlinkParam> getMavlinkParams();

    default IMutablePayloadDescription asMutable() {
        return (IMutablePayloadDescription)this;
    }

}
