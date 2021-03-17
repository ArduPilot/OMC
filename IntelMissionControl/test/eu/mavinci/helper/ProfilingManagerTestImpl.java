/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.helper;

import com.logicstyle.samplr.Request;
import eu.mavinci.core.desktop.main.debug.IProfilingListener;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;

public class ProfilingManagerTestImpl implements IProfilingManager {

    @Override
    public void requestStarting(Request request) {}

    @Override
    public void requestFinished(Request request) {}

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void guiClosingDone() {}

    @Override
    public void addListener(IProfilingListener listener) {}

    @Override
    public void removeListener(IProfilingListener listener) {}

}
