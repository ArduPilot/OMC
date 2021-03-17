/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.sources.utils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;
import org.junit.rules.ExternalResource;

public class AirmapResource extends ExternalResource {
    public JSONObject readAirspaceData(String resourceName) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource(buildFullName(resourceName));
        if (resourceUrl != null) {
            try {
                byte[] resourceAsBytes = Files.readAllBytes(Paths.get(resourceUrl.toURI()));
                return new JSONObject(new String(resourceAsBytes));
            } catch (Exception e) {
            }
        }
        return new JSONObject();
    }

    private String buildFullName(String resourceName) {
        return String.format("com/intel/missioncontrol/airspaces/sources/airmap/%s.json", resourceName);
    }
}
