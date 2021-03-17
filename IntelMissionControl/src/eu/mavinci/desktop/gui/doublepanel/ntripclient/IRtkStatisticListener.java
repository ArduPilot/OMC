/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

public interface IRtkStatisticListener {

    public void connectionStateChanged(NtripConnectionState conState, int msecUnitlReconnect);

    public void timerTickWhileConnected(long msecConnected, long byteTransferredIn, long byteTransferredOut);

    public void packageReceived(byte[] msg, int type);
}
