/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import java.util.concurrent.atomic.AtomicInteger;

public class AlertCounter {

    private final AtomicInteger redAlertCount = new AtomicInteger(0);
    private final AtomicInteger yellowAlertCount = new AtomicInteger(0);

    public void decrementAlertCount(AlertLevel oldAlert) {
        if ((oldAlert == null) || (oldAlert == AlertLevel.GREEN)) {
            return;
        }

        if (oldAlert == AlertLevel.YELLOW) {
            yellowAlertCount.decrementAndGet();
            return;
        }

        redAlertCount.decrementAndGet();
    }

    public void incrementAlertCount(AlertLevel newAlert) {
        if ((newAlert == null) || (newAlert == AlertLevel.GREEN)) {
            return;
        }

        if (newAlert == AlertLevel.YELLOW) {
            yellowAlertCount.incrementAndGet();
            return;
        }

        redAlertCount.incrementAndGet();
    }

    public long getAlertTotalCount() {
        int redCount = redAlertCount.get();
        redCount = ((redCount < 0) ? (0) : (redCount));

        int yellowCount = yellowAlertCount.get();
        yellowCount = ((yellowCount < 0) ? (0) : (yellowCount));

        return (redCount + yellowCount);
    }

    public AlertLevel getAlert() {
        if (redAlertCount.get() > 0) {
            return AlertLevel.RED;
        }

        if (yellowAlertCount.get() > 0) {
            return AlertLevel.YELLOW;
        }

        return AlertLevel.GREEN;
    }

}
