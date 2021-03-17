/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.LockedList;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.NonNull;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncListPropertyTypeAdapter<E>
        extends AsyncPropertyTypeAdapter<AsyncObservableList<E>, AsyncListProperty<E>> {

    public AsyncListPropertyTypeAdapter(TypeAdapter<AsyncObservableList<E>> delegate, boolean throwOnNullProperty) {
        super(delegate, throwOnNullProperty);
    }

    @NonNull
    @Override
    protected AsyncListProperty<E> createProperty(AsyncObservableList<E> deserializedValue) {
        return new SimpleAsyncListProperty<E>(
            this, new PropertyMetadata.Builder<AsyncObservableList<E>>().initialValue(deserializedValue).create());
    }

    @Override
    public void write(JsonWriter out, AsyncListProperty<E> property) throws IOException {
        if (property != null) {
            try (LockedList<E> list = property.lock()) {
                super.write(out, property);
            }
        } else {
            super.write(out, null);
        }
    }

}
