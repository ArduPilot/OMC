/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Drone;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.SrsSettings;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackendConnectionLost;
import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.plane.AirplaneCache;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.Position;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class OtherDetailsViewModel extends AbstractUavDataViewModel<Object> {

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private ISettingsManager settingsManager;

    private StringProperty planeName = new SimpleStringProperty(UavDataParameter.NOT_A_VALUE);
    private BooleanProperty simulation = new SimpleBooleanProperty();
    private ObjectProperty<Drone.StartPositionRaw> startPosition = new SimpleObjectProperty<>();
    private ObjectProperty<Drone.SimulationSettingsRaw> simulationSettings = new SimpleObjectProperty<>();
    private ObjectProperty<PositionData> positionData = new SimpleObjectProperty<>();
    private ObjectProperty<AirplaneFlightphase> flightPhase = new SimpleObjectProperty<>();
    private StringProperty connectionEstablished = new SimpleStringProperty();
    private ObjectProperty<IAirplaneListenerBackendConnectionLost.ConnectionLostReasons> backendConnectionLost =
        new SimpleObjectProperty<>();
    private ObjectProperty<AirplaneConnectorState> connectionState = new SimpleObjectProperty<>();
    private ObjectProperty<PositionOrientationData> positionOrientationData = new SimpleObjectProperty<>();
    private ObjectProperty<DebugData> debugData = new SimpleObjectProperty<>();

    public OtherDetailsViewModel() {
        this(null);
    }

    public OtherDetailsViewModel(Mission mission) {
        super(mission);
    }

    @Override
    protected void preInitialize() {
        @SuppressWarnings("unchecked")
        ObservableList<UavDataParameter<Object>> data = getData();
        data.add(new PlaneNameParam());
        data.add(new SimulationStatusParam());
        data.add(new StartpositionLonParam());
        data.add(new StartpositionLatParam());
        data.add(new SimulationSpeedParam());
        data.add(new FlightTimeParam());
        data.add(new FlightDistanceParam());
        data.add(new PlanePortParam());
        data.add(new LastConnectionLostReasonParam());
        data.add(new LocalPlanePositionParam());
    }

    @Override
    protected void releaseUavReferences() {
        planeName.unbind();
        simulation.unbind();
        startPosition.unbind();
        simulationSettings.unbind();
        positionData.unbind();
        flightPhase.unbind();
        connectionEstablished.unbind();
        backendConnectionLost.unbind();
        connectionState.unbind();
    }

    @Override
    protected void establishUavReferences() {
        Drone uav = getUav();
        planeName.bind(uav.planeNameProperty());
        simulation.bind(uav.simulationRawProperty());
        startPosition.bind(uav.startPositionRawProperty());
        simulationSettings.bind(uav.simulationSettingsRawProperty());
        positionData.bind(uav.positionDataRawProperty());
        flightPhase.bind(uav.airplaneFlightPhaseProperty());
        connectionEstablished.bind(uav.connectionEstablishedRawProperty());
        backendConnectionLost.bind(uav.backendConnectionLostRawProperty());
        connectionState.bind(uav.connectionStateProperty());
        positionOrientationData.bind(uav.positionOrientationDataRawProperty());
        debugData.bind(uav.debugDataRawProperty());
    }

    @Override
    protected synchronized void update(Object newData) {
        // do nothing
    }

    private AirplaneCache getPlaneCache() {
        IAirplane plane = getUav().getLegacyPlane();
        if (plane != null) {
            return plane.getAirplaneCache();
        }

        return null;
    }

    private class PlaneNameParam extends SimpleUavDataParameter<Object> {

        public PlaneNameParam() {
            super(
                languageHelper.getString("com.intel.missioncontrol.ui.tools.OtherDetailsView.airplane.name"),
                UavDataParameterType.GENERAL);
            planeName.addListener((observable, oldData, newData) -> this.updateValue(newData));
        }

        @Override
        protected Object extractRawValue(Object valueContainer) {
            return valueContainer;
        }
    }

    private class SimulationStatusParam extends SimpleUavDataParameter<Object> {

        public SimulationStatusParam() {
            super(
                languageHelper.getString("com.intel.missioncontrol.ui.tools.OtherDetailsView.simulation.status"),
                UavDataParameterType.GENERAL);
            simulation.addListener((observable, oldData, newData) -> this.updateValue(newData));
        }

        @Override
        protected Object extractRawValue(Object valueContainer) {
            return valueContainer.toString();
        }
    }

    private class StartpositionLonParam extends SimpleUavDataParameter<Object> {

        public StartpositionLonParam() {
            super(
                languageHelper.getString("com.intel.missioncontrol.ui.tools.OtherDetailsView.position.start.lon"),
                UavDataParameterType.DOUBLE);
            startPosition.addListener(
                (observable, oldData, newData) ->
                    this.updateValue(Optional.ofNullable(newData).map(Drone.StartPositionRaw::getLon).orElse(null)));
        }

        @Override
        protected Object extractRawValue(Object value) {
            return value;
        }
    }

    private class StartpositionLatParam extends SimpleUavDataParameter<Object> {

        public StartpositionLatParam() {
            super(
                languageHelper.getString("com.intel.missioncontrol.ui.tools.OtherDetailsView.position.start.lat"),
                UavDataParameterType.DOUBLE);
            startPosition.addListener(
                (observable, oldData, newData) ->
                    this.updateValue(Optional.ofNullable(newData).map(Drone.StartPositionRaw::getLat).orElse(null)));
        }

        @Override
        protected Object extractRawValue(Object value) {
            return value;
        }
    }

    private class SimulationSpeedParam extends SimpleUavDataParameter<Object> {

        public SimulationSpeedParam() {
            super(
                languageHelper.getString("com.intel.missioncontrol.ui.tools.OtherDetailsView.simulation.speed"),
                UavDataParameterType.FLOAT);
            simulationSettings.addListener(
                (observable, oldData, newData) ->
                    this.updateValue(
                        Optional.ofNullable(newData).map(Drone.SimulationSettingsRaw::getSpeed).orElse(null)));
        }

        @Override
        protected Object extractRawValue(Object value) {
            return value;
        }
    }

    private class FlightTimeParam extends SimpleUavDataParameter<Object> {

        public FlightTimeParam() {
            super(
                languageHelper.getString("com.intel.missioncontrol.ui.tools.OtherDetailsView.airborne.time"),
                UavDataParameterType.GENERAL);
            positionData.addListener((observable, oldData, newdata) -> this.updateValue(retrieveRawTime()));
            flightPhase.addListener((observable, oldData, newdata) -> this.updateValue(retrieveRawTime()));
        }

        @Override
        protected Object extractRawValue(Object value) {
            @SuppressWarnings("unchecked")
            Double doubleVal = (Double)value;
            return StringHelper.secToShortDHMS(Math.round(doubleVal));
        }

        private Double retrieveRawTime() {
            AirplaneCache cache = getPlaneCache();
            if (cache == null) {
                return null;
            }

            return cache.getAirborneTime();
        }
    }

    private class FlightDistanceParam extends SimpleUavDataParameter<Object> {

        public FlightDistanceParam() {
            super(
                languageHelper.getString("com.intel.missioncontrol.ui.tools.OtherDetailsView.airborne.distance"),
                UavDataParameterType.GENERAL);
            positionData.addListener((observable, oldData, newdata) -> this.updateValue(retrieveRawDistance()));
            flightPhase.addListener((observable, oldData, newdata) -> this.updateValue(retrieveRawDistance()));
        }

        @Override
        protected Object extractRawValue(Object value) {
            @SuppressWarnings("unchecked")
            Double doubleVal = (Double)value;
            return StringHelper.lengthToIngName(Math.round(doubleVal), -3, true);
        }

        private Double retrieveRawDistance() {
            AirplaneCache cache = getPlaneCache();
            if (cache == null) {
                return null;
            }

            return cache.getAirborneDistance();
        }
    }

    private class PlanePortParam extends SimpleUavDataParameter<Object> {

        public PlanePortParam() {
            super(
                languageHelper.getString("com.intel.missioncontrol.ui.tools.OtherDetailsView.airplane.port"),
                UavDataParameterType.GENERAL);
            connectionEstablished.addListener((observable, oldData, newData) -> this.updateValue(newData));
        }

        @Override
        protected Object extractRawValue(Object value) {
            return value;
        }
    }

    private class LastConnectionLostReasonParam extends SimpleUavDataParameter<Object> {

        public LastConnectionLostReasonParam() {
            super(
                languageHelper.getString("com.intel.missioncontrol.ui.tools.OtherDetailsView.connection.lost.reason"),
                UavDataParameterType.GENERAL);
            backendConnectionLost.addListener(
                (observable, oldData, newData) -> this.updateValue(retrieveConnectionLossReasons()));
            connectionState.addListener(
                (observable, oldData, newData) -> this.updateValue(retrieveConnectionLossReasons()));
        }

        @Override
        protected Object extractRawValue(Object value) {
            return value;
        }

        private IAirplaneListenerBackendConnectionLost.ConnectionLostReasons retrieveConnectionLossReasons() {
            AirplaneCache cache = getPlaneCache();
            if (cache == null) {
                return null;
            }

            return cache.getLastConnectionLostReason();
        }
    }

    private class LocalPlanePositionParam extends SimpleUavDataParameter<Object> {

        private double lat;
        private double lon;
        private double alt;

        public LocalPlanePositionParam() {
            super(
                languageHelper.getString("com.intel.missioncontrol.ui.tools.OtherDetailsView.position.local"),
                UavDataParameterType.GENERAL);
            debugData.addListener((observable, oldData, newData) -> receiveDebugData(newData));
            positionOrientationData.addListener(
                (observable, oldData, newData) -> receivePositionOrientationdata(newData));
        }

        @Override
        protected Object extractRawValue(Object value) {
            return value;
        }

        private void receiveDebugData(DebugData d) {
            if (d == null) {
                this.updateValue(NOT_A_VALUE);
                return;
            }

            double altitude = (d.gps_ellipsoid + d.gpsAltitude) / 100.;
            if (Math.abs(this.alt - altitude) > .0000001) {
                return;
            }

            this.alt = altitude;
            refreshLocalPos();
        }

        private void receivePositionOrientationdata(PositionOrientationData po) {
            if (po == null) {
                this.updateValue(NOT_A_VALUE);
                return;
            }

            if (this.lat == po.lat && this.lon == po.lon) {
                return;
            }

            lat = po.lat;
            lon = po.lon;
            refreshLocalPos();
        }

        void refreshLocalPos() {
            final Position position = Position.fromDegrees(lat, lon, alt);
            SrsSettings srsSettings = settingsManager.getSection(SrsSettings.class);
            String val = srsSettings.getApplicationSrs().fromWgs84Formatted(position);
            this.updateValue(val);
        }

    }
}
