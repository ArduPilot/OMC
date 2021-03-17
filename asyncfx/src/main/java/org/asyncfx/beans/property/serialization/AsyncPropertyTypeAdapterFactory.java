/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property.serialization;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncFloatProperty;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncLongProperty;
import org.asyncfx.beans.property.AsyncProperty;
import org.asyncfx.beans.property.AsyncSetProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.AsyncObservableSet;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
public class AsyncPropertyTypeAdapterFactory implements TypeAdapterFactory {

    private final boolean strictProperties;

    private final boolean strictPrimitives;

    public AsyncPropertyTypeAdapterFactory() {
        this(true, true);
    }

    public AsyncPropertyTypeAdapterFactory(boolean throwOnNullProperties, boolean throwOnNullPrimitives) {
        this.strictProperties = throwOnNullProperties;
        this.strictPrimitives = throwOnNullPrimitives;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> clazz = type.getRawType();

        if (!AsyncProperty.class.isAssignableFrom(clazz)) {
            return null;
        }

        if (AsyncBooleanProperty.class.isAssignableFrom(clazz)) {
            return (TypeAdapter<T>)
                new AsyncBooleanPropertyTypeAdapter(gson.getAdapter(boolean.class), strictProperties, strictPrimitives);
        }

        if (AsyncIntegerProperty.class.isAssignableFrom(clazz)) {
            return (TypeAdapter<T>)
                new AsyncIntegerPropertyTypeAdapter(gson.getAdapter(int.class), strictProperties, strictPrimitives);
        }

        if (AsyncLongProperty.class.isAssignableFrom(clazz)) {
            return (TypeAdapter<T>)
                new AsyncLongPropertyTypeAdapter(gson.getAdapter(long.class), strictProperties, strictPrimitives);
        }

        if (AsyncFloatProperty.class.isAssignableFrom(clazz)) {
            return (TypeAdapter<T>)
                new AsyncFloatPropertyTypeAdapter(gson.getAdapter(float.class), strictProperties, strictPrimitives);
        }

        if (AsyncDoubleProperty.class.isAssignableFrom(clazz)) {
            return (TypeAdapter<T>)
                new AsyncDoublePropertyTypeAdapter(gson.getAdapter(double.class), strictProperties, strictPrimitives);
        }

        if (AsyncStringProperty.class.isAssignableFrom(clazz)) {
            return (TypeAdapter<T>)new AsyncStringPropertyTypeAdapter(gson.getAdapter(String.class), strictProperties);
        }

        if (AsyncListProperty.class.isAssignableFrom(clazz)) {
            TypeAdapter<?> delegate = gson.getAdapter(TypeHelper.withRawType(type, AsyncObservableList.class));
            return new AsyncListPropertyTypeAdapter(delegate, strictProperties);
        }

        if (AsyncSetProperty.class.isAssignableFrom(clazz)) {
            TypeAdapter<?> delegate = gson.getAdapter(TypeHelper.withRawType(type, AsyncObservableSet.class));
            return new AsyncSetPropertyTypeAdapter(delegate, strictProperties);
        }

        Type[] typeParams = ((ParameterizedType)type.getType()).getActualTypeArguments();
        Type param = typeParams[0];
        TypeAdapter<?> delegate = gson.getAdapter(TypeToken.get(param));
        return (TypeAdapter<T>)new AsyncObjectPropertyTypeAdapter<>(delegate, strictProperties);
    }
}
