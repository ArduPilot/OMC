/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans;

import java.time.Duration;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.asyncfx.PublishSource;
import org.asyncfx.concurrent.Dispatcher;

/**
 * Wraps an {@link InvalidationListener} and throttles the rate of received events, guaranteeing that for a sequence of
 * events that is sufficiently separated from another sequence of events, the wrapped listener:
 *
 * <ul>
 *   <li>1. will always be called immediately for the first event of the sequence,
 *   <li>2. will not be called sooner than specified by the minPeriod parameter (dropping any intermediate events),
 *   <li>3. will always be called for the last event of the sequence.
 * </ul>
 */
@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public final class RateControlledInvalidationListenerWrapper
        implements InvalidationListener, ListenerWrapper<InvalidationListener> {

    private final InvalidationListener listener;
    private final long minPeriodNanos;

    private boolean scheduled;
    private long lastTimestamp;
    private Observable lastObservable;

    public RateControlledInvalidationListenerWrapper(InvalidationListener listener, Duration minPeriod) {
        this.listener = listener;
        this.minPeriodNanos = minPeriod.toNanos();
    }

    @Override
    @SuppressWarnings("unchecked")
    public InvalidationListener getWrappedListener() {
        InvalidationListener listener = this.listener;
        while (listener instanceof ListenerWrapper) {
            listener = ((ListenerWrapper<InvalidationListener>)listener).getWrappedListener();
        }

        return listener;
    }

    @Override
    public synchronized void invalidated(Observable observable) {
        if (scheduled) {
            lastObservable = observable;
        } else {
            long now = System.nanoTime();

            if (now - lastTimestamp < minPeriodNanos) {
                Dispatcher dispatcher = Dispatcher.fromThread(Thread.currentThread());
                if (dispatcher == null) {
                    throw new IllegalStateException(
                        "No dispatcher is associated with the calling thread [currentThread = "
                            + Thread.currentThread().getName()
                            + "]");
                }

                lastObservable = observable;
                scheduled = true;

                dispatcher.runLater(
                    this::dispatcherInvalidated, Duration.ofNanos(lastTimestamp + minPeriodNanos - now));
            } else {
                listener.invalidated(observable);
                lastTimestamp = now;
            }
        }
    }

    private synchronized void dispatcherInvalidated() {
        Observable observable = lastObservable;
        scheduled = false;
        lastObservable = null;
        lastTimestamp = System.nanoTime();
        listener.invalidated(observable);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ListenerWrapper) {
            return listener.equals(((ListenerWrapper)obj).getWrappedListener());
        }

        return listener.equals(obj);
    }

}
