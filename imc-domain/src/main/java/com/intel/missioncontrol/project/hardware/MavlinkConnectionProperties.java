/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;
import com.intel.missioncontrol.serialization.CompositeSerializable;
import com.intel.missioncontrol.serialization.CompositeSerializationContext;
import com.intel.missioncontrol.serialization.PrimitiveSerializationContext;
import java.util.Objects;

class MavlinkConnectionProperties extends ConnectionProperties implements IMavlinkConnectionProperties, CompositeSerializable {

    private String mavlinkAutopilot;
    private String mavlinkType;

    public MavlinkConnectionProperties(CompositeDeserializationContext context) {
        super(context);
        mavlinkAutopilot = context.readString("mavlinkAutopilot");
        mavlinkType = context.readString("mavlinkType");
    }

    public MavlinkConnectionProperties(
            String droneType, double linkLostTimeoutSeconds, String mavlinkAutopilot, String mavlinkType) {
        super(droneType, linkLostTimeoutSeconds);
        this.mavlinkAutopilot = mavlinkAutopilot;
        this.mavlinkType = mavlinkType;
    }

    public String getMavlinkAutopilot() {
        return mavlinkAutopilot;
    }

    public String getMavlinkType() {
        return mavlinkType;
    }

    @Override
    public void serialize(CompositeSerializationContext context) {
        context.writeString("mavlinkAutopilot", mavlinkAutopilot);
        context.writeString("mavlinkType", mavlinkType);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }

        if (!MavlinkConnectionProperties.class.isAssignableFrom(o.getClass())) {
            return false;
        }

        final MavlinkConnectionProperties other = (MavlinkConnectionProperties)o;

        return Objects.equals(this.mavlinkAutopilot, other.mavlinkAutopilot)
            && Objects.equals(this.mavlinkType, other.mavlinkType);
    }


    @Override
    public void serialize(PrimitiveSerializationContext context) {

    }
}
