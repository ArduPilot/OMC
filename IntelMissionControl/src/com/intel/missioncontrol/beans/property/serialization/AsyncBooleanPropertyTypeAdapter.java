/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property.serialization;

import com.google.gson.TypeAdapter;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;

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
