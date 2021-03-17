/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import javafx.util.StringConverter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class QuantityConverter<Q extends Quantity<Q>> extends StringConverter<Quantity<Q>> {

    private final QuantityFormat quantityFormat;
    private final IQuantityStyleProvider quantityStyleProvider;
    private final UnitInfo<Q> unitInfo;

    public QuantityConverter(
            IQuantityStyleProvider quantityStyleProvider, UnitInfo<Q> unitInfo, int significantDigits) {
        this.quantityStyleProvider = quantityStyleProvider;
        this.unitInfo = unitInfo;
        this.quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
        this.quantityFormat.setMaximumFractionDigits(significantDigits);
    }

    public QuantityConverter(
            IQuantityStyleProvider quantityStyleProvider,
            UnitInfo<Q> unitInfo,
            int significantDigits,
            int maxFractionDigits) {
        this.quantityStyleProvider = quantityStyleProvider;
        this.unitInfo = unitInfo;
        this.quantityFormat = new QuantityFormat();
        this.quantityFormat.setSignificantDigits(significantDigits);
        this.quantityFormat.setMaximumFractionDigits(maxFractionDigits);
    }

    @Override
    public Quantity<Q> fromString(@Nullable String value) {
        if (value == null) {
            return null;
        }

        try {
            Unit<Q> unit = unitInfo.getPreferredUnit(quantityStyleProvider.getSystemOfMeasurement());
            return quantityFormat.parse(value, unit).convertTo(unit);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString(@Nullable Quantity<Q> value) {
        if (value == null) {
            return "";
        }

        return quantityFormat.format(value);
    }

}
