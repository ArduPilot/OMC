/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.asyncfx.Awaiter;
import org.asyncfx.TestBase;
import org.junit.jupiter.api.Test;

class StrandTest extends TestBase {

    @Test
    void Strand_Executes_Operations_Serially() {
        final int iterations = 20;

        Awaiter awaiter = new Awaiter();
        Strand strand = new Strand();
        AtomicInteger count = new AtomicInteger();
        AtomicBoolean concurrentFlag = new AtomicBoolean();

        for (int i = 0; i < iterations; ++i) {
            int expected = i;

            strand.runLater(
                () -> {
                    awaiter.assertFalse(concurrentFlag.compareAndExchange(false, true));

                    int actual = count.getAndIncrement();
                    awaiter.assertEquals(expected, actual);

                    sleep(20);
                    concurrentFlag.set(false);
                    awaiter.signal();
                });
        }

        awaiter.await(iterations);
    }

}
