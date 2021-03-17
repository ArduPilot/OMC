/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import java.util.EnumMap;
import java.util.Map;

public class GpsFixTypeCounter {

    private final Map<GPSFixType, Counter> counters = new EnumMap<>(GPSFixType.class);

    public long getCount(GPSFixType type) {
        if (type == null) {
            return 0L;
        }

        Counter counter = counters.get(type);

        if (counter == null) {
            return 0L;
        }

        return counter.getCount();
    }

    public void increment(GPSFixType type) {
        if (type == null) {
            return;
        }

        Counter counter = counters.computeIfAbsent(type, t -> new Counter());
        counter.increment();
    }

    public void clear() {
        counters.clear();
    }

    private static class Counter {

        private long count;

        public long getCount() {
            return count;
        }

        public void increment() {
            count++;
        }

    }

}
