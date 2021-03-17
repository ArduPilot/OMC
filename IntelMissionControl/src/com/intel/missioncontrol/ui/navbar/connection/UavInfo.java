/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

import org.checkerframework.checker.nullness.qual.NonNull;

public class UavInfo {
    public final String serialNumber;
    public final int hardwareRevision;
    public final String hardwareType;
    public final String softwareRevision;
    public final int protocolVersion;

    public UavInfo(
            @NonNull String serialNumber,
            int hardwareRevision,
            @NonNull String hardwareType,
            @NonNull String softwareRevision,
            int protocolVersion) {
        this.serialNumber = serialNumber;
        this.hardwareRevision = hardwareRevision;
        this.hardwareType = hardwareType;
        this.softwareRevision = softwareRevision;
        this.protocolVersion = protocolVersion;
    }

    @Override
    public int hashCode() {
        int result = serialNumber.hashCode();
        result = result * 31 + hardwareRevision;
        result = result * 31 + hardwareType.hashCode();
        result = result * 31 + softwareRevision.hashCode();
        result = result * 31 + protocolVersion;
        return protocolVersion;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj != null && obj instanceof UavInfo) {
            UavInfo that = (UavInfo)obj;

            return serialNumber.equals(that.serialNumber)
                && hardwareRevision == that.hardwareRevision
                && hardwareType.equals(that.hardwareType)
                && softwareRevision.equals(that.softwareRevision)
                && protocolVersion == that.protocolVersion;
        }

        return false;
    }
}
