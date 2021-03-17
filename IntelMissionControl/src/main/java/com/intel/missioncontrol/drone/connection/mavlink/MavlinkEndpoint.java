/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import com.intel.missioncontrol.drone.connection.TcpIpTransportType;
import java.net.InetSocketAddress;

public class MavlinkEndpoint {
    private final TcpIpTransportType tcpIpTransportType;
    private final InetSocketAddress address;
    private final int systemId;
    private final int componentId;

    public static final int AllComponentIds = 0;

    public static final MavlinkEndpoint UnspecifiedUdp =
        new MavlinkEndpoint(TcpIpTransportType.UDP, null, 0, AllComponentIds);

    public MavlinkEndpoint(
            TcpIpTransportType tcpIpTransportType, InetSocketAddress address, int systemId, int componentId) {
        this.tcpIpTransportType = tcpIpTransportType;
        this.address = address;
        this.systemId = systemId;
        this.componentId = componentId;
    }

    public static MavlinkEndpoint fromUdpBroadcastOnPort(int port) {
        return new MavlinkEndpoint(
            TcpIpTransportType.UDP, new InetSocketAddress("255.255.255.255", port), 0, AllComponentIds);
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public TcpIpTransportType getTcpIpTransportType() {
        return tcpIpTransportType;
    }

    public int getSystemId() {
        return systemId;
    }

    public int getComponentId() {
        return componentId;
    }
}
