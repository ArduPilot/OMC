/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.asyncfx.beans.AccessController;
import org.asyncfx.beans.value.ChangeListenerWrapper;
import org.asyncfx.beans.InvalidationListenerWrapper;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.binding.LifecycleValueConverter;
import org.asyncfx.beans.binding.ProxyAsyncListExpressionHelper;
import org.asyncfx.beans.binding.ValueConverter;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.ListChangeListenerWrapper;
import org.asyncfx.collections.LockedList;

class AsyncListPropertyProxy<E> extends AsyncListProperty<E> {

    enum BindingType {
        UNIDIRECTIONAL,
        CONTENT_UNIDIRECTIONAL
    }

    private final long uniqueId = PropertyHelper.getNextUniqueId();
    private final PropertyPath.PropertyPathSegment endpoint;
    private final AsyncObservableList<E> fallbackValue;
    private PropertyMetadata<AsyncObservableList<E>> metadata;
    private ProxyAsyncListExpressionHelper<E> helper;
    private Observable observable;
    private BindingType bindingType;
    private ValueConverter converter;
    private LifecycleValueConverter lifecycleConverter;
    private AsyncListProperty<E> peer;
    private ReadOnlyAsyncIntegerWrapper size;
    private ReadOnlyAsyncBooleanWrapper empty;

    AsyncListPropertyProxy(PropertyPath.PropertyPathSegment endpoint, AsyncObservableList<E> fallbackValue) {
        this.endpoint = endpoint;
        this.fallbackValue = fallbackValue;
    }

    @Override
    public long getUniqueId() {
        return uniqueId;
    }

    @SuppressWarnings("unchecked")
    synchronized void setPeer(AsyncListProperty<E> peer) {
        if (observable != null) {
            switch (bindingType) {
            case UNIDIRECTIONAL:
                if (this.peer != null) {
                    this.peer.unbind();
                }

                if (converter != null) {
                    peer.bind((ObservableValue)observable, converter);
                } else {
                    peer.bind((ObservableValue)observable);
                }

                break;

            case CONTENT_UNIDIRECTIONAL:
                if (this.peer != null) {
                    this.peer.unbindContent((ObservableList<E>)observable);
                }

                if (lifecycleConverter != null) {
                    peer.bindContent((ObservableList)observable, lifecycleConverter);
                } else if (converter != null) {
                    peer.bindContent((ObservableList)observable, converter);
                } else {
                    peer.bindContent((ObservableList)observable);
                }

                break;
            }
        }

        if (size != null) {
            size.unbind();
            size.set(0);
        }

        if (empty != null) {
            empty.unbind();
            empty.set(true);
        }

        this.peer = peer;

        if (peer != null) {
            if (size != null) {
                size.bind(peer.sizeProperty());
            }

            if (empty != null) {
                empty.bind(peer.emptyProperty());
            }
        }

        ProxyAsyncListExpressionHelper.setPeer(helper, peer);
    }

    @Override
    public synchronized ReadOnlyAsyncIntegerProperty sizeProperty() {
        if (size == null) {
            size = new ReadOnlyAsyncIntegerWrapper(this, new PropertyMetadata.Builder<Number>().name("size").create());

            if (peer != null) {
                size.bind(peer.sizeProperty());
            }
        }

        return size.getReadOnlyProperty();
    }

    @Override
    public synchronized ReadOnlyAsyncBooleanProperty emptyProperty() {
        if (empty == null) {
            empty =
                new ReadOnlyAsyncBooleanWrapper(
                    this, new PropertyMetadata.Builder<Boolean>().name("empty").initialValue(true).create());

            if (peer != null) {
                empty.bind(peer.emptyProperty());
            }
        }

        return empty;
    }

    @Override
    public synchronized void reset() {
        if (peer != null) {
            peer.reset();
        }
    }

    @Override
    public synchronized void bind(ObservableValue<? extends AsyncObservableList<E>> observable) {
        if (peer != null) {
            peer.bind(observable);
        }

        this.bindingType = BindingType.UNIDIRECTIONAL;
        this.observable = observable;
        this.converter = null;
        this.lifecycleConverter = null;
    }

    @Override
    public <U> void bind(
            ObservableValue<? extends U> observable, ValueConverter<U, ? extends AsyncObservableList<E>> converter) {
        if (peer != null) {
            peer.bind(observable, converter);
        }

        this.bindingType = BindingType.UNIDIRECTIONAL;
        this.observable = observable;
        this.converter = converter;
        this.lifecycleConverter = null;
    }

    @Override
    public synchronized void unbind() {
        if (peer != null) {
            peer.unbind();
        }

        bindingType = null;
        observable = null;
        converter = null;
    }

    @Override
    public void bindContent(ObservableList<? extends E> observable) {
        if (peer != null) {
            peer.bindContent(observable);
        }

        this.bindingType = BindingType.CONTENT_UNIDIRECTIONAL;
        this.observable = observable;
        this.converter = null;
        this.lifecycleConverter = null;
    }

    @Override
    public <T> void bindContent(ObservableList<T> observable, ValueConverter<T, E> converter) {
        if (peer != null) {
            peer.bindContent(observable, converter);
        }

        this.bindingType = BindingType.CONTENT_UNIDIRECTIONAL;
        this.observable = observable;
        this.converter = converter;
        this.lifecycleConverter = null;
    }

    @Override
    public <T> void bindContent(ObservableList<T> observable, LifecycleValueConverter<T, E> converter) {
        if (peer != null) {
            peer.bindContent(observable, converter);
        }

        this.bindingType = BindingType.CONTENT_UNIDIRECTIONAL;
        this.observable = observable;
        this.converter = null;
        this.lifecycleConverter = converter;
    }

    @Override
    public void unbindContent(ObservableList<E> content) {
        if (peer != null) {
            peer.unbindContent(content);
        }

        bindingType = null;
        observable = null;
        converter = null;
    }

    @Override
    public synchronized boolean isBound() {
        return observable != null;
    }

    @Override
    public boolean isBoundBidirectionally() {
        return ProxyAsyncListExpressionHelper.containsBidirectionalBindingEndpoints(helper);
    }

    @Override
    public synchronized Object getBean() {
        return peer != null ? peer.getBean() : null;
    }

    @Override
    public synchronized String getName() {
        return peer != null ? peer.getName() : null;
    }

    @Override
    public synchronized AccessController getAccessController() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized PropertyMetadata<AsyncObservableList<E>> getMetadata() {
        if (peer != null) {
            return peer.getMetadata();
        }

        if (metadata == null) {
            metadata = new PropertyMetadata.Builder<AsyncObservableList<E>>().create();
        }

        return metadata;
    }

    @Override
    public synchronized void overrideMetadata(PropertyMetadata<AsyncObservableList<E>> metadata) {
        if (peer != null) {
            peer.overrideMetadata(metadata);
        }
    }

    @Override
    public synchronized AsyncObservableList<E> get() {
        if (peer == null) {
            return fallbackValue;
        }

        return peer.getValue();
    }

    @Override
    public synchronized AsyncObservableList<E> getUncritical() {
        if (peer == null) {
            return fallbackValue;
        }

        return peer.getValueUncritical();
    }

    @Override
    public synchronized LockedList<E> lock() {
        if (peer == null) {
            return LockedList.empty();
        }

        return peer.lock();
    }

    @Override
    public synchronized void addListener(ListChangeListener<? super E> listener) {
        helper = ProxyAsyncListExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public void addListener(ListChangeListener<? super E> listener, Executor executor) {
        helper =
            ProxyAsyncListExpressionHelper.addListener(
                helper, this, peer, get(), ListChangeListenerWrapper.wrap(listener, executor));
    }

    @Override
    public synchronized void removeListener(ListChangeListener<? super E> listener) {
        helper = ProxyAsyncListExpressionHelper.removeListener(helper, get(), listener);
    }

    @Override
    public synchronized void addListener(ChangeListener<? super AsyncObservableList<E>> listener) {
        helper = ProxyAsyncListExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public void addListener(ChangeListener<? super AsyncObservableList<E>> listener, Executor executor) {
        helper =
            ProxyAsyncListExpressionHelper.addListener(
                helper, this, peer, get(), ChangeListenerWrapper.wrap(listener, executor));
    }

    @Override
    public synchronized void removeListener(ChangeListener<? super AsyncObservableList<E>> listener) {
        helper = ProxyAsyncListExpressionHelper.removeListener(helper, get(), listener);
    }

    @Override
    public synchronized void addListener(InvalidationListener listener) {
        helper = ProxyAsyncListExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public void addListener(InvalidationListener listener, Executor executor) {
        helper =
            ProxyAsyncListExpressionHelper.addListener(
                helper, this, peer, get(), InvalidationListenerWrapper.wrap(listener, executor));
    }

    @Override
    public synchronized void removeListener(InvalidationListener listener) {
        helper = ProxyAsyncListExpressionHelper.removeListener(helper, get(), listener);
    }

    @Override
    public synchronized void addListener(SubInvalidationListener listener) {
        helper = ProxyAsyncListExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public synchronized void addListener(SubInvalidationListener listener, Executor executor) {
        helper =
            ProxyAsyncListExpressionHelper.addListener(
                helper, this, peer, get(), SubInvalidationListenerWrapper.wrap(listener, executor));
    }

    @Override
    public synchronized void removeListener(SubInvalidationListener listener) {
        helper = ProxyAsyncListExpressionHelper.removeListener(helper, get(), listener);
    }

    @Override
    public synchronized void addListener(SubChangeListener listener) {
        helper = ProxyAsyncListExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public synchronized void addListener(SubChangeListener listener, Executor executor) {
        helper =
            ProxyAsyncListExpressionHelper.addListener(
                helper, this, peer, get(), SubChangeListenerWrapper.wrap(listener, executor));
    }

    @Override
    public synchronized void removeListener(SubChangeListener listener) {
        helper = ProxyAsyncListExpressionHelper.removeListener(helper, get(), listener);
    }

    @Override
    public synchronized void set(AsyncObservableList<E> value) {
        if (peer != null) {
            peer.setValue(value);
        }
    }

    @Override
    public synchronized String toString() {
        Object bean = this.getBean();
        String name = this.getName();
        StringBuilder stringBuilder = new StringBuilder(getClass().getSimpleName());
        stringBuilder.append(" [");
        if (bean != null) {
            stringBuilder.append("bean: ").append(bean).append(", ");
        }

        if (name != null && !name.equals("")) {
            stringBuilder.append("name: ").append(name).append(", ");
        }

        if (peer == null) {
            stringBuilder.append("unresolved, ");
        }

        stringBuilder.append("value: ").append(this.get()).append("]");
        return stringBuilder.toString();
    }

}
