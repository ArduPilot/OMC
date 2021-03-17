/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.serialization;

import com.intel.missioncontrol.geom.Vec2;
import com.intel.missioncontrol.geom.Vec3;
import com.intel.missioncontrol.geom.Vec4;
import java.time.OffsetDateTime;
import java.util.Collection;
import org.jetbrains.annotations.Nullable;

public abstract class DeserializationContext {

    public boolean readBoolean(String name) {
        return readValue(name, Boolean.class, null);
    }

    public int readByte(String name) {
        return readValue(name, Byte.class, null);
    }

    public int readShort(String name) {
        return readValue(name, Short.class, null);
    }

    public int readInteger(String name) {
        return readValue(name, Integer.class, null);
    }

    public long readLong(String name) {
        return readValue(name, Long.class, null);
    }

    public float readFloat(String name) {
        return readValue(name, Float.class, null);
    }

    public double readDouble(String name) {
        return readValue(name, Double.class, null);
    }

    public String readString(String name) {
        return readValue(name, String.class, null);
    }

    public OffsetDateTime readOffsetDateTime(String name) {
        return readValue(name, OffsetDateTime.class, null);
    }

    public Vec2 readVec2(String name) {
        return readValue(name, Vec2.class, null);
    }

    public Vec3 readVec3(String name) {
        return readValue(name, Vec3.class, null);
    }

    public Vec4 readVec4(String name) {
        return readValue(name, Vec4.class, null);
    }

    public <E extends Enum<E>> E readEnum(String name, Class<E> type) {
        String value = readValue(name, String.class, null);
        return value != null ? Enum.valueOf(type, value) : null;
    }

    public final <T extends Serializable> T readObject(String name, Class<T> type) {
        return readValue(name, type, null);
    }

    @SafeVarargs
    public final <T extends Serializable> T readPolymorphicObject(
            String name, Class<T> targetType, Class<? extends T>... potentialTypes) {
        return readValue(name, targetType, potentialTypes);
    }

    public final <T> void readCollection(String name, Collection<? super T> list, Class<T> targetType) {
        readCollection(name, list, targetType, null);
    }

    @SafeVarargs
    public final <T> void readPolymorphicCollection(
            String name, Collection<? super T> list, Class<T> targetType, Class<? extends T>... potentialTypes) {
        readCollection(name, list, targetType, potentialTypes);
    }

    public abstract byte[] readBytes(String name);

    protected abstract <T> void readCollection(
            String name,
            Collection<? super T> list,
            Class<T> targetType,
            @Nullable Class<? extends T>[] potentialTypes);

    protected abstract <T> T readValue(String name, Class<T> targetType, @Nullable Class<? extends T>[] potentialTypes);

}
