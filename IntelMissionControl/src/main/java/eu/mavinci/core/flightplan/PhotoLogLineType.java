/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public enum PhotoLogLineType {
    TIGGER,
    DELAY_30MS,
    FLASH;

    public boolean isBetterThan(PhotoLogLineType other) {
        return ordinal() > other.ordinal();
    }
}
