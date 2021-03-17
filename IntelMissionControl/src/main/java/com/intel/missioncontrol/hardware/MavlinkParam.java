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

public class MavlinkParam {
    private final String id;
    private final String value;
    private final String type;

    private MavlinkParam(String id, String value, String type) {
        this.id = id;
        this.value = value;
        this.type = type;
    }

    public static class Deserializer implements JsonDeserializer<MavlinkParam> {
        @Override
        public MavlinkParam deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = arg0.getAsJsonObject();
            JsonHelper helper = new JsonHelper(json);
            return new MavlinkParam(helper.getString("id"), helper.getString("value"), helper.getString("type"));
        }
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
