/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.AsyncObservableSet;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings({"WeakerAccess", "unused"})
public class PropertyPath {

    private static AtomicInteger count = new AtomicInteger();

    private static String generatePropertyName() {
        int value = count.incrementAndGet();
        return PropertyPath.class.getSimpleName() + "$" + value;
    }

    public static class PropertyPathSegment<U, V> {
        private final PropertyPathStore store;
        private final Function<U, ObservableValue<V>> propertySelector;
        private final PropertyPathSegment<?, U> previousSegment;
        private final InvalidationListener invalidationListener = o -> onInvalidated();
        private PropertyPathSegment<V, ?> nextSegment;
        private ObservableValue<V> observable;
        private Consumer<V> updateProxy;
        private Object proxy;

        PropertyPathSegment(@Nullable PropertyPathStore store, ObservableValue<V> observable) {
            this.store = store;
            this.propertySelector = null;
            this.previousSegment = null;
            this.observable = observable;
            this.observable.addListener(new WeakInvalidationListener(invalidationListener));
        }

        PropertyPathSegment(
                @Nullable PropertyPathStore store,
                PropertyPathSegment<?, U> previousSegment,
                Function<U, ObservableValue<V>> propertySelector) {
            this.store = store;
            this.previousSegment = previousSegment;
            this.propertySelector = propertySelector;
            ObservableValue<U> previousObservable = previousSegment.observable;
            U previousValue = previousObservable != null ? previousObservable.getValue() : null;
            if (previousValue != null) {
                this.observable = propertySelector.apply(previousObservable.getValue());
                this.observable.addListener(new WeakInvalidationListener(invalidationListener));
            }
        }

        public <W> PropertyPathSegment<V, W> select(Function<V, ObservableValue<W>> selector) {
            PropertyPathSegment<V, W> nextSegment = new PropertyPathSegment<>(store, this, selector);
            this.nextSegment = nextSegment;
            return nextSegment;
        }

        public <W> ListProperty<W> selectList(Function<V, ListProperty<W>> selector) {
            return selectList(selector, null);
        }

        public <W> ListProperty<W> selectList(Function<V, ListProperty<W>> selector, ObservableList<W> fallbackValue) {
            checkState("selectList");

            ListPropertyProxy<W> proxy = new ListPropertyProxy<>(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public <W> ReadOnlyListProperty<W> selectReadOnlyList(Function<V, ReadOnlyListProperty<W>> selector) {
            return selectReadOnlyList(selector, null);
        }

        public <W> ReadOnlyListProperty<W> selectReadOnlyList(
                Function<V, ReadOnlyListProperty<W>> selector, ObservableList<W> fallbackValue) {
            checkState("selectReadOnlyList");

            ReadOnlyListPropertyProxy<W> proxy = new ReadOnlyListPropertyProxy<>(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public <W> AsyncListProperty<W> selectAsyncList(Function<V, AsyncListProperty<W>> selector) {
            return selectAsyncList(selector, null);
        }

        public <W> AsyncListProperty<W> selectAsyncList(
                Function<V, AsyncListProperty<W>> selector, AsyncObservableList<W> fallbackValue) {
            checkState("selectAsyncList");

            AsyncListPropertyProxy<W> proxy = new AsyncListPropertyProxy<>(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public <W> ReadOnlyAsyncListProperty<W> selectReadOnlyAsyncList(
                Function<V, ReadOnlyAsyncListProperty<W>> selector) {
            return selectReadOnlyAsyncList(selector, null);
        }

        public <W> ReadOnlyAsyncListProperty<W> selectReadOnlyAsyncList(
                Function<V, ReadOnlyAsyncListProperty<W>> selector, AsyncObservableList<W> fallbackValue) {
            checkState("selectReadOnlyAsyncList");

            ReadOnlyAsyncListPropertyProxy<W> proxy = new ReadOnlyAsyncListPropertyProxy<>(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public <W> SetProperty<W> selectSet(Function<V, SetProperty<W>> selector) {
            return selectSet(selector, null);
        }

        public <W> SetProperty<W> selectSet(Function<V, SetProperty<W>> selector, ObservableSet<W> fallbackValue) {
            checkState("selectSet");

            SetPropertyProxy<W> proxy = new SetPropertyProxy<>(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public <W> ReadOnlySetProperty<W> selectReadOnlySet(Function<V, ReadOnlySetProperty<W>> selector) {
            return selectReadOnlySet(selector, null);
        }

        public <W> ReadOnlySetProperty<W> selectReadOnlySet(
                Function<V, ReadOnlySetProperty<W>> selector, ObservableSet<W> fallbackValue) {
            checkState("selectReadOnlySet");

            ReadOnlySetPropertyProxy<W> proxy = new ReadOnlySetPropertyProxy<>(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public <W> AsyncSetProperty<W> selectAsyncSet(Function<V, AsyncSetProperty<W>> selector) {
            return selectAsyncSet(selector, null);
        }

        public <W> AsyncSetProperty<W> selectAsyncSet(
                Function<V, AsyncSetProperty<W>> selector, AsyncObservableSet<W> fallbackValue) {
            checkState("selectAsyncSet");

            AsyncSetPropertyProxy<W> proxy = new AsyncSetPropertyProxy<>(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public <W> ReadOnlyAsyncSetProperty<W> selectReadOnlyAsyncSet(
                Function<V, ReadOnlyAsyncSetProperty<W>> selector) {
            return selectReadOnlyAsyncSet(selector, null);
        }

        public <W> ReadOnlyAsyncSetProperty<W> selectReadOnlyAsyncSet(
                Function<V, ReadOnlyAsyncSetProperty<W>> selector, AsyncObservableSet<W> fallbackValue) {
            checkState("selectReadOnlyAsyncSet");

            ReadOnlyAsyncSetPropertyProxy<W> proxy = new ReadOnlyAsyncSetPropertyProxy<>(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public <T> ObjectProperty<T> selectObject(Function<V, Property<T>> selector) {
            return selectObject(selector, null);
        }

        public <T> ObjectProperty<T> selectObject(Function<V, Property<T>> selector, T fallbackValue) {
            checkState("selectObject");

            ObjectPropertyProxy<T> proxy = new ObjectPropertyProxy<>(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public <T> ReadOnlyObjectProperty<T> selectReadOnlyObject(Function<V, ReadOnlyProperty<T>> selector) {
            return selectReadOnlyObject(selector, null);
        }

        public <T> ReadOnlyObjectProperty<T> selectReadOnlyObject(
                Function<V, ReadOnlyProperty<T>> selector, T fallbackValue) {
            checkState("selectReadOnlyObject");

            ReadOnlyObjectPropertyProxy<T> proxy = new ReadOnlyObjectPropertyProxy<>(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public <T> AsyncObjectProperty<T> selectAsyncObject(Function<V, AsyncObjectProperty<T>> selector) {
            return selectAsyncObject(selector, null);
        }

        public <T> AsyncObjectProperty<T> selectAsyncObject(
                Function<V, AsyncObjectProperty<T>> selector, T fallbackValue) {
            checkState("selectAsyncObject");

            AsyncObjectPropertyProxy<T> proxy = new AsyncObjectPropertyProxy<>(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public <T> ReadOnlyAsyncObjectProperty<T> selectReadOnlyAsyncObject(
                Function<V, ReadOnlyAsyncObjectProperty<T>> selector) {
            return selectReadOnlyAsyncObject(selector, null);
        }

        public <T> ReadOnlyAsyncObjectProperty<T> selectReadOnlyAsyncObject(
                Function<V, ReadOnlyAsyncObjectProperty<T>> selector, T fallbackValue) {
            checkState("selectReadOnlyAsyncObject");

            ReadOnlyAsyncObjectPropertyProxy<T> proxy = new ReadOnlyAsyncObjectPropertyProxy<>(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public StringProperty selectString(Function<V, Property<String>> selector) {
            return selectString(selector, null);
        }

        public StringProperty selectString(Function<V, Property<String>> selector, String fallbackValue) {
            checkState("selectString");

            StringPropertyProxy proxy = new StringPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyStringProperty selectReadOnlyString(Function<V, ReadOnlyProperty<String>> selector) {
            return selectReadOnlyString(selector, null);
        }

        public ReadOnlyStringProperty selectReadOnlyString(
                Function<V, ReadOnlyProperty<String>> selector, String fallbackValue) {
            checkState("selectReadOnlyString");

            ReadOnlyStringPropertyProxy proxy = new ReadOnlyStringPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public AsyncStringProperty selectAsyncString(Function<V, AsyncStringProperty> selector) {
            return selectAsyncString(selector, null);
        }

        public AsyncStringProperty selectAsyncString(Function<V, AsyncStringProperty> selector, String fallbackValue) {
            checkState("selectAsyncString");

            AsyncStringPropertyProxy proxy = new AsyncStringPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyAsyncStringProperty selectReadOnlyAsyncString(
                Function<V, ReadOnlyAsyncStringProperty> selector) {
            return selectReadOnlyAsyncString(selector, null);
        }

        public ReadOnlyAsyncStringProperty selectReadOnlyAsyncString(
                Function<V, ReadOnlyAsyncStringProperty> selector, String fallbackValue) {
            checkState("selectReadOnlyAsyncString");

            ReadOnlyAsyncStringPropertyProxy proxy = new ReadOnlyAsyncStringPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public IntegerProperty selectInteger(Function<V, Property<Number>> selector) {
            return selectInteger(selector, 0);
        }

        public IntegerProperty selectInteger(Function<V, Property<Number>> selector, int fallbackValue) {
            checkState("selectInteger");

            IntegerPropertyProxy proxy = new IntegerPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyIntegerProperty selectReadOnlyInteger(Function<V, ReadOnlyProperty<Number>> selector) {
            return selectReadOnlyInteger(selector, 0);
        }

        public ReadOnlyIntegerProperty selectReadOnlyInteger(
                Function<V, ReadOnlyProperty<Number>> selector, int fallbackValue) {
            checkState("selectReadOnlyInteger");

            ReadOnlyIntegerPropertyProxy proxy = new ReadOnlyIntegerPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public AsyncIntegerProperty selectAsyncInteger(Function<V, AsyncIntegerProperty> selector) {
            return selectAsyncInteger(selector, 0);
        }

        public AsyncIntegerProperty selectAsyncInteger(Function<V, AsyncIntegerProperty> selector, int fallbackValue) {
            checkState("selectAsyncInteger");

            AsyncIntegerPropertyProxy proxy = new AsyncIntegerPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyAsyncIntegerProperty selectReadOnlyAsyncInteger(
                Function<V, ReadOnlyAsyncIntegerProperty> selector) {
            return selectReadOnlyAsyncInteger(selector, 0);
        }

        public ReadOnlyAsyncIntegerProperty selectReadOnlyAsyncInteger(
                Function<V, ReadOnlyAsyncIntegerProperty> selector, int fallbackValue) {
            checkState("selectReadOnlyAsyncInteger");

            ReadOnlyAsyncIntegerPropertyProxy proxy = new ReadOnlyAsyncIntegerPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public LongProperty selectLong(Function<V, Property<Number>> selector) {
            return selectLong(selector, 0);
        }

        public LongProperty selectLong(Function<V, Property<Number>> selector, long fallbackValue) {
            checkState("selectLong");

            LongPropertyProxy proxy = new LongPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyLongProperty selectReadOnlyLong(Function<V, ReadOnlyProperty<Number>> selector) {
            return selectReadOnlyLong(selector, 0);
        }

        public ReadOnlyLongProperty selectReadOnlyLong(
                Function<V, ReadOnlyProperty<Number>> selector, long fallbackValue) {
            checkState("selectReadOnlyLong");

            ReadOnlyLongPropertyProxy proxy = new ReadOnlyLongPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public AsyncLongProperty selectAsyncLong(Function<V, AsyncLongProperty> selector) {
            return selectAsyncLong(selector, 0);
        }

        public AsyncLongProperty selectAsyncLong(Function<V, AsyncLongProperty> selector, long fallbackValue) {
            checkState("selectAsyncLong");

            AsyncLongPropertyProxy proxy = new AsyncLongPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyAsyncLongProperty selectReadOnlyAsyncLong(Function<V, ReadOnlyAsyncLongProperty> selector) {
            return selectReadOnlyAsyncLong(selector, 0);
        }

        public ReadOnlyAsyncLongProperty selectReadOnlyAsyncLong(
                Function<V, ReadOnlyAsyncLongProperty> selector, long fallbackValue) {
            checkState("selectReadOnlyAsyncLong");

            ReadOnlyAsyncLongPropertyProxy proxy = new ReadOnlyAsyncLongPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public DoubleProperty selectDouble(Function<V, Property<Number>> selector) {
            return selectDouble(selector, 0);
        }

        public DoubleProperty selectDouble(Function<V, Property<Number>> selector, double fallbackValue) {
            checkState("selectDouble");

            DoublePropertyProxy proxy = new DoublePropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyDoubleProperty selectReadOnlyDouble(Function<V, ReadOnlyProperty<Number>> selector) {
            return selectReadOnlyDouble(selector, 0);
        }

        public ReadOnlyDoubleProperty selectReadOnlyDouble(
                Function<V, ReadOnlyProperty<Number>> selector, double fallbackValue) {
            checkState("selectReadOnlyDouble");

            ReadOnlyDoublePropertyProxy proxy = new ReadOnlyDoublePropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public AsyncDoubleProperty selectAsyncDouble(Function<V, AsyncDoubleProperty> selector) {
            return selectAsyncDouble(selector, 0);
        }

        public AsyncDoubleProperty selectAsyncDouble(Function<V, AsyncDoubleProperty> selector, double fallbackValue) {
            checkState("selectAsyncDouble");

            AsyncDoublePropertyProxy proxy = new AsyncDoublePropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyAsyncDoubleProperty selectReadOnlyAsyncDouble(
                Function<V, ReadOnlyAsyncDoubleProperty> selector) {
            return selectReadOnlyAsyncDouble(selector, 0);
        }

        public ReadOnlyAsyncDoubleProperty selectReadOnlyAsyncDouble(
                Function<V, ReadOnlyAsyncDoubleProperty> selector, double fallbackValue) {
            checkState("selectReadOnlyAsyncDouble");

            ReadOnlyAsyncDoublePropertyProxy proxy = new ReadOnlyAsyncDoublePropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public FloatProperty selectFloat(Function<V, Property<Number>> selector) {
            return selectFloat(selector, 0);
        }

        public FloatProperty selectFloat(Function<V, Property<Number>> selector, float fallbackValue) {
            checkState("selectFloat");

            FloatPropertyProxy proxy = new FloatPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyFloatProperty selectReadOnlyFloat(Function<V, ReadOnlyProperty<Number>> selector) {
            return selectReadOnlyFloat(selector, 0);
        }

        public ReadOnlyFloatProperty selectReadOnlyFloat(
                Function<V, ReadOnlyProperty<Number>> selector, float fallbackValue) {
            checkState("selectReadOnlyFloat");

            ReadOnlyFloatPropertyProxy proxy = new ReadOnlyFloatPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public AsyncFloatProperty selectAsyncFloat(Function<V, AsyncFloatProperty> selector) {
            return selectAsyncFloat(selector, 0);
        }

        public AsyncFloatProperty selectAsyncFloat(Function<V, AsyncFloatProperty> selector, float fallbackValue) {
            checkState("selectAsyncFloat");

            AsyncFloatPropertyProxy proxy = new AsyncFloatPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyAsyncFloatProperty selectReadOnlyAsyncFloat(Function<V, ReadOnlyAsyncFloatProperty> selector) {
            return selectReadOnlyAsyncFloat(selector, 0);
        }

        public ReadOnlyAsyncFloatProperty selectReadOnlyAsyncFloat(
                Function<V, ReadOnlyAsyncFloatProperty> selector, float fallbackValue) {
            checkState("selectReadOnlyAsyncFloat");

            ReadOnlyAsyncFloatPropertyProxy proxy = new ReadOnlyAsyncFloatPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public BooleanProperty selectBoolean(Function<V, Property<Boolean>> selector) {
            return selectBoolean(selector, false);
        }

        public BooleanProperty selectBoolean(Function<V, Property<Boolean>> selector, boolean fallbackValue) {
            checkState("selectBoolean");

            BooleanPropertyProxy proxy = new BooleanPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyBooleanProperty selectReadOnlyBoolean(Function<V, ReadOnlyProperty<Boolean>> selector) {
            return selectReadOnlyBoolean(selector, false);
        }

        public ReadOnlyBooleanProperty selectReadOnlyBoolean(
                Function<V, ReadOnlyProperty<Boolean>> selector, boolean fallbackValue) {
            checkState("selectReadOnlyBoolean");

            ReadOnlyBooleanPropertyProxy proxy = new ReadOnlyBooleanPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public AsyncBooleanProperty selectAsyncBoolean(Function<V, AsyncBooleanProperty> selector) {
            return selectAsyncBoolean(selector, false);
        }

        public AsyncBooleanProperty selectAsyncBoolean(
                Function<V, AsyncBooleanProperty> selector, boolean fallbackValue) {
            checkState("selectAsyncBoolean");

            AsyncBooleanPropertyProxy proxy = new AsyncBooleanPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyAsyncBooleanProperty selectReadOnlyAsyncBoolean(
                Function<V, ReadOnlyAsyncBooleanProperty> selector) {
            return selectReadOnlyAsyncBoolean(selector, false);
        }

        public ReadOnlyAsyncBooleanProperty selectReadOnlyAsyncBoolean(
                Function<V, ReadOnlyAsyncBooleanProperty> selector, boolean fallbackValue) {
            checkState("selectReadOnlyAsyncBoolean");

            ReadOnlyAsyncBooleanPropertyProxy proxy = new ReadOnlyAsyncBooleanPropertyProxy(this, fallbackValue);
            updateProxy =
                newCurrentValue -> proxy.setPeer(newCurrentValue != null ? selector.apply(newCurrentValue) : null);
            updateProxy.accept(observable != null ? observable.getValue() : null);
            setProxy(proxy);
            return proxy;
        }

        private void onInvalidated() {
            if (previousSegment != null) {
                if (observable != null) {
                    observable.removeListener(invalidationListener);
                }

                observable = queryObservable();

                if (observable != null) {
                    observable.addListener(invalidationListener);
                }
            }

            if (nextSegment != null) {
                nextSegment.onInvalidated();
            } else if (updateProxy != null) {
                updateProxy.accept(observable != null ? observable.getValue() : null);
            }
        }

        private ObservableValue<V> queryObservable() {
            if (previousSegment.observable == null) {
                return null;
            }

            U previousValue = previousSegment.observable.getValue();
            if (previousValue != null) {
                return propertySelector.apply(previousValue);
            }

            return null;
        }

        private void setProxy(ReadOnlyProperty<?> proxy) {
            if (store != null) {
                store.add(proxy);
            }

            this.proxy = proxy;
        }

        private void setProxy(ReadOnlyAsyncProperty<?> proxy) {
            if (store != null) {
                store.add(proxy);
            }

            this.proxy = proxy;
        }

        private void checkState(String methodName) {
            final String className = PropertyPathSegment.class.getSimpleName();

            if (nextSegment != null) {
                throw new IllegalStateException(
                    String.format(
                        "%s::%s() can not be called after %s::select() has been called.",
                        className, methodName, className));
            }

            if (proxy != null) {
                throw new IllegalStateException(
                    String.format("%s::%s() can only be called once.", className, methodName));
            }
        }
    }

    public static <T> PropertyPathSegment<Void, T> from(ObservableValue<T> observable) {
        return new PropertyPathSegment<>(null, observable);
    }

}
