/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.LockedList;
import org.jetbrains.annotations.NotNull;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncListPropertyTypeAdapter<E>
        extends AsyncPropertyTypeAdapter<AsyncObservableList<E>, AsyncListProperty<E>> {

    public AsyncListPropertyTypeAdapter(TypeAdapter<AsyncObservableList<E>> delegate, boolean throwOnNullProperty) {
        super(delegate, throwOnNullProperty);
    }

    @NotNull
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
