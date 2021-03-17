/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import org.asyncfx.PublishSource;
import org.asyncfx.collections.AsyncObservableList;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class SimpleAsyncListProperty<E> extends AsyncListPropertyBase<E> {

    private final Object bean;

    public SimpleAsyncListProperty(Object bean) {
        this(bean, new PropertyMetadata.Builder<AsyncObservableList<E>>().create());
    }

    public SimpleAsyncListProperty(PropertyObject bean) {
        this(bean, new PropertyMetadata.Builder<AsyncObservableList<E>>().create());
    }

    public SimpleAsyncListProperty(Object bean, PropertyMetadata<AsyncObservableList<E>> metadata) {
        super(metadata);
        this.bean = bean;

        if (bean instanceof PropertyObject) {
            ((PropertyObject)bean).registerProperty(this);
        }
    }

    public SimpleAsyncListProperty(PropertyObject bean, PropertyMetadata<AsyncObservableList<E>> metadata) {
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

}
