/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property.serialization;

import com.google.gson.reflect.TypeToken;
import com.intel.missioncontrol.PublishSource;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
final class TypeHelper {

    private TypeHelper() throws InstantiationException {
        throw new InstantiationException("Instances of this type are forbidden.");
    }

    @NonNull
    static TypeToken<?> withRawType(@NonNull TypeToken<?> sourceTypeToken, @NonNull Type newRawType) {
        ParameterizedType sourceType = (ParameterizedType)sourceTypeToken.getType();
        Type[] typeParams = sourceType.getActualTypeArguments();
        Type targetType = newParametrizedType(newRawType, typeParams);
        return TypeToken.get(targetType);
    }

    @NonNull
    private static ParameterizedType newParametrizedType(@NonNull Type rawType, @NonNull Type... typeArguments) {
        return new CustomParameterizedType(rawType, null, typeArguments);
    }

    private static class CustomParameterizedType implements ParameterizedType {

        private Type rawType;

        private Type ownerType;

        private Type[] typeArguments;

        private CustomParameterizedType(@NonNull Type rawType, @Nullable Type ownerType, @NonNull Type... typeArgs) {
            this.rawType = rawType;
            this.ownerType = ownerType;
            this.typeArguments = typeArgs;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }
    }

}
