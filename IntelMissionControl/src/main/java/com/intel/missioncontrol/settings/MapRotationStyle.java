/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.Localizable;
import eu.mavinci.core.obfuscation.IKeepAll;

@Localizable
public enum MapRotationStyle implements IKeepAll {
    DEFAULT,
    INVERTED;

    public boolean isFlipViewPitchEnabled() {
        return this == INVERTED;
    }

    public boolean isFlipViewRotationEnabled() {
        return this == DEFAULT;
    }
}
