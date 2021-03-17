/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.serialization;

import java.time.OffsetDateTime;
import java.util.Collection;

public interface CompositeSerializationContext extends SerializationContext {

    void writeBoolean(String name, boolean value);

    void writeByte(String name, byte value);

    void writeShort(String name, short value);

    void writeInteger(String name, int value);

    void writeLong(String name, long value);

    void writeFloat(String name, float value);

    void writeDouble(String name, double value);

    void writeString(String name, String value);

    void writeOffsetDateTime(String name, OffsetDateTime value);

    void writeBytes(String name, byte[] value);

    <E extends Enum<E>> void writeEnum(String name, E value);

    void writeObject(String name, Serializable value);

    void writePolymorphicObject(String name, Serializable value);

    <T> void writeCollection(String name, Collection<T> value, Class<? super T> type);

    <T> void writePolymorphicCollection(String name, Collection<T> value, Class<? super T> type);

}
