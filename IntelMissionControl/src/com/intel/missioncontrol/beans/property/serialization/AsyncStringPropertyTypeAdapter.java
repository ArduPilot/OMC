/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property.serialization;

import com.google.gson.TypeAdapter;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.AsyncStringProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncStringProperty;
import org.checkerframework.checker.nullness.qual.NonNull;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncStringPropertyTypeAdapter extends AsyncPropertyTypeAdapter<String, AsyncStringProperty> {

    public AsyncStringPropertyTypeAdapter(TypeAdapter<String> delegate, boolean throwOnNullProperty) {
        super(delegate, throwOnNullProperty);
    }

    @NonNull
    @Override
    protected AsyncStringProperty createProperty(String deserializedValue) {
        return new SimpleAsyncStringProperty(
            this, new PropertyMetadata.Builder<String>().initialValue(deserializedValue).create());
    }
}
