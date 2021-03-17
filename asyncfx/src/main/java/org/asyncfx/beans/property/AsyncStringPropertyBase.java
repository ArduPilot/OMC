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

import com.google.common.base.Objects;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.AsyncExpressionHelper;
import org.asyncfx.beans.binding.ValueConverter;

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
        long valueStamp = 0;
        long eventStamp = 0;
        boolean invalidate, fireEvent = false;

        try {
            valueStamp = accessController.writeLock(VALUE, GROUP);
            PropertyHelper.verifyAccess(this, metadata);

            if (observable != null) {
                throw new RuntimeException("A bound value cannot be set.");
            }

            if (Objects.equal(value, newValue)) {
                return;
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
                    String newValueCopy = newValue;

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
    <U> void bindCore(final ObservableValue<? extends U> source, ValueConverter<U, ? extends String> converter) {
        if (source == null) {
            throw new NullPointerException("Cannot bind to null.");
        }

        if (isBoundBidirectionally()) {
            throw new IllegalStateException(
                "A bidirectionally bound property cannot be the target of a unidirectional binding.");
        }

        long valueStamp = 0;
        long eventStamp = 0;
        String newValue = null;
        PropertyMetadata<String> metadata;
        boolean invalidate = false;

        try {
            valueStamp = accessController.writeLock(VALUE, INSTANCE);
            metadata = this.metadata;

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
            final String newValueCopy = newValue;
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
