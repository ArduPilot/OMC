/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.serialization;

public interface PrimitiveSerializable extends Serializable {

    void serialize(PrimitiveSerializationContext context);
}
