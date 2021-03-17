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

public class MavlinkCameraSpecification {
    private final String vendor;
    private final String model;

    private MavlinkCameraSpecification(String vendor, String model) {
        this.vendor = vendor;
        this.model = model;
    }

    public static class Deserializer implements JsonDeserializer<MavlinkCameraSpecification> {
        @Override
        public MavlinkCameraSpecification deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = arg0.getAsJsonObject();
            JsonHelper helper = new JsonHelper(json);
            return new MavlinkCameraSpecification(helper.getString("vendor"), helper.getString("model"));
        }
    }

    public String getModel() {
        return model;
    }

    public String getVendor() {
        return vendor;
    }
}
