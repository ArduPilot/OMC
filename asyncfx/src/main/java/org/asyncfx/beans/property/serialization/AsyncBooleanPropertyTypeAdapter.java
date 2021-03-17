/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property.serialization;

import com.google.gson.TypeAdapter;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncBooleanPropertyTypeAdapter extends PrimitiveAsyncPropertyTypeAdapter<Boolean, AsyncBooleanProperty> {

    public AsyncBooleanPropertyTypeAdapter(
            TypeAdapter<Boolean> delegate, boolean throwOnNullProperty, boolean crashOnNullValue) {
        super(delegate, throwOnNullProperty, crashOnNullValue);
    }

    @Override
    protected Boolean extractPrimitiveValue(AsyncBooleanProperty property) {
        return property.get();
    }

    @Override
    protected AsyncBooleanProperty createDefaultProperty() {
        return new SimpleAsyncBooleanProperty(this);
    }

    @Override
    protected AsyncBooleanProperty wrapNonNullPrimitiveValue(Boolean deserializedValue) {
        return new SimpleAsyncBooleanProperty(
            this, new PropertyMetadata.Builder<Boolean>().initialValue(deserializedValue).create());
    }
}
