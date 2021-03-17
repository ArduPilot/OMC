/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;
import com.intel.missioncontrol.serialization.CompositeSerializationContext;
import com.intel.missioncontrol.serialization.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.collections.LockedSet;

@SuppressWarnings("unused")
public class PropertySerializationHelper {

    public static void writeBoolean(CompositeSerializationContext context, TrackingAsyncBooleanProperty property) {
        context.writeCollection(
            property.getName(), new PairList<>(property.getValue(), property.getCleanValue()), Boolean.class);
    }

    public static void writeInteger(CompositeSerializationContext context, TrackingAsyncIntegerProperty property) {
        context.writeCollection(
            property.getName(), new PairList<>(property.getValue(), property.getCleanValue()), Integer.class);
    }

    public static void writeLong(CompositeSerializationContext context, TrackingAsyncLongProperty property) {
        context.writeCollection(
            property.getName(), new PairList<>(property.getValue(), property.getCleanValue()), Long.class);
    }

    public static void writeFloat(CompositeSerializationContext context, TrackingAsyncFloatProperty property) {
        context.writeCollection(
            property.getName(), new PairList<>(property.getValue(), property.getCleanValue()), Float.class);
    }

    public static void writeDouble(CompositeSerializationContext context, TrackingAsyncDoubleProperty property) {
        context.writeCollection(
            property.getName(), new PairList<>(property.getValue(), property.getCleanValue()), Double.class);
    }

    public static void writeString(CompositeSerializationContext context, TrackingAsyncStringProperty property) {
        context.writeCollection(
            property.getName(), new PairList<>(property.getValue(), property.getCleanValue()), String.class);
    }

    public static <T extends Serializable> void writeObject(
            CompositeSerializationContext context, TrackingAsyncObjectProperty<T> property) {
        context.writeCollection(
            property.getName(), new PairList<>(property.getValue(), property.getCleanValue()), Serializable.class);
    }

    public static <T extends Serializable> void writePolymorphicObject(
            CompositeSerializationContext context, TrackingAsyncObjectProperty<T> property) {
        context.writePolymorphicCollection(
            property.getName(), new PairList<>(property.getValue(), property.getCleanValue()), Serializable.class);
    }

    public static void writeOffsetDateTime(
            CompositeSerializationContext context, TrackingAsyncObjectProperty<OffsetDateTime> property) {
        context.writeCollection(
            property.getName(), new PairList<>(property.getValue(), property.getCleanValue()), OffsetDateTime.class);
    }

    public static <E extends Enum<E>> void writeEnum(
            CompositeSerializationContext context, TrackingAsyncObjectProperty<E> property, Class<E> type) {
        Enum<E> value = property.getValue();
        Enum<E> cleanValue = property.getCleanValue();
        String valueString = value != null ? value.name() : null;
        String cleanValueString = cleanValue != null ? cleanValue.name() : null;
        context.writeCollection(property.getName(), new PairList<>(valueString, cleanValueString), String.class);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Identifiable> void writeList(
            CompositeSerializationContext context, TrackingAsyncListProperty<T> property, Class<T> type) {
        try (LockedList<T> list = property.lock();
            LockedList<T> cleanList = property.getCleanValue().lock()) {
            context.writeCollection(property.getName(), new PairList(list, cleanList), type);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void writeList(
            CompositeSerializationContext context,
            TrackingAsyncObjectProperty<AsyncObservableList<T>> property,
            Class<T> type) {
        try (LockedList<T> list = property.get().lock();
            LockedList<T> cleanList = property.getCleanValue().lock()) {
            context.writeCollection(property.getName(), new PairList(list, cleanList), type);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Identifiable> void writePolymorphicList(
            CompositeSerializationContext context, TrackingAsyncListProperty<T> property, Class<T> type) {
        try (LockedList<T> list = property.lock();
            LockedList<T> cleanList = property.getCleanValue().lock()) {
            context.writePolymorphicCollection(property.getName(), new PairList(list, cleanList), type);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void writePolymorphicList(
            CompositeSerializationContext context,
            TrackingAsyncObjectProperty<AsyncObservableList<T>> property,
            Class<T> type) {
        try (LockedList<T> list = property.get().lock();
            LockedList<T> cleanList = property.getCleanValue().lock()) {
            context.writePolymorphicCollection(property.getName(), new PairList(list, cleanList), type);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Identifiable> void writeSet(
            CompositeSerializationContext context, TrackingAsyncSetProperty<T> property, Class<T> type) {
        try (LockedSet<T> list = property.lock();
            LockedSet<T> cleanSet = property.getCleanValue().lock()) {
            context.writeCollection(property.getName(), new PairList(list, cleanSet), type);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void writeSet(
            CompositeSerializationContext context,
            TrackingAsyncObjectProperty<AsyncObservableSet<T>> property,
            Class<T> type) {
        try (LockedSet<T> list = property.get().lock();
            LockedSet<T> cleanSet = property.getCleanValue().lock()) {
            context.writeCollection(property.getName(), new PairList(list, cleanSet), type);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Identifiable> void writePolymorphicSet(
            CompositeSerializationContext context, TrackingAsyncSetProperty<T> property, Class<T> type) {
        try (LockedSet<T> list = property.lock();
            LockedSet<T> cleanSet = property.getCleanValue().lock()) {
            context.writePolymorphicCollection(property.getName(), new PairList(list, cleanSet), type);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void writePolymorphicSet(
            CompositeSerializationContext context,
            TrackingAsyncObjectProperty<AsyncObservableSet<T>> property,
            Class<T> type) {
        try (LockedSet<T> list = property.get().lock();
            LockedSet<T> cleanSet = property.getCleanValue().lock()) {
            context.writePolymorphicCollection(property.getName(), new PairList(list, cleanSet), type);
        }
    }

    public static void readBoolean(CompositeDeserializationContext context, TrackingAsyncBooleanProperty property) {
        PairList<Boolean> list = new PairList<>();
        context.readList(property.getName(), list, Boolean.class);
        property.init(list.get(0), list.get(1));
    }

    public static void readInteger(CompositeDeserializationContext context, TrackingAsyncIntegerProperty property) {
        PairList<Integer> list = new PairList<>();
        context.readList(property.getName(), list, Integer.class);
        property.init(list.get(0), list.get(1));
    }

    public static void readLong(CompositeDeserializationContext context, TrackingAsyncLongProperty property) {
        PairList<Long> list = new PairList<>();
        context.readList(property.getName(), list, Long.class);
        property.init(list.get(0), list.get(1));
    }

    public static void readFloat(CompositeDeserializationContext context, TrackingAsyncFloatProperty property) {
        PairList<Float> list = new PairList<>();
        context.readList(property.getName(), list, Float.class);
        property.init(list.get(0), list.get(1));
    }

    public static void readDouble(CompositeDeserializationContext context, TrackingAsyncDoubleProperty property) {
        PairList<Double> list = new PairList<>();
        context.readList(property.getName(), list, Double.class);
        property.init(list.get(0), list.get(1));
    }

    public static void readString(CompositeDeserializationContext context, TrackingAsyncStringProperty property) {
        PairList<String> list = new PairList<>();
        context.readList(property.getName(), list, String.class);
        property.init(list.get(0), list.get(1));
    }

    public static <E extends Enum<E>> void readEnum(
            CompositeDeserializationContext context, TrackingAsyncObjectProperty<E> property, Class<E> type) {
        PairList<E> list = new PairList<>();
        context.readList(property.getName(), list, type);
        property.init(list.get(0), list.get(1));
    }

    public static void readOffsetDateTime(
            CompositeDeserializationContext context, TrackingAsyncObjectProperty<OffsetDateTime> property) {
        PairList<OffsetDateTime> list = new PairList<>();
        context.readList(property.getName(), list, OffsetDateTime.class);
        property.init(list.get(0), list.get(1));
    }

    public static <T extends Serializable> void readObject(
            CompositeDeserializationContext context, TrackingAsyncObjectProperty<T> property, Class<T> type) {
        PairList<T> list = new PairList<>();
        context.readList(property.getName(), list, type);
        property.init(list.get(0), list.get(1));
    }

    @SafeVarargs
    public static <T extends Serializable> void readPolymorphicObject(
            CompositeDeserializationContext context,
            TrackingAsyncObjectProperty<T> property,
            Class<T> type,
            Class<? extends T>... potentialTypes) {
        PairList<T> list = new PairList<>();
        context.readPolymorphicList(property.getName(), list, type, potentialTypes);
        property.init(list.get(0), list.get(1));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Identifiable> void readList(
            CompositeDeserializationContext context, TrackingAsyncListProperty<T> property, Class<T> type) {
        PairList list = new PairList();
        context.readList(property.getName(), list, type);
        property.init(
            FXAsyncCollections.observableList((List<T>)list.get(0)),
            FXAsyncCollections.observableList((List<T>)list.get(1)));
    }

    @SuppressWarnings("unchecked")
    public static <T> void readList(
            CompositeDeserializationContext context,
            TrackingAsyncObjectProperty<AsyncObservableList<T>> property,
            Class<T> type) {
        PairList list = new PairList();
        context.readList(property.getName(), list, type);
        property.init(
            FXAsyncCollections.observableList((List<T>)list.get(0)),
            FXAsyncCollections.observableList((List<T>)list.get(1)));
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T extends Identifiable> void readPolymorphicList(
            CompositeDeserializationContext context,
            TrackingAsyncListProperty<T> property,
            Class<T> type,
            Class<? extends T>... potentialTypes) {
        PairList list = new PairList();
        context.readPolymorphicList(property.getName(), list, type, potentialTypes);
        property.init(
            FXAsyncCollections.observableList((List<T>)list.get(0)),
            FXAsyncCollections.observableList((List<T>)list.get(1)));
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> void readPolymorphicList(
            CompositeDeserializationContext context,
            TrackingAsyncObjectProperty<AsyncObservableList<T>> property,
            Class<T> type,
            Class<? extends T>... potentialTypes) {
        PairList list = new PairList();
        context.readPolymorphicList(property.getName(), list, type, potentialTypes);
        property.init(
            FXAsyncCollections.observableList((List<T>)list.get(0)),
            FXAsyncCollections.observableList((List<T>)list.get(1)));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Identifiable> void readSet(
            CompositeDeserializationContext context, TrackingAsyncSetProperty<T> property, Class<T> type) {
        PairSet set = new PairSet();
        context.readSet(property.getName(), set, type);
        property.init(
            FXAsyncCollections.observableSet((Set<T>)set.get(0)), FXAsyncCollections.observableSet((Set<T>)set.get(1)));
    }

    @SuppressWarnings("unchecked")
    public static <T> void readSet(
            CompositeDeserializationContext context,
            TrackingAsyncObjectProperty<AsyncObservableSet<T>> property,
            Class<T> type) {
        PairSet set = new PairSet();
        context.readSet(property.getName(), set, type);
        property.init(
            FXAsyncCollections.observableSet((Set<T>)set.get(0)), FXAsyncCollections.observableSet((Set<T>)set.get(1)));
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T extends Identifiable> void readPolymorphicSet(
            CompositeDeserializationContext context,
            TrackingAsyncSetProperty<T> property,
            Class<T> type,
            Class<? extends T>... potentialTypes) {
        PairSet set = new PairSet();
        context.readPolymorphicSet(property.getName(), set, type, potentialTypes);
        property.init(
            FXAsyncCollections.observableSet((Set<T>)set.get(0)), FXAsyncCollections.observableSet((Set<T>)set.get(1)));
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> void readPolymorphicSet(
            CompositeDeserializationContext context,
            TrackingAsyncObjectProperty<AsyncObservableSet<T>> property,
            Class<T> type,
            Class<? extends T>... potentialTypes) {
        PairSet set = new PairSet();
        context.readPolymorphicSet(property.getName(), set, type, potentialTypes);
        property.init(
            FXAsyncCollections.observableSet((Set<T>)set.get(0)), FXAsyncCollections.observableSet((Set<T>)set.get(1)));
    }

}
