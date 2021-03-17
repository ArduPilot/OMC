/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

public enum CallerThread {
    ANY,
    UI,
    BACKGROUND;

    @Override
    public String toString() {
        switch (this) {
        case UI:
            return "JavaFX application thread";
        case BACKGROUND:
            return "background thread";
        default:
            return "any thread";
        }
    }
}
