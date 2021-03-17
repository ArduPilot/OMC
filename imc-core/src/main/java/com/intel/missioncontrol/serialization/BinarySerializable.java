/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.serialization;

public interface BinarySerializable extends Serializable {

    void serialize(BinarySerializationContext context);
}
