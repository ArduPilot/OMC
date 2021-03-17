/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import javafx.util.StringConverter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LocationConverter extends StringConverter<Location> {

    private final LocationFormat locationFormat = new LocationFormat();
    private final IQuantityStyleProvider quantityStyleProvider;
    private final UnitInfo<Length> unitInfo;

    public LocationConverter(IQuantityStyleProvider quantityStyleProvider, UnitInfo<Length> unitInfo) {
        this.quantityStyleProvider = quantityStyleProvider;
        this.unitInfo = unitInfo;
    }

    @Override
    public Location fromString(@Nullable String value) {
        if (value == null) {
            return null;
        }

        try {
            Unit<Length> unit = unitInfo.getPreferredUnit(quantityStyleProvider.getSystemOfMeasurement());
            Location location = locationFormat.parse(value);
            if (location.getXyDimension() == Dimension.LENGTH) {
                Quantity<Length> x = location.getX(Length.class).convertTo(unit);
                Quantity<Length> y = location.getY(Length.class).convertTo(unit);
                if (location.getZ().isPresent()) {
                    Quantity<Length> z = location.getZ().get().convertTo(unit);
                    return new Location(x, y, z);
                }

                return new Location(x, y);
            } else {
                Quantity<Angle> x = location.getX(Angle.class);
                Quantity<Angle> y = location.getY(Angle.class);
                if (location.getZ().isPresent()) {
                    Quantity<Length> z = location.getZ().get().convertTo(unit);
                    return new Location(x, y, z);
                }

                return new Location(x, y);
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString(@Nullable Location value) {
        if (value == null) {
            return "";
        }

        return value.toString();
    }

}
