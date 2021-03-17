/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

public enum FilterTypes {
    RGB,
    RGN;

    public String toPix4dIdentifier() {
        return this.name();
    }
}
