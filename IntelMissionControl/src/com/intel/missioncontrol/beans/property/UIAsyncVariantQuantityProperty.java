/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.measure.VariantQuantity;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;

public class UIAsyncVariantQuantityProperty extends SimpleAsyncVariantQuantityProperty
        implements Property<VariantQuantity> {

    public UIAsyncVariantQuantityProperty(Object bean) {
        this(bean, new UIVariantQuantityPropertyMetadata.Builder().create());
    }

    public UIAsyncVariantQuantityProperty(Object bean, UIVariantQuantityPropertyMetadata metadata) {
        super(bean, metadata);
    }

    @Override
    public void bindBidirectional(Property<VariantQuantity> other) {
        Bindings.bindBidirectional(this, other);
    }

    @Override
    public void unbindBidirectional(Property<VariantQuantity> other) {
        Bindings.unbindBidirectional(this, other);
    }

}
