/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.AsyncSetProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncSetProperty;
import com.intel.missioncontrol.collections.AsyncObservableSet;
import com.intel.missioncontrol.collections.LockedSet;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.NonNull;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncSetPropertyTypeAdapter<E>
        extends AsyncPropertyTypeAdapter<AsyncObservableSet<E>, AsyncSetProperty<E>> {

    public AsyncSetPropertyTypeAdapter(TypeAdapter<AsyncObservableSet<E>> delegate, boolean throwOnNullProperty) {
        super(delegate, throwOnNullProperty);
    }

    @NonNull
    @Override
    protected AsyncSetProperty<E> createProperty(AsyncObservableSet<E> deserializedValue) {
        return new SimpleAsyncSetProperty<>(
            this, new PropertyMetadata.Builder<AsyncObservableSet<E>>().initialValue(deserializedValue).create());
    }

    @Override
    public void write(JsonWriter out, AsyncSetProperty<E> property) throws IOException {
        if (property != null) {
            try (LockedSet<E> set = property.lock()) {
                super.write(out, property);
            }
        } else {
            super.write(out, null);
        }
    }

}
