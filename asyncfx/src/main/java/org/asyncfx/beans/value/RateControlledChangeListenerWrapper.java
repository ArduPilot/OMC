/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.value;

import java.time.Duration;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.ListenerWrapper;
import org.asyncfx.concurrent.Dispatcher;

/**
 * Wraps an {@link ChangeListener} and throttles the rate of received events, guaranteeing that for a sequence of events
 * that is sufficiently separated from another sequence of events, the wrapped listener:
 *
 * <ul>
 *   <li>1. will always be called immediately for the first event of the sequence,
 *   <li>2. will not be called sooner than specified by the minPeriod parameter (dropping any intermediate events),
 *   <li>3. will always be called for the last event of the sequence.
 * </ul>
 */
@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public final class RateControlledChangeListenerWrapper<T>
        implements ChangeListener<T>, ListenerWrapper<ChangeListener<T>> {

    private final ChangeListener<T> listener;
    private final long minPeriodNanos;

    private boolean scheduled;
    private long lastTimestamp;
    private ObservableValue<? extends T> lastObservableValue;
    private T lastOldValue;
    private T lastNewValue;

    public RateControlledChangeListenerWrapper(ChangeListener<T> listener, Duration minPeriod) {
        this.listener = listener;
        this.minPeriodNanos = minPeriod.toNanos();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChangeListener<T> getWrappedListener() {
        ChangeListener<T> listener = this.listener;
        while (listener instanceof ListenerWrapper) {
            listener = ((ListenerWrapper<ChangeListener<T>>)listener).getWrappedListener();
        }

        return listener;
    }

    @Override
    public synchronized void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        if (scheduled) {
            lastObservableValue = observable;
            lastOldValue = oldValue;
            lastNewValue = newValue;
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
                scheduled = true;

                dispatcher.runLater(
                    this::dispatcherInvalidated, Duration.ofNanos(lastTimestamp + minPeriodNanos - now));
            } else {
                listener.changed(observable, oldValue, newValue);
                lastTimestamp = now;
            }
        }
    }

    private synchronized void dispatcherInvalidated() {
        ObservableValue<? extends T> observable = lastObservableValue;
        T oldValue = lastOldValue;
        T newValue = lastNewValue;
        scheduled = false;
        lastObservableValue = null;
        lastOldValue = null;
        lastNewValue = null;
        lastTimestamp = System.nanoTime();
        listener.changed(observable, oldValue, newValue);
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
