/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import java.time.Duration;
import java.util.Objects;

public class SpecialDuration {

    public static final Duration UNKNOWN = Duration.ofSeconds(Long.MIN_VALUE);
    public static final Duration INDEFINITE = Duration.ofSeconds(Long.MAX_VALUE, 999_999_999);

    public static boolean isIndefinite(Duration duration) {
        return Objects.equals(duration, INDEFINITE);
    }

    public static boolean isUnknown(Duration duration) {
        return Objects.equals(duration, UNKNOWN);
    }

}
