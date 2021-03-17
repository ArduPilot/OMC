/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.AsyncProperty;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hildan.fxgson.adapters.properties.NullPropertyException;

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

    @NonNull
    protected abstract P createProperty(@Nullable I deserializedValue);
}
