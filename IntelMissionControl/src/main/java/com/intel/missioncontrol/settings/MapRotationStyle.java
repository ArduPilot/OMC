/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.Localizable;

public enum MapRotationStyle implements Localizable {
    DEFAULT,
    INVERTED;

    public boolean isFlipViewPitchEnabled() {
        return this == INVERTED;
    }

    public boolean isFlipViewRotationEnabled() {
        return this == DEFAULT;
    }
}
