/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.Localizable;
import eu.mavinci.core.obfuscation.IKeepAll;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Localizable
public enum OperationLevel implements IKeepAll {
    USER,
    TECHNICIAN,
    DEBUG;

    public static List<OperationLevel> getAllowedLevels(OperationLevel maxLevel) {
        List<OperationLevel> allowedLevels = new ArrayList<>();
        // Append values from the top level to the bottom
        if(maxLevel == null) {
            maxLevel = USER;
        }
        switch (maxLevel) {
        case DEBUG:
            allowedLevels.add(OperationLevel.DEBUG);
        case TECHNICIAN:
            allowedLevels.add(OperationLevel.TECHNICIAN);
        case USER:
        default:
            allowedLevels.add(OperationLevel.USER);
            break;
        }
        // Reverse the order
        Collections.reverse(allowedLevels);
        return allowedLevels;
    }

    public static OperationLevel validateLevel(OperationLevel currentLevel, List<OperationLevel> allowedLevels) {
        // Assume allowed levels is not empty
        return allowedLevels.contains(currentLevel) ? currentLevel : allowedLevels.get(0);
    }

}
