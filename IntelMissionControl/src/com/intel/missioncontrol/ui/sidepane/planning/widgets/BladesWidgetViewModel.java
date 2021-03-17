/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.binding.QuantityBindings;
import com.intel.missioncontrol.beans.property.PropertyPathStore;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.beans.property.SimpleQuantityProperty;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** Created by akorotenko on 7/3/17. */
public class BladesWidgetViewModel implements ViewModel {

    private static final double RADIUS_MIN = 1.0;

    private final QuantityProperty<Length> bladeDiameterQuantity;
    private final Property<Number> bladeDiameter = new SimpleDoubleProperty(RADIUS_MIN);
    private final StringProperty bladeDiameterLabelProperty = new SimpleStringProperty();

    private final QuantityProperty<Length> bladeLengthQuantity;
    private final Property<Number> bladeLength = new SimpleDoubleProperty(0.0);
    private final StringProperty bladeLengthLabelProperty = new SimpleStringProperty();

    private final QuantityProperty<Length> bladeThinRadiusQuantity;
    private final Property<Number> bladeThinRadius = new SimpleDoubleProperty(0.0);
    private final StringProperty bladeThinRadiusLabelProperty = new SimpleStringProperty();

    private final QuantityProperty<Length> bladeCoverLengthQuantity;
    private final Property<Number> bladeCoverLength = new SimpleDoubleProperty(0.0);
    private final StringProperty bladeCoverLengthLabelProperty = new SimpleStringProperty();

    private final QuantityProperty<Angle> bladePitchQuantity;
    private final Property<Number> bladePitch = new SimpleDoubleProperty(0.0);
    private final StringProperty bladePitchLabelProperty = new SimpleStringProperty();

    private final QuantityProperty<Angle> bladeStartRotationQuantity;
    private final Property<Number> bladeStartRotation = new SimpleDoubleProperty(0.0);
    private final StringProperty bladeStartRotationLabelProperty = new SimpleStringProperty();

    private final QuantityProperty<Length> numberOfBladesQuantity;
    private final Property<Number> numberOfBlades = new SimpleIntegerProperty(3);
    private final StringProperty numberOfBladesLabelProperty = new SimpleStringProperty();

    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @Inject
    public BladesWidgetViewModel(
            ISettingsManager settingsManager, IApplicationContext applicationContext, ILanguageHelper languageHelper) {
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;

        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

        bladeDiameterQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(bladeDiameterQuantity, bladeDiameter, Unit.METER);

        bladeLengthQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(bladeLengthQuantity, bladeLength, Unit.METER);

        bladeThinRadiusQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(bladeThinRadiusQuantity, bladeThinRadius, Unit.METER);

        bladeCoverLengthQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(bladeCoverLengthQuantity, bladeCoverLength, Unit.METER);

        bladePitchQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));
        QuantityBindings.bindBidirectional(bladePitchQuantity, bladePitch, Unit.DEGREE);

        bladeStartRotationQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.ANGLE_DEGREES, Quantity.of(0.0, Unit.DEGREE));
        QuantityBindings.bindBidirectional(bladeStartRotationQuantity, bladeStartRotation, Unit.DEGREE);

        numberOfBladesQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(numberOfBladesQuantity, numberOfBlades, Unit.METER);
    }

    public double getBladeDiameter() {
        return bladeDiameter.getValue().doubleValue();
    }

    public Property<Number> bladeDiameterProperty() {
        return bladeDiameter;
    }

    public QuantityProperty<Length> bladeDiameterQuantityProperty() {
        return bladeDiameterQuantity;
    }

    public double getbladeLength() {
        return bladeLength.getValue().doubleValue();
    }

    public Property<Number> bladeLengthProperty() {
        return bladeLength;
    }

    public QuantityProperty<Length> bladeLengthQuantityProperty() {
        return bladeLengthQuantity;
    }

    public double getbladeThinRadius() {
        return bladeThinRadius.getValue().doubleValue();
    }

    public Property<Number> bladeThinRadiusProperty() {
        return bladeThinRadius;
    }

    public QuantityProperty<Length> bladeThinRadiusQuantityProperty() {
        return bladeThinRadiusQuantity;
    }

    public double getbladeCoverLength() {
        return bladeCoverLength.getValue().doubleValue();
    }

    public Property<Number> bladeCoverLengthProperty() {
        return bladeCoverLength;
    }

    public QuantityProperty<Length> bladeCoverLengthQuantityProperty() {
        return bladeCoverLengthQuantity;
    }

    public double getbladePitch() {
        return bladePitch.getValue().doubleValue();
    }

    public Property<Number> bladePitchProperty() {
        return bladePitch;
    }

    public QuantityProperty<Angle> bladePitchQuantityProperty() {
        return bladePitchQuantity;
    }

    public double getbladeStartRotation() {
        return bladeStartRotation.getValue().doubleValue();
    }

    public Property<Number> bladeStartRotationProperty() {
        return bladeStartRotation;
    }

    public QuantityProperty<Angle> bladeStartRotationQuantityProperty() {
        return bladeStartRotationQuantity;
    }

    public int getNumberOfBlades() {
        return numberOfBlades.getValue().intValue();
    }

    public Property<Number> numberOfBladesProperty() {
        return numberOfBlades;
    }

    public QuantityProperty<Length> numberOfBladesQuantityProperty() {
        return numberOfBladesQuantity;
    }
}
