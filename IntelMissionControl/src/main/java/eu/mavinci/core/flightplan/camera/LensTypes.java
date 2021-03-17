/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.camera;

public enum LensTypes {
    STANDARD,
    FISH_EYE;

    public boolean needsMask() {
        return this == LensTypes.FISH_EYE;
    }
}
