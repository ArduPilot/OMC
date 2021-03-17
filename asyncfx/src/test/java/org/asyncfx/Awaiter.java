/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx;

import static org.asyncfx.TestBase.sleep;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

public class Awaiter {

    private final Thread currentThread = Thread.currentThread();
    private final boolean dumpThreads;
    private int signalsReceived;
    private AssertionFailedError error;

    public Awaiter() {
        this.dumpThreads = false;
    }

    public Awaiter(boolean dumpThreads) {
        this.dumpThreads = dumpThreads;
    }

    public void await(int signals) {
        await(signals, Duration.ofSeconds(5));
    }

    public synchronized void await(int signals, Duration timeout) {
        long remainingMillis = timeout.toMillis();

        while (signalsReceived < signals) {
            try {
                long millis = System.currentTimeMillis();
                wait(remainingMillis);
                remainingMillis = Math.max(0, remainingMillis - System.currentTimeMillis() + millis);
                if (remainingMillis == 0) {
                    if (dumpThreads) {
                        throw new AssertionFailedError("Waiter timed out. Thread dump:\n" + printStackTraces());
                    } else {
                        throw new AssertionFailedError("Waiter timed out.");
                    }
                }
            } catch (InterruptedException e) {
                if (dumpThreads) {
                    throw new AssertionFailedError("Waiter timed out. Thread dump:\n" + printStackTraces());
                } else {
                    throw new AssertionFailedError("Waiter timed out.");
                }
            }
        }

        if (error != null) {
            throw error;
        }

        signalsReceived = 0;
    }

    public void waitUntil(Supplier<Boolean> condition) {
        while (!condition.get()) {
            sleep(10);
        }
    }

    public synchronized void signal() {
        ++signalsReceived;
        notifyAll();
    }

    private synchronized void finish(AssertionFailedError error) {
        this.error = error;
        signalsReceived = Integer.MAX_VALUE;
        notifyAll();
    }

    public void assertTrue(boolean condition, String message) {
        if (postException()) {
            try {
                Assertions.assertTrue(condition, message);
            } catch (AssertionFailedError error) {
                finish(error);
                throw error;
            }
        } else {
            Assertions.assertTrue(condition, message);
        }
    }

    public void assertTrue(boolean condition) {
        assertTrue(condition, null);
    }

    public void assertFalse(String message, boolean condition) {
        assertTrue(!condition, message);
    }

    public void assertFalse(boolean condition) {
        assertTrue(!condition);
    }

    private void assertEqualsImpl(Object expected, Object actual, String message) {
        if (postException()) {
            try {
                Assertions.assertEquals(expected, actual, message);
            } catch (AssertionFailedError error) {
                finish(error);
                throw error;
            }
        } else {
            Assertions.assertEquals(expected, actual, message);
        }
    }

    public void assertEquals(Object expected, Object actual, String message) {
        assertEqualsImpl(expected, actual, message);
    }

    public void assertEquals(Object expected, Object actual) {
        assertEqualsImpl(expected, actual, null);
    }

    public void assertEquals(int expected, int actual, String message) {
        assertEqualsImpl(expected, actual, message);
    }

    public void assertEquals(int expected, int actual) {
        assertEqualsImpl(expected, actual, null);
    }

    public void assertEquals(long expected, long actual, String message) {
        assertEqualsImpl(expected, actual, message);
    }

    public void assertEquals(long expected, long actual) {
        assertEqualsImpl(expected, actual, null);
    }

    public void assertEquals(String message, String expected, String actual) {
        assertEqualsImpl(expected, actual, message);
    }

    public void assertEquals(String expected, String actual) {
        assertEqualsImpl(expected, actual, null);
    }

    private void assertNotEqualsImpl(Object unexpected, Object actual, String message) {
        if (postException()) {
            try {
                Assertions.assertNotEquals(unexpected, actual, message);
            } catch (AssertionFailedError error) {
                finish(error);
                throw error;
            }
        } else {
            Assertions.assertNotEquals(unexpected, actual, message);
        }
    }

    public void assertNotEquals(Object unexpected, Object actual, String message) {
        assertNotEqualsImpl(unexpected, actual, message);
    }

    public void assertNotEquals(Object unexpected, Object actual) {
        assertNotEqualsImpl(unexpected, actual, null);
    }

    public void assertNotEquals(int unexpected, int actual, String message) {
        assertNotEqualsImpl(unexpected, actual, message);
    }

    public void assertNotEquals(int unexpected, int actual) {
        assertNotEqualsImpl(unexpected, actual, null);
    }

    public void assertNotEquals(long unexpected, long actual, String message) {
        assertNotEqualsImpl(unexpected, actual, message);
    }

    public void assertNotEquals(long unexpected, long actual) {
        assertNotEqualsImpl(unexpected, actual, null);
    }

    public void assertNotEquals(String unexpected, String actual, String message) {
        assertNotEqualsImpl(unexpected, actual, message);
    }

    public void assertNotEquals(String unexpected, String actual) {
        assertNotEqualsImpl(unexpected, actual, null);
    }

    public void fail(String message) {
        if (postException()) {
            try {
                Assertions.fail(message);
            } catch (AssertionFailedError error) {
                finish(error);
                throw error;
            }
        } else {
            Assertions.fail(message);
        }
    }

    public void fail() {
        fail(null);
    }

    private boolean postException() {
        return currentThread != Thread.currentThread();
    }

    private String printStackTraces() {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            builder.append("\n");
            builder.append(entry.getKey());
            builder.append("\n");

            for (StackTraceElement element : entry.getValue()) {
                builder.append("\tat ").append(element).append("\n");
            }
        }

        return builder.toString();
    }

}
