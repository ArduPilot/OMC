/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.property.AsyncProperty;
import org.hildan.fxgson.adapters.properties.NullPropertyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public abstract class AsyncPropertyTypeAdapter<I, P extends AsyncProperty<? extends I>> extends TypeAdapter<P> {

    private final TypeAdapter<I> delegate;

    private final boolean throwOnNullProperty;

    AsyncPropertyTypeAdapter(TypeAdapter<I> innerValueTypeAdapter, boolean throwOnNullProperty) {
        this.delegate = innerValueTypeAdapter;
        this.throwOnNullProperty = throwOnNullProperty;
    }

    @Override
    public void write(JsonWriter out, P property) throws IOException {
        if (property == null) {
            if (throwOnNullProperty) {
                throw new NullPropertyException();
            }

            out.nullValue();
            return;
        }

        delegate.write(out, property.getValue());
    }

    @Override
    public P read(JsonReader in) throws IOException {
        return createProperty(delegate.read(in));
    }

    @NotNull
    protected abstract P createProperty(@Nullable I deserializedValue);
}
