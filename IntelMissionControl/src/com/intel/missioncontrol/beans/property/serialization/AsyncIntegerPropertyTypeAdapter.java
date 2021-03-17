/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property.serialization;

import com.google.gson.TypeAdapter;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.AsyncIntegerProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncIntegerProperty;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncIntegerPropertyTypeAdapter extends PrimitiveAsyncPropertyTypeAdapter<Integer, AsyncIntegerProperty> {

    public AsyncIntegerPropertyTypeAdapter(
            TypeAdapter<Integer> delegate, boolean throwOnNullProperty, boolean crashOnNullValue) {
        super(delegate, throwOnNullProperty, crashOnNullValue);
    }

    @Override
    protected Integer extractPrimitiveValue(AsyncIntegerProperty property) {
        return property.get();
    }

    @Override
    protected AsyncIntegerProperty createDefaultProperty() {
        return new SimpleAsyncIntegerProperty(this);
    }

    @Override
    protected AsyncIntegerProperty wrapNonNullPrimitiveValue(Integer deserializedValue) {
        return new SimpleAsyncIntegerProperty(
            this, new PropertyMetadata.Builder<Number>().initialValue(deserializedValue).create());
    }

}
