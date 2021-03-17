/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RunnableWithResult<T> implements Runnable {
    private T result;
    private final CountDownLatch latch = new CountDownLatch(1);
    private final Callable<T> callable;
    private Exception exception;

    public RunnableWithResult(Callable<T> callable) {
        this.callable = callable;
    }

    @Override
    public void run() {
        try {
            result = callable.call();
        } catch (Exception e) {
            exception = e;
        } finally {
            latch.countDown();
        }
    }

    public T getResult() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            exception = e;
        }

        if (exception != null) {
            throw new RuntimeException(exception);
        }

        return result;
    }

    public T getResult(long timeout, TimeUnit unit) {
        try {
            latch.await(timeout, unit);
        } catch (InterruptedException e) {
            exception = e;
        }

        if (exception != null) {
            throw new RuntimeException(exception);
        }

        return result;
    }
}
