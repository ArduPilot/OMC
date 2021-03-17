/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.google.inject.Inject;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.QuantityBindings;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class AoiDimensionsTabViewModel extends ViewModelBase<AreaOfInterest> {

    private final QuantityProperty<Length> cropHeightMinQuantity;
    private final DoubleProperty cropHeightMin = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Length> cropHeightMaxQuantity;
    private final DoubleProperty cropHeightMax = new SimpleDoubleProperty(0.0);

    private AreaOfInterest areaOfInterest;

    @Inject
    public AoiDimensionsTabViewModel(ISettingsManager settingsManager) {
        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

        cropHeightMinQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));

        cropHeightMaxQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));

        QuantityBindings.bindBidirectional(cropHeightMinQuantity, cropHeightMin, Unit.METER);
        QuantityBindings.bindBidirectional(cropHeightMaxQuantity, cropHeightMax, Unit.METER);
    }

    @Override
    public void initializeViewModel(AreaOfInterest areaOfInterest) {
        super.initializeViewModel(areaOfInterest);
        this.areaOfInterest = areaOfInterest;
        cropHeightMin.bindBidirectional(areaOfInterest.cropHeightMinProperty());
        cropHeightMax.bindBidirectional(areaOfInterest.cropHeightMaxProperty());
    }

    public QuantityProperty<Length> cropHeightMinQuantityProperty() {
        return cropHeightMinQuantity;
    }

    public QuantityProperty<Length> cropHeightMaxQuantityProperty() {
        return cropHeightMaxQuantity;
    }

    public AreaOfInterest getAreaOfInterest() {
        return areaOfInterest;
    }

}
