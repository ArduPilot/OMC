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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

/** Created by eivanchenko on 8/28/2017. */
public class SimulatedSystemFailuresViewModel extends AbstractUavDataViewModel<Drone.ExpertSimulatedFailures> {

    public static final String ENGINE_OFF_PARAM = "engine-off";
    public static final String GPS_LOSS_PARAM = "gps-loss";
    public static final String NO_RC_LINK_PARAM = "no-rc-link";
    public static final String NO_DATA_LINK_PARAM = "no-data-link";
    public static final String BATTERY_LOW_PARAM = "battery-low";
    public static final String FAIL_TIME_PARAM = "fail-time";
    public static final String BATTERY_LEVEL_PARAM = "battery-level";
    public static final String DEBUG_3_PARAM = "debug-3";

    @Inject
    private ILanguageHelper languageHelper;

    private ObjectProperty<Drone.ExpertSimulatedFailures> expertSimulatedFailures = new SimpleObjectProperty<>();

    private UavDataParameter<Drone.ExpertSimulatedFailures> engineOffParam =
        new ExpertSimulatedFailureParam(0, ENGINE_OFF_PARAM);

    private SimpleUavDataParameter<Drone.ExpertSimulatedFailures> gpsLossParam =
        new ExpertSimulatedFailureParam(1, GPS_LOSS_PARAM);

    private SimpleUavDataParameter<Drone.ExpertSimulatedFailures> noRcLinkParam =
        new ExpertSimulatedFailureParam(2, NO_RC_LINK_PARAM);

    private SimpleUavDataParameter<Drone.ExpertSimulatedFailures> noDataLinkParam =
        new ExpertSimulatedFailureParam(3, NO_DATA_LINK_PARAM);

    private SimpleUavDataParameter<Drone.ExpertSimulatedFailures> batteryLowParam =
        new ExpertSimulatedFailureParam(4, BATTERY_LOW_PARAM);

    private SimpleUavDataParameter<Drone.ExpertSimulatedFailures> failTimeParam =
        new SimpleUavDataParameter<Drone.ExpertSimulatedFailures>(FAIL_TIME_PARAM, UavDataParameterType.GENERAL) {
            @Override
            protected Integer extractRawValue(Drone.ExpertSimulatedFailures valueContainer) {
                return valueContainer.getDebug1();
            }
        };

    private SimpleUavDataParameter<Drone.ExpertSimulatedFailures> batteryLevelParam =
        new SimpleUavDataParameter<Drone.ExpertSimulatedFailures>(BATTERY_LEVEL_PARAM, UavDataParameterType.GENERAL) {
            @Override
            protected Integer extractRawValue(Drone.ExpertSimulatedFailures valueContainer) {
                return valueContainer.getDebug2();
            }
        };

    private SimpleUavDataParameter<Drone.ExpertSimulatedFailures> debug3Param =
        new SimpleUavDataParameter<Drone.ExpertSimulatedFailures>(DEBUG_3_PARAM, UavDataParameterType.GENERAL) {
            @Override
            protected Integer extractRawValue(Drone.ExpertSimulatedFailures valueContainer) {
                return valueContainer.getDebug3();
            }
        };

    public SimulatedSystemFailuresViewModel() {
        this(null);
    }

    public SimulatedSystemFailuresViewModel(Mission mission) {
        super(mission);
    }

    @Override
    protected void preInitialize() {
        ObservableList<UavDataParameter<Drone.ExpertSimulatedFailures>> data = getData();
        engineOffParam
            .displayedNameProperty()
            .setValue(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.tools.SimulatedSystemFailuresView.failure.engine.off"));
        data.add(engineOffParam);
        gpsLossParam
            .displayedNameProperty()
            .setValue(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.tools.SimulatedSystemFailuresView.failure.gps.loss"));
        data.add(gpsLossParam);
        noRcLinkParam
            .displayedNameProperty()
            .setValue(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.tools.SimulatedSystemFailuresView.failure.no.rc.link"));
        data.add(noRcLinkParam);
        noDataLinkParam
            .displayedNameProperty()
            .setValue(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.tools.SimulatedSystemFailuresView.failure.no.data.link"));
        data.add(noDataLinkParam);
        batteryLowParam
            .displayedNameProperty()
            .setValue(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.tools.SimulatedSystemFailuresView.failure.battery.low"));
        data.add(batteryLowParam);
        failTimeParam
            .displayedNameProperty()
            .setValue(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.tools.SimulatedSystemFailuresView.settings.fail.time"));
        data.add(failTimeParam);
        batteryLevelParam
            .displayedNameProperty()
            .setValue(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.tools.SimulatedSystemFailuresView.settings.battery.level"));
        data.add(batteryLevelParam);
        debug3Param
            .displayedNameProperty()
            .setValue(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.tools.SimulatedSystemFailuresView.settings.debug.3"));
        data.add(debug3Param);
        expertSimulatedFailures.addListener((observable, oldData, newData) -> this.update(newData));
    }

    @Override
    protected void releaseUavReferences() {
        expertSimulatedFailures.unbind();
    }

    @Override
    protected void establishUavReferences() {
        expertSimulatedFailures.bind(getUav().expertSimulatedFailuresproperty());
    }

    public UavDataParameter<Drone.ExpertSimulatedFailures> getUavParameter(String paramInternalName) {
        return getData()
            .stream()
            .filter(p -> p instanceof SimpleUavDataParameter)
            .map(p -> (SimpleUavDataParameter<Drone.ExpertSimulatedFailures>)p)
            .filter(sp -> sp.getInternalName().equals(paramInternalName))
            .findFirst()
            .orElse(null);
    }

    public static class ExpertSimulatedFailureParam extends SimpleUavDataParameter<Drone.ExpertSimulatedFailures> {

        private int order;

        public ExpertSimulatedFailureParam(int order, String internalName) {
            super(internalName, UavDataParameterType.GENERAL);
            this.order = order;
        }

        public int getOrder() {
            return order;
        }

        @Override
        protected Boolean extractRawValue(Drone.ExpertSimulatedFailures valueContainer) {
            return (valueContainer.getFailBitMask() & (1 << getOrder())) != 0;
        }
    }
}
