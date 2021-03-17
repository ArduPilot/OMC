/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.commands;

import javafx.application.Platform;

class CommandHelper {

    static void verifyAccess() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                "Commands can only be invoked on the JavaFX application thread [currentThread = "
                    + Thread.currentThread().getName()
                    + "].");
        }
    }

}
