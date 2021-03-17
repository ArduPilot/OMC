/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.EnvironmentOptions;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.InvalidationListenerWrapper;
import com.intel.missioncontrol.beans.binding.AsyncExpressionHelper;
import com.intel.missioncontrol.beans.binding.ValueConverter;
import com.intel.missioncontrol.beans.value.ChangeListenerWrapper;
import com.intel.missioncontrol.concurrent.DebugStampedLock;
import com.intel.missioncontrol.concurrent.ReentrantStampedLock;
import com.intel.missioncontrol.diagnostics.Debugger;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.StampedLock;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncIntegerPropertyBase extends AsyncIntegerProperty {

    private final StampedLock valueLock =
        EnvironmentOptions.ENABLE_DEADLOCK_DETECTION ? new DebugStampedLock() : new StampedLock();
    private final ReentrantStampedLock eventLock = new ReentrantStampedLock();
    private PropertyMetadata<Number> metadata;
    private int value;
    private boolean valid = true;
    private ObservableIntegerValue observable;
    private InvalidationListener listener;
    private AsyncExpressionHelper<Number> helper;
    private Queue<DeferredListener<? super Number>> deferredListeners;

    public AsyncIntegerPropertyBase(PropertyMetadata<Number> metadata) {
        this.metadata = metadata;

        Number initialValue = metadata.getInitialValue();
        this.value = initialValue != null ? initialValue.intValue() : 0;
    }

    StampedLock getValueLock() {
        return valueLock;
    }

    PropertyMetadata<Number> getMetadataUnsynchronized() {
        return metadata;
    }

    @Override
    public final void reset() {
        Number initialValue = metadata.getInitialValue();
        set(initialValue != null ? initialValue.intValue() : 0);
    }

    @Override
    public PropertyMetadata<Number> getMetadata() {
        long stamp = 0;
        try {
            if ((stamp = valueLock.tryOptimisticRead()) != 0) {
                PropertyMetadata<Number> metadata = this.metadata;
                if (valueLock.validate(stamp)) {
                    return metadata;
                }
            }

            stamp = valueLock.readLock();
            return this.metadata;
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) {
                valueLock.unlockRead(stamp);
            }
        }
    }

    @Override
    public void overrideMetadata(PropertyMetadata<Number> metadata) {
        long stamp = 0;
        try {
            stamp = valueLock.writeLock();
            this.metadata = this.metadata.merge(metadata);
            this.value = this.metadata.getInitialValue().intValue();
        } finally {
            valueLock.unlockWrite(stamp);
        }
    }

    @Override
    public void addListener(InvalidationListener listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper = AsyncExpressionHelper.addListener(helper, this, getUnsynchronized(), listener);
            } else {
                addListenerDeferred(listener);
            }
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
                eventLock.unlockWrite(eventStamp);
            }

            valueLock.unlockWrite(valueStamp);
        }
    }

    @Override
    public void addListener(InvalidationListener listener, Executor executor) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper =
                    AsyncExpressionHelper.addListener(
                        helper, this, getUnsynchronized(), InvalidationListenerWrapper.wrap(listener, executor));
            } else {
                addListenerDeferred(InvalidationListenerWrapper.wrap(listener, executor));
            }
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
                eventLock.unlockWrite(eventStamp);
            }

            valueLock.unlockWrite(valueStamp);
        }
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper = AsyncExpressionHelper.removeListener(helper, getUnsynchronized(), listener);
            } else {
                removeListenerDeferred(listener);
            }
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
                eventLock.unlockWrite(eventStamp);
            }

            valueLock.unlockWrite(valueStamp);
        }
    }

    @Override
    public void addListener(ChangeListener<? super Number> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper = AsyncExpressionHelper.addListener(helper, this, getUnsynchronized(), listener);
            } else {
                addListenerDeferred(listener);
            }
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
                eventLock.unlockWrite(eventStamp);
            }

            valueLock.unlockWrite(valueStamp);
        }
    }

    @Override
    public void addListener(ChangeListener<? super Number> listener, Executor executor) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper =
                    AsyncExpressionHelper.addListener(
                        helper, this, getUnsynchronized(), ChangeListenerWrapper.wrap(listener, executor));
            } else {
                addListenerDeferred(ChangeListenerWrapper.wrap(listener, executor));
            }
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
                eventLock.unlockWrite(eventStamp);
            }

            valueLock.unlockWrite(valueStamp);
        }
    }

    @Override
    public void removeListener(ChangeListener<? super Number> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper = AsyncExpressionHelper.removeListener(helper, getUnsynchronized(), listener);
            } else {
                removeListenerDeferred(listener);
            }
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
                eventLock.unlockWrite(eventStamp);
            }

            valueLock.unlockWrite(valueStamp);
        }
    }

    @Override
    public int get() {
        long stamp = 0;
        try {
            if ((stamp = valueLock.tryOptimisticRead()) != 0) {
                boolean valid = this.valid;
                int value = this.value;
                if (valueLock.validate(stamp) && valid) {
                    return value;
                }
            }

            stamp = valueLock.writeLock();
            return getUnsynchronized();
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                valueLock.unlockWrite(stamp);
            }
        }
    }

    private int getUnsynchronized() {
        if (valid) {
            return value;
        }

        if (observable != null) {
            value = observable.get();
        }

        valid = true;
        return value;
    }

    @Override
    public void set(int newValue) {
        long valueStamp = 0;
        long eventStamp = 0;
        boolean invalidate;

        try {
            valueStamp = valueLock.writeLock();
            metadata.verifyAccess();

            if (observable != null) {
                Object bean = getBean();
                String name = getName();

                throw new RuntimeException(
                    (bean != null && name != null ? bean.getClass().getSimpleName() + "." + name + " : " : "")
                        + "A bound value cannot be set.");
            }

            if (value == newValue) {
                return;
            }

            value = newValue;
            invalidate = valid;

            if (invalidate) {
                valid = false;
                eventStamp = eventLock.writeLock();
                resolveDeferredListeners();

                if (AsyncExpressionHelper.validatesValue(helper)) {
                    try {
                        newValue = getUnsynchronized();
                    } catch (Exception e) {
                        if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
                            eventLock.unlockWrite(eventStamp);
                        }

                        throw e;
                    }
                }
            }
        } finally {
            if (StampedLock.isWriteLockStamp(valueStamp)) {
                valueLock.unlockWrite(valueStamp);
            }
        }

        if (invalidate) {
            try {
                invalidated();
                AsyncExpressionHelper.fireValueChangedEvent(helper, newValue);
            } finally {
                if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
                    eventLock.unlockWrite(eventStamp);
                }
            }
        }
    }

    @Override
    public boolean isBound() {
        long stamp = 0;
        try {
            if ((stamp = valueLock.tryOptimisticRead()) != 0) {
                boolean bound = observable != null;
                if (valueLock.validate(stamp)) {
                    return bound;
                }
            }

            stamp = valueLock.readLock();
            return observable != null;
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) {
                valueLock.unlockRead(stamp);
            }
        }
    }

    @Override
    public boolean isBoundBidirectionally() {
        long stamp = 0;

        try {
            stamp = eventLock.readLock();
            return AsyncExpressionHelper.containsBidirectionalBindingEndpoints(helper);
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                eventLock.unlockRead(stamp);
            }
        }
    }

    @Override
    public void bind(final ObservableValue<? extends Number> source) {
        bind(source, null);
    }

    @Override
    public <U> void bind(final ObservableValue<? extends U> source, ValueConverter<U, ? extends Number> converter) {
        if (source == null) {
            throw new NullPointerException("Cannot bind to null");
        }

        if (isBoundBidirectionally()) {
            throw new IllegalStateException(
                "A bidirectionally bound property cannot be the target of a unidirectional binding.");
        }

        long valueStamp = 0;
        long eventStamp = 0;
        int newValue = 0;
        PropertyMetadata<Number> metadata;
        boolean invalidate = false;

        try {
            valueStamp = valueLock.writeLock();
            metadata = this.metadata;

            ObservableIntegerValue newObservable;
            if (source instanceof ObservableIntegerValue) {
                newObservable = (ObservableIntegerValue)source;
            } else if (source instanceof ObservableNumberValue) {
                final ObservableNumberValue numberValue = (ObservableNumberValue)source;
                newObservable =
                    new ValueWrapper(source) {
                        @Override
                        protected int computeValue() {
                            return numberValue.intValue();
                        }
                    };
            } else {
                newObservable =
                    new ValueWrapper(source) {
                        @Override
                        protected int computeValue() {
                            final Number value = converter.convert(source.getValue());
                            return (value == null) ? 0 : value.intValue();
                        }
                    };
            }

            if (!newObservable.equals(observable)) {
                unbindUnsynchronized();
                observable = newObservable;
                if (listener == null) {
                    listener = new Listener(this, metadata.getExecutor());
                }

                observable.addListener(listener);

                newValue = observable.get();
                if (value == newValue) {
                    return;
                }

                invalidate = valid;
                if (invalidate) {
                    valid = false;
                    eventStamp = eventLock.writeLock();
                    resolveDeferredListeners();

                    if (AsyncExpressionHelper.validatesValue(helper)) {
                        try {
                            newValue = getUnsynchronized();
                        } catch (Exception e) {
                            if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
                                eventLock.unlockWrite(eventStamp);
                            }

                            throw e;
                        }
                    }
                }
            }
        } finally {
            if (StampedLock.isWriteLockStamp(valueStamp)) {
                valueLock.unlockWrite(valueStamp);
            }
        }

        if (invalidate) {
            final int newValueCopy = newValue;
            final long eventStampCopy = eventStamp;

            metadata.getExecutor()
                .execute(
                    () -> {
                        try {
                            eventLock.changeOwner(Thread.currentThread());
                            invalidated();
                            AsyncExpressionHelper.fireValueChangedEvent(helper, newValueCopy);
                        } finally {
                            if (ReentrantStampedLock.isWriteLockStamp(eventStampCopy)) {
                                eventLock.unlockWrite(eventStampCopy);
                            }
                        }
                    });
        }
    }

    @Override
    public void unbind() {
        long stamp = 0;
        try {
            stamp = valueLock.writeLock();
            unbindUnsynchronized();
        } finally {
            valueLock.unlockWrite(stamp);
        }
    }

    private void unbindUnsynchronized() {
        if (observable != null) {
            value = observable.get();
            observable.removeListener(listener);
            if (observable instanceof ValueWrapper) {
                ((ValueWrapper)observable).dispose();
            }

            observable = null;
        }
    }

    protected void invalidated() {}

    private void markInvalid() {
        long valueStamp = 0;
        long eventStamp = 0;
        int currentValue = 0;
        boolean invalidate;

        try {
            if ((valueStamp = valueLock.tryOptimisticRead()) != 0) {
                boolean valid = this.valid;
                if (valueLock.validate(valueStamp) && !valid) {
                    return;
                }
            }

            valueStamp = valueLock.writeLock();
            invalidate = valid;

            if (invalidate) {
                valid = false;
                eventStamp = eventLock.writeLock();
                resolveDeferredListeners();

                if (AsyncExpressionHelper.validatesValue(helper)) {
                    try {
                        currentValue = getUnsynchronized();
                    } catch (Exception e) {
                        if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
                            eventLock.unlockWrite(eventStamp);
                        }

                        throw e;
                    }
                }
            }
        } finally {
            if (StampedLock.isWriteLockStamp(valueStamp)) {
                valueLock.unlockWrite(valueStamp);
            }
        }

        if (invalidate) {
            try {
                invalidated();
                AsyncExpressionHelper.fireValueChangedEvent(helper, currentValue);
            } finally {
                if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
                    eventLock.unlockWrite(eventStamp);
                }
            }
        }
    }

    private void addListenerDeferred(InvalidationListener listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListener<>(listener, true));
    }

    private void addListenerDeferred(ChangeListener<? super Number> listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListener<>(listener, true));
    }

    private void removeListenerDeferred(InvalidationListener listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListener<>(listener, false));
    }

    private void removeListenerDeferred(ChangeListener<? super Number> listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListener<>(listener, false));
    }

    private void resolveDeferredListeners() {
        if (deferredListeners == null) {
            return;
        }

        Number value = getUnsynchronized();

        while (!deferredListeners.isEmpty()) {
            DeferredListener<? super Number> deferredListener = deferredListeners.remove();
            if (deferredListener.invalidationListener != null) {
                if (deferredListener.added) {
                    helper =
                        AsyncExpressionHelper.addListener(helper, this, value, deferredListener.invalidationListener);
                } else {
                    helper = AsyncExpressionHelper.removeListener(helper, value, deferredListener.invalidationListener);
                }
            } else {
                if (deferredListener.added) {
                    helper = AsyncExpressionHelper.addListener(helper, this, value, deferredListener.changeListener);
                } else {
                    helper = AsyncExpressionHelper.removeListener(helper, value, deferredListener.changeListener);
                }
            }
        }

        deferredListeners = null;
    }

    @Override
    public final String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("AsyncIntegerProperty [");
        long stamp = 0;

        try {
            if (!Debugger.isAttached()) {
                stamp = valueLock.writeLock();
            }

            if (bean != null) {
                result.append("bean: ").append(bean).append(", ");
            }

            if ((name != null) && (!name.equals(""))) {
                result.append("name: ").append(name).append(", ");
            }

            if (observable != null) {
                result.append("bound, ");

                if (valid) {
                    result.append("value: ").append(getUnsynchronized());
                } else {
                    result.append("invalid");
                }
            } else {
                result.append("value: ").append(getUnsynchronized());
            }

            result.append("]");
            return result.toString();
        } finally {
            if (!Debugger.isAttached() && StampedLock.isWriteLockStamp(stamp)) {
                valueLock.unlockWrite(stamp);
            }
        }
    }

    private static class Listener implements InvalidationListener, WeakListener {
        private final WeakReference<AsyncIntegerPropertyBase> wref;
        private final Executor executor;

        public Listener(AsyncIntegerPropertyBase ref, Executor executor) {
            this.wref = new WeakReference<>(ref);
            this.executor = executor;
        }

        @Override
        public void invalidated(Observable observable) {
            AsyncIntegerPropertyBase ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                executor.execute(ref::markInvalid);
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return wref.get() == null;
        }
    }

    private abstract class ValueWrapper extends IntegerBinding {
        private ObservableValue observable;

        ValueWrapper(ObservableValue observable) {
            this.observable = observable;
            bind(observable);
        }

        @Override
        public void dispose() {
            unbind(observable);
        }
    }

}
