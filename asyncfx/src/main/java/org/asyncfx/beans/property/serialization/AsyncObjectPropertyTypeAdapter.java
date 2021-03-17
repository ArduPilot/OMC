/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property.serialization;

import com.google.gson.TypeAdapter;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.jetbrains.annotations.NotNull;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncObjectPropertyTypeAdapter<T> extends AsyncPropertyTypeAdapter<T, AsyncObjectProperty<T>> {

    public AsyncObjectPropertyTypeAdapter(TypeAdapter<T> delegate, boolean throwOnNullProperty) {
        super(delegate, throwOnNullProperty);
    }

    @NotNull
    @Override
    protected AsyncObjectProperty<T> createProperty(T deserializedValue) {
        return new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<T>().initialValue(deserializedValue).create());
    }

}
