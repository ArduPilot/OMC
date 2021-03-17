/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LocationFormat {

    private static class Symbols {
        final String N;
        final String S;
        final String E;
        final String W;

        Symbols(String n, String s, String e, String w) {
            this.N = n;
            this.S = s;
            this.E = e;
            this.W = w;
        }
    }

    private static final Symbols symbols;

    static {
        ResourceBundle resourceBundle =
            ResourceBundle.getBundle(
                "com/intel/missioncontrol/IntelMissionControl", Locale.getDefault());
        String className = LocationFormat.class.getName();
        String n = resourceBundle.getString(className + ".N");
        String s = resourceBundle.getString(className + ".S");
        String e = resourceBundle.getString(className + ".E");
        String w = resourceBundle.getString(className + ".W");
        symbols = new Symbols(n, s, e, w);
    }

    private QuantityFormat quantityFormat = new QuantityFormat();

    private int getFractionDigits(AngleStyle angleStyle) {
        switch (angleStyle) {
        case DEGREE_MINUTE_SECOND:
            return 2;
        case DEGREE_DECIMAL_MINUTE:
            return 4;
        default:
            return 6;
        }
    }

    public LocationFormat() {
        this.quantityFormat.setAngleStyle(AngleStyle.DEGREE_MINUTE_SECOND);
        this.quantityFormat.setSignificantDigits(16);
        this.quantityFormat.setMaximumFractionDigits(2);
    }

    public LocationFormat(AngleStyle angleStyle) {
        this.quantityFormat.setAngleStyle(angleStyle);
        this.quantityFormat.setSignificantDigits(16);
        this.quantityFormat.setMaximumFractionDigits(2);
    }

    public void setAngleStyle(AngleStyle value) {
        this.quantityFormat.setAngleStyle(value);
    }

    public AngleStyle getAngleStyle() {
        return this.quantityFormat.getAngleStyle();
    }

    public String[] splitFormat(Location location) {
        var angleX = location.getX(Angle.class);
        var angleY = location.getY(Angle.class);
        boolean flipX = angleX.getValue().doubleValue() < 0;
        boolean flipY = angleY.getValue().doubleValue() < 0;
        int angleDigits = getFractionDigits(getAngleStyle());
        int prevDigits = quantityFormat.getMaximumFractionDigits();
        quantityFormat.setMaximumFractionDigits(angleDigits);

        String lat, lon;
        try {
            lat =
                quantityFormat.format(flipX ? Quantity.of(-angleX.getValue().doubleValue(), angleX.getUnit()) : angleX)
                    + " "
                    + (flipX ? symbols.S : symbols.N);

            lon =
                quantityFormat.format(flipY ? Quantity.of(-angleY.getValue().doubleValue(), angleY.getUnit()) : angleY)
                    + " "
                    + (flipY ? symbols.W : symbols.E);

        } finally {
            quantityFormat.setMaximumFractionDigits(prevDigits);
        }

        if (location.getZ().isPresent()) {
            String alt = quantityFormat.format(location.getZ().get());
            return new String[] {lat, lon, alt};
        }

        return new String[] {lat, lon};
    }

    public String format(Location location) {
        String[] parts = splitFormat(location);
        if (parts.length == 3) {
            return parts[0] + " " + parts[1] + " " + parts[2];
        }

        return parts[0] + " " + parts[1];
    }

    public Location parse(String input) {
        Expect.notNull(input, "input");
        Matcher matcher = threeComponentsDegMinSec.matcher(input);
        if (matcher.matches()) {
            String dir1 = matcher.group(4);
            String dir2 = matcher.group(8);
            Quantity<Angle> coord1 = makeAngle(matcher.group(1), matcher.group(2), matcher.group(3));
            Quantity<Angle> coord2 = makeAngle(matcher.group(5), matcher.group(6), matcher.group(7));
            Quantity<Length> alt = quantityFormat.parse(matcher.group(9), Unit.METER);
            return (symbols.W.equals(dir1) || symbols.N.equals(dir2))
                ? new Location(coord2, coord1, alt)
                : new Location(coord1, coord2, alt);
        }

        matcher = threeComponentsDegDecimalMin.matcher(input);
        if (matcher.matches()) {
            String dir1 = matcher.group(3);
            String dir2 = matcher.group(6);
            Quantity<Angle> coord1 = makeAngle(matcher.group(1), matcher.group(2), null);
            Quantity<Angle> coord2 = makeAngle(matcher.group(4), matcher.group(5), null);
            Quantity<Length> alt = quantityFormat.parse(matcher.group(7), Unit.METER);
            return (symbols.W.equals(dir1) || symbols.N.equals(dir2))
                ? new Location(coord2, coord1, alt)
                : new Location(coord1, coord2, alt);
        }

        matcher = threeComponentsDecimalDeg.matcher(input);
        if (matcher.matches()) {
            String dir1 = matcher.group(2);
            String dir2 = matcher.group(4);
            Quantity<Angle> coord1 = quantityFormat.parse(matcher.group(1), Unit.DEGREE);
            Quantity<Angle> coord2 = quantityFormat.parse(matcher.group(3), Unit.DEGREE);
            Quantity<Length> alt = quantityFormat.parse(matcher.group(5), Unit.METER);
            return (symbols.W.equals(dir1) || symbols.N.equals(dir2))
                ? new Location(coord2, coord1, alt)
                : new Location(coord1, coord2, alt);
        }

        matcher = twoComponentsDegMinSec.matcher(input);
        if (matcher.matches()) {
            String dir1 = matcher.group(4);
            String dir2 = matcher.group(8);
            Quantity<Angle> coord1 = makeAngle(matcher.group(1), matcher.group(2), matcher.group(3));
            Quantity<Angle> coord2 = makeAngle(matcher.group(5), matcher.group(6), matcher.group(7));
            return (symbols.W.equals(dir1) || symbols.N.equals(dir2))
                ? new Location(coord2, coord1)
                : new Location(coord1, coord2);
        }

        matcher = twoComponentsDegDecimalMin.matcher(input);
        if (matcher.matches()) {
            String dir1 = matcher.group(3);
            String dir2 = matcher.group(6);
            Quantity<Angle> coord1 = makeAngle(matcher.group(1), matcher.group(2), null);
            Quantity<Angle> coord2 = makeAngle(matcher.group(4), matcher.group(5), null);
            return (symbols.W.equals(dir1) || symbols.N.equals(dir2))
                ? new Location(coord2, coord1)
                : new Location(coord1, coord2);
        }

        matcher = twoComponentsDecimalDeg.matcher(input);
        if (matcher.matches()) {
            String dir1 = matcher.group(2);
            String dir2 = matcher.group(4);
            Quantity<Angle> coord1 = quantityFormat.parse(matcher.group(1), Unit.DEGREE);
            Quantity<Angle> coord2 = quantityFormat.parse(matcher.group(3), Unit.DEGREE);
            return (symbols.W.equals(dir1) || symbols.N.equals(dir2))
                ? new Location(coord2, coord1)
                : new Location(coord1, coord2);
        }

        matcher = threeComponentsXyz.matcher(input);
        if (matcher.matches()) {
            Quantity<Length> x = quantityFormat.parse(matcher.group(1), Unit.METER);
            Quantity<Length> y = quantityFormat.parse(matcher.group(2), Unit.METER);
            return new Location(x, y, quantityFormat.parse(matcher.group(3), Unit.METER));
        }

        matcher = threeComponentsYxz.matcher(input);
        if (matcher.matches()) {
            Quantity<Length> y = quantityFormat.parse(matcher.group(1), Unit.METER);
            Quantity<Length> x = quantityFormat.parse(matcher.group(2), Unit.METER);
            return new Location(x, y, quantityFormat.parse(matcher.group(3), Unit.METER));
        }

        throw new IllegalArgumentException("Invalid location: " + input);
    }

    private Quantity<Angle> makeAngle(@Nullable String deg, @Nullable String min, @Nullable String sec) {
        Quantity<Angle> degrees =
            (deg == null || deg.isEmpty()) ? Quantity.of(0.0, Unit.DEGREE) : quantityFormat.parse(deg, Unit.DEGREE);

        Quantity<Angle> minutes =
            (min == null || min.isEmpty())
                ? Quantity.of(0.0, Unit.ARCMINUTE)
                : quantityFormat.parse(min, Unit.ARCMINUTE);

        Quantity<Angle> seconds =
            (sec == null || sec.isEmpty())
                ? Quantity.of(0.0, Unit.ARCSECOND)
                : quantityFormat.parse(sec, Unit.ARCSECOND);

        return degrees.add(minutes).add(seconds);
    }

    private static <Q extends Quantity<Q>> String getUnitSymbols(Unit<Q> unit) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Unit.Symbol symbol : unit.getLocalizedSymbols()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append('|');
            }

            stringBuilder.append(symbol.getText());
        }

        for (Unit.Symbol symbol : unit.getNeutralSymbols()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append('|');
            }

            stringBuilder.append(symbol.getText());
        }

        return stringBuilder.toString();
    }

    private static <Q extends Quantity<Q>> String getUnitSymbols(Class<Q> unitClass) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Unit<? extends Quantity<?>> unit : Unit.getUnits(unitClass)) {
            for (Unit.Symbol symbol : unit.getLocalizedSymbols()) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append('|');
                }

                stringBuilder.append(symbol.getText());
            }

            for (Unit.Symbol symbol : unit.getNeutralSymbols()) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append('|');
                }

                stringBuilder.append(symbol.getText());
            }
        }

        return stringBuilder.toString();
    }

    private static final String lengthSymbols = getUnitSymbols(Length.class);
    private static final String degreeSymbols = getUnitSymbols(Unit.DEGREE);
    private static final String arcMinuteSymbols = getUnitSymbols(Unit.ARCMINUTE);
    private static final String arcSecondSymbols = getUnitSymbols(Unit.ARCSECOND);
    private static final String nw = symbols.N + symbols.W;

    private static final Pattern threeComponentsXyz =
        Pattern.compile(
            "^\\s*(?:[Xx]\\s*[:=])?\\s*([-+]?[0-9]*[.,]?[0-9]+\\s*(?:"
                + lengthSymbols
                + ")?)\\s*[;,]?\\s*(?:[Yy]\\s*[:=])?\\s*([-+]?[0-9]*[.,]?[0-9]+\\s*(?:"
                + lengthSymbols
                + ")?)\\s*[;,]?\\s*(?:[Zz]\\s*[:=])?\\s*([-+]?[0-9]*[.,]?[0-9]+\\s*(?:"
                + lengthSymbols
                + ")?)\\s*(?!.)");

    private static final Pattern threeComponentsYxz =
        Pattern.compile(
            "^\\s*(?:[Yy]\\s*[:=])?\\s*([-+]?[0-9]*[.,]?[0-9]+\\s*(?:"
                + lengthSymbols
                + ")?)\\s*[;,]?\\s*(?:[Xx]\\s*[:=])?\\s*([-+]?[0-9]*[.,]?[0-9]+\\s*(?:"
                + lengthSymbols
                + ")?)\\s*[;,]?\\s*(?:[Zz]\\s*[:=])?\\s*([-+]?[0-9]*[.,]?[0-9]+\\s*(?:"
                + lengthSymbols
                + ")?)\\s*(?!.)");

    private static final Pattern threeComponentsDegMinSec =
        Pattern.compile(
            "^\\s*([-+]?[0-9]+\\s*(?:"
                + degreeSymbols
                + "|\\s+))\\s*([0-9]+\\s*(?:"
                + arcMinuteSymbols
                + "|\\s+))\\s*([0-9]*[.,]?[0-9]+\\s*(?:"
                + arcSecondSymbols
                + ")?)\\s*(["
                + nw
                + "])?\\s*[;,\\s]\\s*([-+]?[0-9]+\\s*(?:"
                + degreeSymbols
                + "|\\s+))\\s*([0-9]+\\s*(?:"
                + arcMinuteSymbols
                + "|\\s+))\\s*([0-9]*[.,]?[0-9]+\\s*(?:"
                + arcSecondSymbols
                + ")?)\\s*(["
                + nw
                + "])?\\s*[;,\\s]\\s*([-+]?[0-9]*[.,]?[0-9]+\\s*(?:"
                + lengthSymbols
                + ")?)\\s*(?!.)");

    private static final Pattern threeComponentsDegDecimalMin =
        Pattern.compile(
            "^\\s*([-+]?[0-9]+\\s*(?:"
                + degreeSymbols
                + "|\\s+))\\s*([0-9]*[.,]?[0-9]+\\s*(?:"
                + arcMinuteSymbols
                + ")?)\\s*(["
                + nw
                + "])?\\s*[;,\\s]\\s*([-+]?[0-9]+\\s*(?:"
                + degreeSymbols
                + "|\\s+))\\s*([0-9]*[.,]?[0-9]+\\s*(?:"
                + arcMinuteSymbols
                + ")?)\\s*(["
                + nw
                + "])?\\s*[;,\\s]\\s*([-+]?[0-9]*[.,]?[0-9]+\\s*(?:"
                + lengthSymbols
                + ")?)\\s*(?!.)");

    private static final Pattern threeComponentsDecimalDeg =
        Pattern.compile(
            "^\\s*([-+]?[0-9]*[.,]?[0-9]+\\s*(?:"
                + degreeSymbols
                + "|\\s*))\\s*(["
                + nw
                + "])?\\s*[;,\\s]\\s*([-+]?[0-9]*[.,]?[0-9]+\\s*(?:"
                + degreeSymbols
                + "|\\s*))\\s*(["
                + nw
                + "])?\\s*[;,\\s]\\s*([-+]?[0-9]*[.,]?[0-9]+\\s*(?:"
                + lengthSymbols
                + ")?)\\s*(?!.)");

    private static final Pattern twoComponentsDegMinSec =
        Pattern.compile(
            "^\\s*([-+]?[0-9]+\\s*(?:"
                + degreeSymbols
                + "|\\s+))\\s*([0-9]+\\s*(?:"
                + arcMinuteSymbols
                + "|\\s+))\\s*([0-9]*[.,]?[0-9]+\\s*(?:"
                + arcSecondSymbols
                + ")?)\\s*(["
                + nw
                + "])?\\s*[;,\\s]\\s*([-+]?[0-9]+\\s*(?:"
                + degreeSymbols
                + "|\\s+))\\s*([0-9]+\\s*(?:"
                + arcMinuteSymbols
                + "|\\s+))\\s*([0-9]*[.,]?[0-9]+\\s*(?:"
                + arcSecondSymbols
                + ")?)\\s*(["
                + nw
                + "])?\\s*(?!.)");

    private static final Pattern twoComponentsDegDecimalMin =
        Pattern.compile(
            "^\\s*([-+]?[0-9]+\\s*(?:"
                + degreeSymbols
                + "|\\s+))\\s*([0-9]*[.,]?[0-9]+\\s*(?:"
                + arcMinuteSymbols
                + ")?)\\s*(["
                + nw
                + "])?\\s*[;,\\s]\\s*([-+]?[0-9]+\\s*(?:"
                + degreeSymbols
                + "|\\s+))\\s*([0-9]*[.,]?[0-9]+\\s*(?:"
                + arcMinuteSymbols
                + ")?)\\s*(["
                + nw
                + "])?\\s*(?!.)");

    private static final Pattern twoComponentsDecimalDeg =
        Pattern.compile(
            "^\\s*([-+]?[0-9]*[.,]?[0-9]+\\s*(?:"
                + degreeSymbols
                + "|\\s*))\\s*(["
                + nw
                + "])?\\s*[;,\\s]\\s*([-+]?[0-9]*[.,]?[0-9]+\\s*(?:"
                + degreeSymbols
                + "|\\s*))\\s*(["
                + nw
                + "])?\\s*(?!.)");

}
