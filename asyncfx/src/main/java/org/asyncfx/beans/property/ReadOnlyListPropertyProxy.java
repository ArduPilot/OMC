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
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.asyncfx.beans.binding.ProxyListExpressionHelper;

class ReadOnlyListPropertyProxy<E> extends ReadOnlyListProperty<E> {

    private final PropertyPath.PropertyPathSegment endpoint;
    private final ObservableList<E> fallbackValue;
    private ProxyListExpressionHelper<E> helper;
    private ReadOnlyListProperty<E> peer;
    private ReadOnlyIntegerWrapper size;
    private ReadOnlyBooleanWrapper empty;

    ReadOnlyListPropertyProxy(PropertyPath.PropertyPathSegment endpoint, ObservableList<E> fallbackValue) {
        this.endpoint = endpoint;
        this.fallbackValue = fallbackValue;
    }

    void setPeer(ReadOnlyListProperty<E> peer) {
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

        ProxyListExpressionHelper.setPeer(helper, peer);
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
    public ObservableList<E> get() {
        if (peer == null) {
            return fallbackValue;
        }

        return peer.getValue();
    }

    @Override
    public void addListener(ListChangeListener<? super E> listChangeListener) {
        helper = ProxyListExpressionHelper.addListener(helper, this, peer, listChangeListener);
    }

    @Override
    public void removeListener(ListChangeListener<? super E> listChangeListener) {
        helper = ProxyListExpressionHelper.removeListener(helper, listChangeListener);
    }

    @Override
    public void addListener(ChangeListener<? super ObservableList<E>> changeListener) {
        helper = ProxyListExpressionHelper.addListener(helper, this, peer, changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super ObservableList<E>> changeListener) {
        helper = ProxyListExpressionHelper.removeListener(helper, changeListener);
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        helper = ProxyListExpressionHelper.addListener(helper, this, peer, invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        helper = ProxyListExpressionHelper.removeListener(helper, invalidationListener);
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
