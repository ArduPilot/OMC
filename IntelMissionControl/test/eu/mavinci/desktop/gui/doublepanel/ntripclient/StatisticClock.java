/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class StatisticClock extends Clock {

    private final List<Long> times;
    private int index;

    StatisticClock(Collection<Long> times) {
        this.times = new ArrayList<>(times);
    }

    @Override
    public ZoneId getZone() {
        return null;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return null;
    }

    @Override
    public Instant instant() {
        Instant now = Instant.ofEpochMilli(times.get(index));
        index++;
        return now;
    }
}
