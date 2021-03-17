/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

public class FutureTest extends AwaitableTestBase {

    @Test
    public void ONLY_ON_SUCCESS_Continuation_Does_Not_Execute_When_Previous_Future_Has_Failed() {
        await(
            1,
            () -> {
                FluentFuture.fromThrowable(new RuntimeException())
                    .continueWith(
                        f -> {
                            Assert.fail();
                            return null;
                        },
                        ContinuationOption.ONLY_ON_SUCCESS)
                    .onDone(
                        f -> {
                            if (f.isCancelled()) {
                                signal();
                            } else {
                                Assert.fail();
                            }
                        });
            });
    }

    @Test
    public void ALWAYS_Continuation_Executes_When_Previous_Future_Has_Failed() {
        AtomicInteger count = new AtomicInteger();

        await(
            1,
            () -> {
                FluentFuture.fromThrowable(new RuntimeException())
                    .continueWith(
                        f -> {
                            count.incrementAndGet();
                            return FluentFuture.fromCancelled();
                        },
                        ContinuationOption.ALWAYS)
                    .onDone(
                        f -> {
                            if (count.get() == 1) {
                                signal();
                            } else {
                                Assert.fail();
                            }
                        });
            });
    }

    @Test
    public void futureUnwrap() {
        FluentFuture<FluentFuture<Integer>> future = Dispatcher.post(() -> Dispatcher.post(() -> 5));
        FluentFuture<Integer> innerFuture = future.unwrap();
        Assert.assertEquals(5, (long)Futures.getUnchecked(innerFuture));
    }

}
