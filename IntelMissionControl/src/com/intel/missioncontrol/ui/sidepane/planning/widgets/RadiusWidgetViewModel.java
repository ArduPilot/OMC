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
import com.intel.missioncontrol.beans.binding.QuantityBindings;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.beans.property.SimpleQuantityProperty;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.core.flightplan.CPicArea;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;

public class RadiusWidgetViewModel implements ViewModel {

    private static final double RADIUS_MIN = CPicArea.MIN_CORRIDOR_WIDTH_METER;

    private final QuantityProperty<Length> radiusQuantity;
    private final Property<Number> radius = new SimpleDoubleProperty(RADIUS_MIN);

    @Inject
    public RadiusWidgetViewModel(ISettingsManager settingsManager) {
        radiusQuantity =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(0.0, Unit.METER));

        QuantityBindings.bindBidirectional(radiusQuantity, radius, Unit.METER);
    }

    public Property<Number> radiusProperty() {
        return radius;
    }

    public double getRadius() {
        return radius.getValue().doubleValue();
    }

    public QuantityProperty<Length> radiusQuantityProperty() {
        return radiusQuantity;
    }

}
