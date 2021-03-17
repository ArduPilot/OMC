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

public class HeightWidgetViewModel implements ViewModel {

    private final StringProperty heightLabel = new SimpleStringProperty();
    private final QuantityProperty<Length> heightQuantity;
    private final Property<Number> height = new SimpleDoubleProperty(0.0);

    private final PropertyPathStore propertyPathStore = new PropertyPathStore();
    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;

    @Inject
    public HeightWidgetViewModel(
            ISettingsManager settingsManager, IApplicationContext applicationContext, ILanguageHelper languageHelper) {
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        heightQuantity =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(0.0, Unit.METER));

        QuantityBindings.bindBidirectional(heightQuantity, height, Unit.METER);
    }

    public double getHeight() {
        return height.getValue().doubleValue();
    }

    public Property<Number> heightProperty() {
        return height;
    }

    public QuantityProperty<Length> heightQuantityProperty() {
        return heightQuantity;
    }

}
