/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.measure.Quantity;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;

public class UIAsyncQuantityProperty<Q extends Quantity<Q>> extends SimpleAsyncQuantityProperty<Q>
        implements QuantityProperty<Q> {

    public UIAsyncQuantityProperty(Object bean) {
        this(bean, new UIQuantityPropertyMetadata.Builder<Q>().create());
    }

    public UIAsyncQuantityProperty(Object bean, UIQuantityPropertyMetadata<Q> metadata) {
        super(bean, metadata);
    }

    @Override
    public void bindBidirectional(Property<Quantity<Q>> other) {
        Bindings.bindBidirectional(this, other);
    }

    @Override
    public void unbindBidirectional(Property<Quantity<Q>> other) {
        Bindings.unbindBidirectional(this, other);
    }

}
