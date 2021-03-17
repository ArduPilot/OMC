/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import com.intel.missioncontrol.serialization.PrimitiveDeserializationContext;
import com.intel.missioncontrol.serialization.PrimitiveSerializable;
import com.intel.missioncontrol.serialization.PrimitiveSerializationContext;
import java.util.Objects;

public class SnapPointId implements PrimitiveSerializable {
    // Keep synchronized with platform json schema
    public static final SnapPointId BODY_ORIGIN = new SnapPointId("BODY_ORIGIN");
    public static final SnapPointId GNSS_ANTENNA = new SnapPointId("GNSS_ANTENNA");
    public static final SnapPointId WAYPOINT_POSITION_OUT = new SnapPointId("WAYPOINT_POSITION_OUT");
    public static final SnapPointId POSITION_TELEMETRY_IN = new SnapPointId("POSITION_TELEMETRY_IN");

    private final String name;

    @Override
    public void serialize(PrimitiveSerializationContext context) {
        context.write(name);
    }

    public SnapPointId(PrimitiveDeserializationContext context) {
        this(context.read());
    }

    public SnapPointId(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name: snap point id name must not be null or empty");
        }

        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "SnapPointId{" + "name='" + name + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SnapPointId that = (SnapPointId)o;

        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
