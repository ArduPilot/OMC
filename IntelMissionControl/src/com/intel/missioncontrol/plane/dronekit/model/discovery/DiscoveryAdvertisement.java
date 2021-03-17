/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model.discovery;

import java.util.Objects;

public class DiscoveryAdvertisement {
    public static final String PREFIX = "MAVLINK_ENDPOINT";

    public String connectionHost;
    public int connectionPort;
    public String connectionType;

    public String name;
    public String type;
    public String extra;


    // MAVLINK_ENDPOINT|conntype:hostname:port|namestring|type
    // e.g. MAVLINK_ENDPOINT|udp;10.0.0.1;1944|blueph1|MavinciPro
    public static DiscoveryAdvertisement parse(String str) {
        try {
            if (str == null || !str.startsWith(PREFIX)) {
                return null;
            }
            String[] parts = str.split("[|]");
            if (parts.length < 2) {
                // not enough components
                return null;
            }
            DiscoveryAdvertisement adv = new DiscoveryAdvertisement();
            String[] conn = parts[1].split("[;]");
            if (conn.length < 3) return null;
            adv.connectionType = conn[0];
            adv.connectionHost = conn[1];
            adv.connectionPort = Integer.parseInt(conn[2]);

            adv.name = parts.length > 2 ? parts[2] : "";
            adv.type = parts.length > 3 ? parts[3] : "";
            adv.extra = parts.length > 4 ? parts[4] : "";

            return adv;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscoveryAdvertisement that = (DiscoveryAdvertisement) o;
        return connectionPort == that.connectionPort &&
                Objects.equals(connectionHost, that.connectionHost) &&
                Objects.equals(connectionType, that.connectionType) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionHost, connectionPort, connectionType, name, type, extra);
    }

    public String serialize() {
        return PREFIX
                + "|" + connectionType + ";" + connectionHost + ";" + connectionPort
                + "|" + (name == null ? "" : name)
                + (type == null ? "" : "|" + type);
    }

    @Override
    public String toString() {
        return serialize();
    }
}
