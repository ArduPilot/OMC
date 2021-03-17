/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

import com.google.common.collect.AbstractIterator;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.AngularSpeed;
import com.intel.missioncontrol.measure.Dimension.Area;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Percentage;
import com.intel.missioncontrol.measure.Dimension.Speed;
import com.intel.missioncontrol.measure.Dimension.Storage;
import com.intel.missioncontrol.measure.Dimension.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Unit<Q extends Quantity<Q>> {

    private static final Map<Dimension, List<Unit<? extends Quantity<?>>>> knownUnits = new HashMap<>();
    private static final Map<Class<? extends Quantity<?>>, Dimension> knownDimensions = new HashMap<>();

    public static final Unit<Length> MILLIMETER =
        new Unit<>(0.001, DoubleQuantity.class, Length.class, Dimension.LENGTH);
    public static final Unit<Length> CENTIMETER =
        new Unit<>(0.01, DoubleQuantity.class, Length.class, Dimension.LENGTH);
    public static final Unit<Length> DECIMETER = new Unit<>(0.1, DoubleQuantity.class, Length.class, Dimension.LENGTH);
    public static final Unit<Length> METER = new Unit<>(1, DoubleQuantity.class, Length.class, Dimension.LENGTH);
    public static final Unit<Length> DECAMETER = new Unit<>(10, DoubleQuantity.class, Length.class, Dimension.LENGTH);
    public static final Unit<Length> HECTOMETER = new Unit<>(100, DoubleQuantity.class, Length.class, Dimension.LENGTH);
    public static final Unit<Length> KILOMETER = new Unit<>(1000, DoubleQuantity.class, Length.class, Dimension.LENGTH);
    public static final Unit<Length> INCH = new Unit<>(0.0254, DoubleQuantity.class, Length.class, Dimension.LENGTH);
    public static final Unit<Length> FOOT = new Unit<>(0.3048, DoubleQuantity.class, Length.class, Dimension.LENGTH);
    public static final Unit<Length> US_SURVEY_FOOT =
        new Unit<>(1200. / 3937., DoubleQuantity.class, Length.class, Dimension.LENGTH);
    public static final Unit<Length> CLARKES_FOOT =
        new Unit<>(0.3047972654, DoubleQuantity.class, Length.class, Dimension.LENGTH);
    public static final Unit<Length> YARD = new Unit<>(0.9144, DoubleQuantity.class, Length.class, Dimension.LENGTH);
    public static final Unit<Length> MILE = new Unit<>(1609.344, DoubleQuantity.class, Length.class, Dimension.LENGTH);

    public static final Unit<Area> SQUARE_MILLIMETER =
        new Unit<>(0.000001, DoubleQuantity.class, Area.class, Dimension.AREA);
    public static final Unit<Area> SQUARE_CENTIMETER =
        new Unit<>(0.0001, DoubleQuantity.class, Area.class, Dimension.AREA);
    public static final Unit<Area> SQUARE_METER = new Unit<>(1, DoubleQuantity.class, Area.class, Dimension.AREA);
    public static final Unit<Area> SQUARE_KILOMETER =
        new Unit<>(1000000, DoubleQuantity.class, Area.class, Dimension.AREA);
    public static final Unit<Area> SQUARE_INCH =
        new Unit<>(0.00064516, DoubleQuantity.class, Area.class, Dimension.AREA);
    public static final Unit<Area> SQUARE_FOOT =
        new Unit<>(0.09290304, DoubleQuantity.class, Area.class, Dimension.AREA);
    public static final Unit<Area> SQUARE_YARD =
        new Unit<>(0.83612736, DoubleQuantity.class, Area.class, Dimension.AREA);
    public static final Unit<Area> SQUARE_MILE =
        new Unit<>(2589988.110336, DoubleQuantity.class, Area.class, Dimension.AREA);
    public static final Unit<Area> ACRE = new Unit<>(4046.8564224, DoubleQuantity.class, Area.class, Dimension.AREA);
    public static final Unit<Area> ARE = new Unit<>(100, DoubleQuantity.class, Area.class, Dimension.AREA);
    public static final Unit<Area> DECARE = new Unit<>(1000, DoubleQuantity.class, Area.class, Dimension.AREA);
    public static final Unit<Area> HECTARE = new Unit<>(10000, DoubleQuantity.class, Area.class, Dimension.AREA);

    public static final Unit<Time> MICROSECOND = new Unit<>(0.000001, DoubleQuantity.class, Time.class, Dimension.TIME);
    public static final Unit<Time> MILLISECOND = new Unit<>(0.001, DoubleQuantity.class, Time.class, Dimension.TIME);
    public static final Unit<Time> SECOND = new Unit<>(1.0, DoubleQuantity.class, Time.class, Dimension.TIME);
    public static final Unit<Time> MINUTE = new Unit<>(60.0, DoubleQuantity.class, Time.class, Dimension.TIME);
    public static final Unit<Time> HOUR = new Unit<>(3600.0, DoubleQuantity.class, Time.class, Dimension.TIME);
    public static final Unit<Time> DAY = new Unit<>(84600.0, DoubleQuantity.class, Time.class, Dimension.TIME);

    public static final Unit<Angle> MILLIARCSECOND =
        new Unit<>(Math.PI / 648000000, DoubleQuantity.class, Angle.class, Dimension.ANGLE);
    public static final Unit<Angle> ARCSECOND =
        new Unit<>(Math.PI / 648000, DoubleQuantity.class, Angle.class, Dimension.ANGLE);
    public static final Unit<Angle> ARCMINUTE =
        new Unit<>(Math.PI / 10800, DoubleQuantity.class, Angle.class, Dimension.ANGLE);
    public static final Unit<Angle> DEGREE =
        new Unit<>(Math.PI / 180.0, DoubleQuantity.class, Angle.class, Dimension.ANGLE);
    public static final Unit<Angle> RADIAN = new Unit<>(1.0, DoubleQuantity.class, Angle.class, Dimension.ANGLE);

    public static final Unit<Speed> METER_PER_SECOND =
        new Unit<>(1.0, DoubleQuantity.class, Speed.class, Dimension.SPEED);
    public static final Unit<Speed> FOOT_PER_SECOND =
        new Unit<>(0.3048, DoubleQuantity.class, Speed.class, Dimension.SPEED);
    public static final Unit<Speed> KILOMETER_PER_HOUR =
        new Unit<>(1. / 3.6, DoubleQuantity.class, Speed.class, Dimension.SPEED);
    public static final Unit<Speed> MILE_PER_HOUR =
        new Unit<>(0.44704, DoubleQuantity.class, Speed.class, Dimension.SPEED);
    public static final Unit<Speed> KNOT =
        new Unit<>(0.514444444444444, DoubleQuantity.class, Speed.class, Dimension.SPEED);

    public static final Unit<AngularSpeed> RADIAN_PER_SECOND =
        new Unit<>(1.0, DoubleQuantity.class, AngularSpeed.class, Dimension.ANGULAR_SPEED);
    public static final Unit<AngularSpeed> RADIAN_PER_MINUTE =
        new Unit<>(0.01666666666667, DoubleQuantity.class, AngularSpeed.class, Dimension.ANGULAR_SPEED);
    public static final Unit<AngularSpeed> RADIAN_PER_HOUR =
        new Unit<>(2.77777777777778E-4, DoubleQuantity.class, AngularSpeed.class, Dimension.ANGULAR_SPEED);
    public static final Unit<AngularSpeed> DEGREE_PER_SECOND =
        new Unit<>(57.2957795130823, DoubleQuantity.class, AngularSpeed.class, Dimension.ANGULAR_SPEED);
    public static final Unit<AngularSpeed> DEGREE_PER_MINUTE =
        new Unit<>(0.954929658551372, DoubleQuantity.class, AngularSpeed.class, Dimension.ANGULAR_SPEED);
    public static final Unit<AngularSpeed> DEGREE_PER_HOUR =
        new Unit<>(0.015915494309189, DoubleQuantity.class, AngularSpeed.class, Dimension.ANGULAR_SPEED);

    public static final Unit<Storage> BYTE = new Unit<>(1.0, LongBaseQuantity.class, Storage.class, Dimension.STORAGE);
    public static final Unit<Storage> KILOBYTE =
        new Unit<>(1024.0, LongBaseQuantity.class, Storage.class, Dimension.STORAGE);
    public static final Unit<Storage> MEGABYTE =
        new Unit<>(1048576.0, LongBaseQuantity.class, Storage.class, Dimension.STORAGE);
    public static final Unit<Storage> GIGABYTE =
        new Unit<>(1073741824.0, LongBaseQuantity.class, Storage.class, Dimension.STORAGE);
    public static final Unit<Storage> TERABYTE =
        new Unit<>(1099511627776.0, LongBaseQuantity.class, Storage.class, Dimension.STORAGE);
    public static final Unit<Storage> KIBIBYTE =
        new Unit<>(1000.0, LongBaseQuantity.class, Storage.class, Dimension.STORAGE);
    public static final Unit<Storage> MEBIBYTE =
        new Unit<>(1.0e6, LongBaseQuantity.class, Storage.class, Dimension.STORAGE);
    public static final Unit<Storage> GIBIBYTE =
        new Unit<>(1.0e9, LongBaseQuantity.class, Storage.class, Dimension.STORAGE);
    public static final Unit<Storage> TEBIBYTE =
        new Unit<>(1.0e12, LongBaseQuantity.class, Storage.class, Dimension.STORAGE);

    public static final Unit<Percentage> PERCENTAGE =
        new Unit<>(0.01, DoubleQuantity.class, Percentage.class, Dimension.PERCENTAGE);

    public static final Unit<Percentage> FACTOR =
            new Unit<>(1.0, DoubleQuantity.class, Percentage.class, Dimension.PERCENTAGE);

    static {
        MILLIMETER.addSymbol("mm");
        CENTIMETER.addSymbol("cm");
        DECIMETER.addSymbol("dm");
        METER.addSymbol("m");
        DECAMETER.addSymbol("dam");
        HECTOMETER.addSymbol("hm");
        KILOMETER.addSymbol("km");
        INCH.addSymbol("\u2033").addSymbol("\"").addSymbol("in");
        INCH.addSymbol(Locale.ENGLISH, "in");
        FOOT.addSymbol("\u2032").addSymbol("'").addSymbol("ft");
        FOOT.addSymbol(Locale.ENGLISH, "ft");
        US_SURVEY_FOOT.addSymbol("\u2032").addSymbol("'").addSymbol("ft").addSymbol("US survey foot");
        US_SURVEY_FOOT.addSymbol(Locale.ENGLISH, "ft");
        CLARKES_FOOT.addSymbol("\u2032").addSymbol("'").addSymbol("ft").addSymbol("Clarke's foot");
        CLARKES_FOOT.addSymbol(Locale.ENGLISH, "ft");
        YARD.addSymbol("yd");
        MILE.addSymbol("mi");

        SQUARE_MILLIMETER.addSymbol("mm²").addSymbol("mm^2").addSymbol("sq mm").addSymbol("sqmm");
        SQUARE_CENTIMETER.addSymbol("cm²").addSymbol("cm^2").addSymbol("sq cm").addSymbol("sqcm");
        SQUARE_METER.addSymbol("m²").addSymbol("m^2").addSymbol("sq m").addSymbol("sqm");
        SQUARE_KILOMETER.addSymbol("km²").addSymbol("km^2").addSymbol("sq km").addSymbol("sqkm");
        SQUARE_INCH.addSymbol("in²").addSymbol("in^2").addSymbol("sq in").addSymbol("sqin");
        SQUARE_FOOT.addSymbol("ft²").addSymbol("ft^2").addSymbol("sq ft").addSymbol("sqft");
        SQUARE_YARD.addSymbol("yd²").addSymbol("yd^2").addSymbol("sq yd").addSymbol("sqyd");
        SQUARE_MILE.addSymbol("mi²").addSymbol("mi^2").addSymbol("sq mi").addSymbol("sqmi");
        ACRE.addSymbol("ac");
        ARE.addSymbol("a");
        DECARE.addSymbol("daa");
        HECTARE.addSymbol("ha");

        MICROSECOND.addSymbol("µs").addSymbol("µsec");
        MILLISECOND.addSymbol("ms").addSymbol("msec");
        SECOND.addSymbol("s").addSymbol("sec");
        MINUTE.addSymbol("min");
        HOUR.addSymbol("h");
        DAY.addSymbol("d");

        MILLIARCSECOND.addSymbol("mas");
        ARCSECOND
            .addSymbol("\u2033", false)
            .addSymbol("\"", false)
            .addSymbol("as")
            .addSymbol("asec")
            .addSymbol("arcsec")
            .addSymbol("sec")
            .addSymbol("s");
        ARCMINUTE
            .addSymbol("\u2032", false)
            .addSymbol("'", false)
            .addSymbol("am")
            .addSymbol("amin")
            .addSymbol("arcmin")
            .addSymbol("min");
        DEGREE.addSymbol("°", false).addSymbol("deg");
        RADIAN.addSymbol("rad").addSymbol("r");

        METER_PER_SECOND.addSymbol("m/s");
        KILOMETER_PER_HOUR.addSymbol("km/h");
        KILOMETER_PER_HOUR.addSymbol(Locale.ENGLISH, "kph");
        MILE_PER_HOUR.addSymbol("mi/h");
        MILE_PER_HOUR.addSymbol(Locale.ENGLISH, "mph");
        KNOT.addSymbol("kn").addSymbol("kt");
        FOOT_PER_SECOND.addSymbol("ft/s");

        RADIAN_PER_SECOND.addSymbol("rad/s").addSymbol("r/s");
        RADIAN_PER_MINUTE.addSymbol("rad/min").addSymbol("r/min");
        RADIAN_PER_HOUR.addSymbol("rad/h").addSymbol("r/h");
        DEGREE_PER_SECOND.addSymbol("°/s", false).addSymbol("deg/s");
        DEGREE_PER_MINUTE.addSymbol("°/min", false).addSymbol("deg/min");
        DEGREE_PER_HOUR.addSymbol("°/h", false).addSymbol("deg/h");

        BYTE.addSymbol("B").addSymbol("byte").addSymbol("bytes");
        KILOBYTE.addSymbol("KB").addSymbol("kilobyte").addSymbol("kilobytes");
        MEGABYTE.addSymbol("MB").addSymbol("megabyte").addSymbol("megabytes");
        GIGABYTE.addSymbol("GB").addSymbol("gigabyte").addSymbol("gigabytes");
        TERABYTE.addSymbol("TB").addSymbol("terabyte").addSymbol("terabytes");
        KIBIBYTE.addSymbol("TiB").addSymbol("tibibtyte").addSymbol("tibibytes");
        MEBIBYTE.addSymbol("MiB").addSymbol("mebibyte").addSymbol("mebibytes");
        GIBIBYTE.addSymbol("GiB").addSymbol("gibibyte").addSymbol("gibibytes");
        TEBIBYTE.addSymbol("TiB").addSymbol("tebibyte").addSymbol("tebibytes");

        PERCENTAGE.addSymbol("%", false);
        FACTOR.addSymbol("x", false);
    }

    public static class Symbol {
        private final String text;
        private final boolean interveningSpace;

        Symbol(String text, boolean interveningSpace) {
            this.text = text;
            this.interveningSpace = interveningSpace;
        }

        public String getText() {
            return text;
        }

        boolean hasInterveningSpace() {
            return interveningSpace;
        }
    }

    private final List<Symbol> neutralSymbols = new ArrayList<>();
    private final Map<Locale, List<Symbol>> localizedSymbols = new HashMap<>();
    private final double conversionFactor;
    private final Class<? extends Quantity> implClass;
    private final Dimension dimension;

    private Unit(
            double conversionFactor,
            Class<? extends Quantity> implClass,
            Class<Q> dimensionClass,
            Dimension dimension) {
        knownDimensions.put(dimensionClass, dimension);

        List<Unit<? extends Quantity<?>>> list = knownUnits.get(dimension);
        if (list == null) {
            knownUnits.put(dimension, list = new ArrayList<>());
        }

        list.add(this);
        this.conversionFactor = conversionFactor;
        this.implClass = implClass;
        this.dimension = dimension;
    }

    private Unit<Q> addSymbol(String symbol) {
        return addSymbol(symbol, true);
    }

    private Unit<Q> addSymbol(String symbol, boolean interveningSpace) {
        neutralSymbols.add(new Symbol(symbol, interveningSpace));
        return this;
    }

    private Unit<Q> addSymbol(Locale locale, String symbol) {
        return addSymbol(locale, symbol, true);
    }

    private Unit<Q> addSymbol(Locale locale, String symbol, boolean interveningSpace) {
        List<Symbol> list = localizedSymbols.get(locale);
        if (list == null) {
            localizedSymbols.put(locale, list = new ArrayList<>());
        }

        list.add(new Symbol(symbol, interveningSpace));
        return this;
    }

    double convertToBase(double value) {
        return value * this.conversionFactor;
    }

    double convertFromBase(double value) {
        return value / this.conversionFactor;
    }

    Class<? extends Quantity> getImplClass() {
        return implClass;
    }

    public Symbol getDisplaySymbol() {
        List<Symbol> localizedSymbols = this.localizedSymbols.get(Locale.getDefault());
        if (localizedSymbols != null && !localizedSymbols.isEmpty()) {
            return localizedSymbols.get(0);
        }

        return neutralSymbols.get(0);
    }

    public List<Symbol> getNeutralSymbols() {
        return neutralSymbols;
    }

    public List<Symbol> getLocalizedSymbols() {
        List<Symbol> list = localizedSymbols.get(Locale.getDefault());
        return list == null ? Collections.emptyList() : list;
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public static <Q extends Quantity<Q>> Dimension getDimension(Class<Q> dimensionClass) {
        return knownDimensions.get(dimensionClass);
    }

    /** Gets all units of the specified dimension. */
    @SuppressWarnings("unchecked")
    public static Unit<? extends Quantity<?>>[] getUnits(Dimension dimension) {
        List<Unit<? extends Quantity<?>>> units = knownUnits.get(dimension);
        if (units == null) {
            throw new IllegalArgumentException("Unknown dimension: " + dimension.toString());
        }

        return units.toArray(new Unit[0]);
    }

    /** Gets all units of the specified dimension. */
    @SuppressWarnings("unchecked")
    public static <Q extends Quantity<Q>> Unit<Q>[] getUnits(Class<Q> dimensionClass) {
        Dimension dimension = knownDimensions.get(dimensionClass);
        if (dimension == null) {
            throw new IllegalArgumentException("Unknown dimension: " + dimensionClass.getName());
        }

        List<Unit<? extends Quantity<?>>> units = knownUnits.get(dimension);
        if (units == null) {
            throw new IllegalArgumentException("Unknown dimension: " + dimensionClass.getName());
        }

        return units.toArray(new Unit[0]);
    }

    /**
     * Gets all units that have the specified unit symbol.
     *
     * @param symbol The unit symbol, e.g. "mm", "m²", "s"...
     */
    public static Unit<? extends Quantity<?>>[] parseSymbol(String symbol) {
        return parseSymbolInternal(symbol, new DimensionIterable(Dimension.values()));
    }

    /**
     * Gets all units that have the specified unit symbol.
     *
     * @param symbol The unit symbol, e.g. "mm", "m²", "s"...
     * @param dimensionClasses The dimensions that will be considered, i.e. a filter for the result set.
     */
    @SafeVarargs
    public static Unit<? extends Quantity<?>>[] parseSymbol(
            String symbol, Class<? extends Quantity<?>>... dimensionClasses) {
        return parseSymbolInternal(symbol, new DimensionIterable(dimensionClasses));
    }

    /**
     * Gets all units that have the specified unit symbol.
     *
     * @param symbol The unit symbol, e.g. "mm", "m²", "s"...
     * @param dimensions The dimensions that will be considered, i.e. a filter for the result set.
     */
    public static Unit<? extends Quantity<?>>[] parseSymbol(String symbol, Dimension... dimensions) {
        return parseSymbolInternal(symbol, new DimensionIterable(dimensions));
    }

    /**
     * Gets all units that have the specified unit symbol.
     *
     * @param symbol The unit symbol, e.g. "mm", "m²", "s"...
     * @param dimensions The dimensions that will be considered, i.e. a filter for the result set.
     */
    public static Unit<? extends Quantity<?>>[] parseSymbol(String symbol, Iterable<Dimension> dimensions) {
        return parseSymbolInternal(symbol, dimensions);
    }

    /**
     * Gets all units that have the specified unit symbol.
     *
     * @param symbol The unit symbol, e.g. "mm", "m²", "s"...
     * @param dimensions The dimensions that will be considered, i.e. a filter for the result set.
     */
    public static Unit<? extends Quantity<?>>[] parseSymbol(String symbol, Collection<Dimension> dimensions) {
        return parseSymbolInternal(symbol, dimensions);
    }

    /**
     * Gets the unit that has the specified unit symbol and dimension.
     *
     * @param symbol The unit symbol, e.g. "mm", "m²", "s"...
     * @param dimensionClass The dimension class of the unit, e.g. Length.class, Area.class...
     * @throws IllegalArgumentException Thrown when the unit symbol is not found.
     */
    @SuppressWarnings("unchecked")
    public static <Q extends Quantity<Q>> Unit<Q> parseSymbol(String symbol, Class<Q> dimensionClass) {
        var iterable = new DimensionIterable(new Dimension[] {knownDimensions.get(dimensionClass)});
        return (Unit<Q>)parseSymbolInternal(symbol, iterable)[0];
    }

    @SuppressWarnings("unchecked")
    private static Unit<? extends Quantity<?>>[] parseSymbolInternal(
            String symbolText, Iterable<Dimension> dimensions) {
        if (symbolText.isEmpty()) {
            throw new IllegalArgumentException("No unit symbol specified.");
        }

        List<Unit<? extends Quantity<?>>> result = new ArrayList<>();
        Locale defaultLocale = Locale.getDefault();

        for (Dimension dimension : dimensions) {
            List<Unit<? extends Quantity<?>>> units = knownUnits.get(dimension);
            if (units == null) {
                continue;
            }

            Unit<? extends Quantity<?>> detectedUnit = null;

            for (Unit<? extends Quantity<?>> unit : units) {
                if (unit == null) {
                    continue;
                }

                List<Symbol> localizedSymbols = unit.localizedSymbols.get(defaultLocale);
                if (localizedSymbols != null) {
                    for (Symbol symbol : localizedSymbols) {
                        if (symbol.text.equals(symbolText)) {
                            detectedUnit = unit;
                            break;
                        } else if (detectedUnit == null && symbol.text.equalsIgnoreCase(symbolText)) {
                            detectedUnit = unit;
                            break;
                        }
                    }
                }

                for (Symbol symbol : unit.neutralSymbols) {
                    if (symbol.text.equals(symbolText)) {
                        detectedUnit = unit;
                        break;
                    } else if (detectedUnit == null && symbol.text.equalsIgnoreCase(symbolText)) {
                        detectedUnit = unit;
                        break;
                    }
                }
            }

            if (detectedUnit != null) {
                result.add(detectedUnit);
            }
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Unknown unit symbol: " + symbolText);
        }

        return result.toArray(new Unit[0]);
    }

    @Override
    public String toString() {
        return "Unit [symbol: " + neutralSymbols.get(0).text + ", dimension: " + dimension + "]";
    }

    static class DimensionIterable implements Iterable<Dimension> {
        static class Itr extends AbstractIterator<Dimension> {
            private final Iterator<Class<? extends Quantity<?>>> iterator;

            Itr(Iterable<Class<? extends Quantity<?>>> iterable) {
                this.iterator = iterable.iterator();
            }

            @Override
            protected Dimension computeNext() {
                if (iterator.hasNext()) {
                    return knownDimensions.get(iterator.next());
                }

                endOfData();
                return null;
            }
        }

        static class ClasArrayItr extends AbstractIterator<Dimension> {
            private final Class<? extends Quantity<?>>[] array;
            private int current;

            ClasArrayItr(Class<? extends Quantity<?>>[] array) {
                this.array = array;
            }

            @Override
            protected Dimension computeNext() {
                if (current < array.length) {
                    return knownDimensions.get(array[current++]);
                }

                endOfData();
                return null;
            }
        }

        static class DimArrayItr extends AbstractIterator<Dimension> {
            private final Dimension[] array;
            private int current;

            DimArrayItr(Dimension[] array) {
                this.array = array;
            }

            @Override
            protected Dimension computeNext() {
                if (current < array.length) {
                    return array[current++];
                }

                endOfData();
                return null;
            }
        }

        private final Iterable<Class<? extends Quantity<?>>> iterable;
        private final Class<? extends Quantity<?>>[] classArray;
        private final Dimension[] dimensionArray;

        DimensionIterable(Iterable<Class<? extends Quantity<?>>> iterable) {
            this.iterable = iterable;
            this.classArray = null;
            this.dimensionArray = null;
        }

        DimensionIterable(Class<? extends Quantity<?>>[] dimensions) {
            this.iterable = null;
            this.classArray = dimensions;
            this.dimensionArray = null;
        }

        DimensionIterable(Dimension[] dimensions) {
            this.iterable = null;
            this.classArray = null;
            this.dimensionArray = dimensions;
        }

        @Override
        public Iterator<Dimension> iterator() {
            if (iterable != null) {
                return new Itr(iterable);
            }

            if (classArray != null) {
                return new ClasArrayItr(classArray);
            }

            return new DimArrayItr(dimensionArray);
        }
    }

}
