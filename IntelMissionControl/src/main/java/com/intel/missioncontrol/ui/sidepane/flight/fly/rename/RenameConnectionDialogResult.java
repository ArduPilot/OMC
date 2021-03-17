/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.rename;

public class RenameConnectionDialogResult {
    private String newString;

    public RenameConnectionDialogResult(String newString) {
        this.newString = newString;
    }

    public String getNewString() {
        return newString;
    }
}
