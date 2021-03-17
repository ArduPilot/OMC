/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.dronefleet.mavlink.common.Statustext;

public class StatusTextException extends Throwable {
    private final Statustext statusText;

    StatusTextException(Statustext statusText) {
        this.statusText = statusText;
    }

    public Statustext getStatusText() {
        return statusText;
    }

    @Override
    public String toString() {
        return statusText.text();
    }

    @Override
    public String getMessage() {
        return statusText.text();
    }
}
