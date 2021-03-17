/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.EnvironmentOptions;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.InvalidationListenerWrapper;
import com.intel.missioncontrol.beans.binding.AsyncListExpressionHelper;
import com.intel.missioncontrol.beans.binding.ValueConverter;
import com.intel.missioncontrol.beans.value.ChangeListenerWrapper;
import com.intel.missioncontrol.collections.AsyncListChangeListener;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.ListChangeListenerWrapper;
import com.intel.missioncontrol.collections.LockedList;
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
import javafx.collections.ListChangeListener;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncListPropertyBase<E> extends AsyncListProperty<E> {

    public interface ListInitializer {
        void initializeList(Executor executor);
    }

    private final AsyncListChangeListener<E> listChangeListener =
        change -> {
            invalidateProperties();
            invalidated();
            fireValueChangedEvent(change);
        };

    private final StampedLock valueLock =
        EnvironmentOptions.ENABLE_DEADLOCK_DETECTION ? new DebugStampedLock() : new StampedLock();
    private final ReentrantStampedLock eventLock = new ReentrantStampedLock();
    private PropertyMetadata<AsyncObservableList<E>> metadata;
    private AsyncObservableList<E> value;
    private ObservableValue<? extends AsyncObservableList<E>> observable = null;
    private InvalidationListener listener = null;
    private boolean valid = true;
    private AsyncListExpressionHelper<E> helper;
    private AsyncIntegerProperty size0;
    private AsyncBooleanProperty empty0;
    private Queue<DeferredListListener<E>> deferredListeners;

    public AsyncListPropertyBase(PropertyMetadata<AsyncObservableList<E>> metadata) {
        this.metadata = metadata;
        this.value = metadata.getInitialValue();

        if (this.value instanceof ListInitializer) {
            ((ListInitializer)this.value).initializeList(metadata.getExecutor());
        }

        if (this.value != null) {
            this.value.addListener(ListChangeListenerWrapper.wrap(listChangeListener, metadata.getExecutor()));
        }
    }

    StampedLock getValueLock() {
        return valueLock;
    }

    PropertyMetadata<AsyncObservableList<E>> getMetadataUnsynchronized() {
        return metadata;
    }

    @Override
    public final void reset() {
        set(metadata.getInitialValue());
    }

    @Override
    public PropertyMetadata<AsyncObservableList<E>> getMetadata() {
        long stamp = 0;
        try {
            if ((stamp = valueLock.tryOptimisticRead()) != 0) {
                PropertyMetadata<AsyncObservableList<E>> metadata = this.metadata;
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
    public void overrideMetadata(PropertyMetadata<AsyncObservableList<E>> metadata) {
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
                helper = AsyncListExpressionHelper.addListener(helper, this, getUnsynchronized(), listener);
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
                    AsyncListExpressionHelper.addListener(
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
                helper = AsyncListExpressionHelper.removeListener(helper, getUnsynchronized(), listener);
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
    public void addListener(ChangeListener<? super AsyncObservableList<E>> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper = AsyncListExpressionHelper.addListener(helper, this, getUnsynchronized(), listener);
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
    public void addListener(ChangeListener<? super AsyncObservableList<E>> listener, Executor executor) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper =
                    AsyncListExpressionHelper.addListener(
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
    public void removeListener(ChangeListener<? super AsyncObservableList<E>> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper = AsyncListExpressionHelper.removeListener(helper, getUnsynchronized(), listener);
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
    public void addListener(ListChangeListener<? super E> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper = AsyncListExpressionHelper.addListener(helper, this, getUnsynchronized(), listener);
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
    public void addListener(ListChangeListener<? super E> listener, Executor executor) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper =
                    AsyncListExpressionHelper.addListener(
                        helper, this, getUnsynchronized(), ListChangeListenerWrapper.wrap(listener, executor));
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
    public void removeListener(ListChangeListener<? super E> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = valueLock.writeLock();
            if ((eventStamp = eventLock.tryWriteLock()) != 0) {
                resolveDeferredListeners();
                helper = AsyncListExpressionHelper.removeListener(helper, getUnsynchronized(), listener);
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
    public LockedList<E> lock() {
        AsyncObservableList<E> list = get();
        return list != null ? list.lock() : LockedList.empty();
    }

    @Override
    public AsyncObservableList<E> get() {
        long stamp = 0;
        try {
            if ((stamp = valueLock.tryOptimisticRead()) != 0) {
                boolean valid = this.valid;
                AsyncObservableList<E> value = this.value;
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

    private AsyncObservableList<E> getUnsynchronized() {
        if (!valid) {
            value = observable == null ? value : observable.getValue();
            valid = true;
            if (value != null) {
                value.addListener(ListChangeListenerWrapper.wrap(listChangeListener, metadata.getExecutor()));
            }
        }

        return value;
    }

    @Override
    public void set(AsyncObservableList<E> newValue) {
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
                value.removeListener(listChangeListener);
            }

            value = newValue;
            invalidate = valid;

            if (invalidate) {
                valid = false;
                eventStamp = eventLock.writeLock();
                resolveDeferredListeners();

                if (AsyncListExpressionHelper.validatesValue(helper)) {
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
                AsyncListExpressionHelper.fireValueChangedEvent(helper, newValue);
            } finally {
                if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
                    eventLock.unlockWrite(eventStamp);
                }
            }
        }
    }

    protected void fireValueChangedEvent(AsyncListChangeListener.AsyncChange<? extends E> change) {
        long stamp = 0;

        try {
            stamp = eventLock.writeLock();
            AsyncListExpressionHelper.fireValueChangedEvent(helper, change);
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

    private void markInvalid(AsyncObservableList<E> oldValue) {
        long valueStamp = 0;
        long eventStamp = 0;
        AsyncObservableList<E> currentValue = null;
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
                    oldValue.removeListener(listChangeListener);
                }

                valid = false;
                eventStamp = eventLock.writeLock();
                resolveDeferredListeners();

                if (AsyncListExpressionHelper.validatesValue(helper)) {
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
                AsyncListExpressionHelper.fireValueChangedEvent(helper, currentValue);
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
            return AsyncListExpressionHelper.containsBidirectionalBindingEndpoints(helper);
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                eventLock.unlockRead(stamp);
            }
        }
    }

    @Override
    public <U> void bind(
            ObservableValue<? extends U> source, ValueConverter<U, ? extends AsyncObservableList<E>> converter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(ObservableValue<? extends AsyncObservableList<E>> source) {
        if (source == null) {
            throw new NullPointerException("Cannot bind to null");
        }

        if (isBoundBidirectionally()) {
            throw new IllegalStateException(
                "A bidirectionally bound property cannot be the target of a unidirectional binding.");
        }

        long valueStamp = 0;
        long eventStamp = 0;
        AsyncObservableList<E> newValue = null;
        PropertyMetadata<AsyncObservableList<E>> metadata;
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

                    if (AsyncListExpressionHelper.validatesValue(helper)) {
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
            final AsyncObservableList<E> newValueCopy = newValue;
            final long eventStampCopy = eventStamp;

            metadata.getExecutor()
                .execute(
                    () -> {
                        try {
                            eventLock.changeOwner(Thread.currentThread());
                            invalidated();
                            AsyncListExpressionHelper.fireValueChangedEvent(helper, newValueCopy);
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

        deferredListeners.add(new DeferredListListener<>(listener, true));
    }

    private void addListenerDeferred(ChangeListener<? super AsyncObservableList<E>> listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListListener<>(listener, true));
    }

    private void addListenerDeferred(ListChangeListener<? super E> listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListListener<>(listener, true));
    }

    private void removeListenerDeferred(InvalidationListener listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListListener<>(listener, false));
    }

    private void removeListenerDeferred(ChangeListener<? super AsyncObservableList<E>> listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListListener<>(listener, false));
    }

    private void removeListenerDeferred(ListChangeListener<? super E> listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredListListener<>(listener, false));
    }

    private void resolveDeferredListeners() {
        if (deferredListeners == null) {
            return;
        }

        AsyncObservableList<E> value = getUnsynchronized();

        while (!deferredListeners.isEmpty()) {
            DeferredListListener<E> deferredListener = deferredListeners.remove();
            if (deferredListener.invalidationListener != null) {
                if (deferredListener.added) {
                    helper =
                        AsyncListExpressionHelper.addListener(
                            helper, this, value, deferredListener.invalidationListener);
                } else {
                    helper =
                        AsyncListExpressionHelper.removeListener(helper, value, deferredListener.invalidationListener);
                }
            } else if (deferredListener.changeListener != null) {
                if (deferredListener.added) {
                    helper =
                        AsyncListExpressionHelper.addListener(helper, this, value, deferredListener.changeListener);
                } else {
                    helper = AsyncListExpressionHelper.removeListener(helper, value, deferredListener.changeListener);
                }
            } else {
                if (deferredListener.added) {
                    helper =
                        AsyncListExpressionHelper.addListener(helper, this, value, deferredListener.listChangeListener);
                } else {
                    helper =
                        AsyncListExpressionHelper.removeListener(helper, value, deferredListener.listChangeListener);
                }
            }
        }

        deferredListeners = null;
    }

    @Override
    public final String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("AsyncListProperty [");
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
        private final WeakReference<AsyncListPropertyBase<E>> wref;
        private final Executor executor;

        public Listener(AsyncListPropertyBase<E> ref, Executor executor) {
            this.wref = new WeakReference<>(ref);
            this.executor = executor;
        }

        @Override
        public void invalidated(Observable observable) {
            AsyncListPropertyBase<E> ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                final AsyncObservableList<E> value = ref.value;
                executor.execute(() -> ref.markInvalid(value));
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return wref.get() == null;
        }
    }

}
