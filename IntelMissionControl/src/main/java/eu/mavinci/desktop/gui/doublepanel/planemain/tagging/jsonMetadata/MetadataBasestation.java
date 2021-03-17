/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonMetadata;

import eu.mavinci.core.obfuscation.IKeepAll;

public class MetadataBasestation implements IKeepAll {

    private final double altitude;
    private final double latitude;
    private final double longitude;

    public MetadataBasestation(
            final double altitude, final double latitude, final double longitude) {
        this.altitude = altitude;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public double getAltitude() {
        return altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }


    @Override
    public String toString() {
        return new StringBuilder()
            .append("MetadataBaseStation [")
            .append("altitude=")
            .append(this.getAltitude())
            .append(", latitude=")
            .append(this.getLatitude())
            .append(", longitude=")
            .append(this.getLongitude())
            .append("]")
            .toString();
    }
}
