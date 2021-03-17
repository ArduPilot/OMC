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

public abstract class SerializationContext {

    public void writeBoolean(String name, boolean value) {
        writeValue(name, value, Boolean.class, false);
    }

    public void writeByte(String name, byte value) {
        writeValue(name, value, Byte.class, false);
    }

    public void writeShort(String name, short value) {
        writeValue(name, value, Short.class, false);
    }

    public void writeInteger(String name, int value) {
        writeValue(name, value, Integer.class, false);
    }

    public void writeLong(String name, long value) {
        writeValue(name, value, Long.class, false);
    }

    public void writeFloat(String name, float value) {
        writeValue(name, value, Float.class, false);
    }

    public void writeDouble(String name, double value) {
        writeValue(name, value, Double.class, false);
    }

    public void writeString(String name, String value) {
        writeValue(name, value, String.class, false);
    }

    public void writeOffsetDateTime(String name, OffsetDateTime value) {
        writeValue(name, value, OffsetDateTime.class, false);
    }

    public void writeVec2(String name, Vec2 value) {
        writeValue(name, value, Vec2.class, false);
    }

    public void writeVec3(String name, Vec3 value) {
        writeValue(name, value, Vec3.class, false);
    }

    public void writeVec4(String name, Vec4 value) {
        writeValue(name, value, Vec4.class, false);
    }

    public void writeObject(String name, Serializable value, Class<? extends Serializable> type) {
        if (value != null) {
            writeValue(name, value, value.getClass(), false);
        } else {
            writeValue(name, null, type, false);
        }
    }

    public void writePolymorphicObject(String name, Serializable value, Class<? extends Serializable> type) {
        if (value != null) {
            writeValue(name, value, value.getClass(), true);
        } else {
            writeValue(name, null, type, true);
        }
    }

    public <E extends Enum<E>> void writeEnum(String name, E value) {
        if (value != null) {
            writeString(name, value.name());
        } else {
            writeString(name, null);
        }
    }

    public final <T> void writeCollection(String name, Collection<T> value, Class<? super T> type) {
        writeCollection(name, value, type, false);
    }

    public final <T> void writePolymorphicCollection(String name, Collection<T> value, Class<? super T> type) {
        writeCollection(name, value, type, true);
    }

    public abstract void writeBytes(String name, byte[] value);

    protected abstract void writeValue(String name, Object value, Class<?> type, boolean writeTypeInfo);

    protected abstract <T> void writeCollection(
            String name, Collection<T> value, Class<? super T> type, boolean writeTypeInfo);

}
