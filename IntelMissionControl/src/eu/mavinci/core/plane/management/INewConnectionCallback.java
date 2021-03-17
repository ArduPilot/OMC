/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.management;

import eu.mavinci.core.plane.ICAirplane;

public interface INewConnectionCallback {
    public void newTcpConnectionArchieved(ICAirplane plane);
}
