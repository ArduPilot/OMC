/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.AsyncExpressionHelper;
import org.asyncfx.beans.binding.ValueConverter;
import org.asyncfx.concurrent.Dispatcher;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncBooleanPropertyBase extends AsyncBooleanPropertyBaseImpl {

    private InvalidationListener listener;

    public AsyncBooleanPropertyBase(PropertyMetadata<Boolean> metadata) {
        super(metadata);
    }

    AsyncBooleanPropertyBase(PropertyObject bean, PropertyMetadata<Boolean> metadata) {
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
    <U> void bindCore(final ObservableValue<? extends U> source, ValueConverter<U, Boolean> converter) {
        if (source == null) {
            throw new NullPointerException("Cannot bind to null.");
        }

        if (isBoundBidirectionally()) {
            throw new IllegalStateException(
                "A bidirectionally bound property cannot be the target of a unidirectional binding.");
        }

        long stamp = 0;
        boolean newValue = false;
        PropertyMetadata<Boolean> metadata;
        AsyncExpressionHelper<Boolean> helper;
        boolean invalidate = false;

        try {
            stamp = accessController.writeLock(false);
            metadata = this.metadata;
            helper = this.helper;

            if (metadata.getConsistencyGroup() != null) {
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
                    listener = new Listener(this);
                }

                PropertyHelper.addListener(observable, listener, accessController);

                if (observable instanceof ReadOnlyAsyncBooleanProperty) {
                    newValue =
                        (boolean)
                            PropertyHelper.getValueUncritical(
                                (ReadOnlyAsyncBooleanProperty)observable, accessController);
                } else {
                    newValue = observable.get();
                }

                if (value == newValue) {
                    return;
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
                final boolean newValueCopy = newValue;

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

    private static class Listener implements InvalidationListener, WeakListener, Runnable {
        private final WeakReference<AsyncBooleanPropertyBase> wref;

        Listener(AsyncBooleanPropertyBase ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            AsyncBooleanPropertyBase ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                ref.getExecutor().execute(this);
            }
        }

        @Override
        public void run() {
            AsyncBooleanPropertyBase ref = wref.get();
            if (ref != null) {
                ref.markInvalid();
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
