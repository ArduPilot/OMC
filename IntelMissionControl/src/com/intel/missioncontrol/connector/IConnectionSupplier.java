/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.connector;

import eu.mavinci.desktop.gui.doublepanel.ntripclient.ConnectionObjects;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionSettings;

public interface IConnectionSupplier {
    ConnectionObjects getNtripConnection(NtripConnectionSettings connectionSettings);
}
