/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import org.asyncfx.beans.AccessController;
import org.asyncfx.beans.AsyncInvalidationListenerWrapper;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.AsyncSubInvalidationListenerWrapper;
import org.asyncfx.beans.binding.ProxyAsyncSetExpressionHelper;
import org.asyncfx.beans.binding.ValueConverter;
import org.asyncfx.beans.value.AsyncChangeListenerWrapper;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.beans.value.AsyncSubChangeListenerWrapper;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.LockedSet;
import org.asyncfx.collections.SetChangeListenerWrapper;

class AsyncSetPropertyProxy<E> extends AsyncSetProperty<E> {

    private final long uniqueId = PropertyHelper.getNextUniqueId();
    private final PropertyPath.PropertyPathSegment endpoint;
    private final AsyncObservableSet<E> fallbackValue;
    private PropertyMetadata<AsyncObservableSet<E>> metadata;
    private ProxyAsyncSetExpressionHelper<E> helper;
    private ObservableValue observable;
    private ValueConverter converter;
    private AsyncSetProperty<E> peer;
    private ReadOnlyAsyncIntegerWrapper size;
    private ReadOnlyAsyncBooleanWrapper empty;

    AsyncSetPropertyProxy(PropertyPath.PropertyPathSegment endpoint, AsyncObservableSet<E> fallbackValue) {
        this.endpoint = endpoint;
        this.fallbackValue = fallbackValue;
    }

    @Override
    public long getUniqueId() {
        return uniqueId;
    }

    @SuppressWarnings("unchecked")
    synchronized void setPeer(AsyncSetProperty<E> peer) {
        if (observable != null) {
            if (this.peer != null) {
                this.peer.unbind();
            }

            if (converter != null) {
                peer.bind(observable, converter);
            } else {
                peer.bind(observable);
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

        ProxyAsyncSetExpressionHelper.setPeer(helper, peer);
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
    public synchronized void bind(ObservableValue<? extends AsyncObservableSet<E>> observable) {
        this.observable = observable;
        this.converter = null;

        if (peer != null) {
            peer.bind(observable);
        }
    }

    @Override
    public <U> void bind(
            ObservableValue<? extends U> observable, ValueConverter<U, ? extends AsyncObservableSet<E>> converter) {
        this.observable = observable;
        this.converter = converter;

        if (peer != null) {
            peer.bind(observable, converter);
        }
    }

    @Override
    public synchronized void unbind() {
        observable = null;
        converter = null;

        if (peer != null) {
            peer.unbind();
        }
    }

    @Override
    public synchronized boolean isBound() {
        return observable != null;
    }

    @Override
    public boolean isBoundBidirectionally() {
        return ProxyAsyncSetExpressionHelper.containsBidirectionalBindingEndpoints(helper);
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
    public synchronized PropertyMetadata<AsyncObservableSet<E>> getMetadata() {
        if (peer != null) {
            return peer.getMetadata();
        }

        if (metadata == null) {
            metadata = new PropertyMetadata.Builder<AsyncObservableSet<E>>().create();
        }

        return metadata;
    }

    @Override
    public synchronized void overrideMetadata(PropertyMetadata<AsyncObservableSet<E>> metadata) {
        if (peer != null) {
            peer.overrideMetadata(metadata);
        }
    }

    @Override
    public synchronized AsyncObservableSet<E> get() {
        if (peer == null) {
            return fallbackValue;
        }

        return peer.getValue();
    }

    @Override
    public synchronized AsyncObservableSet<E> getUncritical() {
        if (peer == null) {
            return fallbackValue;
        }

        return peer.getValueUncritical();
    }

    @Override
    public synchronized LockedSet<E> lock() {
        if (peer == null) {
            return LockedSet.empty();
        }

        return peer.lock();
    }

    @Override
    public synchronized void addListener(SetChangeListener<? super E> listener) {
        helper = ProxyAsyncSetExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public void addListener(SetChangeListener<? super E> listener, Executor executor) {
        helper =
            ProxyAsyncSetExpressionHelper.addListener(
                helper, this, peer, get(), SetChangeListenerWrapper.wrap(listener, executor));
    }

    @Override
    public synchronized void removeListener(SetChangeListener<? super E> listener) {
        helper = ProxyAsyncSetExpressionHelper.removeListener(helper, get(), listener);
    }

    @Override
    public synchronized void addListener(ChangeListener<? super AsyncObservableSet<E>> listener) {
        helper = ProxyAsyncSetExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public void addListener(ChangeListener<? super AsyncObservableSet<E>> listener, Executor executor) {
        helper =
            ProxyAsyncSetExpressionHelper.addListener(
                helper, this, peer, get(), AsyncChangeListenerWrapper.wrap(listener, executor));
    }

    @Override
    public synchronized void removeListener(ChangeListener<? super AsyncObservableSet<E>> listener) {
        helper = ProxyAsyncSetExpressionHelper.removeListener(helper, get(), listener);
    }

    @Override
    public synchronized void addListener(InvalidationListener listener) {
        helper = ProxyAsyncSetExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public void addListener(InvalidationListener listener, Executor executor) {
        helper =
            ProxyAsyncSetExpressionHelper.addListener(
                helper, this, peer, get(), AsyncInvalidationListenerWrapper.wrap(listener, executor));
    }

    @Override
    public synchronized void removeListener(InvalidationListener listener) {
        helper = ProxyAsyncSetExpressionHelper.removeListener(helper, get(), listener);
    }

    @Override
    public synchronized void addListener(SubInvalidationListener listener) {
        helper = ProxyAsyncSetExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public synchronized void addListener(SubInvalidationListener listener, Executor executor) {
        helper =
            ProxyAsyncSetExpressionHelper.addListener(
                helper, this, peer, get(), AsyncSubInvalidationListenerWrapper.wrap(listener, executor));
    }

    @Override
    public synchronized void removeListener(SubInvalidationListener listener) {
        helper = ProxyAsyncSetExpressionHelper.removeListener(helper, get(), listener);
    }

    @Override
    public synchronized void addListener(SubChangeListener listener) {
        helper = ProxyAsyncSetExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public synchronized void addListener(SubChangeListener listener, Executor executor) {
        helper =
            ProxyAsyncSetExpressionHelper.addListener(
                helper, this, peer, get(), AsyncSubChangeListenerWrapper.wrap(listener, executor));
    }

    @Override
    public synchronized void removeListener(SubChangeListener listener) {
        helper = ProxyAsyncSetExpressionHelper.removeListener(helper, get(), listener);
    }

    @Override
    public synchronized void set(AsyncObservableSet<E> value) {
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
