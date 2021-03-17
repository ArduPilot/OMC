/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Optional;

public class Location {
    private Quantity<?> x;
    private Quantity<?> y;
    private Optional<Quantity<Length>> z;

    public Location(Quantity<?> x, Quantity<?> y) {
        if (x.getDimension() != Dimension.LENGTH && x.getDimension() != Dimension.ANGLE) {
            throw new IllegalArgumentException("X coordinate must have dimension LENGTH or ANGLE.");
        }

        if (y.getDimension() != Dimension.LENGTH && y.getDimension() != Dimension.ANGLE) {
            throw new IllegalArgumentException("Y coordinate must have dimension LENGTH or ANGLE.");
        }

        if (x.getDimension() != y.getDimension()) {
            throw new IllegalArgumentException("X and Y coordinates must have the same dimension.");
        }

        this.x = x;
        this.y = y;
        this.z = Optional.empty();
    }

    public Location(LatLon latLon) {
        this.x = Quantity.of(latLon.latitude.degrees, Unit.DEGREE);
        this.y = Quantity.of(latLon.longitude.degrees, Unit.DEGREE);
        this.z = Optional.empty();
    }

    public Location(Position position) {
        this.x = Quantity.of(position.latitude.degrees, Unit.DEGREE);
        this.y = Quantity.of(position.longitude.degrees, Unit.DEGREE);
        this.z = Optional.of(Quantity.of(position.elevation, Unit.METER));
    }

    public Location(Vec4 vec4) {
        this.x = Quantity.of(vec4.x, Unit.METER);
        this.y = Quantity.of(vec4.y, Unit.DEGREE);
        this.z = Optional.of(Quantity.of(vec4.z, Unit.METER));
    }

    public Location(Quantity<?> x, Quantity<?> y, Quantity<Length> z) {
        this(x, y);
        this.z = Optional.of(z);
    }

    public Quantity<?> getX() {
        return x;
    }

    @SuppressWarnings("unchecked")
    public <Q extends Quantity<Q>> Quantity<Q> getX(Class<Q> dimensionClass) {
        Dimension dimension = Unit.getDimension(dimensionClass);
        if (x.getUnit().getDimension() != dimension) {
            throw new IllegalArgumentException(
                "Invalid dimension. Requested: " + dimension + ", Actual: " + x.getUnit().getDimension());
        }

        return (Quantity<Q>)x;
    }

    public Quantity<?> getY() {
        return y;
    }

    @SuppressWarnings("unchecked")
    public <Q extends Quantity<Q>> Quantity<Q> getY(Class<Q> dimensionClass) {
        Dimension dimension = Unit.getDimension(dimensionClass);
        if (y.getUnit().getDimension() != dimension) {
            throw new IllegalArgumentException(
                "Invalid dimension. Requested: " + dimension + ", Actual: " + x.getUnit().getDimension());
        }

        return (Quantity<Q>)y;
    }

    public Optional<Quantity<Length>> getZ() {
        return this.z;
    }

    public Dimension getXyDimension() {
        return x.getDimension();
    }

    public <Q extends Quantity<Q>> void setXy(Quantity<Q> x, Quantity<Q> y) {
        if (x.getDimension() != Dimension.LENGTH && x.getDimension() != Dimension.ANGLE) {
            throw new IllegalArgumentException("Values must be of dimension LENGTH or ANGLE.");
        }

        this.x = x;
        this.y = y;
    }

    public void setZ(Quantity<Length> value) {
        this.z = Optional.of(value);
    }

    public Position toPosition() {
        if (getXyDimension() == Dimension.ANGLE) {
            double lat = getX(Angle.class).convertTo(Unit.RADIAN).getValue().doubleValue();
            double lon = getY(Angle.class).convertTo(Unit.RADIAN).getValue().doubleValue();
            if (z.isPresent()) {
                double alt = z.get().convertTo(Unit.METER).getValue().doubleValue();
                return Position.fromRadians(lat, lon, alt);
            }

            return Position.fromRadians(lat, lon, 0);
        }

        throw new IllegalStateException("The location cannot be converted to a position.");
    }

    public Vec4 toVec4() {
        if (getXyDimension() == Dimension.LENGTH) {
            double x = getX(Length.class).convertTo(Unit.METER).getValue().doubleValue();
            double y = getY(Length.class).convertTo(Unit.METER).getValue().doubleValue();
            if (z.isPresent()) {
                double alt = z.get().convertTo(Unit.METER).getValue().doubleValue();
                return new Vec4(x, y, alt);
            }

            return new Vec4(x, y);
        }

        throw new IllegalStateException("The location cannot be converted to a vec4.");
    }

    @Override
    public String toString() {
        return new LocationFormat(AngleStyle.DECIMAL_DEGREES).format(this);
    }

}
