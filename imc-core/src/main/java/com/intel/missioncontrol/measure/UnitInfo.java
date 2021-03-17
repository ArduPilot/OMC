/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

import com.google.common.collect.Lists;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Area;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Percentage;
import com.intel.missioncontrol.measure.Dimension.Speed;
import com.intel.missioncontrol.measure.Dimension.Storage;
import com.intel.missioncontrol.measure.Dimension.Time;
import com.intel.missioncontrol.measure.Dimension.Voltage;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class UnitInfo<Q extends Quantity<Q>> {

    /**
     * Predefined instance that uses m/ft as its preferred unit, but supports mm, cm, km, in and mi for unit adjustment.
     */
    public static final UnitInfo<Length> LOCALIZED_LENGTH =
        new UnitInfo<>(
            Lists.newArrayList(Unit.METER, Unit.MILLIMETER, Unit.CENTIMETER, Unit.KILOMETER),
            Lists.newArrayList(Unit.FOOT, Unit.INCH, Unit.MILE),
            Lists.newArrayList(Unit.FOOT, Unit.INCH, Unit.MILE));

    public static final UnitInfo<Length> LOCALIZED_LENGTH_NO_KM =
        new UnitInfo<>(
            Lists.newArrayList(Unit.METER, Unit.MILLIMETER, Unit.CENTIMETER),
            Lists.newArrayList(Unit.FOOT, Unit.INCH),
            Lists.newArrayList(Unit.FOOT, Unit.INCH));

    public static final UnitInfo<Length> INVARIANT_LENGTH = new UnitInfo<>(Unit.METER, Unit.FOOT, Unit.FOOT);

    /**
     * Predefined instance that uses sq m/ft as its preferred unit, but supports sq mm, sq cm, sq km, sq in and sq miles
     * for unit adjustment.
     */
    public static final UnitInfo<Area> LOCALIZED_AREA =
        new UnitInfo<>(
            Lists.newArrayList(
                Unit.SQUARE_METER, Unit.SQUARE_MILLIMETER, Unit.SQUARE_CENTIMETER, Unit.HECTARE, Unit.SQUARE_KILOMETER),
            Lists.newArrayList(Unit.SQUARE_FOOT, Unit.SQUARE_INCH, Unit.ACRE, Unit.SQUARE_MILE),
            Lists.newArrayList(Unit.SQUARE_FOOT, Unit.SQUARE_INCH, Unit.ACRE, Unit.SQUARE_MILE));

    public static final UnitInfo<Speed> LOCALIZED_SPEED =
        new UnitInfo<>(
            Lists.newArrayList(Unit.METER_PER_SECOND, Unit.KILOMETER_PER_HOUR),
            Lists.newArrayList(Unit.FOOT_PER_SECOND, Unit.MILE_PER_HOUR),
            Lists.newArrayList(Unit.FOOT_PER_SECOND, Unit.MILE_PER_HOUR));

    public static final UnitInfo<Speed> INVARIANT_SPEED_MPS =
        new UnitInfo<>(Unit.METER_PER_SECOND, Unit.FOOT_PER_SECOND, Unit.KNOT);

    public static final UnitInfo<Time> TIME_MINUTES = new UnitInfo<>(Unit.MINUTE);
    public static final UnitInfo<Time> TIME_SECONDS = new UnitInfo<>(Unit.SECOND);
    public static final UnitInfo<Angle> ANGLE_DEGREES = new UnitInfo<>(Unit.DEGREE);
    public static final UnitInfo<Percentage> PERCENTAGE_FACTOR =
        new UnitInfo<>(Lists.newArrayList(Unit.PERCENTAGE, Unit.FACTOR));
    public static final UnitInfo<Percentage> PERCENTAGE = new UnitInfo<>(Unit.PERCENTAGE);
    public static final UnitInfo<Voltage> VOLTAGE = new UnitInfo<>(Unit.VOLT);

    public static final UnitInfo<Storage> STORAGE =
        new UnitInfo<>(Lists.newArrayList(Unit.BYTE, Unit.KILOBYTE, Unit.MEGABYTE, Unit.GIGABYTE, Unit.TERABYTE));

    public static final UnitInfo<Time> TIME =
        new UnitInfo<>(Lists.newArrayList(Unit.MILLISECOND, Unit.SECOND, Unit.MINUTE, Unit.HOUR));

    private final Map<SystemOfMeasurement, List<Unit<Q>>> units = new EnumMap<>(SystemOfMeasurement.class);

    /**
     * Specifies a list of units for each system of measurement. The first unit in each list is the preferred unit, the
     * other units may be used to adjust formatting output.
     */
    public UnitInfo(List<Unit<Q>> metric) {
        this(metric, metric, metric);
    }

    /**
     * Specifies a list of units for each system of measurement. The first unit in each list is the preferred unit, the
     * other units may be used to adjust formatting output.
     */
    public UnitInfo(List<Unit<Q>> metric, List<Unit<Q>> imperial, List<Unit<Q>> icao) {
        if (metric == null || metric.size() == 0) {
            throw new IllegalArgumentException("metric");
        }

        if (imperial == null || imperial.size() == 0) {
            throw new IllegalArgumentException("imperial");
        }

        if (icao == null || icao.size() == 0) {
            throw new IllegalArgumentException("icao");
        }

        units.put(SystemOfMeasurement.METRIC, Collections.unmodifiableList(metric));
        units.put(SystemOfMeasurement.IMPERIAL, Collections.unmodifiableList(imperial));
        units.put(SystemOfMeasurement.ICAO, Collections.unmodifiableList(icao));
    }

    /** Specifies the preferred unit for each system of measurement. */
    public UnitInfo(Unit<Q> metric, Unit<Q> imperial, Unit<Q> icao) {
        units.put(SystemOfMeasurement.METRIC, Collections.unmodifiableList(Lists.newArrayList(metric)));
        units.put(SystemOfMeasurement.IMPERIAL, Collections.unmodifiableList(Lists.newArrayList(imperial)));
        units.put(SystemOfMeasurement.ICAO, Collections.unmodifiableList(Lists.newArrayList(icao)));
    }

    /** Specifies the preferred unit for all systems of measurement. */
    public UnitInfo(Unit<Q> metric) {
        units.put(SystemOfMeasurement.METRIC, Collections.unmodifiableList(Lists.newArrayList(metric)));
        units.put(SystemOfMeasurement.IMPERIAL, Collections.unmodifiableList(Lists.newArrayList(metric)));
        units.put(SystemOfMeasurement.ICAO, Collections.unmodifiableList(Lists.newArrayList(metric)));
    }

    public Dimension getDimension() {
        return units.get(SystemOfMeasurement.METRIC).get(0).getDimension();
    }

    public Unit<Q> getPreferredUnit(SystemOfMeasurement systemOfMeasurement) {
        List<Unit<Q>> list = units.get(systemOfMeasurement);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }

        return units.get(SystemOfMeasurement.METRIC).get(0);
    }

    public List<Unit<Q>> getAllowedUnits(SystemOfMeasurement systemOfMeasurement) {
        return units.get(systemOfMeasurement);
    }

    @SuppressWarnings("unchecked")
    public <Q1 extends Quantity<Q1>> Unit<Q1> getUnit(
            SystemOfMeasurement systemOfMeasurement, Class<Q1> dimensionClass) {
        Unit<Q> unit = getPreferredUnit(systemOfMeasurement);
        if (unit.getDimension() != Unit.getDimension(dimensionClass)) {
            throw new IllegalArgumentException("Specified dimension does not match UnitInfo dimension.");
        }

        return (Unit<Q1>)unit;
    }

}
