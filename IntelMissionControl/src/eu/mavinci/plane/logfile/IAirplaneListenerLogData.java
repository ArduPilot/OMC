/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.logfile;

import eu.mavinci.core.plane.listeners.IAirplaneListenerGuiClose;
import eu.mavinci.core.plane.listeners.IAirplaneListenerOrientation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.listeners.IAirplaneListenerGuiClose;
import eu.mavinci.core.plane.listeners.IAirplaneListenerOrientation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;

public interface IAirplaneListenerLogData
        extends IAirplaneListenerOrientation, IAirplaneListenerPosition, IAirplaneListenerGuiClose {}
