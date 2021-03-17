/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

/** Created by eivanchenko on 8/31/2017. */
public class ManualServoInputValuesViewModel extends AbstractUavDataViewModel<OrientationData> {

    public static final String ELEVATOR_PARAM = "elevator";
    public static final String RUDDER_PARAM = "rudder";
    public static final String THROTTLE_PARAM = "throttle";
    public static final String AILERON_PARAM = "aileron";
    public static final String LANDING_PARAM = "landing";
    public static final String AUTO_PILOT_PARAM = "auto-pilot";

    @Inject
    private ILanguageHelper languageHelper;

    private ObjectProperty<OrientationData> property = new SimpleObjectProperty<>();
    private ManualServoInputValue elevator = new ManualServoInputValue(ELEVATOR_PARAM, PlaneConstants.SERVO_ELEVATOR);
    private ManualServoInputValue rudder = new ManualServoInputValue(RUDDER_PARAM, PlaneConstants.SERVO_RUDDER);
    private ManualServoInputValue throttle = new ManualServoInputValue(THROTTLE_PARAM, PlaneConstants.SERVO_THROTTLE);
    private ManualServoInputValue aileron = new ManualServoInputValue(AILERON_PARAM, PlaneConstants.SERVO_AILERON);
    private ManualServoModeIndicator autoPilot =
        new ManualServoModeIndicator(AUTO_PILOT_PARAM, PlaneConstants.SERVO_SWITCH_AUTO_ASSISTED);
    private ManualServoModeIndicator landing =
        new ManualServoModeIndicator(LANDING_PARAM, PlaneConstants.SERVO_SWITCH_LANDING);
    private String onValueStr;
    private String offValueStr;

    public ManualServoInputValuesViewModel() {
        this(null);
    }

    public ManualServoInputValuesViewModel(Mission mission) {
        super(mission);
    }

    @Override
    protected void releaseUavReferences() {
        property.unbind();
    }

    @Override
    protected void establishUavReferences() {
        property.bind(getUav().orientationDataRawproperty());
    }

    @Override
    public void preInitialize() {
        onValueStr =
            languageHelper.getString("com.intel.missioncontrol.ui.tools.model.ManualServoInputValuesView.switch.on");
        offValueStr =
            languageHelper.getString("com.intel.missioncontrol.ui.tools.model.ManualServoInputValuesView.switch.off");
        ObservableList<UavDataParameter<OrientationData>> params = getData();
        elevator.setDisplayName(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.tools.model.ManualServoInputValuesView.servo.input.elevator"));
        params.add(elevator);
        rudder.setDisplayName(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.tools.model.ManualServoInputValuesView.servo.input.rudder"));
        params.add(rudder);
        throttle.setDisplayName(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.tools.model.ManualServoInputValuesView.servo.input.throttle"));
        params.add(throttle);
        aileron.setDisplayName(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.tools.model.ManualServoInputValuesView.servo.input.aileron"));
        params.add(aileron);
        autoPilot.setDisplayName(
            languageHelper.getString("com.intel.missioncontrol.ui.tools.model.ManualServoInputValuesView.autopilot"));
        params.add(autoPilot);
        landing.setDisplayName(
            languageHelper.getString("com.intel.missioncontrol.ui.tools.model.ManualServoInputValuesView.landing"));
        params.add(landing);
    }

    @Override
    public void postInitialize() {
        property.addListener((observable, oldValue, newValue) -> update(newValue));
    }

    public ManualServoInputValue getElevator() {
        return elevator;
    }

    public ManualServoInputValue getRudder() {
        return rudder;
    }

    public ManualServoInputValue getThrottle() {
        return throttle;
    }

    public ManualServoInputValue getAileron() {
        return aileron;
    }

    public ManualServoModeIndicator getAutoPilot() {
        return autoPilot;
    }

    public ManualServoModeIndicator getLanding() {
        return landing;
    }

    public static class ManualServoInputValue extends SimpleUavDataParameter<OrientationData> {

        private int channel;

        public ManualServoInputValue(String internalName, int channel) {
            super(internalName, UavDataParameterType.GENERAL);
            this.channel = channel;
        }

        @Override
        protected Object extractRawValue(OrientationData od) {
            MVector<Integer> manServos = od.manualServos;
            if (manServos == null || manServos.size() <= getChannel()) {
                return null;
            }

            return manServos.get(getChannel());
        }

        public Integer getIntValue() {
            String valStr = this.getValue();
            if (valStr == null || NOT_A_VALUE.equals(valStr)) {
                return null;
            }

            try {
                return Integer.valueOf(valStr);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        @Override
        public int getChannel() {
            return channel;
        }
    }

    public class ManualServoModeIndicator extends SimpleUavDataParameter<OrientationData> {

        private int channel;

        public ManualServoModeIndicator(String internalName, int channel) {
            super(internalName, UavDataParameterType.GENERAL);
            this.channel = channel;
        }

        @Override
        protected Object extractRawValue(OrientationData od) {
            MVector<Integer> manServos = od.manualServos;
            if (manServos == null || manServos.size() <= channel) {
                return NOT_A_VALUE;
            }

            if (manServos.get(channel) > 50) {
                return onValueStr;
            }

            return offValueStr;
        }
    }
}
