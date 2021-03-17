/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common;

import com.google.gson.Gson;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.Checklist;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CheckListUtils {

    private static final String PREDEFINED_CHECKLIST_TEMPLATES_LOCATION =
        "com/intel/missioncontrol/preflight.checks/config.json";

    public static Checklist[] readAllCheckLists() {
        Gson gson = new Gson();

        try (InputStream resource =
            CheckListUtils.class.getClassLoader().getResourceAsStream(PREDEFINED_CHECKLIST_TEMPLATES_LOCATION)) {
            if (resource == null) {
                throw new RuntimeException(PREDEFINED_CHECKLIST_TEMPLATES_LOCATION + " not found.");
            }

            Checklist[] list = gson.fromJson(new InputStreamReader(resource), Checklist[].class);
            return list != null ? list : new Checklist[0];
        } catch (IOException ignored) {
        }

        return new Checklist[0];
    }

}
