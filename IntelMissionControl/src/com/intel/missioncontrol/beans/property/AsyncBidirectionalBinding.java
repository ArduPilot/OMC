/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.beans.binding.BidirectionalBindingMarker;
import com.intel.missioncontrol.beans.binding.BidirectionalValueConverter;
import com.intel.missioncontrol.beans.binding.Converters;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.checkerframework.checker.nullness.qual.Nullable;

class AsyncBidirectionalBinding {

    static <T> void bind(AsyncProperty<T> target, AsyncProperty<T> source) {
        if (target.isBound()) {
            throw new IllegalStateException("The property is already bound.");
        }

        if (source.isBound()) {
            throw new IllegalStateException("The source property is already bound.");
        }

        BindingEndpoint<T, T> targetEndpoint = new BindingEndpoint<>(target, null);
        BindingEndpoint<T, T> sourceEndpoint = new BindingEndpoint<>(source, null);
        BindingEndpoint.connect(targetEndpoint, sourceEndpoint);
        target.addListener(targetEndpoint);
        source.addListener(sourceEndpoint);
        targetEndpoint.valueSource = source;

        try {
            target.setValueAsync(source.getValue()).get();
        } catch (InterruptedException | ExecutionException e) {
            target.removeListener(targetEndpoint);
            source.removeListener(sourceEndpoint);
            throw new RuntimeException(e);
        }

        targetEndpoint.valueSource = null;
    }

    static <T, U> void bind(
            AsyncProperty<T> target, AsyncProperty<U> source, BidirectionalValueConverter<U, T> converter) {
        if (target.isBound()) {
            throw new IllegalStateException("A bound property cannot be the target of a bidirectional binding.");
        }

        if (source.isBound()) {
            throw new IllegalStateException("A bound property cannot be the source of a bidirectional binding.");
        }

        BindingEndpoint<T, U> targetEndpoint = new BindingEndpoint<>(target, Converters.invert(converter));
        BindingEndpoint<U, T> sourceEndpoint = new BindingEndpoint<>(source, converter);
        BindingEndpoint.connect(targetEndpoint, sourceEndpoint);
        target.addListener(targetEndpoint);
        source.addListener(sourceEndpoint);
        targetEndpoint.valueSource = source;

        try {
            target.setValueAsync(converter.convert(source.getValue())).get();
        } catch (InterruptedException | ExecutionException e) {
            target.removeListener(targetEndpoint);
            source.removeListener(sourceEndpoint);
            throw new RuntimeException(e);
        }

        targetEndpoint.valueSource = null;
    }

    static <T> void unbind(AsyncProperty<T> target, AsyncProperty<T> source) {
        BindingEndpoint<T, T> targetEndpoint = new BindingEndpoint<>(target, null);
        BindingEndpoint<T, T> sourceEndpoint = new BindingEndpoint<>(source, null);
        BindingEndpoint.connect(targetEndpoint, sourceEndpoint);
        target.removeListener(targetEndpoint);
        source.removeListener(sourceEndpoint);
    }

    private static class BindingEndpoint<T, U> implements ChangeListener<T>, BidirectionalBindingMarker {

        private static class BoxedLong {
            long value;

            long increment() {
                return ++value;
            }
        }

        private static class OrderedValue<T> {
            private final T value;
            private final long serial;
            private final AsyncProperty source;

            OrderedValue(T value, long serial, AsyncProperty source) {
                this.value = value;
                this.serial = serial;
                this.source = source;
            }

            public T getValue() {
                return value;
            }

            public long getSerial() {
                return serial;
            }

            public AsyncProperty getSource() {
                return source;
            }
        }

        private final WeakReference<AsyncProperty<T>> property;
        private final BidirectionalValueConverter<T, U> converter;
        private Object mutex;
        private BindingEndpoint<U, T> remoteEndpoint;
        private AsyncProperty valueSource;
        private BoxedLong serialCounter;
        private long serial;

        BindingEndpoint(AsyncProperty<T> property, @Nullable BidirectionalValueConverter<T, U> converter) {
            this.property = new WeakReference<>(property);
            this.converter = converter;
        }

        public static void connect(BindingEndpoint target, BindingEndpoint source) {
            BoxedLong serialCounter = new BoxedLong();
            Object mutex = new Object();
            target.remoteEndpoint = source;
            target.serialCounter = serialCounter;
            target.mutex = mutex;
            source.remoteEndpoint = target;
            source.serialCounter = serialCounter;
            source.mutex = mutex;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final Object propertyA1 = property.get();
            final Object propertyA2 = remoteEndpoint.property.get();
            if (propertyA1 == null || propertyA2 == null) {
                return false;
            }

            if (obj instanceof BindingEndpoint) {
                final BindingEndpoint otherBinding = (BindingEndpoint)obj;
                final Object propertyB1 = otherBinding.property.get();
                final Object propertyB2 = otherBinding.remoteEndpoint.property.get();
                if (propertyB1 == null || propertyB2 == null) {
                    return false;
                }

                if (propertyA1 == propertyB1 && propertyA2 == propertyB2) {
                    return true;
                }

                return propertyA1 == propertyB2 && propertyA2 == propertyB1;
            }

            return false;
        }

        private void dispose() {
            AsyncProperty<T> property = this.property.get();
            if (property != null) {
                property.removeListener(this);
            }
        }

        @Override
        @SuppressWarnings({"unchecked", "SynchronizeOnNonFinalField"})
        public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
            if (oldValue == newValue) {
                return;
            }

            synchronized (mutex) {
                AsyncProperty<T> property = this.property.get();
                AsyncProperty<U> remoteProperty = remoteEndpoint.property.get();

                if (property == null || remoteProperty == null) {
                    dispose();
                    remoteEndpoint.dispose();
                } else if (valueSource != remoteProperty) {
                    serial = serialCounter.increment();
                    final OrderedValue<U> value =
                        new OrderedValue<>(
                            converter != null ? converter.convert(newValue) : (U)newValue, serial, property);
                    remoteProperty.getMetadata().getExecutor().execute(new UpdateRunnable<>(remoteEndpoint, value));
                }

                valueSource = null;
            }
        }

        private static class UpdateRunnable<T, U> implements Runnable {
            private final BindingEndpoint<U, T> endpoint;
            private final OrderedValue<U> value;

            UpdateRunnable(BindingEndpoint<U, T> endpoint, OrderedValue<U> value) {
                this.endpoint = endpoint;
                this.value = value;
            }

            @Override
            @SuppressWarnings("SynchronizeOnNonFinalField")
            public void run() {
                synchronized (endpoint.mutex) {
                    AsyncProperty<U> property = endpoint.property.get();
                    if (property == null) {
                        return;
                    }

                    long serial = value.getSerial();
                    if (endpoint.serial < serial) {
                        endpoint.serial = serial;
                        endpoint.valueSource = value.getSource();
                        property.setValue(value.getValue());
                    }
                }
            }
        }

        @Override
        public String toString() {
            String propertyName;
            AsyncProperty property = this.property.get();
            if (property != null) {
                propertyName = property.getName();
            } else {
                propertyName = "<null>";
            }

            String remotePropertyName;
            AsyncProperty remoteProperty = remoteEndpoint.property.get();
            if (remoteProperty != null) {
                remotePropertyName = remoteProperty.getName();
            } else {
                remotePropertyName = "<null";
            }

            return AsyncBidirectionalBinding.class.getSimpleName()
                + " [property="
                + propertyName
                + ", remoteProperty="
                + remotePropertyName
                + "]";
        }
    }
}
