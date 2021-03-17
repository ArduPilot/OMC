/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.connector;

import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkStatisticListener;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionState;

public class IRtkStatisticListenerSpy implements IRtkStatisticListener {

    NtripConnectionState conState;
    public volatile boolean timerTicked;

    @Override
    public void connectionStateChanged(NtripConnectionState conState, int msecUnitlReconnect) {
        this.conState = conState;
    }

    @Override
    public void timerTickWhileConnected(long msecConnected, long byteTransferredIn, long byteTransferredOut) {
        timerTicked = true;
    }

    @Override
    public void packageReceived(byte[] msg, int type) {
    }
}
