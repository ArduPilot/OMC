/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.launch.confirm;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.Uav;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.common.LateBinding;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import de.saxsys.mvvmfx.InjectScope;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneType;
import eu.mavinci.flightplan.Flightplan;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import java.util.Optional;

public class LaunchConfirmationViewModel extends ViewModelBase {

    @InjectScope
    private PlanningScope planningScope;

    @InjectScope
    private MainScope mainScope;

    @Inject
    private IApplicationContext applicationContext;

    private ObservableValue<AirplaneConnectorState> connectionState;
    private ObjectProperty<AirplaneType> airplaneType = new SimpleObjectProperty<>();

    public void confirmLaunch() {
        planningScope.launchConfirmedProperty().setValue(true);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        connectionState =
            LateBinding.of(applicationContext.currentMissionProperty())
                .get(Mission::uavProperty)
                .get(Uav::connectionProperty)
                .property();
        connectionState.addListener(
            (observable, oldValue, newValue) -> {
                airplaneType.setValue(getAirplaneType());
            });
    }

    public ObjectProperty<AirplaneType> airplaneTypeProperty() {
        return airplaneType;
    }

    private AirplaneType getAirplaneType() {
        return Optional.ofNullable(applicationContext.getCurrentMission())
            .map(Mission::uavProperty)
            .map(uav -> uav.get().getLegacyPlane())
            .map(plane -> plane.getFPmanager().getOnAirFlightplan())
            .map(Flightplan::getHardwareConfiguration)
            .map(IHardwareConfiguration::getPlatformDescription)
            .map(IPlatformDescription::getAirplaneType)
            .orElse(null);
    }

}
