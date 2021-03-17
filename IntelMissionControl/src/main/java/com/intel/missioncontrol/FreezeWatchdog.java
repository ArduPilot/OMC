/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;
import javafx.application.Platform;
import org.asyncfx.AsyncFX;
import org.asyncfx.concurrent.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FreezeWatchdog extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreezeWatchdog.class);
    // If the UI doesn't respond within 5 seconds, trigger a full thread dump.
    public static final long FREEZE_TOLERANCE_NANOS = 5L * 1000 * 1000 * 1000;
    // After a thread dump, wait 60 seconds until the next thread dump.
    public static final long AFTER_FREEZE_DELAY = 60L * 1000 * 1000 * 1000;

    private static FreezeWatchdog watchdog = null;

    public static synchronized void spawnUnlessDebugger(Dispatcher... dispatchers) {
        if (AsyncFX.isDebuggerAttached()) {
            LOGGER.warn("Freeze Watchdog detected debugger, not starting.");
        } else {
            spawn(dispatchers);
        }
    }

    public static synchronized void spawn(Dispatcher... dispatchers) {
        watchdog = new FreezeWatchdog(dispatchers);
        watchdog.setDaemon(true);
        LOGGER.info("Freeze Watchdog Starting.");
        watchdog.start();
    }

    public static synchronized void halt() {
        LOGGER.info("Freeze Watchdog Stopping.");
        watchdog = null;
    }

    private Map<Dispatcher, Runnable> dispatchers;
    private ConcurrentHashMap<Dispatcher, Object> pending = new ConcurrentHashMap<>();

    public FreezeWatchdog(Dispatcher... dispatchers) {
        super("freeze-watchdog");
        this.dispatchers = new java.util.HashMap<>();
        for (Dispatcher dispatcher : dispatchers) {
            this.dispatchers.put(
                dispatcher,
                () -> {
                    FreezeWatchdog.this.pending.remove(dispatcher);
                    LockSupport.unpark(FreezeWatchdog.this);
                });
        }
    }

    @Override
    public void run() {
        try {
            while (watchdog == this) {
                long deadline = System.nanoTime() + FREEZE_TOLERANCE_NANOS;
                pending.putAll(dispatchers);
                for (Map.Entry<Dispatcher, Runnable> entry : dispatchers.entrySet()) {
                    entry.getKey().runLater(entry.getValue());
                }

                while (!pending.isEmpty()) {
                    LockSupport.parkNanos(this, deadline - System.nanoTime());
                    if (deadline < System.nanoTime()) {
                        LOGGER.warn(
                            "Freeze Watchdog detected freeze for threads "
                                + pending.keySet()
                                + ",  thread dump follows:\n"
                                + Arrays.stream(ManagementFactory.getThreadMXBean().dumpAllThreads(true, true))
                                    .map(ThreadInfo::toString)
                                    .collect(Collectors.joining("\n")));
                        deadline = System.nanoTime() + AFTER_FREEZE_DELAY;
                    }
                }

                LOGGER.debug(
                    "Freeze Watchdog received all pongs after {}ms.",
                    (System.nanoTime() - (deadline - FREEZE_TOLERANCE_NANOS)) / 1000 / 1000);
                while (System.nanoTime() < deadline) {
                    try {
                        Thread.sleep((deadline - System.nanoTime()) / 1000 / 1000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }

            LOGGER.info("Freeze Watchdog stopped.");
        } catch (Throwable t) {
            LOGGER.error("Freeze Watchdog crashed", t);
        }
    }
}
