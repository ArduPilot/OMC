/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.main;

public enum OsTypes {
    Win,
    Deb64,
    Linux64,
    Mac,
    Android;

    public boolean isWindows() {
        return this == Win;
    }

    public boolean isLinux() {
        return this == Linux64 || this == OsTypes.Deb64;
    }

    public boolean isMac() {
        return this == Mac;
    }

    public boolean isAndroid() {
        return this == Android;
    }

    public boolean isDesktop() {
        return !isAndroid();
    }
}
