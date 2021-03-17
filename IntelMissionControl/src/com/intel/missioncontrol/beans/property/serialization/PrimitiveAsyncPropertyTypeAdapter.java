/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.AsyncProperty;
import java.io.IOException;
import org.hildan.fxgson.adapters.properties.NullPropertyException;
import org.hildan.fxgson.adapters.properties.primitives.NullPrimitiveException;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public abstract class PrimitiveAsyncPropertyTypeAdapter<I, P extends AsyncProperty<?>> extends TypeAdapter<P> {

    private final TypeAdapter<I> delegate;

    private final boolean throwOnNullProperty;

    private final boolean crashOnNullValue;

    public PrimitiveAsyncPropertyTypeAdapter(
            TypeAdapter<I> innerValueTypeAdapter, boolean throwOnNullProperty, boolean crashOnNullValue) {
        this.delegate = innerValueTypeAdapter;
        this.throwOnNullProperty = throwOnNullProperty;
        this.crashOnNullValue = crashOnNullValue;
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

        delegate.write(out, extractPrimitiveValue(property));
    }

    @Override
    public P read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            if (crashOnNullValue) {
                throw new NullPrimitiveException(in.getPath());
            } else {
                return createDefaultProperty();
            }
        } else {
            return wrapNonNullPrimitiveValue(delegate.read(in));
        }
    }

    protected abstract I extractPrimitiveValue(P property);

    protected abstract P createDefaultProperty();

    protected abstract P wrapNonNullPrimitiveValue(I deserializedValue);
}
