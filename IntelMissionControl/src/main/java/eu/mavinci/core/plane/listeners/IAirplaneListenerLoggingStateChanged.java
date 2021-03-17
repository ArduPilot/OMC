/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerLoggingStateChanged extends IAirplaneListener {

    public void loggingStateChangedTCP(boolean tcp_log_active);

    public void loggingStateChangedFLG(boolean plane_log_active);
}
