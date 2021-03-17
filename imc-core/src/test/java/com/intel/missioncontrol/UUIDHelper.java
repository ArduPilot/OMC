/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import java.util.UUID;

public class UUIDHelper {

    public static UUID getUUID(int value) {
        int digits = value == 0 ? 1 : (int)Math.floor(Math.log10(value) + 1);
        return UUID.fromString("00000000-0000-0000-0000-" + "0".repeat(12 - digits) + value);
    }

}
