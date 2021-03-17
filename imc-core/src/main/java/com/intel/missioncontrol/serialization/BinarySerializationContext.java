/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.serialization;

public interface BinarySerializationContext extends SerializationContext {

    void writeByte(byte value);

    void writeShort(short value);

    void writeInteger(int value);

    void writeLong(long value);

    void writeFloat(float value);

    void writeDouble(double value);

    void writeBoolean(boolean value);

    void writeString(String value);

}
