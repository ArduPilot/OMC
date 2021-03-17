/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property.serialization;

import com.google.gson.TypeAdapter;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.property.AsyncLongProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncLongProperty;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncLongPropertyTypeAdapter extends PrimitiveAsyncPropertyTypeAdapter<Long, AsyncLongProperty> {

    public AsyncLongPropertyTypeAdapter(
            TypeAdapter<Long> delegate, boolean throwOnNullProperty, boolean crashOnNullValue) {
        super(delegate, throwOnNullProperty, crashOnNullValue);
    }

    @Override
    protected Long extractPrimitiveValue(AsyncLongProperty property) {
        return property.get();
    }

    @Override
    protected AsyncLongProperty createDefaultProperty() {
        return new SimpleAsyncLongProperty(this);
    }

    @Override
    protected AsyncLongProperty wrapNonNullPrimitiveValue(Long deserializedValue) {
        return new SimpleAsyncLongProperty(
            this, new PropertyMetadata.Builder<Number>().initialValue(deserializedValue).create());
    }
}
