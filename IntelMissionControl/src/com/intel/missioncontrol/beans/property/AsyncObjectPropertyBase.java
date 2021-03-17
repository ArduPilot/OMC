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
import com.intel.missioncontrol.beans.binding.LifecycleValueConverter;
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncObjectPropertyBase<T> extends AsyncObjectProperty<T> {

    private final StampedLock valueLock =
        EnvironmentOptions.ENABLE_DEADLOCK_DETECTION ? new DebugStampedLock() : new StampedLock();
    private final ReentrantStampedLock eventLock = new ReentrantStampedLock();
    private PropertyMetadata<T> metadata;
    private T value;
    private boolean valid = true;
    private ObservableValue observable;
    private InvalidationListener listener;
    private AsyncExpressionHelper<T> helper;
    private ValueConverterAdapter converter;
    private Queue<DeferredListener<? super T>> deferredListeners;

    public AsyncObjectPropertyBase(PropertyMetadata<T> metadata) {
        this.metadata = metadata;
        this.value = metadata.getInitialValue();
    }

    StampedLock getValueLock() {
        return valueLock;
    }

    PropertyMetadata<T> getMetadataUnsynchronized() {
        return metadata;
    }

    @Override
    public final void reset() {
        set(metadata.getInitialValue());
    }

    @Override
    public PropertyMetadata<T> getMetadata() {
        long stamp = 0;
        try {
            if ((stamp = valueLock.tryOptimisticRead()) != 0) {
                PropertyMetadata<T> metadata = this.metadata;
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
    public void overrideMetadata(PropertyMetadata<T> metadata) {
        long stamp = 0;
        try {
            stamp = valueLock.writeLock();
            this.metadata = this.metadata.merge(metadata);
            this.value = this.metadata.getInitialValue();
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
    public void addListener(ChangeListener<? super T> listener) {
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
    public void addListener(ChangeListener<? super T> listener, Executor executor) {
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
    public void removeListener(ChangeListener<? super T> listener) {
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
    @SuppressWarnings("unchecked")
    public T get() {
        long stamp = 0;
        try {
            if ((stamp = valueLock.tryOptimisticRead()) != 0) {
                boolean valid = this.valid;
                T value = this.value;
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

    @SuppressWarnings("unchecked")
    private T getUnsynchronized() {
        if (valid) {
            return value;
        }

        if (observable != null) {
            if (converter != null) {
                if (value != null) {
                    converter.remove(value);
                }

                value = (T)converter.convert(observable.getValue());
            } else {
                value = ((ObservableValue<? extends T>)observable).getValue();
            }
        }

        valid = true;
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(T newValue) {
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

            if (value != null && converter != null) {
                converter.remove(value);
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
    public void bind(final ObservableValue<? extends T> source) {
        bindInternal(source, null);
    }

    @Override
    public <U> void bind(final ObservableValue<? extends U> source, ValueConverter<U, ? extends T> converter) {
        bindInternal(source, new ValueConverterAdapter<>(converter));
    }

    @Override
    public <U> void bind(final ObservableValue<? extends U> source, LifecycleValueConverter<U, ? extends T> converter) {
        bindInternal(source, new ValueConverterAdapter<>(converter));
    }

    @SuppressWarnings("unchecked")
    private <U> void bindInternal(
            final ObservableValue<? extends U> source, ValueConverterAdapter<U, ? extends T> converter) {
        if (source == null) {
            throw new NullPointerException("Cannot bind to null");
        }

        if (isBoundBidirectionally()) {
            throw new IllegalStateException(
                "A bidirectionally bound property cannot be the target of a unidirectional binding.");
        }

        long valueStamp = 0;
        long eventStamp = 0;
        T newValue = null;
        PropertyMetadata<T> metadata;
        boolean invalidate = false;

        try {
            valueStamp = valueLock.writeLock();
            metadata = this.metadata;

            if (!source.equals(observable)) {
                unbindUnsynchronized();
                observable = source;
                this.converter = converter;
                if (listener == null) {
                    listener = new Listener(this, metadata.getExecutor());
                }

                observable.addListener(listener);

                if (this.converter != null) {
                    if (value != null) {
                        this.converter.remove(value);
                    }

                    newValue = (T)this.converter.convert(source.getValue());
                } else {
                    newValue = ((ObservableValue<? extends T>)observable).getValue();
                }

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
            final T newValueCopy = newValue;
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

    @SuppressWarnings("unchecked")
    private void unbindUnsynchronized() {
        if (observable != null) {
            if (converter != null) {
                if (value == null) {
                    value = (T)converter.convert(observable.getValue());
                }
            } else {
                value = ((ObservableValue<? extends T>)observable).getValue();
            }

            observable.removeListener(listener);
            observable = null;
            converter = null;
        }
    }

    protected void invalidated() {}

    private void markInvalid() {
        long valueStamp = 0;
        long eventStamp = 0;
        T currentValue = null;
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

    private void addListenerDeferred(ChangeListener<? super T> listener) {
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

    private void removeListenerDeferred(ChangeListener<? super T> listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListener<>(listener, false));
    }

    private void resolveDeferredListeners() {
        if (deferredListeners == null) {
            return;
        }

        T value = getUnsynchronized();

        while (!deferredListeners.isEmpty()) {
            DeferredListener<? super T> deferredListener = deferredListeners.remove();
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
        final StringBuilder result = new StringBuilder("AsyncObjectProperty [");
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
        private final WeakReference<AsyncObjectPropertyBase<?>> wref;
        private final Executor executor;

        public Listener(AsyncObjectPropertyBase<?> ref, Executor executor) {
            this.wref = new WeakReference<>(ref);
            this.executor = executor;
        }

        @Override
        public void invalidated(Observable observable) {
            AsyncObjectPropertyBase ref = wref.get();
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

}
