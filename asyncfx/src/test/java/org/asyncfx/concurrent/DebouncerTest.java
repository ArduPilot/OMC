/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import org.asyncfx.Awaiter;
import org.asyncfx.TestBase;
import org.junit.jupiter.api.Test;

class DebouncerTest extends TestBase {

    @Test
    void AcceptAsyncDebouncer_Accepts_First_And_Last_Request() {
        Awaiter awaiter = new Awaiter();

        Future.FutureConsumer<Integer> consumeInts =
            i ->
                Dispatcher.background()
                    .runLaterAsync(
                        () -> {
                            sleep(50);

                            switch (i) {
                            case 0:
                            case 5:
                                awaiter.signal();
                                break;

                            default:
                                awaiter.fail();
                            }
                        });

        AcceptAsyncDebouncer<Integer> debouncer = new AcceptAsyncDebouncer<>(consumeInts);

        debouncer.acceptAsync(0); // accepted
        debouncer.acceptAsync(1); // NOT accepted
        debouncer.acceptAsync(2); // NOT accepted
        debouncer.acceptAsync(3); // NOT accepted
        debouncer.acceptAsync(4); // NOT accepted
        debouncer.acceptAsync(5); // accepted

        awaiter.await(2);
    }

    @Test
    void ApplyAsyncDebouncer_Applies_First_And_Last_Request() {
        Awaiter awaiter = new Awaiter();

        Future.FutureFunction<Integer, String> applyInts =
            i ->
                Dispatcher.background()
                    .getLaterAsync(
                        () -> {
                            sleep(50);

                            switch (i) {
                            case 0:
                            case 5:
                                awaiter.signal();
                                break;

                            default:
                                awaiter.fail();
                            }

                            return i.toString();
                        });

        ApplyAsyncDebouncer<Integer, String> debouncer = new ApplyAsyncDebouncer<>(applyInts);

        debouncer.applyAsync(0); // applied
        debouncer.applyAsync(1); // NOT applied
        debouncer.applyAsync(2); // NOT applied
        debouncer.applyAsync(3); // NOT applied
        debouncer.applyAsync(4); // NOT applied
        debouncer.applyAsync(5); // applied

        awaiter.await(2);
    }

}
