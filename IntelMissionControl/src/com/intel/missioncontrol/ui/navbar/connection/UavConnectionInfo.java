/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

import eu.mavinci.core.plane.tcp.TCPConnection;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class UavConnectionInfo {
    public final String host;
    public final int port;
    public final String planeUdpUrl;

    public UavConnectionInfo(@NonNull String host, int port, String planeUdpUrl) {
        this.host = host;
        this.port = port;
        this.planeUdpUrl = planeUdpUrl;
    }

    public TCPConnection toLegacyConnection() {
        return new TCPConnection(host, port, planeUdpUrl);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = result * 31 + port;
        result = result * 31 + planeUdpUrl.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj != null && obj instanceof UavConnectionInfo) {
            UavConnectionInfo that = (UavConnectionInfo)obj;

            return host.equals(that.host) && port == that.port && planeUdpUrl.equals(that.planeUdpUrl);
        }

        return false;
    }
}
