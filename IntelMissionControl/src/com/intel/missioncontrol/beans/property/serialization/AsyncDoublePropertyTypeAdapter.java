/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property.serialization;

import com.google.gson.TypeAdapter;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.AsyncDoubleProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncDoubleProperty;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncDoublePropertyTypeAdapter extends PrimitiveAsyncPropertyTypeAdapter<Double, AsyncDoubleProperty> {

    public AsyncDoublePropertyTypeAdapter(
            TypeAdapter<Double> delegate, boolean throwOnNullProperty, boolean crashOnNullValue) {
        super(delegate, throwOnNullProperty, crashOnNullValue);
    }

    @Override
    protected Double extractPrimitiveValue(AsyncDoubleProperty property) {
        return property.get();
    }

    @Override
    protected AsyncDoubleProperty createDefaultProperty() {
        return new SimpleAsyncDoubleProperty(this);
    }

    @Override
    protected AsyncDoubleProperty wrapNonNullPrimitiveValue(Double deserializedValue) {
        return new SimpleAsyncDoubleProperty(
            this, new PropertyMetadata.Builder<Number>().initialValue(deserializedValue).create());
    }
}
