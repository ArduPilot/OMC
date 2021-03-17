/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci;

import java.lang.ref.WeakReference;

public final class WeakRunnable implements Runnable {
    private final WeakReference<Runnable> ref;

    public WeakRunnable(Runnable runnable) {
        ref = new WeakReference<>(runnable);
    }

    @Override
    public void run() {
        Runnable r = ref.get();
        if (r != null) {
            r.run();
        }
    }
}
