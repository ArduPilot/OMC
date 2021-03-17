/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.serialization;

public interface CompositeSerializable extends Serializable {

    void serialize(CompositeSerializationContext context);

}
