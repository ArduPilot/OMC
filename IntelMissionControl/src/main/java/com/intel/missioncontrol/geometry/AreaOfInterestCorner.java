/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.mission.bindings.BeanAdapter;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.flightplan.Point;
import eu.mavinci.flightplan.PointWithAltitudes;
import gov.nasa.worldwind.geom.Position;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.asyncfx.concurrent.Dispatcher;

public class AreaOfInterestCorner {

    private final ISettingsManager settingsManager;

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

    private final StringProperty note = new SimpleStringProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    private final SimpleQuantityProperty<Dimension.Angle> pitch;
    private final SimpleQuantityProperty<Dimension.Angle> yaw;
    private final SimpleQuantityProperty<Dimension.Length> resolution;
    private final BooleanProperty triggerImage = new SimpleBooleanProperty(true);
    private final SimpleQuantityProperty<Dimension.Length> distance;
    private final SimpleQuantityProperty<Dimension.Length> frameDiag;
    private final BooleanProperty target = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<Point.DistanceSource> distanceSource = new SimpleObjectProperty<>(null);

    private final IFlightplanChangeListener fpListener =
        new IFlightplanChangeListener() {
            @Override
            public void flightplanStructureChanged(IFlightplanRelatedObject fp) {}

            @Override
            public void flightplanValuesChanged(IFlightplanRelatedObject fpObj) {
                if (fpObj == legacyPoint.getPoint() || fpObj == legacyArea) {
                    // FIXME: this was Dispatcher.runOnUI before, why was this a blocking call?
                    Dispatcher.platform().run(() -> updateValuesFromSource());
                }
            }

            @Override
            public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {}

            @Override
            public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {}
        };

    public AreaOfInterestCorner(
            int idx,
            PointWithAltitudes point,
            IQuantityStyleProvider quantityStyleProvider,
            CPicArea legacyArea,
            ISettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        this.legacyPoint = point;
        this.legacyArea = legacyArea;
        this.beanAdapter = new BeanAdapter<>(legacyPoint);
        this.index.set(idx);

        lon = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.ANGLE_DEGREES);
        lat = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.ANGLE_DEGREES);
        altAboveRefPoint =
            new SimpleQuantityProperty<>(
                quantityStyleProvider, UnitInfo.LOCALIZED_LENGTH, Quantity.of(10.0, Unit.METER));
        altAboveTakeoff =
            new SimpleQuantityProperty<>(
                quantityStyleProvider, UnitInfo.LOCALIZED_LENGTH, Quantity.of(10.0, Unit.METER));

        pitch =
            new SimpleQuantityProperty<Dimension.Angle>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.ANGLE_DEGREES,
                Quantity.of(0.0, Unit.DEGREE));
        yaw =
            new SimpleQuantityProperty<Dimension.Angle>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.ANGLE_DEGREES,
                Quantity.of(0.0, Unit.DEGREE));

        resolution =
            new SimpleQuantityProperty<Dimension.Length>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(5.0, Unit.CENTIMETER));

        distance =
            new SimpleQuantityProperty<Dimension.Length>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(0.0, Unit.METER));

        frameDiag =
            new SimpleQuantityProperty<Dimension.Length>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(50.0, Unit.CENTIMETER));

        final var legacyFlightplan = point.getFlightplan();
        if (legacyFlightplan != null) {
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
                        wp.getPoint().setAltitude(value.convertTo(Unit.METER).getValue().doubleValue());
                    });
            beanAdapter.bind(altAboveTakeoff).to(wp -> Quantity.of(wp.getAltInMAboveTakeoff(), Unit.METER));

            beanAdapter
                .bind(cornerPosition)
                .to(
                    wp -> new Position(wp.getLatLon().latitude, wp.getLatLon().longitude, 0),
                    (wp, value) -> {
                        wp.setLatLon(value.latitude.degrees, value.longitude.degrees);
                    });

            beanAdapter
                .bind(pitch)
                .to(
                    wp -> Quantity.of(wp.getPitch(), Unit.DEGREE),
                    (wp, value) -> wp.setPitch(value.convertTo(Unit.DEGREE).getValue().doubleValue()));

            beanAdapter
                .bind(yaw)
                .to(
                    wp -> Quantity.of(wp.getYaw(), Unit.DEGREE),
                    (wp, value) -> wp.setYaw(value.convertTo(Unit.DEGREE).getValue().doubleValue()));

            beanAdapter.bind(note).to(wp -> wp.getNote(), (wp, value) -> wp.setNote(value));

            beanAdapter.bind(triggerImage).to(wp -> wp.isTriggerImage(), (wp, value) -> wp.setTriggerImage(value));

            beanAdapter
                .bind(distanceSource)
                .to(wp -> wp.getDistanceSource(), (wp, value) -> wp.setDistanceSource(value));

            beanAdapter.bind(target).to(wp -> wp.isTarget(), (wp, value) -> wp.setTarget(value));

            beanAdapter
                .bind(distance)
                .to(
                    wp -> Quantity.of(wp.getDistanceMeter(), Unit.METER),
                    (wp, value) -> wp.setDistanceMeter(value.convertTo(Unit.METER).getValue().doubleValue()));

            beanAdapter
                .bind(frameDiag)
                .to(
                    wp -> Quantity.of(wp.getFrameDiag(), Unit.METER),
                    (wp, value) -> wp.setFrameDiag(value.convertTo(Unit.METER).getValue().doubleValue()));

            beanAdapter
                .bind(resolution)
                .to(
                    wp -> Quantity.of(wp.getGsdMeter(), Unit.METER),
                    (wp, value) -> wp.setGsdMeter(value.convertTo(Unit.METER).getValue().doubleValue()));
        }
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

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.setValue(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public Quantity<Dimension.Angle> getPitch() {
        return pitch.get();
    }

    public SimpleQuantityProperty<Dimension.Angle> pitchProperty() {
        return pitch;
    }

    public Quantity<Dimension.Angle> getYaw() {
        return yaw.get();
    }

    public SimpleQuantityProperty<Dimension.Angle> yawProperty() {
        return yaw;
    }

    public boolean isTriggerImage() {
        return triggerImage.get();
    }

    public BooleanProperty triggerImageProperty() {
        return triggerImage;
    }

    public Quantity<Dimension.Length> getResolution() {
        return resolution.get();
    }

    public SimpleQuantityProperty<Dimension.Length> resolutionProperty() {
        return resolution;
    }

    public Quantity<Dimension.Length> getDistance() {
        return distance.get();
    }

    public SimpleQuantityProperty<Dimension.Length> distanceProperty() {
        return distance;
    }

    public Quantity<Dimension.Length> getFrameDiag() {
        return frameDiag.get();
    }

    public SimpleQuantityProperty<Dimension.Length> frameDiagProperty() {
        return frameDiag;
    }

    public String getNote() {
        return note.get();
    }

    public StringProperty noteProperty() {
        return note;
    }

    public boolean isTarget() {
        return target.get();
    }

    public BooleanProperty targetProperty() {
        return target;
    }

    public SimpleObjectProperty<Point.DistanceSource> distanceSourceProperty() {
        return distanceSource;
    }
}
