/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripSourceTableEntry;

import java.net.URL;
import java.util.stream.Stream;

public interface NtripRequester {
    Stream<NtripSourceTableEntry> requestNtripStreams(URL url);
}
