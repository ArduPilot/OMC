/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import com.google.common.base.Objects;
import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.AsyncExpressionHelper;
import org.asyncfx.beans.binding.ValueConverter;
import org.asyncfx.concurrent.Dispatcher;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncStringPropertyBase extends AsyncStringPropertyBaseImpl {

    private InvalidationListener listener;

    public AsyncStringPropertyBase(PropertyMetadata<String> metadata) {
        super(metadata);
    }

    AsyncStringPropertyBase(PropertyObject bean, PropertyMetadata<String> metadata) {
        super(bean, metadata);
    }

    @Override
    String getCore() {
        if (valid) {
            return value;
        }

        if (observable instanceof ReadOnlyAsyncStringProperty) {
            value = ((ReadOnlyAsyncStringProperty)observable).getUncritical();
        } else if (observable != null) {
            value = observable.get();
        }

        valid = true;
        return value;
    }

    @Override
    public void set(String newValue) {
        long stamp = 0;
        boolean invalidate, fireEvent = false;
        AsyncExpressionHelper<String> helper;

        try {
            stamp = accessController.writeLock(true);
            PropertyHelper.verifyAccess(this, metadata);

            if (observable != null) {
                throw new RuntimeException("A bound value cannot be set.");
            }

            if (Objects.equal(value, newValue)) {
                return;
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
                    String newValueCopy = newValue;

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
    <U> void bindCore(final ObservableValue<? extends U> source, ValueConverter<U, String> converter) {
        if (source == null) {
            throw new NullPointerException("Cannot bind to null.");
        }

        if (isBoundBidirectionally()) {
            throw new IllegalStateException(
                "A bidirectionally bound property cannot be the target of a unidirectional binding.");
        }

        long stamp = 0;
        String newValue = null;
        PropertyMetadata<String> metadata;
        AsyncExpressionHelper<String> helper;
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
                if (source instanceof ObservableStringValue) {
                    observable = (ObservableStringValue)source;
                } else {
                    observable = new ValueWrapper<>(source, converter);
                }

                if (listener == null) {
                    listener = new Listener(this);
                }

                PropertyHelper.addListener(observable, listener, accessController);

                if (observable instanceof ReadOnlyAsyncStringProperty) {
                    newValue =
                        (String)
                            PropertyHelper.getValueUncritical(
                                (ReadOnlyAsyncStringProperty)observable, accessController);
                } else {
                    newValue = observable.get();
                }

                if (Objects.equal(value, newValue)) {
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
                final String newValueCopy = newValue;

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
            if (observable instanceof ReadOnlyAsyncStringProperty) {
                value = ((ReadOnlyAsyncStringProperty)observable).getUncritical();
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
        private final WeakReference<AsyncStringPropertyBase> wref;

        Listener(AsyncStringPropertyBase ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            AsyncStringPropertyBase ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                ref.getExecutor().execute(this);
            }
        }

        @Override
        public void run() {
            AsyncStringPropertyBase ref = wref.get();
            if (ref != null) {
                ref.markInvalid();
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return wref.get() == null;
        }
    }

    private static class ValueWrapper<U> extends StringBinding {
        private ObservableValue observable;
        private ValueConverter converter;

        ValueWrapper(ObservableValue<? extends U> observable, ValueConverter<U, ? extends String> converter) {
            this.observable = observable;
            this.converter = converter;
            bind(observable);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected String computeValue() {
            if (converter == null) {
                return ((ObservableValue<? extends String>)observable).getValue();
            }

            return (String)converter.convert(observable.getValue());
        }

        @Override
        public void dispose() {
            unbind(observable);
        }
    }

}
