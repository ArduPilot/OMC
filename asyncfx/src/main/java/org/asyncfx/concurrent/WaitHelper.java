/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.time.Duration;
import java.util.function.Supplier;

public class WaitHelper {

    /** Waits in a loop and checks the condition to protect against spurious wake-ups. */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static void wait(Object obj, Supplier<Boolean> condition) {
        synchronized (obj) {
            while (!condition.get()) {
                try {
                    obj.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /** Waits in a loop and checks the condition to protect against spurious wake-ups. */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static void wait(Object obj, Supplier<Boolean> condition, Duration timeout) {
        synchronized (obj) {
            while (!condition.get()) {
                try {
                    obj.wait(timeout.toMillis());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /** Waits in a loop and checks the condition to protect against spurious wake-ups. */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static <T> T waitAndGet(Object obj, Supplier<Boolean> condition, Supplier<T> supplier) {
        synchronized (obj) {
            while (!condition.get()) {
                try {
                    obj.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            return supplier.get();
        }
    }

    /** Waits in a loop and checks the condition to protect against spurious wake-ups. */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static <T> T wait(Object obj, Supplier<Boolean> condition, Supplier<T> supplier, Duration timeout) {
        synchronized (obj) {
            while (!condition.get()) {
                try {
                    obj.wait(timeout.toMillis());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            return supplier.get();
        }
    }

}
