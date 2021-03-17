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
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AsyncInvalidationListenerWrapper;
import org.asyncfx.beans.binding.AsyncExpressionHelper;
import org.asyncfx.beans.binding.LifecycleValueConverter;
import org.asyncfx.beans.binding.ValueConverter;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncObjectPropertyBase<T> extends AsyncObjectPropertyBaseImpl<T> {

    private final InvalidationListener invalidationListener = this::fireSubValueChangedEvent;
    private final Executor effectiveExecutor = runnable -> getExecutor().execute(runnable);
    private boolean subtreeValid = true;
    private InvalidationListener listener;
    private LifecycleValueConverter converter;

    public AsyncObjectPropertyBase(PropertyMetadata<T> metadata) {
        super(metadata);
    }

    AsyncObjectPropertyBase(PropertyObject bean, PropertyMetadata<T> metadata) {
        super(bean, metadata);
    }

    @Override
    T get(boolean critical) {
        long stamp = 0;
        boolean read = true;

        try {
            if ((stamp = accessController.tryOptimisticRead(VALUE, GROUP)) != 0) {
                boolean valid = this.valid;
                boolean subtreeValid = this.subtreeValid;
                T value = this.value;
                PropertyMetadata<T> metadata = this.metadata;
                if (accessController.validate(VALUE, GROUP, stamp)) {
                    if (critical) {
                        PropertyHelper.verifyConsistency(metadata);
                    }

                    if (valid) {
                        if (!subtreeValid && value instanceof PropertyObject) {
                            stamp = accessController.writeLock(VALUE, GROUP);
                            if (!this.subtreeValid && this.value instanceof PropertyObject) {
                                ((PropertyObject)this.value).validate();
                                this.subtreeValid = true;
                            }
                        }

                        return value;
                    }

                    read = false;
                }
            }

            if (read) {
                stamp = accessController.readLock(VALUE, GROUP);
                if (valid && (subtreeValid || !(value instanceof PropertyObject))) {
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

    @Override
    @SuppressWarnings("unchecked")
    T getCore() {
        if (valid) {
            if (!subtreeValid && value instanceof PropertyObject) {
                ((PropertyObject)value).validate();
                subtreeValid = true;
            }

            return value;
        }

        if (observable != null) {
            if (converter != null) {
                if (value != null) {
                    converter.remove(value);
                }

                Object newValue =
                    (observable instanceof ReadOnlyAsyncObjectProperty)
                        ? ((ReadOnlyAsyncObjectProperty<T>)observable).getValueUncritical()
                        : observable.getValue();

                if (newValue instanceof PropertyObject) {
                    PropertyObject obj = (PropertyObject)newValue;
                    obj.addListener(AsyncInvalidationListenerWrapper.wrap(invalidationListener, effectiveExecutor));
                    obj.validate();
                }

                value = (T)converter.convert(newValue);
            } else {
                T newValue =
                    (observable instanceof ReadOnlyAsyncObjectProperty)
                        ? ((ReadOnlyAsyncObjectProperty<T>)observable).getValueUncritical()
                        : ((ObservableValue<? extends T>)observable).getValue();

                if (newValue instanceof PropertyObject) {
                    PropertyObject obj = (PropertyObject)newValue;
                    obj.addListener(AsyncInvalidationListenerWrapper.wrap(invalidationListener, effectiveExecutor));
                    obj.validate();
                }

                value = newValue;
            }
        } else if (value instanceof PropertyObject) {
            PropertyObject obj = (PropertyObject)value;
            obj.addListener(AsyncInvalidationListenerWrapper.wrap(invalidationListener, effectiveExecutor));
            obj.validate();
        }

        valid = true;
        subtreeValid = true;
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(T newValue) {
        long valueStamp = 0;
        long eventStamp = 0;
        boolean invalidate, fireEvent = false;

        try {
            valueStamp = accessController.writeLock(VALUE, GROUP);
            PropertyHelper.verifyAccess(this, metadata);

            if (observable != null) {
                throw new RuntimeException("A bound value cannot be set.");
            }

            if (value == newValue) {
                return;
            }

            if (value != null && converter != null) {
                converter.remove(value);
            }

            if (value instanceof PropertyObject) {
                PropertyObject obj = (PropertyObject)value;
                obj.removeListener(invalidationListener);
            }

            value = newValue;
            invalidate = valid;

            if (invalidate) {
                valid = false;
                eventStamp = accessController.writeLock(EVENT, GROUP);
                resolveDeferredListeners();

                if (AsyncExpressionHelper.validatesValue(helper)) {
                    try {
                        newValue = getCore();
                    } catch (Exception e) {
                        accessController.unlockWrite(EVENT, eventStamp);
                        throw e;
                    }
                }

                if (!(fireEvent = !accessController.isLocked())) {
                    T newValueCopy = newValue;

                    accessController.defer(
                        () -> {
                            long stamp = 0;
                            try {
                                stamp = accessController.writeLock(EVENT, GROUP);
                                invalidated();
                                AsyncExpressionHelper.fireValueChangedEvent(helper, newValueCopy, false);
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
                AsyncExpressionHelper.fireValueChangedEvent(helper, newValue, false);
            } finally {
                accessController.unlockWrite(EVENT, eventStamp);
            }
        }
    }

    @Override
    public <U> void bind(final ObservableValue<? extends U> source, LifecycleValueConverter<U, ? extends T> converter) {
        bindCore(source, new ValueConverterAdapter<>(converter));
    }

    @Override
    @SuppressWarnings("unchecked")
    <U> void bindCore(final ObservableValue<? extends U> source, ValueConverter<U, ? extends T> converter) {
        if (source == null) {
            throw new NullPointerException("Cannot bind to null.");
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
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            metadata = this.metadata;

            if (metadata.getConsistencyGroup() != null) {
                throw new IllegalStateException("A property of a consistency group cannot be bound.");
            }

            if (!source.equals(observable)) {
                unbindCore();
                observable = source;

                if (converter != null) {
                    this.converter =
                        converter instanceof LifecycleValueConverter
                            ? (LifecycleValueConverter)converter
                            : new ValueConverterAdapter<>(converter);
                }

                if (listener == null) {
                    listener = new Listener(this);
                }

                PropertyHelper.addListener(observable, listener, accessController);

                if (this.converter != null) {
                    if (value != null) {
                        this.converter.remove(value);
                    }

                    if (source instanceof ReadOnlyAsyncObjectProperty) {
                        Object value =
                            PropertyHelper.getValueUncritical((ReadOnlyAsyncObjectProperty)source, accessController);
                        newValue = (T)this.converter.convert(value);
                    } else {
                        newValue = (T)this.converter.convert(source.getValue());
                    }
                } else {
                    if (observable instanceof ReadOnlyAsyncObjectProperty) {
                        newValue =
                            (T)PropertyHelper.getValueUncritical((ReadOnlyAsyncProperty)observable, accessController);
                    } else {
                        newValue = ((ObservableValue<? extends T>)observable).getValue();
                    }
                }

                if (value == newValue) {
                    return;
                }

                invalidate = valid;
                if (invalidate) {
                    valid = false;
                    eventStamp = accessController.writeLock(EVENT, INSTANCE);
                    resolveDeferredListeners();

                    if (AsyncExpressionHelper.validatesValue(helper)) {
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
            final T newValueCopy = newValue;
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
                            AsyncExpressionHelper.fireValueChangedEvent(helper, newValueCopy, false);
                        } finally {
                            accessController.unlockWrite(EVENT, eventStampCopy);
                        }
                    });
            } else {
                try {
                    accessController.changeEventLockOwner(Thread.currentThread());
                    invalidated();
                    AsyncExpressionHelper.fireValueChangedEvent(helper, newValueCopy, false);
                } finally {
                    accessController.unlockWrite(EVENT, eventStampCopy);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    void unbindCore() {
        if (observable != null) {
            if (converter != null) {
                if (observable instanceof ReadOnlyAsyncObjectProperty) {
                    value = (T)converter.convert(((ReadOnlyAsyncObjectProperty)observable).getUncritical());
                } else {
                    value = (T)converter.convert(observable.getValue());
                }
            } else {
                if (observable instanceof ReadOnlyAsyncObjectProperty) {
                    value = ((ReadOnlyAsyncObjectProperty<? extends T>)observable).getUncritical();
                } else {
                    value = ((ObservableValue<? extends T>)observable).getValue();
                }
            }

            observable.removeListener(listener);
            observable = null;
            converter = null;
        }
    }

    private void fireSubValueChangedEvent(@SuppressWarnings("unused") Observable observable) {
        long valueStamp = 0;
        long eventStamp = 0;
        boolean invalidate = false;
        T currentValue = null;

        try {
            if ((valueStamp = accessController.tryOptimisticRead(VALUE, GROUP)) != 0) {
                invalidate = this.subtreeValid;
                if (!accessController.validate(VALUE, GROUP, valueStamp)) {
                    valueStamp = accessController.readLock(VALUE, GROUP);
                    invalidate = this.subtreeValid;
                }
            }

            if (invalidate) {
                subtreeValid = false;
                eventStamp = accessController.writeLock(EVENT, INSTANCE);
                resolveDeferredListeners();

                if (AsyncExpressionHelper.validatesValue(helper)) {
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
                AsyncExpressionHelper.fireValueChangedEvent(helper, currentValue, true);
            } finally {
                accessController.unlockWrite(EVENT, eventStamp);
            }
        }
    }

    private static class Listener implements InvalidationListener, WeakListener, Runnable {
        private final WeakReference<AsyncObjectPropertyBase<?>> wref;

        Listener(AsyncObjectPropertyBase<?> ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            AsyncObjectPropertyBase ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                ref.getExecutor().execute(this);
            }
        }

        @Override
        public void run() {
            AsyncObjectPropertyBase ref = wref.get();
            if (ref != null) {
                ref.markInvalid();
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return wref.get() == null;
        }
    }

}
