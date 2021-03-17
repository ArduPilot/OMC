/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerAll
        extends IAirplaneListenerAllExternal,
            IAirplaneListenerLoggingStateChanged,
            IAirplaneListenerGuiClose,
            IAirplaneListenerConnectionState,
            IAirplaneListenerLogReplay
// IBackendBroadcastListener // this istn send from backend after we are connected
{}
