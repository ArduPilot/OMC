/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.inject.Inject;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.QuantityBindings;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;

public class WidthWidgetViewModel implements ViewModel {

    private static final double WIDTH_MIN = 1.0;

    private final QuantityProperty<Length> widthQuantity;
    private final Property<Number> width = new SimpleDoubleProperty(WIDTH_MIN);

    @Inject
    public WidthWidgetViewModel(ISettingsManager settingsManager) {
        widthQuantity =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(0.0, Unit.METER));

        QuantityBindings.bindBidirectional(widthQuantity, width, Unit.METER);
    }

    public double getWidth() {
        return width.getValue().doubleValue();
    }

    public Property<Number> widthProperty() {
        return width;
    }

    public QuantityProperty<Length> widthQuantityProperty() {
        return widthQuantity;
    }

}
