/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.collections.AsyncObservableSet;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class SimpleAsyncSetProperty<E> extends AsyncSetPropertyBase<E> {

    private final Object bean;
    private String name;

    public SimpleAsyncSetProperty(Object bean) {
        this(bean, new PropertyMetadata.Builder<AsyncObservableSet<E>>().create());
    }

    public SimpleAsyncSetProperty(Object bean, PropertyMetadata<AsyncObservableSet<E>> metadata) {
        super(metadata);
        this.bean = bean;
        PropertyHelper.checkProperty(bean, this, metadata);
    }

    @Override
    public Object getBean() {
        return bean;
    }

    @Override
    public String getName() {
        synchronized (mutex) {
            if (name == null) {
                String metadataName = getMetadata().getName();
                // name = metadataName != null ? metadataName : PropertyHelper.getPropertyName(bean, this);
            }

            return name;
        }
    }

    @Override
    public boolean isBoundBidirectionally() {
        return false;
    }
}
