/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import java.util.Arrays;
import java.util.List;

public class StatusTextFilter {
    private final List<String> regexFilters;

    public StatusTextFilter(String... regexFilters) {
        this.regexFilters = Arrays.asList(regexFilters);
    }

    public boolean match(String text) {
        return regexFilters.stream().anyMatch(text::matches);
    }
}
