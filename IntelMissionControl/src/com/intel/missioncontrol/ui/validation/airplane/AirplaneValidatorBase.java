/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.airplane;

import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.validation.ValidatorBase;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConnectionState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerHealth;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPlaneInfo;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPositionOrientation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerStartPos;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.desktop.helper.IRecomputeListener;
import eu.mavinci.desktop.helper.Recomputer;
import javafx.application.Platform;

abstract class AirplaneValidatorBase extends ValidatorBase<FlightplanAirplanePair> {

    private IAirplaneListenerPositionOrientation airplaneListenerPositionOrientation;
    private IRecomputeListener recomputeListener;
    protected ILanguageHelper languageHelper;
    private IAirplaneListenerPlaneInfo listenerPlaneInfo;
    private IFlightplanChangeListener flightplanChangeListener;
    private IAirplaneListenerHealth airplaneListenerHealth;
    private IAirplaneListenerStartPos airplaneListenerStartPos;
    private IAirplaneListenerConnectionState airplaneListenerConnectionState;

    AirplaneValidatorBase(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            FlightplanAirplanePair flightplanAirplanePair) {
        super(flightplanAirplanePair, quantityStyleProvider);
        this.languageHelper = languageHelper;
    }

    protected void addConnectionStateListener() {
        airplaneListenerConnectionState =
            new IAirplaneListenerConnectionState() {

                @Override
                public void connectionStateChange(AirplaneConnectorState newState) {
                    Platform.runLater(() -> invalidate());
                }
            };
        getValidationValue().getPlane().addListener(airplaneListenerConnectionState);
    }

    protected void addStartPosListener() {
        airplaneListenerStartPos =
            new IAirplaneListenerStartPos() {
                @Override
                public void recv_startPos(Double x, Double y, Integer z) {
                    Platform.runLater(() -> invalidate());
                }
            };
        getValidationValue().getPlane().addListener(airplaneListenerStartPos);
    }

    protected void addPlaneHealthListener() {
        airplaneListenerHealth =
            new IAirplaneListenerHealth() {
                @Override
                public void recv_health(HealthData d) {
                    Platform.runLater(() -> invalidate());
                }
            };
        getValidationValue().getPlane().addListener(airplaneListenerHealth);
    }

    protected void addPositionOrientationListener() {
        airplaneListenerPositionOrientation =
            new IAirplaneListenerPositionOrientation() {
                @Override
                public void recv_positionOrientation(PositionOrientationData data) {
                    Platform.runLater(() -> invalidate());
                }
            };
        getValidationValue().getPlane().addListener(airplaneListenerPositionOrientation);
    }

    protected void addRecomputeListener() {
        recomputeListener =
            new IRecomputeListener() {
                @Override
                public void recomputeReady(Recomputer recomputer, boolean anotherRecomputeIsWaiting, long runNo) {
                    Platform.runLater(() -> invalidate());
                }
            };
        getValidationValue().addListener(recomputeListener);
    }

    protected void addPlaneInfoListener() {
        listenerPlaneInfo =
            new IAirplaneListenerPlaneInfo() {
                @Override
                public void recv_planeInfo(PlaneInfo info) {
                    Platform.runLater(() -> invalidate());
                }
            };
        getValidationValue().getPlane().addListener(listenerPlaneInfo);
    }

    protected void addFlightplanChangeListener() {
        flightplanChangeListener =
            new IFlightplanChangeListener() {
                @Override
                public void flightplanStructureChanged(IFlightplanRelatedObject fp) {
                    Platform.runLater(() -> invalidate());
                }

                @Override
                public void flightplanValuesChanged(IFlightplanRelatedObject fpObj) {
                    Platform.runLater(() -> invalidate());
                }

                @Override
                public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {
                    Platform.runLater(() -> invalidate());
                }

                @Override
                public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {
                    Platform.runLater(() -> invalidate());
                }
            };
        getValidationValue().addListener(flightplanChangeListener);
    }

}
