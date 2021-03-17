/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property.serialization;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.asyncfx.PublishSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@PublishSource(
    module = "openjfx",
    licenses = {"fxgson", "intel-gpl-classpath-exception"}
)
final class TypeHelper {

    private TypeHelper() throws InstantiationException {
        throw new InstantiationException("Instances of this type are forbidden.");
    }

    @NotNull
    static TypeToken<?> withRawType(@NotNull TypeToken<?> sourceTypeToken, @NotNull Type newRawType) {
        ParameterizedType sourceType = (ParameterizedType)sourceTypeToken.getType();
        Type[] typeParams = sourceType.getActualTypeArguments();
        Type targetType = newParametrizedType(newRawType, typeParams);
        return TypeToken.get(targetType);
    }

    @NotNull
    private static ParameterizedType newParametrizedType(@NotNull Type rawType, @NotNull Type... typeArguments) {
        return new CustomParameterizedType(rawType, null, typeArguments);
    }

    private static class CustomParameterizedType implements ParameterizedType {

        private Type rawType;

        private Type ownerType;

        private Type[] typeArguments;

        private CustomParameterizedType(@NotNull Type rawType, @Nullable Type ownerType, @NotNull Type... typeArgs) {
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
