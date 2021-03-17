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
import javafx.beans.binding.FloatBinding;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.AsyncExpressionHelper;
import org.asyncfx.beans.binding.ValueConverter;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncFloatPropertyBase extends AsyncFloatPropertyBaseImpl {

    private InvalidationListener listener;

    public AsyncFloatPropertyBase(PropertyMetadata<Number> metadata) {
        super(metadata);
    }

    AsyncFloatPropertyBase(PropertyObject bean, PropertyMetadata<Number> metadata) {
        super(bean, metadata);
    }

    @Override
    float getCore() {
        if (valid) {
            return value;
        }

        if (observable instanceof ReadOnlyAsyncFloatProperty) {
            value = ((ReadOnlyAsyncFloatProperty)observable).getUncritical();
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
        float newValue = 0;
        PropertyMetadata<Number> metadata;
        boolean invalidate = false;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            metadata = this.metadata;

            if (metadata.getConsistencyGroup() != null) {
                throw new IllegalStateException("A property of a consistency group cannot be bound.");
            }

            ObservableFloatValue newObservable;
            if (source instanceof ObservableFloatValue) {
                newObservable = (ObservableFloatValue)source;
            } else if (source instanceof ObservableNumberValue) {
                final ObservableNumberValue numberValue = (ObservableNumberValue)source;
                newObservable =
                    new ValueWrapper(source) {
                        @Override
                        protected float computeValue() {
                            return numberValue.floatValue();
                        }
                    };
            } else if (converter != null) {
                newObservable =
                    new ValueWrapper(source) {
                        @Override
                        protected float computeValue() {
                            final Number value = converter.convert(source.getValue());
                            return (value == null) ? 0 : value.floatValue();
                        }
                    };
            } else {
                throw new IllegalArgumentException("Converter cannot be null.");
            }

            if (!newObservable.equals(observable)) {
                unbindCore();
                observable = newObservable;
                if (listener == null) {
                    listener = new Listener(this);
                }

                PropertyHelper.addListener(observable, listener, accessController);

                if (observable instanceof ReadOnlyAsyncFloatProperty) {
                    newValue =
                        (float)
                            PropertyHelper.getValueUncritical((ReadOnlyAsyncFloatProperty)observable, accessController);
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
            final float newValueCopy = newValue;
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
    void unbindCore() {
        if (observable != null) {
            if (observable instanceof ReadOnlyAsyncFloatProperty) {
                value = ((ReadOnlyAsyncFloatProperty)observable).getUncritical();
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

    private static class Listener implements InvalidationListener, WeakListener, Runnable {
        private final WeakReference<AsyncFloatPropertyBase> wref;

        Listener(AsyncFloatPropertyBase ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            AsyncFloatPropertyBase ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                ref.getExecutor().execute(this);
            }
        }

        @Override
        public void run() {
            AsyncFloatPropertyBase ref = wref.get();
            if (ref != null) {
                ref.markInvalid();
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return wref.get() == null;
        }
    }

    private abstract static class ValueWrapper extends FloatBinding {
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
