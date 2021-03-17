/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

public enum NtripConnectionState {
    unconnected,
    connecting,
    waitingReconnect,
    connected
}
