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
import javafx.beans.binding.LongBinding;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.AsyncExpressionHelper;
import org.asyncfx.beans.binding.ValueConverter;
import org.asyncfx.concurrent.Dispatcher;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncLongPropertyBase extends AsyncLongPropertyBaseImpl {

    private InvalidationListener listener;

    public AsyncLongPropertyBase(PropertyMetadata<Number> metadata) {
        super(metadata);
    }

    AsyncLongPropertyBase(PropertyObject bean, PropertyMetadata<Number> metadata) {
        super(bean, metadata);
    }

    @Override
    long getCore() {
        if (valid) {
            return value;
        }

        if (observable instanceof ReadOnlyAsyncLongProperty) {
            value = ((ReadOnlyAsyncLongProperty)observable).getUncritical();
        } else if (observable != null) {
            value = observable.get();
        }

        valid = true;
        return value;
    }

    @Override
    <U> void bindCore(final ObservableValue<? extends U> source, ValueConverter<U, Number> converter) {
        if (source == null) {
            throw new NullPointerException("Cannot bind to null.");
        }

        if (isBoundBidirectionally()) {
            throw new IllegalStateException(
                "A bidirectionally bound property cannot be the target of a unidirectional binding.");
        }

        long stamp = 0;
        long newValue = 0;
        PropertyMetadata<Number> metadata;
        AsyncExpressionHelper<Number> helper;
        boolean invalidate = false;

        try {
            stamp = accessController.writeLock(false);
            metadata = this.metadata;
            helper = this.helper;

            if (metadata.getConsistencyGroup() != null) {
                throw new IllegalStateException("A property of a consistency group cannot be bound.");
            }

            ObservableLongValue newObservable;
            if (source instanceof ObservableLongValue) {
                newObservable = (ObservableLongValue)source;
            } else if (source instanceof ObservableNumberValue) {
                final ObservableNumberValue numberValue = (ObservableNumberValue)source;
                newObservable =
                    new ValueWrapper(source) {
                        @Override
                        protected long computeValue() {
                            return numberValue.longValue();
                        }
                    };
            } else if (converter != null) {
                newObservable =
                    new ValueWrapper(source) {
                        @Override
                        protected long computeValue() {
                            final Number value = converter.convert(source.getValue());
                            return (value == null) ? 0 : value.longValue();
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

                if (observable instanceof ReadOnlyAsyncLongProperty) {
                    newValue =
                        (long)
                            PropertyHelper.getValueUncritical((ReadOnlyAsyncLongProperty)observable, accessController);
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
                final long newValueCopy = newValue;

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
            if (observable instanceof ReadOnlyAsyncLongProperty) {
                value = ((ReadOnlyAsyncLongProperty)observable).getUncritical();
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
        private final WeakReference<AsyncLongPropertyBase> wref;

        Listener(AsyncLongPropertyBase ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            AsyncLongPropertyBase ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                ref.getExecutor().execute(this);
            }
        }

        @Override
        public void run() {
            AsyncLongPropertyBase ref = wref.get();
            if (ref != null) {
                ref.markInvalid();
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return wref.get() == null;
        }
    }

    private abstract static class ValueWrapper extends LongBinding {
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
