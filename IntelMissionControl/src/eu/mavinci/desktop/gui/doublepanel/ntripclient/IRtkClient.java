/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import eu.mavinci.desktop.bluetooth.BTService;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtcmParser.StatisticEntry;
import eu.mavinci.desktop.rs232.Rs232Params;
import gov.nasa.worldwind.geom.Position;

import java.net.InetAddress;
import java.util.Map;

public interface IRtkClient {
    boolean setConnection(NtripConnectionSettings con);

    void setConnectorTcpPort(InetAddress inetAddressBackend, int tcpPort);

    void setBroadcastPort(int udpPort);

    void disconnect();

    void connect(Rs232Params rs232params);

    void connect(int updPort);

    void connect(String stream);

    void connect(BTService btService);

    Map<Integer, StatisticEntry> getStatistics();

    RtcmParser getParser();

    void addListener(IRtkStatisticListener l);

    void removeListener(IRtkStatisticListener l);

    void setLastPosWGS84(Position pos);

    Position getLastPosWGS84();
}
