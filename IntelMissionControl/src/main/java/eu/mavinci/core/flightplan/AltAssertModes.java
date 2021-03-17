/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public enum AltAssertModes {
    unasserted,
    jump,
    linear;

    public static AltAssertModes parse(String value) {
        try {
            return valueOf(value);
        } catch (Exception e) {
        }

        if (Boolean.parseBoolean(value)) {
            return jump;
        } else {
            return unasserted;
        }
    }

    public static void main(String[] args) {
        System.out.println(parse("true"));
    }

    @Override
    public String toString() {
        switch (this) { // backward compatibility
        case unasserted:
            return "false";
        case jump:
            return "true";
        default:
            return super.toString();
        }
    }
}
