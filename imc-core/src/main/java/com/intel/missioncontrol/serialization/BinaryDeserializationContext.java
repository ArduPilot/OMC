/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.serialization;

public interface BinaryDeserializationContext extends DeserializationContext {

    byte readByte();

    short readShort();

    int readInteger();

    long readLong();

    float readFloat();

    double readDouble();

    boolean readBoolean();

    String readString();

}
