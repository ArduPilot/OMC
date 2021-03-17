/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.helper.Expect;
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
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.ReadOnlyFloatWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import org.checkerframework.checker.nullness.qual.Nullable;

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
        private Object peer;

        PropertyPathSegment(@Nullable PropertyPathStore store, ObservableValue<V> observable) {
            Expect.notNull(observable, "observable");
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
            Expect.notNull(
                previousSegment, "previousSegment",
                propertySelector, "propertySelector");
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
            Expect.notNull(selector, "selector");
            PropertyPathSegment<V, W> nextSegment = new PropertyPathSegment<>(store, this, selector);
            this.nextSegment = nextSegment;
            return nextSegment;
        }

        @SuppressWarnings("unchecked")
        public <W, X> MapProperty<W, X> selectMap(Function<V, MapProperty<W, X>> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectMap");

            MapProperty<W, X> proxy = new SimpleMapProperty<>(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((MapProperty<W, X>)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        MapProperty<W, X> nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(FXCollections.emptyObservableMap());
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public <W, X> ReadOnlyMapProperty<W, X> selectReadOnlyMap(Function<V, ReadOnlyMapProperty<W, X>> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyMap");

            ReadOnlyMapWrapper<W, X> proxy = new ReadOnlyMapWrapper<>(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyMapProperty<W, X> nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.setValue(FXCollections.emptyObservableMap());
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        @SuppressWarnings("unchecked")
        public <W> ListProperty<W> selectList(Function<V, ListProperty<W>> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectList");

            ListProperty<W> proxy = new SimpleListProperty<>(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((ListProperty<W>)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ListProperty<W> nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(FXCollections.emptyObservableList());
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public <W> ReadOnlyListProperty<W> selectReadOnlyList(Function<V, ReadOnlyListProperty<W>> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyList");

            ReadOnlyListWrapper<W> proxy = new ReadOnlyListWrapper<>(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyListProperty<W> nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.setValue(FXCollections.emptyObservableList());
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        @SuppressWarnings("unchecked")
        public <W> AsyncListProperty<W> selectAsyncList(Function<V, AsyncListProperty<W>> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectAsyncList");

            AsyncListProperty<W> proxy =
                new SimpleAsyncListProperty<>(
                    this,
                    new PropertyMetadata.Builder<AsyncObservableList<W>>()
                        .customBean(true)
                        .name(generatePropertyName())
                        .create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((AsyncListProperty<W>)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        AsyncListProperty<W> nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(FXAsyncCollections.emptyObservableList());
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public <W> ReadOnlyAsyncListProperty<W> selectReadOnlyAsyncList(
                Function<V, ReadOnlyAsyncListProperty<W>> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyAsyncList");

            ReadOnlyAsyncListWrapper<W> proxy =
                new ReadOnlyAsyncListWrapper<>(
                    this,
                    new PropertyMetadata.Builder<AsyncObservableList<W>>()
                        .customBean(true)
                        .name(generatePropertyName())
                        .create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyAsyncListProperty<W> nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.setValue(FXAsyncCollections.emptyObservableList());
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        @SuppressWarnings("unchecked")
        public <W> ObjectProperty<W> selectObject(Function<V, ObjectProperty<W>> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectObject");

            ObjectProperty<W> proxy = new SimpleObjectProperty<>(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((ObjectProperty<W>)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ObjectProperty<W> nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(null);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public <W> ReadOnlyObjectProperty<W> selectReadOnlyObject(Function<V, ReadOnlyObjectProperty<W>> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyObject");

            ReadOnlyObjectWrapper<W> proxy = new ReadOnlyObjectWrapper<>(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyObjectProperty<W> nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.set(null);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        @SuppressWarnings("unchecked")
        public <W> AsyncObjectProperty<W> selectAsyncObject(Function<V, AsyncObjectProperty<W>> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectAsyncObject");

            AsyncObjectProperty<W> proxy =
                new SimpleAsyncObjectProperty<>(
                    this, new PropertyMetadata.Builder<W>().customBean(true).name(generatePropertyName()).create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((AsyncObjectProperty<W>)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        AsyncObjectProperty<W> nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(null);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public <W> ReadOnlyAsyncObjectProperty<W> selectReadOnlyAsyncObject(
                Function<V, ReadOnlyAsyncObjectProperty<W>> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyAsyncObject");

            ReadOnlyAsyncObjectWrapper<W> proxy =
                new ReadOnlyAsyncObjectWrapper<>(
                    this, new PropertyMetadata.Builder<W>().customBean(true).name(generatePropertyName()).create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyAsyncObjectProperty<W> nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.set(null);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        public StringProperty selectString(Function<V, StringProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectString");

            StringProperty proxy = new SimpleStringProperty(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((StringProperty)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        StringProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(null);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyStringProperty selectReadOnlyString(Function<V, ReadOnlyStringProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyString");

            ReadOnlyStringWrapper proxy = new ReadOnlyStringWrapper(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyStringProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.set(null);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        public AsyncStringProperty selectAsyncString(Function<V, AsyncStringProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectAsyncString");

            AsyncStringProperty proxy =
                new SimpleAsyncStringProperty(
                    this,
                    new PropertyMetadata.Builder<String>().customBean(true).name(generatePropertyName()).create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((AsyncStringProperty)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        AsyncStringProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(null);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyAsyncStringProperty selectReadOnlyAsyncString(
                Function<V, ReadOnlyAsyncStringProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyAsyncString");

            ReadOnlyAsyncStringWrapper proxy =
                new ReadOnlyAsyncStringWrapper(
                    this,
                    new PropertyMetadata.Builder<String>().customBean(true).name(generatePropertyName()).create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyAsyncStringProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.set(null);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        public IntegerProperty selectInteger(Function<V, IntegerProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectInteger");

            IntegerProperty proxy = new SimpleIntegerProperty(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((IntegerProperty)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        IntegerProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(0);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyIntegerProperty selectReadOnlyInteger(Function<V, ReadOnlyIntegerProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyInteger");

            ReadOnlyIntegerWrapper proxy = new ReadOnlyIntegerWrapper(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyIntegerProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.set(0);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        public AsyncIntegerProperty selectAsyncInteger(Function<V, AsyncIntegerProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectAsyncInteger");

            AsyncIntegerProperty proxy =
                new SimpleAsyncIntegerProperty(
                    this,
                    new PropertyMetadata.Builder<Number>().customBean(true).name(generatePropertyName()).create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((AsyncIntegerProperty)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        AsyncIntegerProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(0);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyAsyncIntegerProperty selectReadOnlyAsyncInteger(
                Function<V, ReadOnlyAsyncIntegerProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyAsyncInteger");

            ReadOnlyAsyncIntegerWrapper proxy =
                new ReadOnlyAsyncIntegerWrapper(
                    this,
                    new PropertyMetadata.Builder<Number>().customBean(true).name(generatePropertyName()).create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyAsyncIntegerProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.set(0);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        public DoubleProperty selectDouble(Function<V, DoubleProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectDouble");

            DoubleProperty proxy = new SimpleDoubleProperty(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((DoubleProperty)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        DoubleProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(0);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyDoubleProperty selectReadOnlyDouble(Function<V, ReadOnlyDoubleProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyDouble");

            ReadOnlyDoubleWrapper proxy = new ReadOnlyDoubleWrapper(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyDoubleProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.set(0);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        public AsyncDoubleProperty selectAsyncDouble(Function<V, AsyncDoubleProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectAsyncDouble");

            AsyncDoubleProperty proxy =
                new SimpleAsyncDoubleProperty(
                    this,
                    new PropertyMetadata.Builder<Number>().customBean(true).name(generatePropertyName()).create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((AsyncDoubleProperty)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        AsyncDoubleProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(0);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyAsyncDoubleProperty selectReadOnlyAsyncDouble(
                Function<V, ReadOnlyAsyncDoubleProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyAsyncDouble");

            ReadOnlyAsyncDoubleWrapper proxy =
                new ReadOnlyAsyncDoubleWrapper(
                    this,
                    new PropertyMetadata.Builder<Number>().customBean(true).name(generatePropertyName()).create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyAsyncDoubleProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.set(0);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        public FloatProperty selectFloat(Function<V, FloatProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectFloat");

            FloatProperty proxy = new SimpleFloatProperty(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((FloatProperty)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        FloatProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(0);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyFloatProperty selectReadOnlyFloat(Function<V, ReadOnlyFloatProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyFloat");

            ReadOnlyFloatWrapper proxy = new ReadOnlyFloatWrapper(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyFloatProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.set(0);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        public AsyncFloatProperty selectAsyncFloat(Function<V, AsyncFloatProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectAsyncFloat");

            AsyncFloatProperty proxy =
                new SimpleAsyncFloatProperty(
                    this,
                    new PropertyMetadata.Builder<Number>().customBean(true).name(generatePropertyName()).create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((AsyncFloatProperty)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        AsyncFloatProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(0);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyAsyncFloatProperty selectReadOnlyAsyncFloat(Function<V, ReadOnlyAsyncFloatProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyAsyncFloat");

            ReadOnlyAsyncFloatWrapper proxy =
                new ReadOnlyAsyncFloatWrapper(
                    this,
                    new PropertyMetadata.Builder<Number>().customBean(true).name(generatePropertyName()).create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyAsyncFloatProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.set(0);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        public BooleanProperty selectBoolean(Function<V, BooleanProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectBoolean");

            BooleanProperty proxy = new SimpleBooleanProperty(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((BooleanProperty)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        BooleanProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(false);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyBooleanProperty selectReadOnlyBoolean(Function<V, ReadOnlyBooleanProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyBoolean");

            ReadOnlyBooleanWrapper proxy = new ReadOnlyBooleanWrapper(this, generatePropertyName());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyBooleanProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.set(false);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
        }

        public AsyncBooleanProperty selectAsyncBoolean(Function<V, AsyncBooleanProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectAsyncBoolean");

            AsyncBooleanProperty proxy =
                new SimpleAsyncBooleanProperty(
                    this,
                    new PropertyMetadata.Builder<Boolean>().customBean(true).name(generatePropertyName()).create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbindBidirectional((AsyncBooleanProperty)this.peer);
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        AsyncBooleanProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bindBidirectional(nextPeer);
                    } else {
                        proxy.set(false);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy;
        }

        public ReadOnlyAsyncBooleanProperty selectReadOnlyAsyncBoolean(
                Function<V, ReadOnlyAsyncBooleanProperty> selector) {
            Expect.notNull(selector, "selector");
            checkState("selectReadOnlyAsyncBoolean");

            ReadOnlyAsyncBooleanWrapper proxy =
                new ReadOnlyAsyncBooleanWrapper(
                    this,
                    new PropertyMetadata.Builder<Boolean>().customBean(true).name(generatePropertyName()).create());
            V currentValue = null;
            if (observable != null) {
                currentValue = observable.getValue();
            }

            updateProxy =
                newCurrentValue -> {
                    if (this.peer != null) {
                        proxy.unbind();
                    }

                    this.peer = null;
                    if (newCurrentValue != null) {
                        ReadOnlyAsyncBooleanProperty nextPeer = selector.apply(newCurrentValue);
                        this.peer = nextPeer;
                        proxy.bind(nextPeer);
                    } else {
                        proxy.set(false);
                    }
                };

            updateProxy.accept(currentValue);
            setProxy(proxy);
            return proxy.getReadOnlyProperty();
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
