/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.serialization;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

public interface CompositeDeserializationContext extends DeserializationContext {

    boolean readBoolean(String name);

    boolean readBoolean(String name, boolean fallbackValue);

    byte readByte(String name);

    byte readByte(String name, byte fallbackValue);

    short readShort(String name);

    short readShort(String name, short fallbackValue);

    int readInteger(String name);

    int readInteger(String name, int fallbackValue);

    long readLong(String name);

    long readLong(String name, long fallbackValue);

    float readFloat(String name);

    float readFloat(String name, float fallbackValue);

    double readDouble(String name);

    double readDouble(String name, double fallbackValue);

    String readString(String name);

    String readString(String name, String fallbackValue);

    OffsetDateTime readOffsetDateTime(String name);

    OffsetDateTime readOffsetDateTime(String name, OffsetDateTime fallbackValue);

    byte[] readBytes(String name);

    byte[] readBytes(String name, byte[] fallbackValue);

    <E extends Enum<E>> E readEnum(String name, Class<E> type);

    <E extends Enum<E>> E readEnum(String name, Class<E> type, E fallbackValue);

    <T extends Serializable> T readObject(String name, Class<T> type);

    <T extends Serializable> T readObject(String name, Class<T> type, T fallbackValue);

    @SuppressWarnings("unchecked")
    <T extends Serializable> T readPolymorphicObject(
            String name, Class<T> targetType, Class<? extends T>... potentialTypes);

    @SuppressWarnings("unchecked")
    <T extends Serializable> T readPolymorphicObject(
            String name, Class<T> targetType, T fallbackValue, Class<? extends T>... potentialTypes);

    <T> void readList(String name, List<? super T> list, Class<T> targetType);

    <T> void readList(String name, List<? super T> list, Class<T> targetType, List<T> fallbackValue);

    @SuppressWarnings("unchecked")
    <T> void readPolymorphicList(
            String name, List<? super T> list, Class<T> targetType, Class<? extends T>... potentialTypes);

    @SuppressWarnings("unchecked")
    <T> void readPolymorphicList(
            String name,
            List<? super T> list,
            Class<T> targetType,
            List<T> fallbackValue,
            Class<? extends T>... potentialTypes);

    <T> void readSet(String name, Set<? super T> list, Class<T> targetType);

    <T> void readSet(String name, Set<? super T> list, Class<T> targetType, Set<T> fallbackValue);

    @SuppressWarnings("unchecked")
    <T> void readPolymorphicSet(
            String name, Set<? super T> list, Class<T> targetType, Class<? extends T>... potentialTypes);

    @SuppressWarnings("unchecked")
    <T> void readPolymorphicSet(
            String name,
            Set<? super T> list,
            Class<T> targetType,
            Set<T> fallbackValue,
            Class<? extends T>... potentialTypes);

}
