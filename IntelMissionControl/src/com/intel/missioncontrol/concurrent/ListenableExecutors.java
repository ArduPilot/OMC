/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ListenableExecutors {

    private static Logger LOGGER = LogManager.getLogger(ListenableExecutors.class);

    private ListenableExecutors() {}

    private static final class DirectExecutor implements ListenableExecutor {
        @Override
        public void execute(Runnable runnable) {
            runnable.run();
        }

        @Override
        public ListenableFuture<Void> executeListen(Runnable runnable) {
            try {
                runnable.run();
                return Futures.immediateFuture(null);
            } catch (Throwable e) {
                LOGGER.debug("Exception in async execution:", e);
                return Futures.immediateFailedFuture(e);
            }
        }
    }

    private static final class FxExecutor implements ListenableExecutor {
        @Override
        public void execute(Runnable runnable) {
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(runnable);
            } else {
                runnable.run();
            }
        }

        @Override
        public ListenableFuture<Void> executeListen(Runnable runnable) {
            if (!Platform.isFxApplicationThread()) {
                return Dispatcher.postToUI(runnable);
            } else {
                try {
                    runnable.run();
                    return Futures.immediateFuture(null);
                } catch (Throwable e) {
                    LOGGER.debug("Exception in async execution:", e);
                    return Futures.immediateFailedFuture(e);
                }
            }
        }

        @Override
        public String toString() {
            return "JavaFX application thread";
        }
    }

    private static final ListenableExecutor DIRECT = new DirectExecutor();

    private static final ListenableExecutor PLATFORM = new FxExecutor();

    public static ListenableExecutor directExecutor() {
        return DIRECT;
    }

    public static ListenableExecutor platformExecutor() {
        return PLATFORM;
    }

}
