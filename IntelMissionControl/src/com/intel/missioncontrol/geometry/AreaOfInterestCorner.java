/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.beans.property.SimpleQuantityProperty;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.bindings.BeanAdapter;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.flightplan.PointWithAltitudes;
import gov.nasa.worldwind.geom.Position;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class AreaOfInterestCorner {
    private final PointWithAltitudes legacyPoint;
    private final CPicArea legacyArea;
    private final BeanAdapter<PointWithAltitudes> beanAdapter;
    private final IntegerProperty index = new SimpleIntegerProperty();
    private final SimpleQuantityProperty<Dimension.Angle> lon;
    private final SimpleQuantityProperty<Dimension.Angle> lat;
    private final SimpleQuantityProperty<Dimension.Length> altAboveRefPoint;
    private final SimpleQuantityProperty<Dimension.Length> altAboveTakeoff;
    private final ObjectProperty<Position> cornerPosition = new SimpleObjectProperty<>();
    private final BooleanProperty isReferencePoint = new SimpleBooleanProperty(false);

    private final IFlightplanChangeListener fpListener =
        new IFlightplanChangeListener() {
            @Override
            public void flightplanStructureChanged(IFlightplanRelatedObject fp) {}

            @Override
            public void flightplanValuesChanged(IFlightplanRelatedObject fpObj) {
                if (fpObj == legacyPoint.getPoint() || fpObj == legacyArea) {
                    Dispatcher.runOnUI(() -> updateValuesFromSource());
                }
            }

            @Override
            public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {}

            @Override
            public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {}
        };

    public AreaOfInterestCorner(
            int idx, PointWithAltitudes point, IQuantityStyleProvider quantityStyleProvider, CPicArea legacyArea) {
        this.legacyPoint = point;
        this.legacyArea = legacyArea;
        this.beanAdapter = new BeanAdapter<>(legacyPoint);
        this.index.set(idx);
        lon = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.ANGLE_DEGREES);
        lat = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.ANGLE_DEGREES);
        altAboveRefPoint =
            new SimpleQuantityProperty<>(
                quantityStyleProvider, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
        altAboveTakeoff =
            new SimpleQuantityProperty<>(
                quantityStyleProvider, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));

        final var legacyFlightplan = point.getFlightplan();
        Expect.notNull(legacyFlightplan, "legacyFlightplan");
        legacyFlightplan.addFPChangeListener(fpListener);
        beanAdapter
            .bind(lat)
            .to(
                wp -> Quantity.of(wp.getLat(), Unit.DEGREE),
                (wp, value) -> wp.setLat(value.convertTo(Unit.DEGREE).getValue().doubleValue()));
        beanAdapter
            .bind(lon)
            .to(
                wp -> Quantity.of(wp.getLon(), Unit.DEGREE),
                (wp, value) -> wp.setLon(value.convertTo(Unit.DEGREE).getValue().doubleValue()));

        beanAdapter
            .bind(cornerPosition)
            .to(
                wp -> new Position(wp.getLatLon().latitude, wp.getLatLon().longitude, 0),
                (wp, value) -> {
                    wp.setLatLon(value.latitude.degrees, value.longitude.degrees);
                });

        beanAdapter
            .bind(altAboveRefPoint)
            .to(
                wp -> Quantity.of(wp.getAltInMAboveFPRefPoint(), Unit.METER),
                (wp, value) -> {
                    wp.setAltInMAboveFPRefPoint(value.convertTo(Unit.METER).getValue().doubleValue());
                });
        beanAdapter.bind(altAboveTakeoff).to(wp -> Quantity.of(wp.getAltInMAboveTakeoff(), Unit.METER));
    }

    public ReadOnlyIntegerProperty indexProperty() {
        return index;
    }

    public QuantityProperty<Dimension.Angle> latProperty() {
        return lat;
    }

    public QuantityProperty<Dimension.Angle> lonProperty() {
        return lon;
    }

    public void deleteMe() {
        legacyPoint.getParent().removeFromFlightplanContainer(legacyPoint.getPoint());
    }

    public PointWithAltitudes getLegacyPoint() {
        return legacyPoint;
    }

    protected void updateValuesFromSource() {
        beanAdapter.updateValuesFromSource();
    }

    public int getIndex() {
        return index.get();
    }

    public ObjectProperty<Position> cornerPositionProperty() {
        return cornerPosition;
    }

    public QuantityProperty<Dimension.Length> altAboveRefPointProperty() {
        return altAboveRefPoint;
    }

    public QuantityProperty<Dimension.Length> altAboveTakeoffProperty() {
        return altAboveTakeoff;
    }

    public boolean isReferencePoint() {
        return isReferencePoint.get();
    }

    public BooleanProperty isReferencePointProperty() {
        return isReferencePoint;
    }

    public void removeFPListeners() {
        final var legacyFlightplan = legacyPoint.getFlightplan();
        Expect.notNull(legacyFlightplan, "legacyFlightplan");
        legacyFlightplan.removeFPChangeListener(fpListener);
    }

    public String getAreaName() {
        return legacyArea.getName();
    }

}
