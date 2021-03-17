/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import org.asyncfx.beans.AccessController;
import org.asyncfx.beans.value.ChangeListenerWrapper;
import org.asyncfx.beans.InvalidationListenerWrapper;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.binding.ProxyAsyncListExpressionHelper;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.ListChangeListenerWrapper;
import org.asyncfx.collections.LockedList;

class ReadOnlyAsyncListPropertyProxy<E> extends ReadOnlyAsyncListProperty<E> {

    private final long uniqueId = PropertyHelper.getNextUniqueId();
    private final PropertyPath.PropertyPathSegment endpoint;
    private final AsyncObservableList<E> fallbackValue;
    private PropertyMetadata<AsyncObservableList<E>> metadata;
    private ProxyAsyncListExpressionHelper<E> helper;
    private ReadOnlyAsyncListProperty<E> peer;
    private ReadOnlyAsyncIntegerWrapper size;
    private ReadOnlyAsyncBooleanWrapper empty;

    ReadOnlyAsyncListPropertyProxy(PropertyPath.PropertyPathSegment endpoint, AsyncObservableList<E> fallbackValue) {
        this.endpoint = endpoint;
        this.fallbackValue = fallbackValue;
    }

    @Override
    public long getUniqueId() {
        return uniqueId;
    }

    synchronized void setPeer(ReadOnlyAsyncListProperty<E> peer) {
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
