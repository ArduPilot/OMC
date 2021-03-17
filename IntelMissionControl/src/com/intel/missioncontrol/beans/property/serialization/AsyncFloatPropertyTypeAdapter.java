/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property.serialization;

import com.google.gson.TypeAdapter;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.AsyncFloatProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncFloatProperty;

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
