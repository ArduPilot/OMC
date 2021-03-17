/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.project.serialization.DeserializationContext;
import com.intel.missioncontrol.project.serialization.Serializable;
import com.intel.missioncontrol.project.serialization.SerializationContext;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Objects;

public abstract class CoordinateSystem<T> implements Serializable {

    final double x;
    final double y;
    final double z;
    private final double roll;
    private final double pitch;
    private final double yaw;
    private final boolean defined;
    private CoordinateSystem parent;

    public CoordinateSystem() {
        x = 0.0;
        y = 0.0;
        z = 0.0;
        this.roll = 0.0;
        this.pitch = 0.0;
        this.yaw = 0.0;
        this.defined = false;
    }

    public CoordinateSystem(DeserializationContext context) {
        x = context.readDouble("x");
        y = context.readDouble("y");
        z = context.readDouble("z");
        this.roll = context.readDouble("roll");
        this.pitch = context.readDouble("pitch");
        this.yaw = context.readDouble("yaw");
        this.defined = context.readBoolean("defined");
    }

    public CoordinateSystem(Vec4 point) {
        x = point.x;
        y = point.y;
        z = point.z;
        this.defined = true;
        this.roll = 0.0;
        this.pitch = 0.0;
        this.yaw = 0.0;
    }

    public CoordinateSystem(CoordinateSystem r) {
        this.x = r.x;
        this.y = r.y;
        this.z = r.z;
        this.roll = r.getRoll();
        this.pitch = r.getPitch();
        this.yaw = r.getYaw();
        this.defined = true;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getYaw() {
        return yaw;
    }

    public double getRoll() {
        return roll;
    }

    public double getPitch() {
        return pitch;
    }

    public boolean isDefined() {
        return defined;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoordinateSystem)) return false;
        if (!super.equals(o)) return false;
        CoordinateSystem that = (CoordinateSystem)o;
        return x == that.x && y == that.y && z == that.z && roll == that.roll && pitch == that.pitch && yaw == that.yaw;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), x, y, z, roll, pitch, yaw);
    }

    @Override
    public void getObjectData(SerializationContext context) {
        context.writeDouble("x", x);
        context.writeDouble("y", y);
        context.writeDouble("z", z);
        context.writeDouble("roll", roll);
        context.writeDouble("pitch", pitch);
        context.writeDouble("yaw", yaw);
        context.writeBoolean("defined", defined);
    }

    public abstract T convertToParent(Vec4 localPoint);

    public abstract Vec4 convertToLocal(T point);

}
