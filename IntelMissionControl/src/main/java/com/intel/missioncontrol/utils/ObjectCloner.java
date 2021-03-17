/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.StringProperty;

public class ObjectCloner {
    private static final List<MapperChainEntry> FIELDS_MAPPING_LIST = getMappingList();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> void copyValues(final T source, final T target) {
        final Field[] fields = source.getClass().getDeclaredFields();
        Stream.of(fields)
            .filter(
                field ->
                    !Modifier.isStatic(field.getModifiers())
                        && !Modifier.isTransient(field.getModifiers())
                        && !field.isSynthetic())
            .forEach(field -> copyField(field, source, target));
    }

    private static <T> void copyField(final Field field, final T source, final T target) {
        final Class<?> fieldType = field.getType();
        final boolean accessible = field.canAccess(target);
        if (!accessible) {
            field.setAccessible(true);
        }

        final Optional<MapperChainEntry> mappingEntry =
            FIELDS_MAPPING_LIST
                .stream()
                .filter(mapperChainEntry -> mapperChainEntry.isAssignable(fieldType))
                .findFirst();

        if (mappingEntry.isPresent()) {
            mappingEntry.get().duplicate(field, source, target);
        } else {
            try {
                field.set(target, field.get(source));
            } catch (IllegalAccessException ignored) {
            }
        }

        if (!accessible) {
            field.setAccessible(false);
        }
    }

    private static List<MapperChainEntry> getMappingList() {
        return Arrays.asList(
            new MapperChainEntry(
                BooleanProperty.class,
                (field, src, dst) -> {
                    final BooleanProperty sourceProperty = (BooleanProperty)field.get(src);
                    final BooleanProperty targetProperty = (BooleanProperty)field.get(dst);
                    if (sourceProperty != null && targetProperty != null) {
                        targetProperty.set(sourceProperty.get());
                    }
                }),
            new MapperChainEntry(
                IntegerProperty.class,
                (field, src, dst) -> {
                    final IntegerProperty sourceProperty = (IntegerProperty)field.get(src);
                    final IntegerProperty targetProperty = (IntegerProperty)field.get(dst);
                    if (sourceProperty != null && targetProperty != null) {
                        targetProperty.set(sourceProperty.get());
                    }
                }),
            new MapperChainEntry(
                LongProperty.class,
                (field, src, dst) -> {
                    final LongProperty sourceProperty = (LongProperty)field.get(src);
                    final LongProperty targetProperty = (LongProperty)field.get(dst);
                    if (sourceProperty != null && targetProperty != null) {
                        targetProperty.set(sourceProperty.get());
                    }
                }),
            new MapperChainEntry(
                FloatProperty.class,
                (field, src, dst) -> {
                    final FloatProperty sourceProperty = (FloatProperty)field.get(src);
                    final FloatProperty targetProperty = (FloatProperty)field.get(dst);
                    if (sourceProperty != null && targetProperty != null) {
                        targetProperty.set(sourceProperty.get());
                    }
                }),
            new MapperChainEntry(
                DoubleProperty.class,
                (field, src, dst) -> {
                    final DoubleProperty sourceProperty = (DoubleProperty)field.get(src);
                    final DoubleProperty targetProperty = (DoubleProperty)field.get(dst);
                    if (sourceProperty != null && targetProperty != null) {
                        targetProperty.set(sourceProperty.get());
                    }
                }),
            new MapperChainEntry(
                StringProperty.class,
                (field, src, dst) -> {
                    final StringProperty sourceProperty = (StringProperty)field.get(src);
                    final StringProperty targetProperty = (StringProperty)field.get(dst);
                    if (sourceProperty != null && targetProperty != null) {
                        targetProperty.set(sourceProperty.get());
                    }
                }),
            new MapperChainEntry(
                ObjectProperty.class,
                (field, src, dst) -> {
                    final ObjectProperty sourceProperty = (ObjectProperty)field.get(src);
                    final ObjectProperty targetProperty = (ObjectProperty)field.get(dst);
                    if (sourceProperty != null && targetProperty != null && sourceProperty.get() != null) {
                        targetProperty.set(sourceProperty.get());
                    }
                }),
            new MapperChainEntry(
                ListProperty.class,
                (field, src, dst) -> {
                    ListProperty sourceProperty = (ListProperty)field.get(src);
                    ListProperty targetProperty = (ListProperty)field.get(dst);
                    if (sourceProperty != null && targetProperty != null) {
                        targetProperty.clear();
                        for (Object value : sourceProperty) {
                            targetProperty.add(value);
                        }
                    }
                }),
            new MapperChainEntry(
                SetProperty.class,
                (field, src, dst) -> {
                    SetProperty sourceProperty = (SetProperty)field.get(src);
                    SetProperty targetProperty = (SetProperty)field.get(dst);
                    if (sourceProperty != null && targetProperty != null) {
                        targetProperty.clear();
                        for (Object value : sourceProperty) {
                            targetProperty.add(value);
                        }
                    }
                }),
            new MapperChainEntry(
                MapProperty.class,
                (field, src, dst) -> {
                    MapProperty sourceProperty = (MapProperty)field.get(src);
                    MapProperty targetProperty = (MapProperty)field.get(dst);
                    if (sourceProperty != null && targetProperty != null) {
                        targetProperty.clear();
                        targetProperty.putAll(sourceProperty);
                    }
                }));
    }

    private static class MapperChainEntry {
        private final Class<?> checkedType;
        private final TriConsumer<Field, Object, Object> duplicator;

        MapperChainEntry(Class<?> checkedType, TriConsumer<Field, Object, Object> duplicator) {
            this.checkedType = checkedType;
            this.duplicator = duplicator;
        }

        boolean isAssignable(Class<?> fieldType) {
            return fieldType.isAssignableFrom(checkedType);
        }

        public void duplicate(Field field, Object src, Object dst) {
            try {
                duplicator.accept(field, src, dst);
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    @FunctionalInterface
    private interface TriConsumer<T, U, K> {
        /**
         * Performs this operation on the given arguments.
         *
         * @param t the first input argument
         * @param u the second input argument
         * @param k the third input argument
         */
        void accept(T t, U u, K k) throws IllegalAccessException;
    }

}
