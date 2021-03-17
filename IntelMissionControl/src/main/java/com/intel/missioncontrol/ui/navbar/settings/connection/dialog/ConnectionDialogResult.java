/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.connection.dialog;

import com.intel.missioncontrol.drone.connection.MavlinkDroneConnectionItem;

public class ConnectionDialogResult {
    private final MavlinkDroneConnectionItem item;

    ConnectionDialogResult(MavlinkDroneConnectionItem item) {
        this.item = item;
    }

    public MavlinkDroneConnectionItem getItem() {
        return item;
    }
}
