/**
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
// This file was generated by a tool. Do not edit.
//
package org.asyncfx.beans.property;

import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.asyncfx.beans.AccessController;
import org.asyncfx.beans.AsyncInvalidationListenerWrapper;
import org.asyncfx.beans.AsyncSubInvalidationListenerWrapper;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.binding.LifecycleValueConverter;
import org.asyncfx.beans.binding.ProxyAsyncExpressionHelper;
import org.asyncfx.beans.binding.ValueConverter;
import org.asyncfx.beans.value.AsyncChangeListenerWrapper;
import org.asyncfx.beans.value.AsyncSubChangeListenerWrapper;
import org.asyncfx.beans.value.SubChangeListener;

#set($isNumberType = $boxedType == "Integer" || $boxedType == "Long" || $boxedType == "Float" || $boxedType == "Double")

class Async${boxedType}PropertyProxy$!{genericType} extends Async${boxedType}Property$!{genericType} {

#if($boxedType != "Object")
    private static final PropertyMetadata<$numberType> DEFAULT_METADATA =
        new PropertyMetadata.Builder<$numberType>().customBean(true).create();
#else
    private PropertyMetadata<$numberType> metadata;
#end

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final PropertyPath.PropertyPathSegment endpoint;
    private final long uniqueId = PropertyHelper.getNextUniqueId();
    private final $primType fallbackValue;
    private ProxyAsyncExpressionHelper<$numberType> helper;
    private ObservableValue observable;
    private ValueConverter converter;
    private AsyncProperty<$numberType> peer;

    Async${boxedType}PropertyProxy(PropertyPath.PropertyPathSegment endpoint, $primType fallbackValue) {
        this.endpoint = endpoint;
        this.fallbackValue = fallbackValue;
    }

    @Override
    public long getUniqueId() {
        return uniqueId;
    }

    @SuppressWarnings("unchecked")
    synchronized void setPeer(AsyncProperty<$numberType> peer) {
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

        this.peer = peer;

        ProxyAsyncExpressionHelper.setPeer(helper, peer);
    }

    @Override
    public synchronized void reset() {
        if (peer != null) {
            peer.reset();
        }
    }

    @Override
    public synchronized void bind(ObservableValue<? extends $numberType> observable) {
        this.observable = observable;
        this.converter = null;

        if (peer != null) {
            peer.bind(observable);
        }
    }

    @Override
    public synchronized <U> void bind(
            ObservableValue<? extends U> observable, ValueConverter<U, $numberType> converter) {
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
    public synchronized boolean isBoundBidirectionally() {
        return ProxyAsyncExpressionHelper.containsBidirectionalBindingEndpoints(helper);
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
        return null;
    }

    @Override
    public synchronized PropertyMetadata<$numberType> getMetadata() {
#if($boxedType == "Object")
        if (peer != null) {
            return peer.getMetadata();
        }

        if (metadata == null) {
            metadata = new PropertyMetadata.Builder<$numberType>().create();
        }

        return metadata;
#else
        return peer != null ? peer.getMetadata() : DEFAULT_METADATA;
#end
    }

    @Override
    public synchronized void overrideMetadata(PropertyMetadata<$numberType> metadata) {
        if (peer != null) {
            peer.overrideMetadata(metadata);
        }
    }

    @Override
    public synchronized $primType get() {
        if (peer == null) {
            return fallbackValue;
        }

#if($boxedType == "Boolean")
        Boolean value = peer.getValue();
        return value != null ? value : false;
#elseif($isNumberType)
        Number value = peer.getValue();
        return value != null ? value.${primType}Value() : 0;
#else
        return peer.getValue();
#end
    }

    @Override
    public synchronized $primType getUncritical() {
        if (peer == null) {
            return fallbackValue;
        }

#if($boxedType == "Boolean")
        Boolean value = peer.getValueUncritical();
        return value != null ? value : false;
#elseif($isNumberType)
        Number value = peer.getValueUncritical();
        return value != null ? value.${primType}Value() : 0;
#else
        return peer.getValueUncritical();
#end
    }

    @Override
    public synchronized void addListener(ChangeListener<? super $numberType> listener) {
        helper = ProxyAsyncExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public synchronized void addListener(ChangeListener<? super $numberType> listener, Executor executor) {
        helper =
            ProxyAsyncExpressionHelper.addListener(
                helper, this, peer, get(), AsyncChangeListenerWrapper.wrap(listener, executor));
    }

    @Override
    public synchronized void removeListener(ChangeListener<? super $numberType> listener) {
        helper = ProxyAsyncExpressionHelper.removeListener(helper, get(), listener);
    }

    @Override
    public synchronized void addListener(InvalidationListener listener) {
        helper = ProxyAsyncExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public synchronized void addListener(InvalidationListener listener, Executor executor) {
        helper =
            ProxyAsyncExpressionHelper.addListener(
                helper, this, peer, get(), AsyncInvalidationListenerWrapper.wrap(listener, executor));
    }

    @Override
    public synchronized void removeListener(InvalidationListener listener) {
        helper = ProxyAsyncExpressionHelper.removeListener(helper, get(), listener);
    }

#if($boxedType == "Object")
    @Override
    public void addListener(SubInvalidationListener listener) {
        helper = ProxyAsyncExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public void addListener(SubInvalidationListener listener, Executor executor) {
        helper =
            ProxyAsyncExpressionHelper.addListener(
                helper, this, peer, get(), AsyncSubInvalidationListenerWrapper.wrap(listener, executor));
    }

    @Override
    public void removeListener(SubInvalidationListener listener) {
        helper = ProxyAsyncExpressionHelper.removeListener(helper, get(), listener);
    }

    @Override
    public void addListener(SubChangeListener listener) {
        helper = ProxyAsyncExpressionHelper.addListener(helper, this, peer, get(), listener);
    }

    @Override
    public void addListener(SubChangeListener listener, Executor executor) {
        helper =
            ProxyAsyncExpressionHelper.addListener(
                helper, this, peer, get(), AsyncSubChangeListenerWrapper.wrap(listener, executor));
    }

    @Override
    public void removeListener(SubChangeListener listener) {
        helper = ProxyAsyncExpressionHelper.removeListener(helper, get(), listener);
    }
#end

    @Override
    public synchronized void set($primType value) {
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
