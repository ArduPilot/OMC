/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.collections.ArrayMap;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.QuantityArithmetic;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.VariantQuantity;
import java.util.Map;
import javafx.util.StringConverter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class VariantQuantityConverter extends StringConverter<VariantQuantity> {

    private static class Rule {
        final UnitInfo<?> unitInfo;
        final QuantityFormat format;

        Rule(UnitInfo<?> unitInfo, QuantityFormat format) {
            this.unitInfo = unitInfo;
            this.format = format;
        }
    }

    private final IQuantityStyleProvider quantityStyleProvider;
    private final QuantityFormat parseFormatter = new QuantityFormat();
    private final Map<Dimension, Rule> rules = new ArrayMap<>();
    private Unit<?> implicitUnit;

    VariantQuantityConverter(IQuantityStyleProvider quantityStyleProvider, QuantityArithmetic arithmetic) {
        this.quantityStyleProvider = quantityStyleProvider;
        this.parseFormatter.setArithmetic(arithmetic);
        this.parseFormatter.setAngleStyle(quantityStyleProvider.getAngleStyle());
        this.parseFormatter.setTimeStyle(quantityStyleProvider.getTimeStyle());
    }

    public Unit<?> getImplicitUnit() {
        return implicitUnit;
    }

    public void setImplicitUnit(Unit<?> implicitUnit) {
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("No rules specified.");
        }

        boolean found = false;
        for (var rule : rules.entrySet()) {
            if (implicitUnit.getDimension() == rule.getKey()) {
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException(
                "Cannot set implicit unit to " + implicitUnit + ". Allowed dimensions are: " + rules.keySet());
        }

        this.implicitUnit = implicitUnit;
    }

    public void addRule(UnitInfo<?> unitInfo, int significantDigits, int maxFractionDigits) {
        var format = new QuantityFormat();
        format.setSignificantDigits(significantDigits);
        format.setMaximumFractionDigits(maxFractionDigits);
        rules.put(unitInfo.getDimension(), new Rule(unitInfo, format));
    }

    @Override
    public VariantQuantity fromString(@Nullable String value) {
        if (value == null) {
            return null;
        }

        try {
            parseFormatter.setTimeStyle(quantityStyleProvider.getTimeStyle());
            parseFormatter.setAngleStyle(quantityStyleProvider.getAngleStyle());
            return parseFormatter.parse(value, rules.keySet(), implicitUnit);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString(@Nullable VariantQuantity value) {
        if (value == null) {
            return "";
        }

        var format = rules.get(value.getDimension()).format;
        format.setTimeStyle(quantityStyleProvider.getTimeStyle());
        format.setAngleStyle(quantityStyleProvider.getAngleStyle());
        return format.format(value);
    }

}
