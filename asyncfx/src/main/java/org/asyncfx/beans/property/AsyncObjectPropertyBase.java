/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

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
import org.asyncfx.concurrent.Dispatcher;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncObjectPropertyBase<T> extends AsyncObjectPropertyBaseImpl<T> {

    private final InvalidationListener invalidationListener = this::fireSubValueChangedEvent;
    private final Executor effectiveExecutor = runnable -> getExecutor().execute(runnable);
    private boolean subtreeValid = true;
    private InvalidationListener listener;
    private ValueConverter<Object, T> converter;

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
            if ((stamp = accessController.tryOptimisticRead(true)) != 0) {
                boolean valid = this.valid;
                boolean subtreeValid = this.subtreeValid;
                T value = this.value;
                PropertyMetadata<T> metadata = this.metadata;
                if (accessController.validate(true, stamp)) {
                    if (critical) {
                        PropertyHelper.verifyConsistency(metadata);
                    }

                    if (valid) {
                        if (!subtreeValid && value instanceof PropertyObject) {
                            stamp = accessController.writeLock(true);
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
                stamp = accessController.readLock(true);
                if (valid && (subtreeValid || !(value instanceof PropertyObject))) {
                    if (critical) {
                        PropertyHelper.verifyConsistency(metadata);
                    }

                    return value;
                }
            }

            long newStamp = accessController.tryConvertToWriteLock(true, stamp);
            if (newStamp == 0) {
                accessController.unlockRead(stamp);
                stamp = accessController.writeLock(true);
            } else {
                stamp = newStamp;
            }

            if (critical) {
                PropertyHelper.verifyConsistency(metadata);
            }

            return getCore();
        } finally {
            accessController.unlock(stamp);
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
            Object newValue =
                (observable instanceof ReadOnlyAsyncObjectProperty)
                    ? ((ReadOnlyAsyncObjectProperty<T>)observable).getValueUncritical()
                    : observable.getValue();

            if (converter != null) {
                if (value != null) {
                    if (newValue != null) {
                        if (converter instanceof LifecycleValueConverter) {
                            ((LifecycleValueConverter<Object, T>)converter).update(newValue, value);
                        } else {
                            value = converter.convert(newValue);
                        }
                    } else {
                        if (converter instanceof LifecycleValueConverter) {
                            ((LifecycleValueConverter<Object, T>)converter).remove(value);
                        }

                        value = null;
                    }
                } else if (newValue != null) {
                    value = converter.convert(newValue);
                }
            } else {
                value = (T)newValue;
            }

            if (newValue instanceof PropertyObject) {
                PropertyObject obj = (PropertyObject)newValue;
                obj.addListener(AsyncInvalidationListenerWrapper.wrap(invalidationListener, effectiveExecutor));
                obj.validate();
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
    public void set(T newValue) {
        long stamp = 0;
        boolean invalidate, fireEvent = false;
        AsyncExpressionHelper<T> helper;

        try {
            stamp = accessController.writeLock(true);
            PropertyHelper.verifyAccess(this, metadata);

            if (observable != null) {
                throw new RuntimeException("A bound value cannot be set.");
            }

            if (value == newValue) {
                return;
            }

            if (converter instanceof LifecycleValueConverter) {
                if (value != null) {
                    ((LifecycleValueConverter<Object, T>)converter).remove(value);
                }
            }

            converter = null;

            if (value instanceof PropertyObject) {
                PropertyObject obj = (PropertyObject)value;
                obj.removeListener(invalidationListener);
            }

            value = newValue;
            invalidate = valid;
            helper = this.helper;

            if (invalidate) {
                valid = false;

                if (AsyncExpressionHelper.validatesValue(helper)) {
                    newValue = getCore();
                }

                if (!(fireEvent = !accessController.isLocked())) {
                    T newValueCopy = newValue;

                    accessController.defer(
                        () -> {
                            invalidated();
                            AsyncExpressionHelper.fireValueChangedEvent(helper, newValueCopy, false);
                        });
                }
            }
        } finally {
            accessController.unlockWrite(stamp);
        }

        if (fireEvent) {
            invalidated();
            AsyncExpressionHelper.fireValueChangedEvent(helper, newValue, false);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    <U> void bindCore(final ObservableValue<? extends U> source, ValueConverter<U, T> converter) {
        if (source == null) {
            throw new NullPointerException("Cannot bind to null.");
        }

        if (isBoundBidirectionally()) {
            throw new IllegalStateException(
                "A bidirectionally bound property cannot be the target of a unidirectional binding.");
        }

        long stamp = 0;
        T newValue = null;
        PropertyMetadata<T> metadata;
        AsyncExpressionHelper<T> helper;
        boolean invalidate = false;

        try {
            stamp = accessController.writeLock(false);
            metadata = this.metadata;
            helper = this.helper;

            if (metadata.getConsistencyGroup() != null) {
                throw new IllegalStateException("A property of a consistency group cannot be bound.");
            }

            if (!source.equals(observable)) {
                unbindCore();
                observable = source;

                if (this.converter instanceof LifecycleValueConverter && value != null) {
                    ((LifecycleValueConverter)this.converter).remove(value);
                }

                this.converter = (ValueConverter<Object, T>)converter;

                if (listener == null) {
                    listener = new Listener(this);
                }

                PropertyHelper.addListener(observable, listener, accessController);

                if (this.converter == null) {
                    if (source instanceof ReadOnlyAsyncObjectProperty) {
                        newValue =
                            (T)PropertyHelper.getValueUncritical((ReadOnlyAsyncObjectProperty)source, accessController);
                    } else {
                        newValue = (T)source.getValue();
                    }

                    if (this.value == newValue) {
                        return;
                    }
                }

                invalidate = valid;

                if (invalidate) {
                    valid = false;

                    if (AsyncExpressionHelper.validatesValue(helper)) {
                        newValue = getCore();
                    }
                }
            }
        } finally {
            accessController.unlockWrite(stamp);
        }

        if (invalidate) {
            Dispatcher dispatcher = metadata.getDispatcher();
            if (dispatcher == null) {
                Object bean = getBean();
                dispatcher = bean instanceof PropertyObject ? ((PropertyObject)bean).getDispatcher() : null;
            }

            if (dispatcher != null) {
                final T newValueCopy = newValue;

                dispatcher.run(
                    () -> {
                        invalidated();
                        AsyncExpressionHelper.fireValueChangedEvent(helper, newValueCopy, false);
                    });
            } else {
                invalidated();
                AsyncExpressionHelper.fireValueChangedEvent(helper, newValue, false);
            }
        }
    }

    @Override
    void unbindCore() {
        if (observable != null) {
            observable.removeListener(listener);
            observable = null;
        }
    }

    private void fireSubValueChangedEvent(@SuppressWarnings("unused") Observable observable) {
        long stamp = 0;
        boolean invalidate = false;
        T currentValue = null;
        AsyncExpressionHelper<T> helper = null;

        try {
            if ((stamp = accessController.tryOptimisticRead(true)) != 0) {
                invalidate = this.subtreeValid;
                if (!accessController.validate(true, stamp)) {
                    stamp = accessController.readLock(true);
                    invalidate = this.subtreeValid;
                }
            }

            if (invalidate) {
                subtreeValid = false;
                helper = this.helper;

                if (AsyncExpressionHelper.validatesValue(helper)) {
                    currentValue = getCore();
                }
            }
        } finally {
            accessController.unlockWrite(stamp);
        }

        if (invalidate) {
            AsyncExpressionHelper.fireValueChangedEvent(helper, currentValue, true);
        }
    }

    private static class Listener implements InvalidationListener, WeakListener, Runnable {
        private final WeakReference<AsyncObjectPropertyBase<?>> wref;

        Listener(AsyncObjectPropertyBase<?> ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            AsyncObjectPropertyBase<?> ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                ref.getExecutor().execute(this);
            }
        }

        @Override
        public void run() {
            AsyncObjectPropertyBase<?> ref = wref.get();
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
