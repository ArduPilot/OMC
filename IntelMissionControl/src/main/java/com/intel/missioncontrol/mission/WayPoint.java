/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.mission.bindings.BeanAdapter;
import eu.mavinci.core.flightplan.AltAssertModes;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.ReentryPoint;
import eu.mavinci.core.flightplan.SpeedMode;
import eu.mavinci.flightplan.Waypoint;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.concurrent.Dispatcher;

public class WayPoint {

    private final Waypoint legacyWaypoint;
    private final BeanAdapter<Waypoint> beanAdapter;
    private final Object mutex = new Object();
    private final UIAsyncBooleanProperty selected = new UIAsyncBooleanProperty(this);
    private final IntegerProperty numberInFlight;
    private SimpleQuantityProperty<Angle> lon;
    private SimpleQuantityProperty<Angle> lat;
    // above ref point
    private SimpleQuantityProperty<Length> altitudeAboveR;
    private SimpleQuantityProperty<Length> altitudeAboveTakeoff;
    private ObjectProperty<AltAssertModes> assertAltitude;
    private SimpleQuantityProperty<Length> radius;
    private StringProperty body;
    private IntegerProperty id;
    private SimpleQuantityProperty<Angle> roll;
    private SimpleQuantityProperty<Angle> pitch;
    private SimpleQuantityProperty<Angle> yaw;
    private BooleanProperty ignore;
    private BooleanProperty assertYawOn;
    private SimpleQuantityProperty<Angle> assertYaw;
    private ObjectProperty<SpeedMode> speedMode;
    private SimpleQuantityProperty<Dimension.Speed> speed;
    private SimpleQuantityProperty<Dimension.Time> stopHereTimeCopter;
    private BooleanProperty triggerImageHereCopterMode;
    private BooleanProperty isBeginFlightline;

    private final BooleanProperty airspaceWarning = new SimpleBooleanProperty();
    private final BooleanProperty heightWarning = new SimpleBooleanProperty();

    private final ReadOnlyBooleanWrapper warning = new ReadOnlyBooleanWrapper();
    private final SimpleQuantityProperty<Length> groundDistance;

    private boolean isInitialized;
    private IFlightplanChangeListener fpListener;
    private final IQuantityStyleProvider quantityStyleProvider;

    public WayPoint(Waypoint legacyWaypoint, IQuantityStyleProvider quantityStyleProvider, int noInFlight) {
        this.legacyWaypoint = legacyWaypoint;
        this.quantityStyleProvider = quantityStyleProvider;
        this.beanAdapter = new BeanAdapter<>(legacyWaypoint);
        this.numberInFlight = new SimpleIntegerProperty(noInFlight);
        this.groundDistance = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.INVARIANT_LENGTH);
    }

    private void initProperties() {
        if (isInitialized) {
            return;
        }

        synchronized (mutex) {
            if (isInitialized) {
                return;
            }

            final var legacyFlightplan = legacyWaypoint.getFlightplan();
            Expect.notNull(legacyFlightplan, "legacyFlightplan");

            lon = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.ANGLE_DEGREES);
            lat = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.ANGLE_DEGREES);
            altitudeAboveR = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.INVARIANT_LENGTH);
            altitudeAboveTakeoff = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.INVARIANT_LENGTH);
            assertAltitude = new SimpleObjectProperty<>();
            radius = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.LOCALIZED_LENGTH);
            body = new SimpleStringProperty();
            id = new SimpleIntegerProperty();
            roll = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.ANGLE_DEGREES);
            pitch = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.ANGLE_DEGREES);
            yaw = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.ANGLE_DEGREES);
            ignore = new SimpleBooleanProperty();
            assertYawOn = new SimpleBooleanProperty();
            assertYaw = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.ANGLE_DEGREES);
            speedMode = new SimpleObjectProperty<>();
            speed = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.INVARIANT_SPEED_MPS);
            stopHereTimeCopter = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.TIME);
            triggerImageHereCopterMode = new SimpleBooleanProperty();
            isBeginFlightline = new SimpleBooleanProperty();

            fpListener =
                new IFlightplanChangeListener() {
                    @Override
                    public void flightplanStructureChanged(IFlightplanRelatedObject fp) {}

                    @Override
                    public void flightplanValuesChanged(IFlightplanRelatedObject fpObj) {
                        // pls dont use equals here, it will slow things down, and acutally we want only to updaet the
                        // UI if the same waypoint has changed, and not for each similar one
                        if (fpObj == legacyWaypoint || fpObj == legacyFlightplan) {
                            Dispatcher.platform().run(beanAdapter::updateValuesFromSource);
                        }
                    }

                    @Override
                    public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {}

                    @Override
                    public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {}
                };

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
                .bind(altitudeAboveR)
                .to(
                    wp -> Quantity.of(wp.getAltInMAboveFPRefPoint(), Unit.METER),
                    (wp, value) -> wp.setAltInMAboveFPRefPoint(value.convertTo(Unit.METER).getValue().doubleValue()));
            beanAdapter
                .bind(altitudeAboveTakeoff)
                .to(
                    wp ->
                        Quantity.of(
                            wp.getAltInMAboveFPRefPoint()
                                + legacyFlightplan.getRefPointAltWgs84WithElevation()
                                - legacyFlightplan.getTakeofftAltWgs84WithElevation(),
                            Unit.METER));

            beanAdapter.bind(assertAltitude).to(Waypoint::getAssertAltitudeMode, Waypoint::setAssertAltitude);
            beanAdapter
                .bind(radius)
                .to(
                    wp -> Quantity.of(wp.getRadiusWithinM(), Unit.METER),
                    (wp, value) -> wp.setRadiusWithinM(value.convertTo(Unit.METER).getValue().doubleValue()));
            beanAdapter.bind(body).to(Waypoint::getBody, Waypoint::setBody);
            beanAdapter.bind(id).to(ReentryPoint::getId, ReentryPoint::setId);
            beanAdapter
                .bind(roll)
                .to(
                    wp -> Quantity.of(wp.getOrientation().getRoll(), Unit.DEGREE),
                    (wp, value) -> wp.setCamRoll(value.convertTo(Unit.DEGREE).getValue().doubleValue()));
            beanAdapter
                .bind(pitch)
                .to(
                    wp -> Quantity.of(wp.getOrientation().getPitch() - 90, Unit.DEGREE),
                    (wp, value) -> wp.setCamPitch(value.convertTo(Unit.DEGREE).getValue().doubleValue() + 90.0));
            beanAdapter
                .bind(yaw)
                .to(
                    wp -> Quantity.of(wp.getOrientation().getYaw(), Unit.DEGREE),
                    (wp, value) -> wp.setCamYaw(value.convertTo(Unit.DEGREE).getValue().doubleValue()));
            beanAdapter.bind(ignore).to(Waypoint::isIgnore, Waypoint::setIgnore);
            beanAdapter.bind(assertYawOn).to(Waypoint::getAssertYawOn, Waypoint::setAssertYawOn);
            beanAdapter
                .bind(assertYaw)
                .to(
                    wp -> Quantity.of(wp.getAssertYaw(), Unit.DEGREE),
                    (wp, value) -> wp.setAssertYaw(value.convertTo(Unit.DEGREE).getValue().doubleValue()));
            beanAdapter.bind(speedMode).to(Waypoint::getSpeedMode, Waypoint::setSpeedMode);
            beanAdapter
                .bind(speed)
                .to(
                    wp -> Quantity.of(wp.getSpeedMpSec(), Unit.METER_PER_SECOND),
                    (wp, value) -> wp.setSpeedMpSec(value.convertTo(Unit.METER_PER_SECOND).getValue().doubleValue()));
            beanAdapter
                .bind(stopHereTimeCopter)
                .to(
                    wp -> Quantity.of(wp.getStopHereTimeCopter(), Unit.MILLISECOND),
                    (wp, value) ->
                        wp.setStopHereTimeCopter(value.convertTo(Unit.MILLISECOND).getValue().doubleValue()));
            beanAdapter
                .bind(triggerImageHereCopterMode)
                .to(Waypoint::isTriggerImageHereCopterMode, Waypoint::setTriggerImageHereCopterMode);
            beanAdapter.bind(isBeginFlightline).to(Waypoint::isBeginFlightline, Waypoint::setBeginFlightline);

            isInitialized = true;
            warning.bind(airspaceWarning.or(heightWarning));
        }
    }

    public QuantityProperty<Angle> lonProperty() {
        initProperties();
        return lon;
    }

    public QuantityProperty<Angle> latProperty() {
        initProperties();
        return lat;
    }

    public QuantityProperty<Length> altitudeAboveRProperty() {
        initProperties();
        return altitudeAboveR;
    }

    public ObjectProperty<AltAssertModes> assertAltitudeProperty() {
        initProperties();
        return assertAltitude;
    }

    public QuantityProperty<Length> radiusProperty() {
        initProperties();
        return radius;
    }

    public StringProperty bodyProperty() {
        initProperties();
        return body;
    }

    public ReadOnlyIntegerProperty idProperty() {
        initProperties();
        return id;
    }

    public QuantityProperty<Angle> rollProperty() {
        initProperties();
        return roll;
    }

    public QuantityProperty<Angle> pitchProperty() {
        initProperties();
        return pitch;
    }

    public QuantityProperty<Angle> yawProperty() {
        initProperties();
        return yaw;
    }

    public BooleanProperty ignoreProperty() {
        initProperties();
        return ignore;
    }

    public ReadOnlyIntegerProperty numberInFlightProperty() {
        return numberInFlight;
    }

    public BooleanProperty assertYawOnProperty() {
        initProperties();
        return assertYawOn;
    }

    public QuantityProperty<Angle> assertYawProperty() {
        initProperties();
        return assertYaw;
    }

    public ObjectProperty<SpeedMode> speedModeProperty() {
        initProperties();
        return speedMode;
    }

    public QuantityProperty<Dimension.Speed> speedProperty() {
        initProperties();
        return speed;
    }

    public QuantityProperty<Dimension.Time> stopHereTimeCopterProperty() {
        initProperties();
        return stopHereTimeCopter;
    }

    public BooleanProperty triggerImageHereCopterModeProperty() {
        initProperties();
        return triggerImageHereCopterMode;
    }

    public BooleanProperty isBeginFlightlineProperty() {
        initProperties();
        return isBeginFlightline;
    }

    public void deleteMe() {
        legacyWaypoint.getParent().removeFromFlightplanContainer(legacyWaypoint);
    }

    public UIAsyncBooleanProperty selectedProperty() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public Waypoint getLegacyWaypoint() {
        return legacyWaypoint;
    }

    public ReadOnlyBooleanProperty warningProperty() {
        return warning.getReadOnlyProperty();
    }

    public BooleanProperty airspaceWarningProperty() {
        return airspaceWarning;
    }

    public BooleanProperty heightWarningProperty() {
        return heightWarning;
    }

    public QuantityProperty<Length> groundDistanceProperty() {
        return groundDistance;
    }

    public QuantityProperty<Length> altAboveTakeoffProperty() {
        return altitudeAboveTakeoff;
    }
}
