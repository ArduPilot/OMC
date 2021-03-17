/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.camera;

public enum FilterTypes {
    RGB,
    RGN;

    public String toPix4dIdentifier() {
        return this.name();
    }
}
