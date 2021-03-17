/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanWrapper;
import org.asyncfx.concurrent.Dispatcher;

/**
 * Represents a request to suspend interaction with the user interface. If an operation wants to suspend interaction, it
 * creates an instance of this class and increments the request count. If the request count of any of the instances of
 * this class is larger than zero, the value of {@link SuspendedInteractionRequest#activeProperty()} will be true.
 */
public final class SuspendedInteractionRequest implements AutoCloseable {

    /** Indicates whether there are any active requests. It is safe to use this property on the UI thread. */
    public static ReadOnlyAsyncBooleanProperty activeProperty() {
        return active.getReadOnlyProperty();
    }

    private static final ReadOnlyAsyncBooleanWrapper active =
        new ReadOnlyAsyncBooleanWrapper(SuspendedInteractionRequest.class);

    private static final List<WeakReference<SuspendedInteractionRequest>> requests = new ArrayList<>();
    private static final Cleaner cleaner = Cleaner.create();

    @SuppressWarnings("FieldCanBeLocal")
    private final CleanupAction cleanAction = new CleanupAction();

    private final Cleaner.Cleanable cleanable;

    private int count;

    private static class CleanupAction implements Runnable {
        @Override
        public void run() {
            updateRequests();
        }
    }

    private static void updateRequests() {
        synchronized (requests) {
            int count = 0;
            Iterator<WeakReference<SuspendedInteractionRequest>> it = requests.iterator();

            while (it.hasNext()) {
                SuspendedInteractionRequest request = it.next().get();
                if (request != null) {
                    count += request.count;
                } else {
                    it.remove();
                }
            }

            boolean active = count > 0;
            Dispatcher.platform().run(() -> SuspendedInteractionRequest.active.set(active));
        }
    }

    /** Creates an instance that represents a single request. */
    public SuspendedInteractionRequest() {
        this(1);
    }

    /** Creates an instance that represents the specified number of requests. */
    public SuspendedInteractionRequest(int count) {
        this.cleanable = cleaner.register(this, cleanAction);
        this.count = count;

        synchronized (requests) {
            requests.add(new WeakReference<>(this));
            updateRequests();
        }
    }

    /** Decrements the request count to zero. */
    @Override
    public void close() {
        cleanable.clean();
    }

    /**
     * Increments the request count. After calling this method, the value of {@link
     * SuspendedInteractionRequest#activeProperty()} will be true.
     */
    public synchronized void add() {
        ++count;
    }

    /** Decrements the request count on this instance. */
    public synchronized void release() {
        if (count == 0) {
            throw new IllegalStateException("Cannot release request: no active requests.");
        }

        --count;
    }

}
