/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property.serialization;

import com.google.gson.TypeAdapter;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.AsyncLongProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncLongProperty;

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
