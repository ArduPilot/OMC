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
import org.asyncfx.beans.property.AsyncSetProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncSetProperty;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.LockedSet;
import org.jetbrains.annotations.NotNull;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncSetPropertyTypeAdapter<E>
        extends AsyncPropertyTypeAdapter<AsyncObservableSet<E>, AsyncSetProperty<E>> {

    public AsyncSetPropertyTypeAdapter(TypeAdapter<AsyncObservableSet<E>> delegate, boolean throwOnNullProperty) {
        super(delegate, throwOnNullProperty);
    }

    @NotNull
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
