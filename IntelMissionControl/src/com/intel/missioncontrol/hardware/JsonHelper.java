/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JsonHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonHelper.class);

    private final JsonObject json;

    JsonHelper(JsonObject json) {
        this.json = json;
    }

    String getString(String key) {
        JsonElement element = json.get(key);
        if (element == null) {
            warnMissingKey(key);
            return null;
        }

        return element.getAsString();
    }

    boolean getBoolean(String key) {
        try {
            JsonElement element = json.get(key);
            if (element == null) {
                warnMissingKey(key);
                return false;
            }

            return element.getAsBoolean();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Illegal value for key " + key, e);
            return false;
        }
    }

    double getDouble(String key) {
        try {
            JsonElement element = json.get(key);
            if (element == null) {
                warnMissingKey(key);
                return 0;
            }

            return element.getAsDouble();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Illegal value for key " + key, e);
            return 0;
        }
    }

    int getInteger(String key) {
        try {
            JsonElement element = json.get(key);
            if (element == null) {
                warnMissingKey(key);
                return 0;
            }

            return element.getAsInt();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Illegal value for key " + key, e);
            return 0;
        }
    }

    static void warnMissingKey(String key) {
        LOGGER.warn("Missing key: " + key);
    }

}
