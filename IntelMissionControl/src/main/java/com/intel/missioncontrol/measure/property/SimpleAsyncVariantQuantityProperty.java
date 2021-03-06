/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure.property;

import org.asyncfx.beans.property.ObservableObject;

public class SimpleAsyncVariantQuantityProperty extends AsyncVariantQuantityProperty {

    private final Object bean;

    public SimpleAsyncVariantQuantityProperty(Object bean) {
        this(bean, new VariantQuantityPropertyMetadata.Builder().create());
    }

    public SimpleAsyncVariantQuantityProperty(Object bean, VariantQuantityPropertyMetadata metadata) {
        super(metadata);
        this.bean = bean;

        if (bean instanceof ObservableObject) {
            ObservableObject.Accessor.registerProperty((ObservableObject)bean, this);
        }
    }

    @Override
    public Object getBean() {
        return bean;
    }

}
