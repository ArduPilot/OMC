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
import org.asyncfx.beans.InvalidationListenerWrapper;
import org.asyncfx.beans.binding.AsyncExpressionHelper;
import org.asyncfx.beans.binding.LifecycleValueConverter;
import org.asyncfx.beans.binding.ValueConverter;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncObjectPropertyBase<T> extends AsyncObjectPropertyBaseImpl<T> {

    private final InvalidationListener fireSubValueChangedEventMethod = this::fireSubValueChangedEvent;
    private boolean subtreeValid = true;
    private InvalidationListener listener;
    private LifecycleValueConverter converter;

    public AsyncObjectPropertyBase(PropertyMetadata<T> metadata) {
        super(metadata);
    }

    AsyncObjectPropertyBase(ObservableObject bean, PropertyMetadata<T> metadata) {
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
                        metadata.verifyConsistency();
                    }

                    if (valid) {
                        if (!subtreeValid && value instanceof ObservableObject) {
                            stamp = accessController.writeLock(VALUE, GROUP);
                            if (!this.subtreeValid && this.value instanceof ObservableObject) {
                                ((ObservableObject)this.value).validate();
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
                if (valid && (subtreeValid || !(value instanceof ObservableObject))) {
                    if (critical) {
                        metadata.verifyConsistency();
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
                metadata.verifyConsistency();
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
            if (!subtreeValid && value instanceof ObservableObject) {
                ((ObservableObject)value).validate();
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

                if (newValue instanceof ObservableObject) {
                    ObservableObject obj = (ObservableObject)newValue;
                    obj.addListener(
                        InvalidationListenerWrapper.wrap(fireSubValueChangedEventMethod, metadata.getExecutor()));
                    obj.validate();
                }

                value = (T)converter.convert(newValue);
            } else {
                T newValue =
                    (observable instanceof ReadOnlyAsyncObjectProperty)
                        ? ((ReadOnlyAsyncObjectProperty<T>)observable).getValueUncritical()
                        : ((ObservableValue<? extends T>)observable).getValue();

                if (newValue instanceof ObservableObject) {
                    ObservableObject obj = (ObservableObject)newValue;
                    obj.addListener(
                        InvalidationListenerWrapper.wrap(fireSubValueChangedEventMethod, metadata.getExecutor()));
                    obj.validate();
                }

                value = newValue;
            }
        } else if (value instanceof ObservableObject) {
            ObservableObject obj = (ObservableObject)value;
            obj.addListener(InvalidationListenerWrapper.wrap(fireSubValueChangedEventMethod, metadata.getExecutor()));
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
            metadata.verifyAccess();

            if (observable != null) {
                throw new RuntimeException("A bound value cannot be set.");
            }

            if (value == newValue) {
                return;
            }

            if (value != null && converter != null) {
                converter.remove(value);
            }

            if (value instanceof ObservableObject) {
                ObservableObject obj = (ObservableObject)value;
                obj.removeListener(fireSubValueChangedEventMethod);
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

            if (PropertyMetadata.Accessor.getConsistencyGroup(metadata).isPresent()) {
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
                    listener = new Listener(this, metadata.getExecutor());
                }

                observable.addListener(listener);

                if (this.converter != null) {
                    if (value != null) {
                        this.converter.remove(value);
                    }

                    if (source instanceof ReadOnlyAsyncObjectProperty) {
                        newValue = (T)this.converter.convert(((ReadOnlyAsyncObjectProperty)source).getUncritical());
                    } else {
                        newValue = (T)this.converter.convert(source.getValue());
                    }
                } else {
                    if (observable instanceof ReadOnlyAsyncObjectProperty) {
                        newValue = ((ReadOnlyAsyncObjectProperty<? extends T>)observable).getUncritical();
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

            metadata.getExecutor()
                .execute(
                    () -> {
                        try {
                            accessController.changeEventLockOwner(Thread.currentThread());
                            invalidated();
                            AsyncExpressionHelper.fireValueChangedEvent(helper, newValueCopy, false);
                        } finally {
                            accessController.unlockWrite(EVENT, eventStampCopy);
                        }
                    });
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

    private static class Listener implements InvalidationListener, WeakListener {
        private final WeakReference<AsyncObjectPropertyBase<?>> wref;
        private final Executor executor;

        Listener(AsyncObjectPropertyBase<?> ref, Executor executor) {
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
