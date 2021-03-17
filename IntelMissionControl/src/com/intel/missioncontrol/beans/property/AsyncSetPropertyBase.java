/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.EnvironmentOptions;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.InvalidationListenerWrapper;
import com.intel.missioncontrol.beans.binding.AsyncSetExpressionHelper;
import com.intel.missioncontrol.beans.binding.ValueConverter;
import com.intel.missioncontrol.beans.value.ChangeListenerWrapper;
import com.intel.missioncontrol.collections.AsyncObservableSet;
import com.intel.missioncontrol.collections.LockedSet;
import com.intel.missioncontrol.collections.SetChangeListenerWrapper;
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
import javafx.collections.SetChangeListener;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncSetPropertyBase<E> extends AsyncSetProperty<E> {

    private final SetChangeListener<E> setChangeListener =
        change -> {
            invalidateProperties();
            invalidated();
            fireValueChangedEvent(change);
        };

    private final StampedLock valueLock =
        EnvironmentOptions.ENABLE_DEADLOCK_DETECTION ? new DebugStampedLock() : new StampedLock();
    private final ReentrantStampedLock eventLock = new ReentrantStampedLock();
    private PropertyMetadata<AsyncObservableSet<E>> metadata;
    private AsyncObservableSet<E> value;
    private ObservableValue<? extends AsyncObservableSet<E>> observable = null;
    private InvalidationListener listener = null;
    private boolean valid = true;
    private AsyncSetExpressionHelper<E> helper;
    private AsyncIntegerProperty size0;
    private AsyncBooleanProperty empty0;
    private Queue<DeferredSetListener<E>> deferredListeners;

    public AsyncSetPropertyBase(PropertyMetadata<AsyncObservableSet<E>> metadata) {
        this.metadata = metadata;
        this.value = metadata.getInitialValue();

        if (this.value != null) {
            this.value.addListener(SetChangeListenerWrapper.wrap(setChangeListener, metadata.getExecutor()));
        }
    }

    StampedLock getValueLock() {
        return valueLock;
    }

    PropertyMetadata<AsyncObservableSet<E>> getMetadataUnsynchronized() {
        return metadata;
    }

    @Override
    public final void reset() {
        set(metadata.getInitialValue());
    }

    @Override
    public PropertyMetadata<AsyncObservableSet<E>> getMetadata() {
        long stamp = 0;
        try {
            if ((stamp = valueLock.tryOptimisticRead()) != 0) {
                PropertyMetadata<AsyncObservableSet<E>> metadata = this.metadata;
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
    public void overrideMetadata(PropertyMetadata<AsyncObservableSet<E>> metadata) {
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
    public ReadOnlyAsyncIntegerProperty sizeProperty() {
        long stamp = 0;
        try {
            if ((stamp = valueLock.tryOptimisticRead()) != 0) {
                AsyncIntegerProperty size0 = this.size0;
                if (valueLock.validate(stamp) && size0 != null) {
                    return size0;
                }
            }

            stamp = valueLock.writeLock();
            if (size0 == null) {
                size0 =
                    new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().name("size").create());
            }

            return size0;
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                valueLock.unlockWrite(stamp);
            }
        }
    }

    @Override
    public ReadOnlyAsyncBooleanProperty emptyProperty() {
        long stamp = 0;
        try {
            if ((stamp = valueLock.tryOptimisticRead()) != 0) {
                AsyncBooleanProperty empty0 = this.empty0;
                if (valueLock.validate(stamp) && empty0 != null) {
                    return empty0;
                }
            }

            stamp = valueLock.writeLock();
            if (empty0 == null) {
                empty0 =
                    new SimpleAsyncBooleanProperty(
                        this, new PropertyMetadata.Builder<Boolean>().name("empty").create());
            }

            return empty0;
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                valueLock.unlockWrite(stamp);
            }
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
                helper = AsyncSetExpressionHelper.addListener(helper, this, getUnsynchronized(), listener);
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
                    AsyncSetExpressionHelper.addListener(
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
                helper = AsyncSetExpressionHelper.removeListener(helper, getUnsynchronized(), listener);
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
    public void addListener(ChangeListener<? super AsyncObservableSet<E>> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.addListener(helper, this, getUnsynchronized(), listener);
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
    public void addListener(ChangeListener<? super AsyncObservableSet<E>> listener, Executor executor) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper =
                    AsyncSetExpressionHelper.addListener(
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
    public void removeListener(ChangeListener<? super AsyncObservableSet<E>> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.removeListener(helper, getUnsynchronized(), listener);
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
    public void addListener(SetChangeListener<? super E> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.addListener(helper, this, getUnsynchronized(), listener);
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
    public void addListener(SetChangeListener<? super E> listener, Executor executor) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper =
                    AsyncSetExpressionHelper.addListener(
                        helper, this, getUnsynchronized(), SetChangeListenerWrapper.wrap(listener, executor));
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
    public void removeListener(SetChangeListener<? super E> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.removeListener(helper, getUnsynchronized(), listener);
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
    public LockedSet<E> lock() {
        AsyncObservableSet<E> list = get();
        return list != null ? list.lock() : LockedSet.empty();
    }

    @Override
    public AsyncObservableSet<E> get() {
        long stamp = 0;
        try {
            if ((stamp = valueLock.tryOptimisticRead()) != 0) {
                boolean valid = this.valid;
                AsyncObservableSet<E> value = this.value;
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

    private AsyncObservableSet<E> getUnsynchronized() {
        if (!valid) {
            value = observable == null ? value : observable.getValue();
            valid = true;
            if (value != null) {
                value.addListener(SetChangeListenerWrapper.wrap(setChangeListener, metadata.getExecutor()));
            }
        }

        return value;
    }

    @Override
    public void set(AsyncObservableSet<E> newValue) {
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

            if (value != null) {
                value.removeListener(setChangeListener);
            }

            value = newValue;
            invalidate = valid;

            if (invalidate) {
                valid = false;
                eventStamp = eventLock.writeLock();
                resolveDeferredListeners();

                if (AsyncSetExpressionHelper.validatesValue(helper)) {
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
                AsyncSetExpressionHelper.fireValueChangedEvent(helper, newValue);
            } finally {
                if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
                    eventLock.unlockWrite(eventStamp);
                }
            }
        }
    }

    protected void fireValueChangedEvent(SetChangeListener.Change<? extends E> change) {
        long stamp = 0;

        try {
            stamp = eventLock.writeLock();
            AsyncSetExpressionHelper.fireValueChangedEvent(helper, change);
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                eventLock.unlockWrite(stamp);
            }
        }
    }

    protected void invalidated() {}

    private void invalidateProperties() {
        if (size0 != null) {
            size0.set(size());
        }

        if (empty0 != null) {
            empty0.set(isEmpty());
        }
    }

    private void markInvalid(AsyncObservableSet<E> oldValue) {
        long valueStamp = 0;
        long eventStamp = 0;
        AsyncObservableSet<E> currentValue = null;
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
                if (oldValue != null) {
                    oldValue.removeListener(setChangeListener);
                }

                valid = false;
                eventStamp = eventLock.writeLock();
                resolveDeferredListeners();

                if (AsyncSetExpressionHelper.validatesValue(helper)) {
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
                invalidateProperties();
                invalidated();
                AsyncSetExpressionHelper.fireValueChangedEvent(helper, currentValue);
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
            return AsyncSetExpressionHelper.containsBidirectionalBindingEndpoints(helper);
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                eventLock.unlockRead(stamp);
            }
        }
    }

    @Override
    public <U> void bind(
            ObservableValue<? extends U> source, ValueConverter<U, ? extends AsyncObservableSet<E>> converter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(ObservableValue<? extends AsyncObservableSet<E>> source) {
        if (source == null) {
            throw new NullPointerException("Cannot bind to null");
        }

        if (isBoundBidirectionally()) {
            throw new IllegalStateException(
                "A bidirectionally bound property cannot be the target of a unidirectional binding.");
        }

        long valueStamp = 0;
        long eventStamp = 0;
        AsyncObservableSet<E> newValue = null;
        PropertyMetadata<AsyncObservableSet<E>> metadata;
        boolean invalidate = false;

        try {
            valueStamp = valueLock.writeLock();
            metadata = this.metadata;

            if (!source.equals(observable)) {
                unbindUnsynchronized();
                observable = source;
                final Executor targetExecutor = metadata.getExecutor();
                if (listener == null) {
                    listener = new Listener<>(this, targetExecutor);
                }

                observable.addListener(listener);

                newValue = observable.getValue();
                if (value == newValue) {
                    return;
                }

                invalidate = valid;
                if (invalidate) {
                    valid = false;
                    eventStamp = eventLock.writeLock();
                    resolveDeferredListeners();

                    if (AsyncSetExpressionHelper.validatesValue(helper)) {
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
            final AsyncObservableSet<E> newValueCopy = newValue;
            final long eventStampCopy = eventStamp;

            metadata.getExecutor()
                .execute(
                    () -> {
                        try {
                            eventLock.changeOwner(Thread.currentThread());
                            invalidated();
                            AsyncSetExpressionHelper.fireValueChangedEvent(helper, newValueCopy);
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
            value = observable.getValue();
            observable.removeListener(listener);
            observable = null;
        }
    }

    private void addListenerDeferred(InvalidationListener listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredSetListener<>(listener, true));
    }

    private void addListenerDeferred(ChangeListener<? super AsyncObservableSet<E>> listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredSetListener<>(listener, true));
    }

    private void addListenerDeferred(SetChangeListener<? super E> listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredSetListener<>(listener, true));
    }

    private void removeListenerDeferred(InvalidationListener listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredSetListener<>(listener, false));
    }

    private void removeListenerDeferred(ChangeListener<? super AsyncObservableSet<E>> listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredSetListener<>(listener, false));
    }

    private void removeListenerDeferred(SetChangeListener<? super E> listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredSetListener<>(listener, false));
    }

    private void resolveDeferredListeners() {
        if (deferredListeners == null) {
            return;
        }

        AsyncObservableSet<E> value = getUnsynchronized();

        while (!deferredListeners.isEmpty()) {
            DeferredSetListener<E> deferredListener = deferredListeners.remove();
            if (deferredListener.invalidationListener != null) {
                if (deferredListener.added) {
                    helper =
                        AsyncSetExpressionHelper.addListener(
                            helper, this, value, deferredListener.invalidationListener);
                } else {
                    helper =
                        AsyncSetExpressionHelper.removeListener(helper, value, deferredListener.invalidationListener);
                }
            } else if (deferredListener.changeListener != null) {
                if (deferredListener.added) {
                    helper = AsyncSetExpressionHelper.addListener(helper, this, value, deferredListener.changeListener);
                } else {
                    helper = AsyncSetExpressionHelper.removeListener(helper, value, deferredListener.changeListener);
                }
            } else {
                if (deferredListener.added) {
                    helper =
                        AsyncSetExpressionHelper.addListener(helper, this, value, deferredListener.setChangeListener);
                } else {
                    helper = AsyncSetExpressionHelper.removeListener(helper, value, deferredListener.setChangeListener);
                }
            }
        }

        deferredListeners = null;
    }

    @Override
    public final String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("AsyncSetProperty [");
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

    private static class Listener<E> implements InvalidationListener, WeakListener {
        private final WeakReference<AsyncSetPropertyBase<E>> wref;
        private final Executor executor;

        public Listener(AsyncSetPropertyBase<E> ref, Executor executor) {
            this.wref = new WeakReference<>(ref);
            this.executor = executor;
        }

        @Override
        public void invalidated(Observable observable) {
            AsyncSetPropertyBase<E> ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                final AsyncObservableSet<E> value = ref.value;
                executor.execute(() -> ref.markInvalid(value));
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return wref.get() == null;
        }
    }

}
