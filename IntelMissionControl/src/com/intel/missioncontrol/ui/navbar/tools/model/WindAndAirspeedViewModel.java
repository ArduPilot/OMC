/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.plane.WindEstimate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/** Created by eivanchenko on 8/7/2017. */
public class WindAndAirspeedViewModel extends AbstractUavDataViewModel<PositionData> {

    @Inject
    private ILanguageHelper languageHelper;

    private ObjectProperty<PositionData> positionData = new SimpleObjectProperty<>();

    public WindAndAirspeedViewModel() {
        this(null);
    }

    public WindAndAirspeedViewModel(Mission mission) {
        super(mission);
    }

    @Override
    protected void preInitialize() {
        getData()
            .addAll(
                new WindDirectionParam(),
                new WindSpeedParam(),
                new WindSpeedNorthParam(),
                new WindSpeedEastParam(),
                new AirspeedParam());
        positionData.addListener((observable, oldData, newData) -> this.update(newData));
    }

    @Override
    protected void releaseUavReferences() {
        positionData.unbind();
    }

    @Override
    protected void establishUavReferences() {
        positionData.bind(getUav().positionDataRawProperty());
    }

    private WindEstimate getWindEstimate() {
        IAirplane airplane = getUav().getLegacyPlane();
        if (airplane == null) {
            return null;
        }

        return airplane.getWindEstimate();
    }

    private class WindDirectionParam extends SimpleUavDataParameter<PositionData> {

        public WindDirectionParam() {
            super(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.tools.WindAndAirspeedEstimationView.wind.direction"),
                UavDataParameterType.GENERAL);
        }

        @Override
        protected Object extractRawValue(PositionData posData) {
            WindEstimate wind = getWindEstimate();
            if (wind == null) {
                return NOT_A_VALUE;
            }

            return StringHelper.round(wind.phi, 1, true);
        }
    }

    private class WindSpeedParam extends SimpleUavDataParameter<PositionData> {

        public WindSpeedParam() {
            super(
                languageHelper.getString("com.intel.missioncontrol.ui.tools.WindAndAirspeedEstimationView.wind.speed"),
                UavDataParameterType.GENERAL);
        }

        @Override
        protected Object extractRawValue(PositionData posData) {
            WindEstimate wind = getWindEstimate();
            if (wind == null) {
                return NOT_A_VALUE;
            }

            return StringHelper.speedToIngName(wind.vW, 1, true);
        }
    }

    private class WindSpeedNorthParam extends SimpleUavDataParameter<PositionData> {

        public WindSpeedNorthParam() {
            super(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.tools.WindAndAirspeedEstimationView.wind.speed.north"),
                UavDataParameterType.GENERAL);
        }

        @Override
        protected Object extractRawValue(PositionData posData) {
            WindEstimate wind = getWindEstimate();
            if (wind == null) {
                return NOT_A_VALUE;
            }

            return StringHelper.speedToIngName(wind.vWN, 1, true);
        }
    }

    private class WindSpeedEastParam extends SimpleUavDataParameter<PositionData> {

        public WindSpeedEastParam() {
            super(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.tools.WindAndAirspeedEstimationView.wind.speed.east"),
                UavDataParameterType.GENERAL);
        }

        @Override
        protected Object extractRawValue(PositionData posData) {
            WindEstimate wind = getWindEstimate();
            if (wind == null) {
                return NOT_A_VALUE;
            }

            return StringHelper.speedToIngName(wind.vWE, 1, true);
        }
    }

    private class AirspeedParam extends SimpleUavDataParameter<PositionData> {

        public AirspeedParam() {
            super(
                languageHelper.getString("com.intel.missioncontrol.ui.tools.WindAndAirspeedEstimationView.airspeed"),
                UavDataParameterType.GENERAL);
        }

        @Override
        protected Object extractRawValue(PositionData posData) {
            WindEstimate wind = getWindEstimate();
            if (wind == null) {
                return NOT_A_VALUE;
            }

            return StringHelper.speedToIngName(wind.vA, 1, true);
        }
    }
}
