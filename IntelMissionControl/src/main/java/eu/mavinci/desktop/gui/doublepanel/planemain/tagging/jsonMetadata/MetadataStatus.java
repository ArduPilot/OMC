/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonMetadata;

import eu.mavinci.core.obfuscation.IKeepAll;

public class MetadataStatus implements IKeepAll {

    private final MetadataGPS gps;
    private final MetadataAttitude airframe;
    private final MetadataAttitude gimbal;

    public MetadataStatus(final MetadataGPS gps, final MetadataAttitude airframe, final MetadataAttitude gimbal) {
        this.gps = gps;
        this.airframe = airframe;
        this.gimbal = gimbal;
    }

    public MetadataGPS getGPS() {
        return this.gps;
    }

    public MetadataAttitude getAirframe() {
        return this.airframe;
    }

    public MetadataAttitude getGimbal() {
        return this.gimbal;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("MetadataStatus [")
                .append("GPS=").append(this.getGPS())
                .append(", airframe=").append(this.getAirframe())
                .append(", gimbal=").append(this.getGimbal())
                .append("]").toString();
    }
}
