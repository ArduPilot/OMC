/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package gov.nasa.worldwind.javafx;

class AwaitableRunnable implements Runnable {

    private final Runnable runnable;
    private ResetEvent resetEvent = new ResetEvent();

    AwaitableRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
        resetEvent.set();
    }

    void await() {
        try {
            resetEvent.await();
        } catch (InterruptedException e) {
            // do nothing
        }
    }

}
