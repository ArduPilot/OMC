/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.asyncfx.beans.binding.ProxySetExpressionHelper;

class ReadOnlySetPropertyProxy<E> extends ReadOnlySetProperty<E> {

    private final PropertyPath.PropertyPathSegment endpoint;
    private final ObservableSet<E> fallbackValue;
    private ObservableValue<? extends ObservableSet<E>> observable;
    private ProxySetExpressionHelper<E> helper;
    private ReadOnlySetProperty<E> peer;
    private ReadOnlyIntegerWrapper size;
    private ReadOnlyBooleanWrapper empty;

    ReadOnlySetPropertyProxy(PropertyPath.PropertyPathSegment endpoint, ObservableSet<E> fallbackValue) {
        this.endpoint = endpoint;
        this.fallbackValue = fallbackValue;
    }

    void setPeer(ReadOnlySetProperty<E> peer) {
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

        ProxySetExpressionHelper.setPeer(helper, peer);
    }

    @Override
    public ReadOnlyIntegerProperty sizeProperty() {
        if (size == null) {
            size = new ReadOnlyIntegerWrapper(this, "size");

            if (peer != null) {
                size.bind(peer.sizeProperty());
            }
        }

        return size.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyBooleanProperty emptyProperty() {
        if (empty == null) {
            empty = new ReadOnlyBooleanWrapper(this, "empty", true);

            if (peer != null) {
                empty.bind(peer.emptyProperty());
            }
        }

        return empty;
    }

    @Override
    public Object getBean() {
        return peer != null ? peer.getBean() : null;
    }

    @Override
    public String getName() {
        return peer != null ? peer.getName() : null;
    }

    @Override
    public ObservableSet<E> get() {
        if (peer == null) {
            return fallbackValue;
        }

        return peer.getValue();
    }

    @Override
    public void addListener(SetChangeListener<? super E> listChangeListener) {
        helper = ProxySetExpressionHelper.addListener(helper, this, peer, listChangeListener);
    }

    @Override
    public void removeListener(SetChangeListener<? super E> listChangeListener) {
        helper = ProxySetExpressionHelper.removeListener(helper, listChangeListener);
    }

    @Override
    public void addListener(ChangeListener<? super ObservableSet<E>> changeListener) {
        helper = ProxySetExpressionHelper.addListener(helper, this, peer, changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super ObservableSet<E>> changeListener) {
        helper = ProxySetExpressionHelper.removeListener(helper, changeListener);
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        helper = ProxySetExpressionHelper.addListener(helper, this, peer, invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        helper = ProxySetExpressionHelper.removeListener(helper, invalidationListener);
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
