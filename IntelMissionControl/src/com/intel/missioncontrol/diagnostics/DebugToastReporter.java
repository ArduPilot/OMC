/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.diagnostics;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.concurrent.DebugReentrantReadWriteLock;
import com.intel.missioncontrol.concurrent.DebugStampedLock;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import javafx.util.Duration;

public class DebugToastReporter {

    public static void install(IApplicationContext applicationContext) {
        DebugStampedLock.setTimeoutHandler(
            token -> {
                applicationContext.addToast(
                    Toast.of(ToastType.ALERT)
                        .setText("Lock timeout [id=" + token + "]. Debug information may be available in the log file.")
                        .setTimeout(Duration.seconds(30))
                        .create());
            });

        DebugReentrantReadWriteLock.setTimeoutHandler(
            token -> {
                applicationContext.addToast(
                    Toast.of(ToastType.ALERT)
                        .setText("Lock timeout [id=" + token + "]. Debug information may be available in the log file.")
                        .setTimeout(Duration.seconds(30))
                        .create());
            });
    }

}
