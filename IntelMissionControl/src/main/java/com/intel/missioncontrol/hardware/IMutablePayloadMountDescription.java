/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;

public interface IMutablePayloadMountDescription extends IPayloadMountDescription {

    void setRoll(Quantity<Angle> value);

    void setPitch(Quantity<Angle> value);

    void setYaw(Quantity<Angle> value);

    void setIsRollFixed(boolean value);

    void setIsPitchFixed(boolean value);

    void setIsYawFixed(boolean value);

    void setIsRollStabilized(boolean value);

    void setIsPitchStabilized(boolean value);

    void setIsYawStabilized(boolean value);

    void setMaxRoll(Quantity<Angle> value);

    void setMinRoll(Quantity<Angle> value);

    void setMaxPitch(Quantity<Angle> value);

    void setMinPitch(Quantity<Angle> value);

    void setMaxYaw(Quantity<Angle> value);

    void setMinYaw(Quantity<Angle> value);

    void setOffsetToTail(Quantity<Length> value);

    void setOffsetToRightWing(Quantity<Length> value);

    void setOffsetToSky(Quantity<Length> value);

}
