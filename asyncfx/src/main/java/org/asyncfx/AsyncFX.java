/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx;

import java.lang.management.ManagementFactory;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class AsyncFX {

    public static class Accessor {
        public static void registerThread(Thread thread) {
            synchronized (threads) {
                threads.add(new WeakReference<>(thread));
            }
        }

        public static void trackAsyncSubmit(int count) {
            asyncSubmitCount.getAndAdd(count);
        }

        public static void trackElidedAsyncSubmit() {
            elidedAsyncSubmitCount.getAndIncrement();
        }

        public static void trackPlatformSubmit(long nanos) {
            if (breakAfterMillis > 0 && (int)(nanos / 1000000) > breakAfterMillis) {
                Runnable handler = breakRequestHandler;
                if (handler != null) {
                    handler.run();
                }
            }

            platformSubmitCount.getAndIncrement();
            platformSubmitNanos.getAndAdd(nanos);
        }

        public static void trackAwaitPlatform(long nanos) {
            if (breakAfterMillis > 0 && (int)(nanos / 1000000) > breakAfterMillis) {
                Runnable handler = breakRequestHandler;
                if (handler != null) {
                    handler.run();
                }
            }

            platformAwaitedCount.getAndIncrement();
            platformAwaitedNanos.getAndAdd(nanos);
        }
    }

    private static boolean checkJdwpArgument() {
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.contains("jdwp=")) {
                return true;
            }
        }

        return false;
    }

    private static final boolean DEBUGGER_ATTACHED = checkJdwpArgument();
    private static final List<WeakReference<Thread>> threads = new ArrayList<>();
    private static final AtomicInteger asyncSubmitCount = new AtomicInteger();
    private static final AtomicInteger elidedAsyncSubmitCount = new AtomicInteger();
    private static final AtomicInteger platformSubmitCount = new AtomicInteger();
    private static final AtomicLong platformSubmitNanos = new AtomicLong();
    private static final AtomicInteger platformAwaitedCount = new AtomicInteger();
    private static final AtomicLong platformAwaitedNanos = new AtomicLong();
    private static int breakAfterMillis;
    private static Runnable breakRequestHandler;
    private static boolean futureElisionOptimization = true;
    private static boolean verifyPropertyAccess = true;
    private static boolean runningTests;
    private static int deadlockDetectionTimeoutMillis;

    public static boolean isDebuggerAttached() {
        return DEBUGGER_ATTACHED;
    }

    public static List<Thread> getThreadList() {
        synchronized (threads) {
            return threads.stream().map(Reference::get).filter(Objects::nonNull).collect(Collectors.toList());
        }
    }

    public static int getThreadCount() {
        int count = 0;

        synchronized (threads) {
            ListIterator<WeakReference<Thread>> it = threads.listIterator();
            while (it.hasNext()) {
                Thread thread = it.next().get();
                if (thread == null) {
                    it.remove();
                } else if (thread.isAlive()) {
                    ++count;
                }
            }
        }

        return count;
    }

    public static boolean isRunningTests() {
        return runningTests;
    }

    @SuppressWarnings("SameParameterValue")
    static void setRunningTests(boolean enabled) {
        runningTests = enabled;
    }

    public static void setBreakAfterMillis(int millis) {
        breakAfterMillis = millis;
    }

    public static void setBreakRequestHandler(Runnable handler) {
        breakRequestHandler = handler;
    }

    public static boolean isFutureElisionOptimizationEnabled() {
        return futureElisionOptimization;
    }

    public static void setFutureElisionOptimizationEnabled(boolean enabled) {
        futureElisionOptimization = enabled;
    }

    public static boolean isVerifyPropertyAccess() {
        return verifyPropertyAccess;
    }

    public static void setVerifyPropertyAccess(boolean enabled) {
        verifyPropertyAccess = enabled;
    }

    public static int getDeadlockDetectionTimeoutMillis() {
        return deadlockDetectionTimeoutMillis;
    }

    public static void setDeadlockDetectionTimeoutMillis(int millis) {
        deadlockDetectionTimeoutMillis = millis;
    }

    // ------------------------------------------------------------------------
    // Performance reporting
    //
    public static int getAsyncSubmitCount() {
        return asyncSubmitCount.getAndSet(0);
    }

    public static int getElidedAsyncSubmitCount() {
        return elidedAsyncSubmitCount.getAndSet(0);
    }

    public static int getPlatformSubmitCount() {
        return platformSubmitCount.getAndSet(0);
    }

    public static int getPlatformAwaitedCount() {
        return platformAwaitedCount.getAndSet(0);
    }

    public static int getPlatformSubmitMillis() {
        return (int)(platformSubmitNanos.getAndSet(0) / 1000000);
    }

    public static int getPlatformAwaitedMillis() {
        return (int)(platformAwaitedNanos.getAndSet(0) / 1000000);
    }

}
