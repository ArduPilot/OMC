/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import org.asyncfx.PublishSource;

@PublishSource(
        module = "openjfx",
        licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public class AsyncBindingHelperObserver implements InvalidationListener, WeakListener {

    private final WeakReference<AsyncBinding<?>> ref;

    AsyncBindingHelperObserver(AsyncBinding<?> binding) {
        if (binding == null) {
            throw new NullPointerException("Binding must be specified.");
        }

        ref = new WeakReference<>(binding);
    }

    @Override
    public void invalidated(Observable observable) {
        final AsyncBinding<?> binding = ref.get();
        if (binding == null) {
            observable.removeListener(this);
        } else {
            binding.invalidate();
        }
    }

    @Override
    public boolean wasGarbageCollected() {
        return ref.get() == null;
    }

}
