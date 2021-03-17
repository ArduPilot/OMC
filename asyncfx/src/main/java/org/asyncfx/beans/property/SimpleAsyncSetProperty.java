/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import org.asyncfx.PublishSource;
import org.asyncfx.collections.AsyncObservableSet;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class SimpleAsyncSetProperty<E> extends AsyncSetPropertyBase<E> {

    private final Object bean;

    public SimpleAsyncSetProperty(Object bean) {
        this(bean, new PropertyMetadata.Builder<AsyncObservableSet<E>>().create());
    }

    public SimpleAsyncSetProperty(PropertyObject bean) {
        this(bean, new PropertyMetadata.Builder<AsyncObservableSet<E>>().create());
    }

    public SimpleAsyncSetProperty(Object bean, PropertyMetadata<AsyncObservableSet<E>> metadata) {
        super(metadata);
        this.bean = bean;

        if (bean instanceof PropertyObject) {
            ((PropertyObject)bean).registerProperty(this);
        }
    }

    public SimpleAsyncSetProperty(PropertyObject bean, PropertyMetadata<AsyncObservableSet<E>> metadata) {
        super(bean, metadata);
        this.bean = bean;

        if (bean != null) {
            bean.registerProperty(this);
        }
    }

    @Override
    public Object getBean() {
        return bean;
    }

    @Override
    public boolean isBoundBidirectionally() {
        return false;
    }

}
