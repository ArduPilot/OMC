/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.diagnostics;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import org.asyncfx.concurrent.DebugReentrantReadWriteLock;
import org.asyncfx.concurrent.DebugStampedLock;

public class DebugToastReporter {

    public static void install(IApplicationContext applicationContext) {
        DebugStampedLock.setTimeoutHandler(
            token -> {
                applicationContext.addToast(
                    Toast.of(ToastType.ALERT)
                        .setText("Lock timeout [id=" + token + "]. Debug information may be available in the log file.")
                        .setTimeout(Toast.LONG_TIMEOUT)
                        .setShowIcon(true)
                        .create());
            });

        DebugReentrantReadWriteLock.setTimeoutHandler(
            token -> {
                applicationContext.addToast(
                    Toast.of(ToastType.ALERT)
                        .setText("Lock timeout [id=" + token + "]. Debug information may be available in the log file.")
                        .setTimeout(Toast.LONG_TIMEOUT)
                        .setShowIcon(true)
                        .create());
            });
    }

}
