/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common;

import com.google.gson.Gson;
import com.intel.missioncontrol.ui.sidepane.flight.checklist.Checklist;
import eu.mavinci.desktop.main.debug.Debug;

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
            return gson.fromJson(new InputStreamReader(resource), Checklist[].class);
        } catch (IOException ie) {
            Debug.getLog().log(Debug.WARNING, "CheckListUtils resource", ie);
        } catch (NullPointerException e) {
            Debug.getLog().log(Debug.WARNING, "CheckListUtils null", e);
        }

        return null;
    }

}
