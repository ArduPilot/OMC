/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import static org.asyncfx.beans.AccessControllerImpl.LockName.EVENT;
import static org.asyncfx.beans.AccessControllerImpl.LockName.VALUE;
import static org.asyncfx.beans.AccessControllerImpl.LockType.GROUP;
import static org.asyncfx.beans.AccessControllerImpl.LockType.INSTANCE;

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
import org.asyncfx.AsyncFX;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AccessController;
import org.asyncfx.beans.AccessControllerImpl;
import org.asyncfx.beans.AsyncInvalidationListenerWrapper;
import org.asyncfx.beans.AsyncSubInvalidationListenerWrapper;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.binding.AsyncSetExpressionHelper;
import org.asyncfx.beans.binding.ValueConverter;
import org.asyncfx.beans.value.AsyncChangeListenerWrapper;
import org.asyncfx.beans.value.AsyncSubChangeListenerWrapper;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.LockedSet;
import org.asyncfx.collections.SetChangeListenerWrapper;
import org.asyncfx.collections.SubObservableSet;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncSetPropertyBase<E> extends AsyncSetProperty<E> {

    public interface SetInitializer {
        void initializeSet(Executor executor);
    }

    private final SetChangeListener<E> setChangeListener =
        change -> {
            Executor executor = getMetadata().getDispatcher();
            if (executor == null) {
                Object bean = getBean();
                executor = bean instanceof PropertyObject ? ((PropertyObject)bean).getDispatcher() : null;
            }

            if (executor != null) {
                executor.execute(
                    () -> {
                        invalidateProperties();
                        invalidated();
                        fireValueChangedEvent(change);
                    });
            } else {
                invalidateProperties();
                invalidated();
                fireValueChangedEvent(change);
            }
        };

    private final SubInvalidationListener subInvalidationListener =
        (observable, subInvalidation) -> {
            if (subInvalidation) {
                Executor executor = getMetadata().getDispatcher();
                if (executor == null) {
                    Object bean = getBean();
                    executor = bean instanceof PropertyObject ? ((PropertyObject)bean).getDispatcher() : null;
                }

                if (executor != null) {
                    executor.execute(() -> fireSubValueChangedEvent(observable));
                } else {
                    fireSubValueChangedEvent(observable);
                }
            }
        };

    private final long uniqueId = PropertyHelper.getNextUniqueId();
    private final AccessControllerImpl accessController;
    private final Executor effectiveExecutor = runnable -> getExecutor().execute(runnable);
    private PropertyMetadata<AsyncObservableSet<E>> metadata;
    private volatile boolean metadataSealed;
    private String name;
    private AsyncObservableSet<E> value;
    private ObservableValue<? extends AsyncObservableSet<E>> observable = null;
    private InvalidationListener listener = null;
    private boolean valid = true;
    private AsyncIntegerProperty size0;
    private AsyncBooleanProperty empty0;
    private Queue<DeferredSetListener<E>> deferredListeners;
    AsyncSetExpressionHelper<E> helper;

    public AsyncSetPropertyBase(PropertyMetadata<AsyncObservableSet<E>> metadata) {
        this.metadata = metadata;
        this.accessController = new AccessControllerImpl();
        this.value = metadata.getInitialValue();

        ConsistencyGroup consistencyGroup = metadata.getConsistencyGroup();
        if (consistencyGroup != null) {
            consistencyGroup.add(this);
        }

        if (this.value instanceof SetInitializer) {
            ((SetInitializer)this.value).initializeSet(effectiveExecutor);
        }

        if (this.value != null) {
            this.value.addListener(setChangeListener);

            if (this.value instanceof SubObservableSet) {
                ((SubObservableSet)this.value).addListener(subInvalidationListener);
            }
        }
    }

    AsyncSetPropertyBase(PropertyObject bean, PropertyMetadata<AsyncObservableSet<E>> metadata) {
        this.metadata = metadata;
        this.accessController = bean != null ? bean.getSharedAccessController() : new AccessControllerImpl();
        this.value = metadata.getInitialValue();

        ConsistencyGroup consistencyGroup = metadata.getConsistencyGroup();
        if (consistencyGroup != null) {
            consistencyGroup.add(this);
        }

        if (this.value instanceof SetInitializer) {
            ((SetInitializer)this.value).initializeSet(effectiveExecutor);
        }

        if (this.value != null) {
            this.value.addListener(setChangeListener);

            if (this.value instanceof SubObservableSet) {
                ((SubObservableSet)this.value).addListener(subInvalidationListener);
            }
        }
    }

    @Override
    public long getUniqueId() {
        return uniqueId;
    }

    @Override
    public String getName() {
        long stamp = 0;
        try {
            stamp = AsyncFX.isDebuggerAttached() ? 0 : accessController.readLock(VALUE, INSTANCE);
            if (name == null) {
                this.name = PropertyHelper.getPropertyName(getBean(), this, metadata);
            }

            return name;
        } finally {
            accessController.unlockRead(VALUE, stamp);
        }
    }

    @Override
    public AccessController getAccessController() {
        return accessController;
    }

    @Override
    public PropertyMetadata<AsyncObservableSet<E>> getMetadata() {
        boolean sealed = metadataSealed;
        if (!sealed) {
            long stamp = 0;
            try {
                stamp = accessController.readLock(VALUE, INSTANCE);
                sealed = metadataSealed;
                if (!sealed) {
                    metadataSealed = true;
                }
            } finally {
                accessController.unlockRead(VALUE, stamp);
            }
        }

        return metadata;
    }

    @Override
    public void overrideMetadata(PropertyMetadata<AsyncObservableSet<E>> metadata) {
        long stamp = 0;
        try {
            stamp = accessController.writeLock(VALUE, INSTANCE);
            if (metadataSealed) {
                throw new IllegalStateException("Metadata cannot be overridden because it is sealed after first use.");
            }

            this.metadata = this.metadata.merge(metadata);
            this.value = this.metadata.getInitialValue();
            this.name = null;
        } finally {
            accessController.unlockWrite(VALUE, stamp);
        }
    }

    @Override
    public ReadOnlyAsyncIntegerProperty sizeProperty() {
        long stamp = 0;
        try {
            if ((stamp = accessController.tryOptimisticRead(VALUE, INSTANCE)) != 0) {
                AsyncIntegerProperty size0 = this.size0;
                if (accessController.validate(VALUE, INSTANCE, stamp) && size0 != null) {
                    return size0;
                }
            }

            stamp = accessController.writeLock(VALUE, INSTANCE);
            if (size0 == null) {
                size0 =
                    new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().name("size").create());
                AsyncObservableSet<E> set = getCore();
                size0.set(set != null ? set.size() : 0);
            }

            return size0;
        } finally {
            accessController.unlockWrite(VALUE, stamp);
        }
    }

    @Override
    public ReadOnlyAsyncBooleanProperty emptyProperty() {
        long stamp = 0;
        try {
            if ((stamp = accessController.tryOptimisticRead(VALUE, INSTANCE)) != 0) {
                AsyncBooleanProperty empty0 = this.empty0;
                if (accessController.validate(VALUE, INSTANCE, stamp) && empty0 != null) {
                    return empty0;
                }
            }

            stamp = accessController.writeLock(VALUE, INSTANCE);
            if (empty0 == null) {
                empty0 =
                    new SimpleAsyncBooleanProperty(
                        this, new PropertyMetadata.Builder<Boolean>().name("empty").create());
                AsyncObservableSet<E> set = getCore();
                empty0.set(set == null || set.isEmpty());
            }

            return empty0;
        } finally {
            accessController.unlockWrite(VALUE, stamp);
        }
    }

    @Override
    public void addListener(InvalidationListener listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.addListener(helper, this, getCore(), listener);
            } else {
                addListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void addListener(InvalidationListener listener, Executor executor) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper =
                    AsyncSetExpressionHelper.addListener(
                        helper, this, getCore(), AsyncInvalidationListenerWrapper.wrap(listener, executor));
            } else {
                addListenerDeferred(AsyncInvalidationListenerWrapper.wrap(listener, executor));
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.removeListener(helper, getCore(), listener);
            } else {
                removeListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void addListener(SubInvalidationListener listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.addListener(helper, this, getCore(), listener);
            } else {
                addListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void addListener(SubInvalidationListener listener, Executor executor) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper =
                    AsyncSetExpressionHelper.addListener(
                        helper, this, getCore(), AsyncSubInvalidationListenerWrapper.wrap(listener, executor));
            } else {
                addListenerDeferred(AsyncSubInvalidationListenerWrapper.wrap(listener, executor));
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void removeListener(SubInvalidationListener listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.removeListener(helper, getCore(), listener);
            } else {
                removeListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void addListener(ChangeListener<? super AsyncObservableSet<E>> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.addListener(helper, this, getCore(), listener);
            } else {
                addListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void addListener(ChangeListener<? super AsyncObservableSet<E>> listener, Executor executor) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper =
                    AsyncSetExpressionHelper.addListener(
                        helper, this, getCore(), AsyncChangeListenerWrapper.wrap(listener, executor));
            } else {
                addListenerDeferred(AsyncChangeListenerWrapper.wrap(listener, executor));
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void removeListener(ChangeListener<? super AsyncObservableSet<E>> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.removeListener(helper, getCore(), listener);
            } else {
                removeListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void addListener(SubChangeListener listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.addListener(helper, this, getCore(), listener);
            } else {
                addListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void addListener(SubChangeListener listener, Executor executor) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper =
                    AsyncSetExpressionHelper.addListener(
                        helper, this, getCore(), AsyncSubChangeListenerWrapper.wrap(listener, executor));
            } else {
                addListenerDeferred(AsyncSubChangeListenerWrapper.wrap(listener, executor));
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void removeListener(SubChangeListener listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.removeListener(helper, getCore(), listener);
            } else {
                removeListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void addListener(SetChangeListener<? super E> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.addListener(helper, this, getCore(), listener);
            } else {
                removeListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void addListener(SetChangeListener<? super E> listener, Executor executor) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper =
                    AsyncSetExpressionHelper.addListener(
                        helper, this, getCore(), SetChangeListenerWrapper.wrap(listener, executor));
            } else {
                addListenerDeferred(SetChangeListenerWrapper.wrap(listener, executor));
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public void removeListener(SetChangeListener<? super E> listener) {
        long valueStamp = 0;
        long eventStamp = 0;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            if ((eventStamp = accessController.tryWriteLock(EVENT, INSTANCE)) != 0) {
                resolveDeferredListeners();
                helper = AsyncSetExpressionHelper.removeListener(helper, getCore(), listener);
            } else {
                removeListenerDeferred(listener);
            }
        } finally {
            accessController.unlockWrite(valueStamp, eventStamp);
        }
    }

    @Override
    public LockedSet<E> lock() {
        AsyncObservableSet<E> list = get();
        return list != null ? list.lock() : LockedSet.empty();
    }

    @Override
    public AsyncObservableSet<E> getUncritical() {
        return get(false);
    }

    @Override
    public AsyncObservableSet<E> get() {
        return get(true);
    }

    private AsyncObservableSet<E> get(boolean critical) {
        long stamp = 0;
        boolean read = true;

        try {
            if ((stamp = accessController.tryOptimisticRead(VALUE, GROUP)) != 0) {
                boolean valid = this.valid;
                AsyncObservableSet<E> value = this.value;
                PropertyMetadata<AsyncObservableSet<E>> metadata = this.metadata;
                if (accessController.validate(VALUE, GROUP, stamp)) {
                    if (critical) {
                        PropertyHelper.verifyConsistency(metadata);
                    }

                    if (valid) {
                        return value;
                    }

                    read = false;
                }
            }

            if (read) {
                stamp = accessController.readLock(VALUE, GROUP);
                if (valid) {
                    if (critical) {
                        PropertyHelper.verifyConsistency(metadata);
                    }

                    return value;
                }
            }

            long newStamp = accessController.tryConvertToWriteLock(VALUE, GROUP, stamp);
            if (newStamp == 0) {
                accessController.unlockRead(VALUE, stamp);
                stamp = accessController.writeLock(VALUE, GROUP);
            } else {
                stamp = newStamp;
            }

            if (critical) {
                PropertyHelper.verifyConsistency(metadata);
            }

            return getCore();
        } finally {
            accessController.unlock(VALUE, stamp);
        }
    }

    @SuppressWarnings("unchecked")
    AsyncObservableSet<E> getCore() {
        if (!valid) {
            if (observable instanceof ReadOnlyAsyncSetProperty) {
                value = ((ReadOnlyAsyncSetProperty<E>)observable).getUncritical();
            } else if (observable != null) {
                value = observable.getValue();
            }

            valid = true;
            if (value != null) {
                value.addListener(SetChangeListenerWrapper.wrap(setChangeListener, effectiveExecutor));

                if (this.value instanceof SubObservableSet) {
                    ((SubObservableSet)this.value)
                        .addListener(
                            AsyncSubInvalidationListenerWrapper.wrap(subInvalidationListener, effectiveExecutor));
                }
            }
        }

        return value;
    }

    @Override
    public void set(AsyncObservableSet<E> newValue) {
        long valueStamp = 0;
        long eventStamp = 0;
        boolean invalidate, fireEvent = false;

        try {
            valueStamp = accessController.writeLock(VALUE, GROUP);
            PropertyHelper.verifyAccess(this, metadata);
            PropertyHelper.verifyConsistency(metadata);

            if (observable != null) {
                throw new RuntimeException("A bound value cannot be set.");
            }

            if (value == newValue) {
                return;
            }

            if (value != null) {
                value.removeListener(setChangeListener);

                if (value instanceof SubObservableSet) {
                    ((SubObservableSet)value).removeListener(subInvalidationListener);
                }
            }

            value = newValue;
            invalidate = valid;

            if (invalidate) {
                valid = false;
                eventStamp = accessController.writeLock(EVENT, INSTANCE);
                resolveDeferredListeners();

                if (AsyncSetExpressionHelper.validatesValue(helper)) {
                    try {
                        newValue = getCore();
                    } catch (Exception e) {
                        accessController.unlockWrite(EVENT, eventStamp);
                        throw e;
                    }
                }

                if (!(fireEvent = !accessController.isLocked())) {
                    AsyncObservableSet<E> newValueCopy = newValue;

                    accessController.defer(
                        () -> {
                            long stamp = 0;
                            try {
                                stamp = accessController.writeLock(EVENT, GROUP);
                                invalidated();
                                AsyncSetExpressionHelper.fireValueChangedEvent(helper, newValueCopy, false);
                            } finally {
                                accessController.unlockWrite(EVENT, stamp);
                            }
                        });

                    accessController.unlockWrite(EVENT, eventStamp);
                }
            }
        } finally {
            accessController.unlockWrite(VALUE, valueStamp);
        }

        if (fireEvent) {
            try {
                invalidated();
                AsyncSetExpressionHelper.fireValueChangedEvent(helper, newValue, false);
            } finally {
                accessController.unlockWrite(EVENT, eventStamp);
            }
        }
    }

    @Override
    public final void reset() {
        set(metadata.getInitialValue());
    }

    protected void fireValueChangedEvent(SetChangeListener.Change<? extends E> change) {
        long stamp = 0;

        try {
            stamp = accessController.writeLock(EVENT, INSTANCE);
            AsyncSetExpressionHelper.fireValueChangedEvent(helper, change);
        } finally {
            accessController.unlockWrite(EVENT, stamp);
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
            if ((valueStamp = accessController.tryOptimisticRead(VALUE, GROUP)) != 0) {
                boolean valid = this.valid;
                if (accessController.validate(VALUE, GROUP, valueStamp) && !valid) {
                    return;
                }
            }

            valueStamp = accessController.writeLock(VALUE, GROUP);
            invalidate = valid;

            if (invalidate) {
                if (oldValue != null) {
                    oldValue.removeListener(setChangeListener);
                }

                valid = false;
                eventStamp = accessController.writeLock(EVENT, INSTANCE);
                resolveDeferredListeners();

                if (AsyncSetExpressionHelper.validatesValue(helper)) {
                    try {
                        currentValue = getCore();
                    } catch (Exception e) {
                        accessController.unlockWrite(EVENT, eventStamp);
                        throw e;
                    }
                }
            }
        } finally {
            accessController.unlockWrite(VALUE, valueStamp);
        }

        if (invalidate) {
            try {
                invalidateProperties();
                invalidated();
                AsyncSetExpressionHelper.fireValueChangedEvent(helper, currentValue, false);
            } finally {
                accessController.unlockWrite(EVENT, eventStamp);
            }
        }
    }

    @Override
    public boolean isBound() {
        long stamp = 0;

        try {
            if ((stamp = accessController.tryOptimisticRead(VALUE, INSTANCE)) != 0) {
                boolean bound = observable != null;
                if (accessController.validate(VALUE, INSTANCE, stamp)) {
                    return bound;
                }
            }

            stamp = accessController.readLock(VALUE, INSTANCE);
            return observable != null;
        } finally {
            accessController.unlockRead(VALUE, stamp);
        }
    }

    @Override
    public boolean isBoundBidirectionally() {
        long stamp = 0;

        try {
            stamp = accessController.readLock(EVENT, INSTANCE);
            return AsyncSetExpressionHelper.containsBidirectionalBindingEndpoints(helper);
        } finally {
            accessController.unlockRead(EVENT, stamp);
        }
    }

    @Override
    public <U> void bind(
            ObservableValue<? extends U> source, ValueConverter<U, ? extends AsyncObservableSet<E>> converter) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void bind(ObservableValue<? extends AsyncObservableSet<E>> source) {
        if (source == null) {
            throw new NullPointerException("Cannot bind to null.");
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
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            metadata = this.metadata;

            if (metadata.getConsistencyGroup() != null) {
                throw new IllegalStateException("A property of a consistency group cannot be bound.");
            }

            if (!source.equals(observable)) {
                unbindUnsynchronized();
                observable = source;
                if (listener == null) {
                    listener = new Listener<>(this);
                }

                PropertyHelper.addListener(observable, listener, accessController);

                if (observable instanceof ReadOnlyAsyncSetProperty) {
                    newValue =
                        (AsyncObservableSet<E>)
                            PropertyHelper.getValueUncritical(
                                (ReadOnlyAsyncSetProperty<E>)observable, accessController);
                } else {
                    newValue = observable.getValue();
                }

                if (value == newValue) {
                    return;
                }

                invalidate = valid;
                if (invalidate) {
                    valid = false;
                    eventStamp = accessController.writeLock(EVENT, INSTANCE);
                    resolveDeferredListeners();

                    if (AsyncSetExpressionHelper.validatesValue(helper)) {
                        try {
                            newValue = getCore();
                        } catch (Exception e) {
                            accessController.unlockWrite(EVENT, eventStamp);
                            throw e;
                        }
                    }
                }
            }
        } finally {
            accessController.unlockWrite(VALUE, valueStamp);
        }

        if (invalidate) {
            final AsyncObservableSet<E> newValueCopy = newValue;
            final long eventStampCopy = eventStamp;

            Executor executor = metadata.getDispatcher();
            if (executor == null) {
                Object bean = getBean();
                executor = bean instanceof PropertyObject ? ((PropertyObject)bean).getDispatcher() : null;
            }

            if (executor != null) {
                executor.execute(
                    () -> {
                        try {
                            accessController.changeEventLockOwner(Thread.currentThread());
                            invalidated();
                            AsyncSetExpressionHelper.fireValueChangedEvent(helper, newValueCopy, false);
                        } finally {
                            accessController.unlockWrite(EVENT, eventStampCopy);
                        }
                    });
            } else {
                try {
                    accessController.changeEventLockOwner(Thread.currentThread());
                    invalidated();
                    AsyncSetExpressionHelper.fireValueChangedEvent(helper, newValueCopy, false);
                } finally {
                    accessController.unlockWrite(EVENT, eventStampCopy);
                }
            }
        }
    }

    @Override
    public void unbind() {
        long stamp = 0;

        try {
            stamp = accessController.writeLock(VALUE, INSTANCE);
            unbindUnsynchronized();
        } finally {
            accessController.unlockWrite(VALUE, stamp);
        }
    }

    @SuppressWarnings("unchecked")
    private void unbindUnsynchronized() {
        if (observable != null) {
            if (observable instanceof ReadOnlyAsyncSetProperty) {
                value = ((ReadOnlyAsyncSetProperty<E>)observable).getUncritical();
            } else {
                value = observable.getValue();
            }

            observable.removeListener(listener);
            observable = null;
        }
    }

    private void fireSubValueChangedEvent(@SuppressWarnings("unused") Observable observable) {
        long eventStamp = 0;
        try {
            eventStamp = accessController.writeLock(EVENT, INSTANCE);
            AsyncSetExpressionHelper.fireValueChangedEvent(
                helper, AsyncSetExpressionHelper.validatesValue(helper) ? get() : null, true);
        } finally {
            accessController.unlockWrite(EVENT, eventStamp);
        }
    }

    private void addListenerDeferred(InvalidationListener listener) {
        if (deferredListeners == null) {
            deferredListeners = new ArrayDeque<>();
        }

        deferredListeners.add(new DeferredSetListener<>(listener, true));
    }

    private void addListenerDeferred(SubInvalidationListener listener) {
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

    private void addListenerDeferred(SubChangeListener listener) {
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

    private void removeListenerDeferred(SubInvalidationListener listener) {
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

    private void removeListenerDeferred(SubChangeListener listener) {
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

        AsyncObservableSet<E> value = getCore();

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
            } else if (deferredListener.subInvalidationListener != null) {
                if (deferredListener.added) {
                    helper =
                        AsyncSetExpressionHelper.addListener(
                            helper, this, value, deferredListener.subInvalidationListener);
                } else {
                    helper =
                        AsyncSetExpressionHelper.removeListener(
                            helper, value, deferredListener.subInvalidationListener);
                }
            } else if (deferredListener.changeListener != null) {
                if (deferredListener.added) {
                    helper = AsyncSetExpressionHelper.addListener(helper, this, value, deferredListener.changeListener);
                } else {
                    helper = AsyncSetExpressionHelper.removeListener(helper, value, deferredListener.changeListener);
                }
            } else if (deferredListener.subChangeListener != null) {
                if (deferredListener.added) {
                    helper =
                        AsyncSetExpressionHelper.addListener(helper, this, value, deferredListener.subChangeListener);
                } else {
                    helper = AsyncSetExpressionHelper.removeListener(helper, value, deferredListener.subChangeListener);
                }
            } else if (deferredListener.setChangeListener != null) {
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

    protected void verifyAccess() {
        long stamp = 0;

        try {
            if ((stamp = accessController.tryOptimisticRead(VALUE, INSTANCE)) != 0) {
                PropertyMetadata metadata = this.metadata;
                if (accessController.validate(VALUE, GROUP, stamp)) {
                    PropertyHelper.verifyAccess(this, metadata);
                } else {
                    accessController.readLock(VALUE, INSTANCE);
                    PropertyHelper.verifyAccess(this, this.metadata);
                }
            }
        } finally {
            accessController.unlock(VALUE, stamp);
        }
    }

    @Override
    public final String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("AsyncSetProperty [");
        long stamp = 0;

        try {
            if (!AsyncFX.isDebuggerAttached()) {
                stamp = accessController.writeLock(VALUE, INSTANCE);
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
                    result.append("value: ").append(getCore());
                } else {
                    result.append("invalid");
                }
            } else {
                result.append("value: ").append(getCore());
            }

            result.append("]");
            return result.toString();
        } finally {
            if (!AsyncFX.isDebuggerAttached() && StampedLock.isWriteLockStamp(stamp)) {
                accessController.unlockWrite(VALUE, stamp);
            }
        }
    }

    private static class Listener<E> implements InvalidationListener, WeakListener, Runnable {
        private final WeakReference<AsyncSetPropertyBase<E>> wref;

        Listener(AsyncSetPropertyBase<E> ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            AsyncSetPropertyBase<E> ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                ref.getExecutor().execute(this);
            }
        }

        @Override
        public void run() {
            AsyncSetPropertyBase<E> ref = wref.get();
            if (ref != null) {
                ref.markInvalid(ref.value);
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return wref.get() == null;
        }
    }

}
