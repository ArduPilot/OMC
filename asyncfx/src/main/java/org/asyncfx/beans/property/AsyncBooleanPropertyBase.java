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
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.AsyncExpressionHelper;
import org.asyncfx.beans.binding.ValueConverter;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncBooleanPropertyBase extends AsyncBooleanPropertyBaseImpl {

    private InvalidationListener listener;

    public AsyncBooleanPropertyBase(PropertyMetadata<Boolean> metadata) {
        super(metadata);
    }

    AsyncBooleanPropertyBase(ObservableObject bean, PropertyMetadata<Boolean> metadata) {
        super(bean, metadata);
    }

    @Override
    boolean getCore() {
        if (valid) {
            return value;
        }

        if (observable instanceof ReadOnlyAsyncBooleanProperty) {
            value = ((ReadOnlyAsyncBooleanProperty)observable).getUncritical();
        } else if (observable != null) {
            value = observable.get();
        }

        valid = true;
        return value;
    }

    @Override
    <U> void bindCore(final ObservableValue<? extends U> source, ValueConverter<U, ? extends Boolean> converter) {
        if (source == null) {
            throw new NullPointerException("Cannot bind to null.");
        }

        if (isBoundBidirectionally()) {
            throw new IllegalStateException(
                "A bidirectionally bound property cannot be the target of a unidirectional binding.");
        }

        long valueStamp = 0;
        long eventStamp = 0;
        boolean newValue = false;
        PropertyMetadata<Boolean> metadata;
        boolean invalidate = false;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            metadata = this.metadata;

            if (PropertyMetadata.Accessor.getConsistencyGroup(metadata).isPresent()) {
                throw new IllegalStateException("A property of a consistency group cannot be bound.");
            }

            final ObservableBooleanValue newObservable =
                (source instanceof ObservableBooleanValue)
                    ? (ObservableBooleanValue)source
                    : new ValueWrapper<>(source, converter);

            if (!newObservable.equals(observable)) {
                unbindCore();
                observable = newObservable;
                if (listener == null) {
                    listener = new Listener(this, metadata.getExecutor());
                }

                observable.addListener(listener);

                if (observable instanceof ReadOnlyAsyncBooleanProperty) {
                    newValue = ((ReadOnlyAsyncBooleanProperty)observable).getUncritical();
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
            final boolean newValueCopy = newValue;
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
            if (observable instanceof ReadOnlyAsyncBooleanProperty) {
                value = ((ReadOnlyAsyncBooleanProperty)observable).getUncritical();
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
        private final WeakReference<AsyncBooleanPropertyBase> wref;
        private final Executor executor;

        Listener(AsyncBooleanPropertyBase ref, Executor executor) {
            this.wref = new WeakReference<>(ref);
            this.executor = executor;
        }

        @Override
        public void invalidated(Observable observable) {
            AsyncBooleanPropertyBase ref = wref.get();
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

    private static class ValueWrapper<U> extends BooleanBinding {
        private ObservableValue observable;
        private ValueConverter converter;

        ValueWrapper(ObservableValue<? extends U> observable, ValueConverter<U, ? extends Boolean> converter) {
            this.observable = observable;
            this.converter = converter;
            bind(observable);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected boolean computeValue() {
            Boolean value;
            if (converter == null) {
                value = ((ObservableValue<? extends Boolean>)observable).getValue();
            } else {
                value = (Boolean)converter.convert(observable.getValue());
            }

            return (value == null) ? false : value;
        }

        @Override
        public void dispose() {
            unbind(observable);
        }
    }

}
