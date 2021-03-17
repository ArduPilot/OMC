/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property.serialization;

import com.google.gson.TypeAdapter;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import org.checkerframework.checker.nullness.qual.NonNull;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncObjectPropertyTypeAdapter<T> extends AsyncPropertyTypeAdapter<T, AsyncObjectProperty<T>> {

    public AsyncObjectPropertyTypeAdapter(TypeAdapter<T> delegate, boolean throwOnNullProperty) {
        super(delegate, throwOnNullProperty);
    }

    @NonNull
    @Override
    protected AsyncObjectProperty<T> createProperty(T deserializedValue) {
        return new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<T>().initialValue(deserializedValue).create());
    }

}
