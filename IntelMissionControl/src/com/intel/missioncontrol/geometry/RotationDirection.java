/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

public enum RotationDirection {
    LEFT(true),
    RIGHT(false);

    private final boolean isLeft;

    private RotationDirection(boolean isLeft) {
        this.isLeft = isLeft;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public static RotationDirection valueOf(boolean isLeft) {
        if (isLeft) {
            return LEFT;
        }

        return RIGHT;
    }

}
