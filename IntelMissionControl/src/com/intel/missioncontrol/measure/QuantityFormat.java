/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.intel.missioncontrol.collections.ArrayMap;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Time;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;

public class QuantityFormat {

    private static Pattern degMin = Pattern.compile(getPattern(Unit.DEGREE, Unit.ARCMINUTE));
    private static Pattern degSec = Pattern.compile(getPattern(Unit.DEGREE, Unit.ARCSECOND));
    private static Pattern minSec = Pattern.compile(getPattern(Unit.ARCMINUTE, Unit.ARCSECOND));
    private static Pattern degMinSec = Pattern.compile(getPattern(Unit.DEGREE, Unit.ARCMINUTE, Unit.ARCSECOND));

    private final DecimalFormat decimalFormat;
    private QuantityArithmetic arithmetic = QuantityArithmetic.DEFAULT;
    private SystemOfMeasurement systemOfMeasurement = SystemOfMeasurement.METRIC;
    private AngleStyle angleStyle = AngleStyle.DECIMAL_DEGREES;
    private TimeStyle timeStyle = TimeStyle.DECIMAL;
    private int significantDigits = 5;
    private int maximumFractionDigits = 16;

    public QuantityFormat() {
        decimalFormat = (DecimalFormat)NumberFormat.getInstance(Locale.getDefault());
        decimalFormat.setGroupingUsed(false);
    }

    public void setArithmetic(QuantityArithmetic arithmetic) {
        this.arithmetic = arithmetic;
    }

    public QuantityArithmetic getArithmetic() {
        return arithmetic;
    }

    public void setSystemOfMeasurement(SystemOfMeasurement systemOfMeasurement) {
        this.systemOfMeasurement = systemOfMeasurement;
    }

    public SystemOfMeasurement getSystemOfMeasurement() {
        return systemOfMeasurement;
    }

    public void setAngleStyle(AngleStyle angleStyle) {
        this.angleStyle = angleStyle;
    }

    public AngleStyle getAngleStyle() {
        return this.angleStyle;
    }

    public void setTimeStyle(TimeStyle timeStyle) {
        this.timeStyle = timeStyle;
    }

    public TimeStyle getTimeStyle() {
        return this.timeStyle;
    }

    public void setSignificantDigits(int significantDigits) {
        this.significantDigits = significantDigits;
    }

    public int getSignificantDigits() {
        return significantDigits;
    }

    public void setMaximumFractionDigits(int maxFractionDigits) {
        this.maximumFractionDigits = maxFractionDigits;
    }

    public int getMaximumFractionDigits() {
        return maximumFractionDigits;
    }

    public <Q extends Quantity<Q>> String format(Quantity<Q> quantity) {
        return format(quantity.toVariant(), null);
    }

    public <Q extends Quantity<Q>> String format(Quantity<Q> quantity, @Nullable UnitInfo<Q> unitInfo) {
        return format(quantity.toVariant(), unitInfo);
    }

    public <Q extends Quantity<Q>> String format(VariantQuantity quantity) {
        return format(quantity, null);
    }

    public String format(VariantQuantity quantity, @Nullable UnitInfo<?> unitInfo) {
        AngleStyle angleStyle = getAngleStyle();
        TimeStyle timeStyle = getTimeStyle();

        if (quantity.getDimension() == Dimension.ANGLE
                && (angleStyle == AngleStyle.DEGREE_DECIMAL_MINUTE || angleStyle == AngleStyle.DEGREE_MINUTE_SECOND)) {
            return formatAngle(quantity);
        }

        if (quantity.getDimension() == Dimension.TIME && timeStyle == TimeStyle.HOUR_MINUTE_SECOND) {
            return formatTime(quantity);
        }

        if (unitInfo != null) {
            quantity = adjustUnit(quantity, unitInfo);
        }

        return formatNumber(quantity.getValue(), quantity.getUnit());
    }

    @SuppressWarnings("unchecked")
    public <Q extends Quantity<Q>> Quantity<Q> parse(String input, Unit<Q> implicitUnit)
            throws IllegalArgumentException {
        var quantity = parse(input, Lists.newArrayList(implicitUnit.getDimension()), implicitUnit, false);
        return Quantity.of(quantity.getValue(), (Unit<Q>)quantity.getUnit());
    }

    public VariantQuantity parse(String input, Dimension... allowedDimensions) {
        return parse(input, Lists.newArrayList(allowedDimensions), null, false);
    }

    public VariantQuantity parse(String input, Collection<Dimension> allowedDimensions) {
        return parse(input, allowedDimensions, null, false);
    }

    public VariantQuantity parse(
            String input, Collection<Dimension> allowedDimensions, @Nullable Unit<?> implicitUnit) {
        return parse(input, allowedDimensions, implicitUnit, false);
    }

    private VariantQuantity parse(
            String input,
            Collection<Dimension> allowedDimensions,
            @Nullable Unit<?> implicitUnit,
            boolean insideExpression)
            throws IllegalArgumentException {
        if (input.length() == 0) {
            throw new IllegalArgumentException(input);
        }

        input = CharMatcher.whitespace().trimFrom(input);

        ParsePosition parsePosition = new ParsePosition(0);
        decimalFormat.setMaximumFractionDigits(16);

        Number number = null;
        String numberString = input;
        while (!numberString.isEmpty()) {
            number = decimalFormat.parse(input, parsePosition);
            if (numberString.length() == parsePosition.getIndex()) {
                break;
            }

            numberString = numberString.substring(0, numberString.length() - 1);
            parsePosition.setIndex(0);
            parsePosition.setErrorIndex(-1);
        }

        String unitString = CharMatcher.whitespace().trimFrom(input.substring(numberString.length()));
        if (number != null && implicitUnit != null && unitString.isEmpty()) {
            return VariantQuantity.of(number, implicitUnit);
        }

        Unit<? extends Quantity<?>>[] units;
        try {
            units = Unit.parseSymbol(unitString, allowedDimensions);
        } catch (IllegalArgumentException e) {
            if (allowedDimensions.contains(Dimension.TIME)) {
                try {
                    return parseTime(input).toVariant();
                } catch (IllegalArgumentException e2) {
                    if (!insideExpression && allowedDimensions.size() == 1) {
                        return resolveExpression(input, allowedDimensions, implicitUnit);
                    }
                }
            }

            if (allowedDimensions.contains(Dimension.ANGLE)) {
                try {
                    return parseAngle(input).toVariant();
                } catch (IllegalArgumentException e2) {
                    if (!insideExpression && allowedDimensions.size() == 1) {
                        return resolveExpression(input, allowedDimensions, implicitUnit);
                    }
                }
            }

            if (unitString.isEmpty()) {
                if (implicitUnit != null) {
                    return parse(input, implicitUnit).toVariant();
                }

                throw new IllegalArgumentException("No unit symbol detected: " + input);
            }

            if (!insideExpression) {
                return resolveExpression(input, allowedDimensions, implicitUnit);
            }

            throw e;
        }

        if (number == null || numberString.isEmpty()) {
            throw new IllegalArgumentException("Invalid quantity: " + input);
        }

        return Quantity.of(number, units[0]).toVariant();
    }

    private Quantity<Time> parseTime(String input) {
        String[] parts = input.split(":", -1);
        double hours = 0;
        double minutes = 0;
        double seconds = 0;
        boolean negative = false;

        try {
            if (parts.length < 2 || parts.length > 3) {
                throw new IllegalArgumentException("Invalid time string: " + input);
            }

            if (parts[0].charAt(0) == '-') {
                parts[0] = parts[0].substring(1);
                negative = true;
            }

            hours = Math.abs(Integer.parseInt(parts[0]));
            minutes = Integer.parseInt(parts[1]);

            if (parts.length == 3) {
                seconds = Double.parseDouble(parts[2]);
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid time string: " + input);
        }

        if (minutes < 0 || seconds < 0) {
            throw new IllegalArgumentException("Invalid time string: " + input);
        }

        double totalHours = hours + minutes / 60.0 + seconds / 3600.0;
        if (negative) {
            totalHours = -totalHours;
        }

        return Quantity.of(totalHours, Unit.HOUR);
    }

    private Quantity<Angle> parseAngle(String input) {
        Matcher matcher = degMin.matcher(input);
        if (matcher.matches()) {
            String deg = matcher.group(1);
            String min = matcher.group(2);
            return makeAngle(deg, min, null);
        }

        matcher = degSec.matcher(input);
        if (matcher.matches()) {
            String deg = matcher.group(1);
            String sec = matcher.group(2);
            return makeAngle(deg, null, sec);
        }

        matcher = minSec.matcher(input);
        if (matcher.matches()) {
            String min = matcher.group(1);
            String sec = matcher.group(2);
            return makeAngle(null, min, sec);
        }

        matcher = degMinSec.matcher(input);
        if (matcher.matches()) {
            String deg = matcher.group(1);
            String min = matcher.group(2);
            String sec = matcher.group(3);
            return makeAngle(deg, min, sec);
        }

        throw new IllegalArgumentException("Invalid angle string: " + input);
    }

    private VariantQuantity resolveExpression(
            String input, Collection<Dimension> allowedDimensions, @Nullable Unit<?> implicitUnit) {
        try {
            var expr = parseExpression(input, new ParsePosition(0), allowedDimensions, implicitUnit);
            if (expr.isDimensionless()) {
                if (implicitUnit == null) {
                    throw new IllegalArgumentException("No unit symbol detected: " + input);
                }

                return VariantQuantity.of(expr.getDimensionless(), implicitUnit);
            }

            return expr.getQuantity();
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg == null || msg.isEmpty()) {
                throw new IllegalArgumentException("Invalid expression: " + input);
            }

            throw new IllegalArgumentException("Invalid expression: " + input + " (" + msg + ")");
        } catch (ExpressionException e) {
            throw new IllegalArgumentException(
                "Invalid expression: "
                    + input.substring(0, e.getErrorStartIndex())
                    + ">>>"
                    + input.substring(e.getErrorStartIndex(), e.getErrorEndIndex())
                    + "<<<"
                    + input.substring(e.getErrorEndIndex(), input.length()));
        }
    }

    private IExpression parseExpression(
            String input,
            ParsePosition position,
            Collection<Dimension> allowedDimensions,
            @Nullable Unit<?> implicitUnit) {
        int sequenceStart = position.getIndex();
        IExpression firstOperand = null, secondOperand = null;
        Character operator = null;
        IExpression expr;

        while (position.getIndex() < input.length()) {
            char c = input.charAt(position.getIndex());
            switch (c) {
            case '(':
                position.setIndex(position.getIndex() + 1);
                if (firstOperand == null) {
                    firstOperand = parseExpression(input, position, allowedDimensions, implicitUnit);
                } else {
                    secondOperand = parseExpression(input, position, allowedDimensions, implicitUnit);
                }

                sequenceStart = position.getIndex();
                break;
            case ')':
                position.setIndex(position.getIndex() + 1);
                if (firstOperand != null && position.getIndex() - sequenceStart > 1) {
                    secondOperand =
                        new LiteralExpression(
                            input.substring(sequenceStart, position.getIndex() - 1),
                            sequenceStart,
                            position.getIndex() - 1,
                            allowedDimensions,
                            implicitUnit);
                }

                if (firstOperand != null && secondOperand == null && operator == null) {
                    return firstOperand;
                } else if (firstOperand != null && secondOperand != null) {
                    if (operator == null) {
                        throw new IllegalArgumentException("Invalid expression: " + input);
                    }

                    return new BinaryExpression(firstOperand, secondOperand, operator);
                } else if (sequenceStart != position.getIndex()) {
                    return new LiteralExpression(
                        input.substring(sequenceStart, position.getIndex() - 2),
                        sequenceStart,
                        position.getIndex() - 2,
                        allowedDimensions,
                        implicitUnit);
                }

                throw new IllegalArgumentException("Invalid expression: " + input);
            case '+':
            case '-':
            case '*':
            case '/':
                if (firstOperand == null) {
                    operator = c;
                    firstOperand =
                        new LiteralExpression(
                            input.substring(sequenceStart, position.getIndex()),
                            sequenceStart,
                            position.getIndex(),
                            allowedDimensions,
                            implicitUnit);
                    sequenceStart = position.getIndex() + 1;
                    position.setIndex(sequenceStart);
                } else if (secondOperand != null && operator != null) {
                    position.setIndex(position.getIndex() + 1);
                    if (compareOperator(operator, c) < 0) {
                        return new BinaryExpression(
                            firstOperand,
                            new BinaryExpression(
                                secondOperand, parseExpression(input, position, allowedDimensions, implicitUnit), c),
                            operator);
                    }

                    return new BinaryExpression(
                        new BinaryExpression(firstOperand, secondOperand, operator),
                        parseExpression(input, position, allowedDimensions, implicitUnit),
                        c);
                } else if (sequenceStart != position.getIndex()) {
                    if (operator == null) {
                        operator = c;
                    }

                    secondOperand =
                        new LiteralExpression(
                            input.substring(sequenceStart, position.getIndex()),
                            sequenceStart,
                            position.getIndex(),
                            allowedDimensions,
                            implicitUnit);
                    expr = new BinaryExpression(firstOperand, secondOperand, operator);
                    position.setIndex(position.getIndex() + 1);
                    return new BinaryExpression(
                        expr, parseExpression(input, position, allowedDimensions, implicitUnit), c);
                } else {
                    operator = c;
                    sequenceStart = position.getIndex() + 1;
                    position.setIndex(sequenceStart);
                }

                break;
            default:
                if (sequenceStart == position.getIndex() && CharMatcher.whitespace().matches(c)) {
                    ++sequenceStart;
                }

                position.setIndex(position.getIndex() + 1);
                break;
            }
        }

        boolean moreText = sequenceStart != position.getIndex();

        if (firstOperand != null) {
            if (!moreText) {
                if (operator == null) {
                    return firstOperand;
                }

                if (secondOperand != null) {
                    return new BinaryExpression(firstOperand, secondOperand, operator);
                }

                throw new IllegalArgumentException("Invalid expression: " + input);
            } else if (operator != null) {
                if (secondOperand != null) {
                    var units =
                        Unit.parseSymbol(input.substring(sequenceStart, position.getIndex()), allowedDimensions);
                    var secondOp = (BinaryExpression)secondOperand;
                    secondOp.setExpressionUnit(units[0]);
                } else {
                    secondOperand =
                        new LiteralExpression(
                            input.substring(sequenceStart, position.getIndex()),
                            sequenceStart,
                            position.getIndex(),
                            allowedDimensions,
                            implicitUnit);
                }

                return new BinaryExpression(firstOperand, secondOperand, operator);
            } else {
                throw new IllegalArgumentException("Invalid expression: " + input);
            }
        }

        if (moreText) {
            return new LiteralExpression(
                input.substring(sequenceStart, position.getIndex()),
                sequenceStart,
                position.getIndex(),
                allowedDimensions,
                implicitUnit);
        }

        throw new IllegalArgumentException("Invalid expression: " + input);
    }

    private int compareOperator(char left, char right) {
        if (left == '*' || left == '/') {
            if (right == '+' || right == '-') {
                return 1;
            }

            return 0;
        }

        if (right == '*' || right == '/') {
            return -1;
        }

        return 0;
    }

    private String formatAngle(VariantQuantity quantity) {
        switch (getAngleStyle()) {
        case DEGREE_MINUTE_SECOND:
            {
                double degf = quantity.convertTo(Unit.DEGREE).getValue().doubleValue();
                int deg = (int)degf;
                double minf = (degf - deg) * 60;
                int min = (int)minf;
                double secf = (minf - min) * 60;
                int sec = (int)Math.round(secf);
                if (sec == 60) {
                    ++min;
                    sec = 0;
                }

                if (min == 60) {
                    ++deg;
                    min = 0;
                }

                return deg
                    + formatSymbol(Unit.DEGREE.getDisplaySymbol())
                    + " "
                    + min
                    + formatSymbol(Unit.ARCMINUTE.getDisplaySymbol())
                    + " "
                    + decimalFormat.format(sec)
                    + formatSymbol(Unit.ARCSECOND.getDisplaySymbol());
            }

        case DEGREE_DECIMAL_MINUTE:
            {
                double degf = quantity.convertTo(Unit.DEGREE).getValue().doubleValue();
                int deg = (int)degf;
                double minf = (degf - deg) * 60;
                decimalFormat.setMaximumFractionDigits(4);
                return deg
                    + formatSymbol(Unit.DEGREE.getDisplaySymbol())
                    + " "
                    + decimalFormat.format(minf)
                    + formatSymbol(Unit.ARCMINUTE.getDisplaySymbol());
            }
        }

        throw new IllegalArgumentException();
    }

    private String formatTime(VariantQuantity time) {
        if (getTimeStyle() == TimeStyle.HOUR_MINUTE_SECOND) {
            final Quantity<Time> hours = time.convertTo(Unit.HOUR);
            double hoursDouble = hours.getValue().doubleValue();
            int hoursInt = (int)hoursDouble;

            double minutesDouble = (hoursDouble - hoursInt) * 60.0;
            int minutesInt = (int)minutesDouble;
            if (Math.abs(minutesDouble - (double)(minutesInt + 1)) <= 10E-12) {
                minutesInt += 1;
                minutesDouble = minutesInt;
            }

            double secondsDouble = (minutesDouble - minutesInt) * 60.0;
            int secondsInt = (int)secondsDouble;
            if (Math.abs(secondsDouble - (double)(secondsInt + 1)) <= 10E-12) {
                secondsInt += 1;
            }

            StringBuilder stringBuilder = new StringBuilder();

            if (hoursDouble < 0) {
                stringBuilder.append('-');
                hoursInt = Math.abs(hoursInt);
                minutesInt = Math.abs(minutesInt);
                secondsInt = Math.abs(secondsInt);
            }

            if (hoursInt < 10) {
                stringBuilder.append('0');
            }

            stringBuilder.append(Integer.toString(hoursInt));
            stringBuilder.append(':');

            if (minutesInt < 10) {
                stringBuilder.append('0');
            }

            stringBuilder.append(Integer.toString(minutesInt));
            stringBuilder.append(':');

            if (secondsInt < 10) {
                stringBuilder.append('0');
            }

            stringBuilder.append(Integer.toString(secondsInt));

            return stringBuilder.toString();
        }

        throw new IllegalArgumentException();
    }

    private <Q extends Quantity<Q>> String formatNumber(Number number, Unit<?> unit) {
        int intValue = Math.abs(number.intValue());
        int intDigits = intValue > 0 ? (int)(Math.log10(intValue) + 1) : 1;
        int remainingFracDigits = significantDigits - intDigits;
        if (remainingFracDigits > maximumFractionDigits) {
            remainingFracDigits = maximumFractionDigits;
        }

        if (remainingFracDigits < 0) {
            remainingFracDigits = 0;
        }

        decimalFormat.setMaximumFractionDigits(remainingFracDigits);
        return decimalFormat.format(number) + formatSymbol(unit.getDisplaySymbol());
    }

    private String formatSymbol(Unit.Symbol symbol) {
        if (symbol.hasInterveningSpace()) {
            return "\u202F" + symbol.getText();
        }

        return symbol.getText();
    }

    private Quantity<Angle> makeAngle(@Nullable String deg, @Nullable String min, @Nullable String sec) {
        Quantity<Angle> degrees =
            (deg == null || deg.isEmpty()) ? Quantity.of(0.0, Unit.DEGREE) : parse(deg, Unit.DEGREE);

        Quantity<Angle> minutes =
            (min == null || min.isEmpty()) ? Quantity.of(0.0, Unit.ARCMINUTE) : parse(min, Unit.ARCMINUTE);

        Quantity<Angle> seconds =
            (sec == null || sec.isEmpty()) ? Quantity.of(0.0, Unit.ARCSECOND) : parse(sec, Unit.ARCSECOND);

        return degrees.add(minutes).add(seconds);
    }

    private VariantQuantity adjustUnit(VariantQuantity quantity, UnitInfo<?> unitInfo) {
        List<? extends Unit<?>> units = unitInfo.getAllowedUnits(getSystemOfMeasurement());
        Map<Integer, List<Quantity<?>>> adjustUnitsMap = new ArrayMap<>();

        for (Unit<?> unit : units) {
            Quantity<?> convertedQuantity = quantity.convertTo(unit);
            double value = Math.abs(convertedQuantity.getValue().doubleValue());
            int magnitude = (int)value > 0 ? (int)Math.floor(Math.log10((int)value)) + 1 : 0;
            List<Quantity<?>> quantityList = adjustUnitsMap.get(magnitude);
            if (quantityList != null) {
                quantityList.add(convertedQuantity);
            } else {
                List<Quantity<?>> list = new ArrayList<>();
                list.add(convertedQuantity);
                adjustUnitsMap.put(magnitude, list);
            }
        }

        List<Quantity<?>> bestList = null;
        List<Quantity<?>> nullMagList = null;
        int bestMagnitude = Integer.MAX_VALUE;
        for (SortedMap.Entry<Integer, List<Quantity<?>>> entry : adjustUnitsMap.entrySet()) {
            int magnitude = entry.getKey();
            if (magnitude == 0) {
                nullMagList = entry.getValue();
            } else if (magnitude < bestMagnitude && magnitude > 0) {
                bestMagnitude = entry.getKey();
                bestList = entry.getValue();
            }
        }

        if (bestList == null) {
            bestList = nullMagList;
        }

        if (bestList != null) {
            Quantity<?> bestQuantity = bestList.get(0);

            if (bestList.size() > 1) {
                for (Quantity<?> q : bestList) {
                    if (q.getValue().doubleValue() > bestQuantity.getValue().doubleValue()) {
                        bestQuantity = q;
                    }
                }
            }

            quantity = bestQuantity.toVariant();
        }

        return quantity;
    }

    private static String getPattern(Unit<?> unit0, Unit<?> unit1) {
        return "^\\s*([0-9]*[.,]?[0-9]+\\s*(?:"
            + getUnitSymbols(unit0)
            + ")?)\\s*([0-9]*[.,]?[0-9]+\\s*(?:"
            + getUnitSymbols(unit1)
            + ")?)$";
    }

    private static String getPattern(Unit<?> unit0, Unit<?> unit1, Unit<?> unit2) {
        return "^\\s*([0-9]*[.,]?[0-9]+\\s*(?:"
            + getUnitSymbols(unit0)
            + ")?)\\s*([0-9]*[.,]?[0-9]+\\s*(?:"
            + getUnitSymbols(unit1)
            + ")?)\\s*([0-9]*[.,]?[0-9]+\\s*(?:"
            + getUnitSymbols(unit2)
            + ")?)$";
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

    private static class ExpressionException extends RuntimeException {
        private final int errorStartIndex;
        private final int errorEndIndex;

        ExpressionException(int errorStartIndex, int errorEndIndex) {
            this.errorStartIndex = errorStartIndex;
            this.errorEndIndex = errorEndIndex;
        }

        public int getErrorStartIndex() {
            return errorStartIndex;
        }

        public int getErrorEndIndex() {
            return errorEndIndex;
        }
    }

    private interface IExpression {
        VariantQuantity getQuantity();

        Number getDimensionless();

        boolean isDimensionless();
    }

    private class LiteralExpression implements IExpression {
        private final int textStart;
        private final int textEnd;
        private final Number scalar;
        private final VariantQuantity quantity;

        LiteralExpression(
                String text,
                int textStart,
                int textEnd,
                Collection<Dimension> allowedDimensions,
                @Nullable Unit<?> implicitUnit) {
            text = CharMatcher.whitespace().trimFrom(text);
            ParsePosition parsePosition = new ParsePosition(0);
            VariantQuantity quantity = null;
            Number scalar = decimalFormat.parse(text, parsePosition);
            if (parsePosition.getIndex() != text.length()) {
                scalar = null;
                quantity = parse(text, allowedDimensions, implicitUnit, true);
            }

            this.textStart = textStart;
            this.textEnd = textEnd;
            this.scalar = scalar;
            this.quantity = quantity;
        }

        @Override
        public VariantQuantity getQuantity() {
            if (quantity == null) {
                throw new ExpressionException(textStart, textEnd);
            }

            return quantity;
        }

        @Override
        public Number getDimensionless() {
            if (scalar == null) {
                throw new ExpressionException(textStart, textEnd);
            }

            return scalar;
        }

        @Override
        public boolean isDimensionless() {
            return scalar != null;
        }
    }

    private class BinaryExpression implements IExpression {
        private final IExpression left;
        private final IExpression right;
        private final char operator;
        private Unit<?> expressionUnit;

        BinaryExpression(IExpression left, IExpression right, char operator) {
            if (left instanceof LiteralExpression && right instanceof LiteralExpression) {
                var leftExpr = (LiteralExpression)left;
                var rightExpr = (LiteralExpression)right;

                if ((operator == '*' || operator == '/')
                        && !leftExpr.isDimensionless()
                        && !rightExpr.isDimensionless()) {
                    throw new ExpressionException(leftExpr.textStart, rightExpr.textEnd);
                }
            }

            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        void setExpressionUnit(Unit<?> unit) {
            if (left.isDimensionless() && right.isDimensionless()) {
                this.expressionUnit = unit;
            } else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public VariantQuantity getQuantity() {
            boolean leftDimensionless = left.isDimensionless();
            boolean rightDimensionless = right.isDimensionless();
            if (leftDimensionless && rightDimensionless && expressionUnit == null) {
                throw new IllegalArgumentException();
            }

            switch (operator) {
            case '+':
                if (leftDimensionless && rightDimensionless) {
                    return VariantQuantity.of(left.getDimensionless(), expressionUnit).add(right.getDimensionless());
                } else if (leftDimensionless) {
                    return right.getQuantity().add(left.getDimensionless());
                } else if (rightDimensionless) {
                    return left.getQuantity().add(right.getDimensionless());
                }

                return arithmetic.add(left.getQuantity(), right.getQuantity());
            case '-':
                if (leftDimensionless && rightDimensionless) {
                    return VariantQuantity.of(left.getDimensionless(), expressionUnit)
                        .subtract(right.getDimensionless());
                } else if (leftDimensionless) {
                    var rightQuantity = right.getQuantity();
                    return VariantQuantity.of(left.getDimensionless(), rightQuantity.getUnit())
                        .subtract(rightQuantity.getValue());
                } else if (rightDimensionless) {
                    return left.getQuantity().subtract(right.getDimensionless());
                }

                return arithmetic.subtract(left.getQuantity(), right.getQuantity());
            case '*':
                if (leftDimensionless && rightDimensionless) {
                    return VariantQuantity.of(left.getDimensionless(), expressionUnit)
                        .multiply(right.getDimensionless());
                } else if (leftDimensionless) {
                    return right.getQuantity().multiply(left.getDimensionless());
                }

                return left.getQuantity().multiply(right.getDimensionless());
            default:
                if (leftDimensionless && rightDimensionless) {
                    return VariantQuantity.of(left.getDimensionless(), expressionUnit).divide(right.getDimensionless());
                } else if (leftDimensionless) {
                    var rightQuantity = right.getQuantity();
                    return VariantQuantity.of(left.getDimensionless(), rightQuantity.getUnit())
                        .divide(rightQuantity.getValue());
                }

                return left.getQuantity().divide(right.getDimensionless());
            }
        }

        @Override
        public Number getDimensionless() {
            switch (operator) {
            case '+':
                return addNumbers(left.getDimensionless(), right.getDimensionless());
            case '-':
                return subtractNumbers(left.getDimensionless(), right.getDimensionless());
            case '*':
                return multiplyNumbers(left.getDimensionless(), right.getDimensionless());
            default:
                return divideNumbers(left.getDimensionless(), right.getDimensionless().doubleValue());
            }
        }

        @Override
        public boolean isDimensionless() {
            return expressionUnit == null && left.isDimensionless() && right.isDimensionless();
        }

        @Override
        public String toString() {
            return "(" + left + operator + right + ")";
        }

        private Number addNumbers(Number left, Number right) {
            if (left instanceof Double || right instanceof Double) {
                return left.doubleValue() + right.doubleValue();
            }

            if (left instanceof Float || right instanceof Float) {
                return left.floatValue() + right.floatValue();
            }

            if (left instanceof Long || right instanceof Long) {
                return left.longValue() + right.longValue();
            }

            if (left instanceof Integer || right instanceof Integer) {
                return left.intValue() + right.intValue();
            }

            if (left instanceof Short || right instanceof Short) {
                return left.shortValue() + right.shortValue();
            }

            return left.byteValue() + right.byteValue();
        }

        private Number subtractNumbers(Number left, Number right) {
            if (left instanceof Double || right instanceof Double) {
                return left.doubleValue() - right.doubleValue();
            }

            if (left instanceof Float || right instanceof Float) {
                return left.floatValue() - right.floatValue();
            }

            if (left instanceof Long || right instanceof Long) {
                return left.longValue() - right.longValue();
            }

            if (left instanceof Integer || right instanceof Integer) {
                return left.intValue() - right.intValue();
            }

            if (left instanceof Short || right instanceof Short) {
                return left.shortValue() - right.shortValue();
            }

            return left.byteValue() - right.byteValue();
        }

        private Number multiplyNumbers(Number left, Number right) {
            if (left instanceof Double || right instanceof Double) {
                return left.doubleValue() * right.doubleValue();
            }

            if (left instanceof Float || right instanceof Float) {
                return left.floatValue() * right.floatValue();
            }

            if (left instanceof Long || right instanceof Long) {
                return left.longValue() * right.longValue();
            }

            if (left instanceof Integer || right instanceof Integer) {
                return left.intValue() * right.intValue();
            }

            if (left instanceof Short || right instanceof Short) {
                return left.shortValue() * right.shortValue();
            }

            return left.byteValue() * right.byteValue();
        }

        private Number divideNumbers(Number left, Number right) {
            if (left instanceof Double || right instanceof Double) {
                return left.doubleValue() / right.doubleValue();
            }

            if (left instanceof Float || right instanceof Float) {
                return left.floatValue() / right.floatValue();
            }

            if (left instanceof Long || right instanceof Long) {
                return left.longValue() / right.longValue();
            }

            if (left instanceof Integer || right instanceof Integer) {
                return left.intValue() / right.intValue();
            }

            if (left instanceof Short || right instanceof Short) {
                return left.shortValue() / right.shortValue();
            }

            return left.byteValue() / right.byteValue();
        }
    }

}
