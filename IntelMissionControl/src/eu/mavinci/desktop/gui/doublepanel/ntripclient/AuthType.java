/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

public enum AuthType {
    NONE,
    BASIC,
    DIGEST;

    public static AuthType parse(String s) {
        if ("N".equalsIgnoreCase(s)) {
            return NONE;
        } else if ("B".equalsIgnoreCase(s)) {
            return BASIC;
        } else if ("D".equalsIgnoreCase(s)) {
            return DIGEST;
        } else {
            return null;
        }
    }
}
