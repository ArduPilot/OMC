/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.serialization.BinarySerializationContext;
import com.intel.missioncontrol.serialization.CompositeDeserializationContext;
import com.intel.missioncontrol.serialization.CompositeSerializable;
import com.intel.missioncontrol.serialization.CompositeSerializationContext;
import com.intel.missioncontrol.serialization.PrimitiveSerializationContext;

class ConnectionProperties implements IConnectionProperties, CompositeSerializable {

    private String droneType;
    private double linkLostTimeoutSeconds;

    ConnectionProperties() {}

    public ConnectionProperties(String droneType, double linkLostTimeoutSeconds) {
        this.droneType = droneType;
        this.linkLostTimeoutSeconds = linkLostTimeoutSeconds;
    }

    public ConnectionProperties(CompositeDeserializationContext context) {
        this.droneType = context.readString("droneType");
        this.linkLostTimeoutSeconds = context.readDouble("linkLostTimeoutSeconds");
    }

    @Override
    public String getDroneType() {
        return droneType;
    }

    @Override
    public double getLinkLostTimeoutSeconds() {
        return linkLostTimeoutSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!ConnectionProperties.class.isAssignableFrom(o.getClass())) {
            return false;
        }

        final ConnectionProperties other = (ConnectionProperties)o;

        return this.linkLostTimeoutSeconds == other.linkLostTimeoutSeconds;
    }

    @Override
    public void serialize(CompositeSerializationContext context) {
        throw new NotImplementedException();
    }

    @Override
    public void serialize(PrimitiveSerializationContext context) {}

    @Override
    public void serialize(BinarySerializationContext context) {

    }
}
