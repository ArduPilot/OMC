/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.inject.Inject;
import com.intel.missioncontrol.beans.binding.QuantityBindings;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.beans.property.SimpleQuantityProperty;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.PlanType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

public class NoFlyWidgetViewModel implements ViewModel {

    private final QuantityProperty<Dimension.Length> floorQuantity;
    private final QuantityProperty<Dimension.Length> ceilingQuantity;
    private final QuantityProperty<Dimension.Length> egmOffsetQuantity;

    private final DoubleProperty floor = new SimpleDoubleProperty();
    private final DoubleProperty ceiling = new SimpleDoubleProperty();
    private final DoubleProperty egmOffset = new SimpleDoubleProperty();

    private final BooleanProperty floorEnabled = new SimpleBooleanProperty();
    private final BooleanProperty ceilingEnabled = new SimpleBooleanProperty();

    private final ObjectProperty<CPicArea.RestrictedAreaHeightReferenceTypes> floorReference =
        new SimpleObjectProperty<>();
    private final ObjectProperty<CPicArea.RestrictedAreaHeightReferenceTypes> ceilingReference =
        new SimpleObjectProperty<>();

    private final ObjectProperty<PlanType> aoiType = new SimpleObjectProperty<>();
    private final ObjectProperty<AreaOfInterest> aoi = new SimpleObjectProperty<>();

    private final BooleanProperty needRadius = new SimpleBooleanProperty();
    private final QuantityProperty<Dimension.Length> radiusQuantity;
    private final DoubleProperty radius = new SimpleDoubleProperty(CPicArea.MIN_CORRIDOR_WIDTH_METER);

    @Inject
    public NoFlyWidgetViewModel(ISettingsManager settingsManager, IMapModel mapModel, IMapController mapController) {
        aoi.addListener(
            (observable, oldValue, newValue) -> {
                bindMe();
            });

        floorQuantity =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(floorQuantity, floor, Unit.METER);

        ceilingQuantity =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(ceilingQuantity, ceiling, Unit.METER);

        needRadius.bind(
            javafx.beans.binding.Bindings.createBooleanBinding(
                () -> {
                    PlanType p = aoiType.get();
                    return p == null ? false : p.isCircular();
                },
                aoiType));

        egmOffsetQuantity =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(egmOffsetQuantity, egmOffset, Unit.METER);

        aoiType.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue == null) {
                    return;
                }

                AreaOfInterest aoi = aoiProperty().get();
                if (aoi != null
                        && newValue.getMinCorners() < aoi.cornerListProperty().size()
                        && aoi.isInitialAddingProperty().getValue()) {
                    // check if mouse mode is adding waypoints, if yes, switch it back to edit since due to a
                    // change from poly to circle we might otherwise are stuck in adding meaningless points
                    if (mapController.getMouseMode() == InputMode.ADD_POINTS) {
                        mapController.tryCancelMouseModes(InputMode.ADD_POINTS);
                    }
                }
            });

        radiusQuantity =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(radiusQuantity, radius, Unit.METER);
    }

    private void bindMe() {
        aoiType.unbind();

        AreaOfInterest aoi = this.aoi.get();
        if (aoi == null) {
            return;
        }

        aoiType.bindBidirectional(aoi.typeProperty());
        floor.bindBidirectional(aoi.restrictionFloorProperty());
        floorEnabled.bindBidirectional(aoi.restrictionFloorEnabledProperty());
        floorReference.bindBidirectional(aoi.restrictionFloorRefProperty());

        egmOffset.bind(aoi.restrictionEgmOffsetProperty());

        ceiling.bindBidirectional(aoi.restrictionCeilingProperty());
        ceilingEnabled.bindBidirectional(aoi.restrictionCeilingEnabledProperty());
        ceilingReference.bindBidirectional(aoi.restrictionCeilingRefProperty());

        radius.bindBidirectional(aoi.widthProperty());
    }

    public ObjectProperty<PlanType> aoiTypeProperty() {
        return aoiType;
    }

    public ObjectProperty<AreaOfInterest> aoiProperty() {
        return aoi;
    }

    public QuantityProperty<Dimension.Length> floorQuantityProperty() {
        return floorQuantity;
    }

    public BooleanProperty floorEnabledProperty() {
        return floorEnabled;
    }

    public ObjectProperty<CPicArea.RestrictedAreaHeightReferenceTypes> floorReferenceProperty() {
        return floorReference;
    }

    public QuantityProperty<Dimension.Length> ceilingQuantityProperty() {
        return ceilingQuantity;
    }

    public BooleanProperty ceilingEnabledProperty() {
        return ceilingEnabled;
    }

    public ObjectProperty<CPicArea.RestrictedAreaHeightReferenceTypes> ceilingReferenceProperty() {
        return ceilingReference;
    }

    public BooleanProperty needRadiusProperty() {
        return needRadius;
    }

    public QuantityProperty<Dimension.Length> radiusQuantityProperty() {
        return radiusQuantity;
    }

    public QuantityProperty<Dimension.Length> egmOffsetProperty() {
        return egmOffsetQuantity;
    }
}
