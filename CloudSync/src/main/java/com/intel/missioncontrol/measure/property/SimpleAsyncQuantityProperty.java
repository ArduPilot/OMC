/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure.property;

import com.intel.missioncontrol.measure.Quantity;
import org.asyncfx.beans.property.ObservableObject;

public class SimpleAsyncQuantityProperty<Q extends Quantity<Q>> extends AsyncQuantityProperty<Q> {

    private final Object bean;

    public SimpleAsyncQuantityProperty(Object bean) {
        this(bean, new QuantityPropertyMetadata.Builder<Q>().create());
    }

    public SimpleAsyncQuantityProperty(Object bean, QuantityPropertyMetadata<Q> metadata) {
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
