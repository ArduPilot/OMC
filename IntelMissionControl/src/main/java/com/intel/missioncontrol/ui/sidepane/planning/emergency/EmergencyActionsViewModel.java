/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.emergency;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.QuantityBindings;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import de.saxsys.mvvmfx.InjectScope;
import eu.mavinci.core.plane.AirplaneEventActions;
import java.time.Duration;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.asyncfx.beans.binding.BidirectionalValueConverter;
import org.asyncfx.beans.binding.ConversionBindings;
import org.asyncfx.beans.property.PropertyPathStore;

/** ViewModel for Emergency Actions mission settings panel. */
public class EmergencyActionsViewModel extends ViewModelBase {

    @InjectScope
    private PlanningScope planningScope;

    private final BooleanProperty autoComputeSafetyHeight = new SimpleBooleanProperty();
    private final QuantityProperty<Length> safetyAltitudeQuantity;
    private final QuantityProperty<Dimension.Time> rcLinkLossDurationQuantity;
    private final QuantityProperty<Dimension.Time> primaryLinkLossDurationQuantity;
    private final QuantityProperty<Dimension.Time> gnssLinkLossDurationQuantity;
    private final QuantityProperty<Dimension.Time> geofenceBreachDurationQuantity;

    private final DoubleProperty safetyAltitudeMeterProperty = new SimpleDoubleProperty();
    private final IntegerProperty rcLinkLossActionDelaySecondsProperty = new SimpleIntegerProperty();
    private final IntegerProperty primaryLinkLossActionDelaySecondsProperty = new SimpleIntegerProperty();
    private final IntegerProperty gnssLinkLossActionDelaySecondsProperty = new SimpleIntegerProperty();
    private final IntegerProperty geofenceBreachActionDelaySecondsProperty = new SimpleIntegerProperty();

    @SuppressWarnings("FieldCanBeLocal")
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    private ObjectProperty<AirplaneEventActions> geofenceBreachActionProperty = new SimpleObjectProperty<>();
    private ObjectProperty<AirplaneEventActions> gnssLinkLossActionProperty = new SimpleObjectProperty<>();
    private ObjectProperty<AirplaneEventActions> primaryLinkLossActionProperty = new SimpleObjectProperty<>();
    private ObjectProperty<AirplaneEventActions> rcLinkLossActionProperty = new SimpleObjectProperty<>();

    private final BooleanProperty emergencyActionsSettable = new SimpleBooleanProperty();

    @Inject
    public EmergencyActionsViewModel(ISettingsManager settingsManager, IApplicationContext applicationContext) {
        GeneralSettings settings = settingsManager.getSection(GeneralSettings.class);

        autoComputeSafetyHeight.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectBoolean(FlightPlan::autoComputeSafetyHeightProperty));

        safetyAltitudeMeterProperty.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectDouble(FlightPlan::safetyAltitudeProperty));

        final BidirectionalValueConverter<Duration, Number> durationToSecondsConverter =
            new BidirectionalValueConverter<>() {
                @Override
                public Number convert(Duration value) {
                    return value != null ? value.toSeconds() : 0;
                }

                @Override
                public Duration convertBack(Number value) {
                    return value != null ? Duration.ofSeconds(value.longValue()) : Duration.ZERO;
                }
            };

        ConversionBindings.bindBidirectional(
            rcLinkLossActionDelaySecondsProperty,
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::rcLinkLossActionDelayProperty),
            durationToSecondsConverter);

        ConversionBindings.bindBidirectional(
            primaryLinkLossActionDelaySecondsProperty,
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::primaryLinkLossActionDelayProperty),
            durationToSecondsConverter);

        ConversionBindings.bindBidirectional(
            gnssLinkLossActionDelaySecondsProperty,
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::positionLossActionDelayProperty),
            durationToSecondsConverter);

        ConversionBindings.bindBidirectional(
            geofenceBreachActionDelaySecondsProperty,
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::geofenceBreachActionDelayProperty),
            durationToSecondsConverter);

        rcLinkLossActionProperty.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::rcLinkLossActionProperty));

        primaryLinkLossActionProperty.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::primaryLinkLossActionProperty));

        gnssLinkLossActionProperty.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::positionLossActionProperty));

        geofenceBreachActionProperty.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::geofenceBreachActionProperty));

        safetyAltitudeQuantity =
            new SimpleQuantityProperty<>(
                settings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(safetyAltitudeMeterProperty.getValue(), Unit.METER));

        rcLinkLossDurationQuantity =
            new SimpleQuantityProperty<>(
                settings,
                UnitInfo.TIME_SECONDS,
                Quantity.of(rcLinkLossActionDelaySecondsProperty.getValue(), Unit.SECOND));

        primaryLinkLossDurationQuantity =
            new SimpleQuantityProperty<>(
                settings,
                UnitInfo.TIME_SECONDS,
                Quantity.of(primaryLinkLossActionDelaySecondsProperty.getValue(), Unit.SECOND));

        gnssLinkLossDurationQuantity =
            new SimpleQuantityProperty<>(
                settings,
                UnitInfo.TIME_SECONDS,
                Quantity.of(gnssLinkLossActionDelaySecondsProperty.getValue(), Unit.SECOND));

        geofenceBreachDurationQuantity =
            new SimpleQuantityProperty<>(
                settings,
                UnitInfo.TIME_SECONDS,
                Quantity.of(geofenceBreachActionDelaySecondsProperty.getValue(), Unit.SECOND));

        QuantityBindings.bindBidirectional(safetyAltitudeQuantity, safetyAltitudeMeterProperty, Unit.METER);
        QuantityBindings.bindBidirectional(
            rcLinkLossDurationQuantity, rcLinkLossActionDelaySecondsProperty, Unit.SECOND);
        QuantityBindings.bindBidirectional(
            primaryLinkLossDurationQuantity, primaryLinkLossActionDelaySecondsProperty, Unit.SECOND);
        QuantityBindings.bindBidirectional(
            gnssLinkLossDurationQuantity, gnssLinkLossActionDelaySecondsProperty, Unit.SECOND);
        QuantityBindings.bindBidirectional(
            geofenceBreachDurationQuantity, geofenceBreachActionDelaySecondsProperty, Unit.SECOND);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        emergencyActionsSettable.bind(
            Bindings.createBooleanBinding(
                () -> {
                    IHardwareConfiguration hwConfig = planningScope.selectedHardwareConfigurationProperty().get();

                    return hwConfig != null && hwConfig.getPlatformDescription().areEmergencyActionsSettable();
                },
                planningScope.selectedHardwareConfigurationProperty()));
    }

    QuantityProperty<Length> safetyAltitudeQuantityProperty() {
        return safetyAltitudeQuantity;
    }

    QuantityProperty<Dimension.Time> rcLinkLossDurationQuantityProperty() {
        return rcLinkLossDurationQuantity;
    }

    QuantityProperty<Dimension.Time> primaryLinkLossDurationQuantityProperty() {
        return primaryLinkLossDurationQuantity;
    }

    QuantityProperty<Dimension.Time> gnssLinkLossDurationQuantityProperty() {
        return gnssLinkLossDurationQuantity;
    }

    QuantityProperty<Dimension.Time> geofenceBreachDurationQuantityProperty() {
        return geofenceBreachDurationQuantity;
    }

    BooleanProperty autoComputeSafetyHeightProperty() {
        return autoComputeSafetyHeight;
    }

    ObjectProperty<AirplaneEventActions> geofenceBreachComboBoxProperty() {
        return geofenceBreachActionProperty;
    }

    ObjectProperty<AirplaneEventActions> gnssLinkLossComboBoxProperty() {
        return gnssLinkLossActionProperty;
    }

    ObjectProperty<AirplaneEventActions> primaryLinkLossComboBoxProperty() {
        return primaryLinkLossActionProperty;
    }

    ObjectProperty<AirplaneEventActions> rcLinkLossComboBoxProperty() {
        return rcLinkLossActionProperty;
    }

    ReadOnlyBooleanProperty emergencyActionsSettableProperty() {
        return emergencyActionsSettable;
    }
}
