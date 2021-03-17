/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.desktop.main.debug;

import com.logicstyle.samplr.Request;

public interface IProfilingManager {

    public void requestStarting(Request request);

    public void requestFinished(Request request);

    public boolean isActive();

    public void guiClosingDone();

    public void addListener(IProfilingListener listener);

    public void removeListener(IProfilingListener listener);

}
