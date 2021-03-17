/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

/** Created by ekorotko on 29.11.2017. */
public class FalconStateMachine /*extends PageAwareViewModel*/ {
    /*

        @InjectScope
        private UavConnectionScope connectionScope;
    */

    public enum State {
        GROUND,
        MANUAL_TAKEOFF,
        AUTO_TAKEOFF_MOTORS_ON,
        AUTO_TAKEOFF_READY_FOR_TAKEOFF,
        TAKING_OFF,
        SENDING_FP,
        MISSION_FLIGHT,
        GO_HOME,
        HOLD_POSITION,
        MANUAL_FLIGHT,
        LANDING,
        LANDED;
    }

    /* private ObservableValue<State> falconState = new SimpleObjectProperty<>(State.GROUND);
        private ObservableValue<Uav> uav;
        private BooleanBinding uavConnected;

        ReadOnlyObjectProperty<Mission> currentMission() {
            return mainScope.currentMissionProperty();
        }

        public void initialize() {
            uavConnected = Bindings.equal(connectionScope.connectionStateProperty(), ConnectionState.CONNECTED);
            uav = LateBinding.of(currentMission())
                    .get(Mission::uavProperty)
                    .property();
        }
    */
}
