/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.manualcontrol;

import eu.mavinci.core.listeners.IListener;
import eu.mavinci.core.listeners.IListener;

public interface IJoystickReaderListener extends IListener {

    public void connectionStateChanged(boolean connected);

    public void newDataAvaliable(int values[]);
}
