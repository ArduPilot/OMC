/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.disconnect;

public class DisconnectDialogResult {
    private boolean confirmed;

    public DisconnectDialogResult(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean getConfirmed() {
        return confirmed;
    }
}
