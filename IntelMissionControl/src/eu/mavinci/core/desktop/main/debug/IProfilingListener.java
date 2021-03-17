/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.desktop.main.debug;

import com.logicstyle.samplr.Request;

public interface IProfilingListener {

    public void requestStarted(Request request);

    public void requestFinished(Request request);

}
