/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property.serialization;

import com.google.gson.TypeAdapter;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import org.jetbrains.annotations.NotNull;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncStringPropertyTypeAdapter extends AsyncPropertyTypeAdapter<String, AsyncStringProperty> {

    public AsyncStringPropertyTypeAdapter(TypeAdapter<String> delegate, boolean throwOnNullProperty) {
        super(delegate, throwOnNullProperty);
    }

    @NotNull
    @Override
    protected AsyncStringProperty createProperty(String deserializedValue) {
        return new SimpleAsyncStringProperty(
            this, new PropertyMetadata.Builder<String>().initialValue(deserializedValue).create());
    }
}
