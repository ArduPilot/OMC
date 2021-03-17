/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.AngleStyle;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.SystemOfMeasurement;
import com.intel.missioncontrol.measure.TimeStyle;

@SuppressWarnings("FieldCanBeLocal")
public class AdaptiveQuantityFormat extends QuantityFormat {

    private final IQuantityStyleProvider quantityStyleProvider;

    public AdaptiveQuantityFormat(IQuantityStyleProvider quantityStyleProvider) {
        this.quantityStyleProvider = quantityStyleProvider;
    }

    @Override
    public void setSystemOfMeasurement(SystemOfMeasurement systemOfMeasurement) {
        throw new UnsupportedOperationException(
            "System of measurement is managed by " + AdaptiveQuantityFormat.class.getSimpleName());
    }

    @Override
    public SystemOfMeasurement getSystemOfMeasurement() {
        return quantityStyleProvider.getSystemOfMeasurement();
    }

    @Override
    public void setAngleStyle(AngleStyle angleStyle) {
        throw new UnsupportedOperationException(
            "Angle style is managed by " + AdaptiveQuantityFormat.class.getSimpleName());
    }

    @Override
    public AngleStyle getAngleStyle() {
        return quantityStyleProvider.getAngleStyle();
    }

    @Override
    public void setTimeStyle(TimeStyle timeStyle) {
        throw new UnsupportedOperationException(
            "Time style is managed by " + AdaptiveQuantityFormat.class.getSimpleName());
    }

    @Override
    public TimeStyle getTimeStyle() {
        return quantityStyleProvider.getTimeStyle();
    }

}
