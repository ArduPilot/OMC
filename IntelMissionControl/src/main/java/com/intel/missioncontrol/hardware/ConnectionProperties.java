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

class ConnectionProperties implements IConnectionProperties {

    static class Deserializer implements JsonDeserializer<IConnectionProperties> {
        @Override
        public IConnectionProperties deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();
            return context.deserialize(json, ConnectionProperties.class);
        }
    }

    ConnectionProperties() {}

    ConnectionProperties(String droneType, double linkLostTimeoutSeconds) {
        this.droneType = droneType;
        this.linkLostTimeoutSeconds = linkLostTimeoutSeconds;
    }

    private String droneType;
    private double linkLostTimeoutSeconds;

    @Override
    public String getDroneType() {
        return droneType;
    }

    @Override
    public double getLinkLostTimeoutSeconds() {
        return linkLostTimeoutSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!ConnectionProperties.class.isAssignableFrom(o.getClass())) {
            return false;
        }

        final ConnectionProperties other = (ConnectionProperties)o;

        return this.linkLostTimeoutSeconds == other.linkLostTimeoutSeconds;
    }
}
