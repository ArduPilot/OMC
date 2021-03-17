/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.serialization;

import com.intel.missioncontrol.geom.Vec2;
import com.intel.missioncontrol.geom.Vec3;
import com.intel.missioncontrol.geom.Vec4;
import com.intel.missioncontrol.project.property.TrackingAsyncListProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncObjectProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncSetProperty;
import java.time.OffsetDateTime;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncProperty;
import org.asyncfx.beans.property.AsyncSetProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.LockedList;
import org.asyncfx.collections.LockedSet;

@SuppressWarnings("unused")
public class PropertySerializationHelper {

    public static void writeBoolean(SerializationContext context, AsyncProperty<Boolean> property) {
        context.writeBoolean(property.getName(), property.getValue());
    }

    public static void writeByte(SerializationContext context, AsyncProperty<Number> property) {
        Number value = property.getValue();
        context.writeByte(property.getName(), value != null ? value.byteValue() : 0);
    }

    public static void writeShort(SerializationContext context, AsyncProperty<Number> property) {
        Number value = property.getValue();
        context.writeShort(property.getName(), value != null ? value.shortValue() : 0);
    }

    public static void writeInteger(SerializationContext context, AsyncProperty<Number> property) {
        Number value = property.getValue();
        context.writeInteger(property.getName(), value != null ? value.intValue() : 0);
    }

    public static void writeLong(SerializationContext context, AsyncProperty<Number> property) {
        Number value = property.getValue();
        context.writeLong(property.getName(), value != null ? value.longValue() : 0);
    }

    public static void writeFloat(SerializationContext context, AsyncProperty<Number> property) {
        Number value = property.getValue();
        context.writeFloat(property.getName(), value != null ? value.floatValue() : 0);
    }

    public static void writeDouble(SerializationContext context, AsyncProperty<Number> property) {
        Number value = property.getValue();
        context.writeDouble(property.getName(), value != null ? value.doubleValue() : 0);
    }

    public static void writeString(SerializationContext context, AsyncProperty<String> property) {
        context.writeString(property.getName(), property.getValue());
    }

    public static <T extends Serializable> void writeObject(
            SerializationContext context, AsyncProperty<T> property, Class<? extends Serializable> type) {
        context.writeObject(property.getName(), property.getValue(), type);
    }

    public static <T extends Serializable> void writePolymorphicObject(
            SerializationContext context, AsyncProperty<T> property, Class<? extends Serializable> type) {
        context.writePolymorphicObject(property.getName(), property.getValue(), type);
    }

    public static void writeOffsetDateTime(SerializationContext context, AsyncProperty<OffsetDateTime> property) {
        context.writeOffsetDateTime(property.getName(), property.getValue());
    }

    public static void writeVec2(SerializationContext context, AsyncProperty<Vec2> property) {
        context.writeVec2(property.getName(), property.getValue());
    }

    public static void writeVec3(SerializationContext context, AsyncProperty<Vec3> property) {
        context.writeVec3(property.getName(), property.getValue());
    }

    public static void writeVec4(SerializationContext context, AsyncProperty<Vec4> property) {
        context.writeVec4(property.getName(), property.getValue());
    }

    public static <E extends Enum<E>> void writeEnum(
            SerializationContext context, AsyncProperty<E> property, Class<E> type) {
        Enum<E> value = property.getValue();
        context.writeString(property.getName(), value != null ? value.name() : null);
    }

    public static <T> void writeList(SerializationContext context, AsyncListProperty<T> property, Class<T> type) {
        try (LockedList<T> list = property.lock()) {
            context.writeCollection(property.getName(), list, type);
        }
    }

    public static <T> void writeList(
            SerializationContext context, AsyncObjectProperty<AsyncObservableList<T>> property, Class<T> type) {
        try (LockedList<T> list = property.get().lock()) {
            context.writeCollection(property.getName(), list, type);
        }
    }

    public static <T> void writePolymorphicList(
            SerializationContext context, AsyncListProperty<T> property, Class<T> type) {
        try (LockedList<T> list = property.lock()) {
            context.writePolymorphicCollection(property.getName(), list, type);
        }
    }

    public static <T> void writeSet(SerializationContext context, AsyncSetProperty<T> property, Class<T> type) {
        try (LockedSet<T> set = property.lock()) {
            context.writeCollection(property.getName(), set, type);
        }
    }

    public static <T> void writeSet(
            SerializationContext context, AsyncObjectProperty<AsyncObservableSet<T>> property, Class<T> type) {
        try (LockedSet<T> set = property.get().lock()) {
            context.writeCollection(property.getName(), set, type);
        }
    }

    public static <T> void writePolymorphicSet(
            SerializationContext context, AsyncObjectProperty<AsyncObservableSet<T>> property, Class<T> type) {
        try (LockedSet<T> set = property.get().lock()) {
            context.writePolymorphicCollection(property.getName(), set, type);
        }
    }

    public static void readBoolean(DeserializationContext context, AsyncProperty<Boolean> property) {
        if (property instanceof TrackingAsyncProperty) {
            ((TrackingAsyncProperty<Boolean>)property).update(context.readBoolean(property.getName()));
        } else {
            property.setValue(context.readBoolean(property.getName()));
        }
    }

    public static void readInteger(DeserializationContext context, AsyncProperty<Number> property) {
        if (property instanceof TrackingAsyncProperty) {
            ((TrackingAsyncProperty<Number>)property).update(context.readInteger(property.getName()));
        } else {
            property.setValue(context.readInteger(property.getName()));
        }
    }

    public static void readLong(DeserializationContext context, AsyncProperty<Number> property) {
        if (property instanceof TrackingAsyncProperty) {
            ((TrackingAsyncProperty<Number>)property).update(context.readLong(property.getName()));
        } else {
            property.setValue(context.readLong(property.getName()));
        }
    }

    public static void readFloat(DeserializationContext context, AsyncProperty<Number> property) {
        if (property instanceof TrackingAsyncProperty) {
            ((TrackingAsyncProperty<Number>)property).update(context.readFloat(property.getName()));
        } else {
            property.setValue(context.readFloat(property.getName()));
        }
    }

    public static void readDouble(DeserializationContext context, AsyncProperty<Number> property) {
        if (property instanceof TrackingAsyncProperty) {
            ((TrackingAsyncProperty<Number>)property).update(context.readDouble(property.getName()));
        } else {
            property.setValue(context.readDouble(property.getName()));
        }
    }

    public static void readString(DeserializationContext context, AsyncProperty<String> property) {
        if (property instanceof TrackingAsyncProperty) {
            ((TrackingAsyncProperty<String>)property).update(context.readString(property.getName()));
        } else {
            property.setValue(context.readString(property.getName()));
        }
    }

    public static <E extends Enum<E>> void readEnum(
            DeserializationContext context, AsyncProperty<E> property, Class<E> type) {
        if (property instanceof TrackingAsyncProperty) {
            ((TrackingAsyncProperty<E>)property).update(context.readEnum(property.getName(), type));
        } else {
            property.setValue(context.readEnum(property.getName(), type));
        }
    }

    public static void readOffsetDateTime(DeserializationContext context, AsyncProperty<OffsetDateTime> property) {
        OffsetDateTime value = context.readOffsetDateTime(property.getName());
        if (property instanceof TrackingAsyncProperty) {
            ((TrackingAsyncProperty<OffsetDateTime>)property).update(value);
        } else {
            property.setValue(value);
        }
    }

    public static void readVec2(DeserializationContext context, AsyncProperty<Vec2> property) {
        Vec2 value = context.readVec2(property.getName());
        if (property instanceof TrackingAsyncProperty) {
            ((TrackingAsyncProperty<Vec2>)property).update(value);
        } else {
            property.setValue(value);
        }
    }

    public static void readVec3(DeserializationContext context, AsyncProperty<Vec3> property) {
        Vec3 value = context.readVec3(property.getName());
        if (property instanceof TrackingAsyncProperty) {
            ((TrackingAsyncProperty<Vec3>)property).update(value);
        } else {
            property.setValue(value);
        }
    }

    public static void readVec4(DeserializationContext context, AsyncProperty<Vec4> property) {
        Vec4 value = context.readVec4(property.getName());
        if (property instanceof TrackingAsyncProperty) {
            ((TrackingAsyncProperty<Vec4>)property).update(value);
        } else {
            property.setValue(value);
        }
    }

    public static <T extends Serializable> void readObject(
            DeserializationContext context, AsyncObjectProperty<T> property, Class<T> type) {
        if (property instanceof TrackingAsyncObjectProperty) {
            ((TrackingAsyncObjectProperty<T>)property).update(context.readObject(property.getName(), type));
        } else {
            property.setValue(context.readObject(property.getName(), type));
        }
    }

    @SafeVarargs
    public static <T extends Serializable> void readPolymorphicObject(
            DeserializationContext context,
            AsyncObjectProperty<T> property,
            Class<T> type,
            Class<? extends T>... potentialTypes) {
        if (property instanceof TrackingAsyncObjectProperty) {
            ((TrackingAsyncObjectProperty<T>)property)
                .update(context.readPolymorphicObject(property.getName(), type, potentialTypes));
        } else {
            property.setValue(context.readPolymorphicObject(property.getName(), type, potentialTypes));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void readList(DeserializationContext context, AsyncListProperty<T> property, Class<T> type) {
        try (LockedList<T> list = property.lock()) {
            context.readCollection(property.getName(), list, type);

            if (property instanceof TrackingAsyncListProperty) {
                ((TrackingAsyncListProperty)property).update(property.get());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void readList(
            DeserializationContext context, AsyncObjectProperty<AsyncObservableList<T>> property, Class<T> type) {
        try (LockedList<T> list = property.get().lock()) {
            context.readCollection(property.getName(), list, type);

            if (property instanceof TrackingAsyncObjectProperty) {
                ((TrackingAsyncObjectProperty)property).update(property.get());
            }
        }
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> void readPolymorphicList(
            DeserializationContext context,
            AsyncListProperty<T> property,
            Class<T> type,
            Class<? extends T>... potentialTypes) {
        try (LockedList<T> list = property.lock()) {
            context.readCollection(property.getName(), list, type, potentialTypes);

            if (property instanceof TrackingAsyncListProperty) {
                ((TrackingAsyncListProperty)property).update(property.get());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void readSet(DeserializationContext context, AsyncSetProperty<T> property, Class<T> type) {
        try (LockedSet<T> set = property.lock()) {
            context.readCollection(property.getName(), set, type);

            if (property instanceof TrackingAsyncSetProperty) {
                ((TrackingAsyncSetProperty)property).update(property.get());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void readSet(
            DeserializationContext context, AsyncObjectProperty<AsyncObservableSet<T>> property, Class<T> type) {
        try (LockedSet<T> set = property.get().lock()) {
            context.readCollection(property.getName(), set, type);

            if (property instanceof TrackingAsyncObjectProperty) {
                ((TrackingAsyncObjectProperty)property).update(property.get());
            }
        }
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> void readPolymorphicSet(
            DeserializationContext context,
            AsyncSetProperty<T> property,
            Class<T> type,
            Class<? extends T>... potentialTypes) {
        try (LockedSet<T> set = property.lock()) {
            context.readCollection(property.getName(), set, type, potentialTypes);

            if (property instanceof TrackingAsyncSetProperty) {
                ((TrackingAsyncSetProperty)property).update(property.get());
            }
        }
    }

}
