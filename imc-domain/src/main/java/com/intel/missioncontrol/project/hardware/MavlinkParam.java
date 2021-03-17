/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.serialization.CompositeDeserializationContext;
import com.intel.missioncontrol.serialization.CompositeSerializable;
import com.intel.missioncontrol.serialization.CompositeSerializationContext;

public class MavlinkParam implements CompositeSerializable {
    private final String id;
    private final double value;
    private final String type;

    private MavlinkParam(String id, double value, String type) {
        this.id = id;
        this.value = value;
        this.type = type;
    }

    public MavlinkParam(CompositeDeserializationContext context) {
        id = context.readString("id");
        value = context.readDouble("value");
        type = context.readString("type");
    }

    public String getId() {
        return id;
    }

    public double getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    @Override
    public void serialize(CompositeSerializationContext context) {
        throw new NotImplementedException();
    }
}
