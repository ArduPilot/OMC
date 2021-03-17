/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.util.concurrent.Executor;
import java.util.function.Supplier;
import javafx.application.Platform;

public class UIDispatcher {

    private static Executor executor = Platform::runLater;
    private static Supplier<Boolean> isDispatcherThreadSupplier = Platform::isFxApplicationThread;

    public static void runLater(Runnable runnable) {
        executor.execute(runnable);
    }

    public static void run(Runnable runnable) {
        if (isDispatcherThreadSupplier.get()) {
            runnable.run();
        } else {
            executor.execute(runnable);
        }
    }

    public static boolean isDispatcherThread() {
        return isDispatcherThreadSupplier.get();
    }

    public static void setDispatcher(Executor executor, Supplier<Boolean> isDispatcherThreadSupplier) {
        UIDispatcher.executor = executor;
        UIDispatcher.isDispatcherThreadSupplier = isDispatcherThreadSupplier;
    }

}
