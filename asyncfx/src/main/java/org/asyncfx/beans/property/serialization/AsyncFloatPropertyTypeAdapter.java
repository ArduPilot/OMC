/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property.serialization;

import com.google.gson.TypeAdapter;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.property.AsyncFloatProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncFloatProperty;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncFloatPropertyTypeAdapter extends PrimitiveAsyncPropertyTypeAdapter<Float, AsyncFloatProperty> {

    public AsyncFloatPropertyTypeAdapter(
            TypeAdapter<Float> delegate, boolean throwOnNullProperty, boolean crashOnNullValue) {
        super(delegate, throwOnNullProperty, crashOnNullValue);
    }

    @Override
    protected Float extractPrimitiveValue(AsyncFloatProperty property) {
        return property.get();
    }

    @Override
    protected AsyncFloatProperty createDefaultProperty() {
        return new SimpleAsyncFloatProperty(this);
    }

    @Override
    protected AsyncFloatProperty wrapNonNullPrimitiveValue(Float deserializedValue) {
        return new SimpleAsyncFloatProperty(
            this, new PropertyMetadata.Builder<Number>().initialValue(deserializedValue).create());
    }
}
