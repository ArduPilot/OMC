/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import static org.asyncfx.beans.AccessControllerImpl.LockName.EVENT;
import static org.asyncfx.beans.AccessControllerImpl.LockName.VALUE;
import static org.asyncfx.beans.AccessControllerImpl.LockType.INSTANCE;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.AsyncExpressionHelper;
import org.asyncfx.beans.binding.ValueConverter;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncDoublePropertyBase extends AsyncDoublePropertyBaseImpl {

    private InvalidationListener listener;

    public AsyncDoublePropertyBase(PropertyMetadata<Number> metadata) {
        super(metadata);
    }

    AsyncDoublePropertyBase(ObservableObject bean, PropertyMetadata<Number> metadata) {
        super(bean, metadata);
    }

    @Override
    double getCore() {
        if (valid) {
            return value;
        }

        if (observable instanceof ReadOnlyAsyncDoubleProperty) {
            value = ((ReadOnlyAsyncDoubleProperty)observable).getUncritical();
        } else if (observable != null) {
            value = observable.get();
        }

        valid = true;
        return value;
    }

    @Override
    <U> void bindCore(final ObservableValue<? extends U> source, ValueConverter<U, ? extends Number> converter) {
        if (source == null) {
            throw new NullPointerException("Cannot bind to null.");
        }

        if (isBoundBidirectionally()) {
            throw new IllegalStateException(
                "A bidirectionally bound property cannot be the target of a unidirectional binding.");
        }

        long valueStamp = 0;
        long eventStamp = 0;
        double newValue = 0;
        PropertyMetadata<Number> metadata;
        boolean invalidate = false;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            metadata = this.metadata;

            if (PropertyMetadata.Accessor.getConsistencyGroup(metadata).isPresent()) {
                throw new IllegalStateException("A property of a consistency group cannot be bound.");
            }

            ObservableDoubleValue newObservable;
            if (source instanceof ObservableDoubleValue) {
                newObservable = (ObservableDoubleValue)source;
            } else if (source instanceof ObservableNumberValue) {
                final ObservableNumberValue numberValue = (ObservableNumberValue)source;
                newObservable =
                    new ValueWrapper(source) {
                        @Override
                        protected double computeValue() {
                            return numberValue.doubleValue();
                        }
                    };
            } else if (converter != null) {
                newObservable =
                    new ValueWrapper(source) {
                        @Override
                        protected double computeValue() {
                            final Number value = converter.convert(source.getValue());
                            return (value == null) ? 0 : value.doubleValue();
                        }
                    };
            } else {
                throw new IllegalArgumentException("Converter cannot be null.");
            }

            if (!newObservable.equals(observable)) {
                unbindCore();
                observable = newObservable;
                if (listener == null) {
                    listener = new Listener(this, metadata.getExecutor());
                }

                observable.addListener(listener);

                if (observable instanceof ReadOnlyAsyncDoubleProperty) {
                    newValue = ((ReadOnlyAsyncDoubleProperty)observable).getUncritical();
                } else {
                    newValue = observable.get();
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
            final double newValueCopy = newValue;
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
    void unbindCore() {
        if (observable != null) {
            if (observable instanceof ReadOnlyAsyncDoubleProperty) {
                value = ((ReadOnlyAsyncDoubleProperty)observable).getUncritical();
            } else {
                value = observable.get();
            }

            observable.removeListener(listener);
            if (observable instanceof ValueWrapper) {
                ((ValueWrapper)observable).dispose();
            }

            observable = null;
        }
    }

    private static class Listener implements InvalidationListener, WeakListener {
        private final WeakReference<AsyncDoublePropertyBase> wref;
        private final Executor executor;

        Listener(AsyncDoublePropertyBase ref, Executor executor) {
            this.wref = new WeakReference<>(ref);
            this.executor = executor;
        }

        @Override
        public void invalidated(Observable observable) {
            AsyncDoublePropertyBase ref = wref.get();
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

    private abstract static class ValueWrapper extends DoubleBinding {
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
