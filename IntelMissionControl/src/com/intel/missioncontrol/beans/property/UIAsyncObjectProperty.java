/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.PublishSource;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class UIAsyncObjectProperty<T> extends SimpleAsyncObjectProperty<T> implements Property<T> {

    public UIAsyncObjectProperty(Object bean) {
        this(bean, new UIPropertyMetadata.Builder<T>().create());
    }

    public UIAsyncObjectProperty(Object bean, UIPropertyMetadata<T> metadata) {
        super(bean, metadata);
    }

    @Override
    public void overrideMetadata(PropertyMetadata<T> metadata) {
        if (!(metadata instanceof UIPropertyMetadata)) {
            throw new IllegalArgumentException(
                "Metadata can only be overridden with an instance of " + UIPropertyMetadata.class.getSimpleName());
        }

        super.overrideMetadata(metadata);
    }

    @Override
    public void bindBidirectional(Property<T> other) {
        Bindings.bindBidirectional(this, other);
    }

    @Override
    public void unbindBidirectional(Property<T> other) {
        Bindings.unbindBidirectional(this, other);
    }

}
