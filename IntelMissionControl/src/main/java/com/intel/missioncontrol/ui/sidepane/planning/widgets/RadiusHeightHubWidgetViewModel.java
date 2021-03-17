/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.asyncfx.beans.property.PropertyPathStore;

public class RadiusHeightHubWidgetViewModel implements ViewModel {

    private static final double RADIUS_MIN = 1.0;

    private final QuantityProperty<Length> radiusQuantity;
    private final Property<Number> radius = new SimpleDoubleProperty(RADIUS_MIN);

    private final QuantityProperty<Length> heightQuantity;
    private final Property<Number> height = new SimpleDoubleProperty(0.0);

    private final StringProperty heightLabelProperty = new SimpleStringProperty();

    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @Inject
    public RadiusHeightHubWidgetViewModel(
            ISettingsManager settingsManager, IApplicationContext applicationContext, ILanguageHelper languageHelper) {
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;

        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

        radiusQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));

        heightQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));

        QuantityBindings.bindBidirectional(radiusQuantity, radius, Unit.METER);
        QuantityBindings.bindBidirectional(heightQuantity, height, Unit.METER);
    }

    public double getRadius() {
        return radius.getValue().doubleValue();
    }

    public Property<Number> radiusProperty() {
        return radius;
    }

    public double getHeight() {
        return height.getValue().doubleValue();
    }

    public Property<Number> heightProperty() {
        return height;
    }

    public QuantityProperty<Length> radiusQuantityProperty() {
        return radiusQuantity;
    }

    public QuantityProperty<Length> heightQuantityProperty() {
        return heightQuantity;
    }

}
