/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.value;

import java.time.Duration;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.ListenerWrapper;
import org.asyncfx.concurrent.Dispatcher;

/**
 * Wraps an {@link SubChangeListener} and throttles the rate of received events, guaranteeing that for a sequence of
 * events that is sufficiently separated from another sequence of events, the wrapped listener:
 *
 * <ul>
 *   <li>1. will always be called immediately for the first event of the sequence,
 *   <li>2. will not be called sooner than specified by the minPeriod parameter (dropping any intermediate events),
 *   <li>3. will always be called for the last event of the sequence.
 * </ul>
 */
@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public final class RateControlledSubChangeListenerWrapper
        implements SubChangeListener, ListenerWrapper<SubChangeListener> {

    private final SubChangeListener listener;
    private final long minPeriodNanos;

    private boolean scheduled;
    private long lastTimestamp;
    private ObservableValue<?> lastObservableValue;
    private Object lastOldValue;
    private Object lastNewValue;
    private boolean lastSubChange;

    public RateControlledSubChangeListenerWrapper(SubChangeListener listener, Duration minPeriod) {
        this.listener = listener;
        this.minPeriodNanos = minPeriod.toNanos();
    }

    @Override
    @SuppressWarnings("unchecked")
    public SubChangeListener getWrappedListener() {
        SubChangeListener listener = this.listener;
        while (listener instanceof ListenerWrapper) {
            listener = ((ListenerWrapper<SubChangeListener>)listener).getWrappedListener();
        }

        return listener;
    }

    @Override
    public synchronized void changed(
            ObservableValue<?> observable, Object oldValue, Object newValue, boolean subChange) {
        if (scheduled) {
            lastObservableValue = observable;
            lastOldValue = oldValue;
            lastNewValue = newValue;
            lastSubChange = subChange;
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

                lastObservableValue = observable;
                lastOldValue = oldValue;
                lastNewValue = newValue;
                lastSubChange = subChange;
                scheduled = true;

                dispatcher.runLater(
                    this::dispatcherInvalidated, Duration.ofNanos(lastTimestamp + minPeriodNanos - now));
            } else {
                listener.changed(observable, oldValue, newValue, subChange);
                lastTimestamp = now;
            }
        }
    }

    private synchronized void dispatcherInvalidated() {
        ObservableValue<?> observable = lastObservableValue;
        Object oldValue = lastOldValue;
        Object newValue = lastNewValue;
        boolean subChange = lastSubChange;
        scheduled = false;
        lastObservableValue = null;
        lastOldValue = null;
        lastNewValue = null;
        lastSubChange = false;
        lastTimestamp = System.nanoTime();
        listener.changed(observable, oldValue, newValue, subChange);
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
