/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Objects;

class MavlinkConnectionProperties extends ConnectionProperties implements IMavlinkConnectionProperties {

    static class Deserializer implements JsonDeserializer<IConnectionProperties> {
        public IConnectionProperties deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();
            return context.deserialize(json, MavlinkConnectionProperties.class);
        }
    }

    private String mavlinkAutopilot;
    private String mavlinkType;

    @SuppressWarnings("unused")
    private MavlinkConnectionProperties() {}

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
}
