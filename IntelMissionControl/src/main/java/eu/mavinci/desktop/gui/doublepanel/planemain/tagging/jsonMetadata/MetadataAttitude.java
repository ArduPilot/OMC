/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonMetadata;

import eu.mavinci.core.obfuscation.IKeepAll;

public class MetadataAttitude implements IKeepAll {

    private final double roll;
    private final double pitch;
    private final double yaw;

    public MetadataAttitude(final double roll, final double pitch, final double yaw) {
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public String getRoll() {
        return Double.toString(roll);
    }

    public String getPitch() {
        return Double.toString(pitch);
    }

    public String getYaw() {
        return Double.toString(yaw);
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("MetadataAttitude [")
            .append("Roll=")
            .append(this.getRoll())
            .append(", pitch=")
            .append(this.getPitch())
            .append(", yaw=")
            .append(this.getYaw())
            .append("]")
            .toString();
    }
}
