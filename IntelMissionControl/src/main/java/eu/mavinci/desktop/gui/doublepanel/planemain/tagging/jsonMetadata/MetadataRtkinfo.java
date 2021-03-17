/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonMetadata;

import eu.mavinci.core.obfuscation.IKeepAll;

public class MetadataRtkinfo implements IKeepAll {

    private final MetadataBasestation BaseStation;
    private final String fixtype;

    public MetadataRtkinfo(final MetadataBasestation base_station, final String fixtype) {
        this.BaseStation = base_station;
        this.fixtype = fixtype;
    }

    public MetadataBasestation getBaseStation() {
        return this.BaseStation;
    }

    public String getFixType() {
        return fixtype;
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("MetadataRtkinfo [")
            .append("BaseStation=")
            .append(this.getBaseStation())
            .append(", fixtype=")
            .append(this.getFixType())
            .append("]")
            .toString();
    }
}
