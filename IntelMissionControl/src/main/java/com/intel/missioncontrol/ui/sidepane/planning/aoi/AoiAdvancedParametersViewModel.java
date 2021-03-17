/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.api.IFlightPlanService;
import org.asyncfx.beans.property.PropertyPathStore;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.mission.Drone;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import com.intel.missioncontrol.ui.sidepane.planning.StartPlanningViewModel;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.plane.AirplaneType;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

@ScopeProvider(scopes = AoiScope.class)
@SuppressLinter(value = "IllegalViewModelMethod", reviewer = "mstrauss", justification = "legacy file")
public class AoiAdvancedParametersViewModel extends DialogViewModel<Object, AreaOfInterest> {

    @InjectScope
    private MainScope mainScope;

    @InjectScope
    private PlanningScope planningScope;

    @Inject
    private IFlightPlanService flightPlanService;

    @Inject
    private NotificationCenter notificationCenter;

    @Inject
    private ISettingsManager settingsManager;

    @Inject
    private IApplicationContext applicationContext;

    private AreaOfInterest areaOfInterest;

    private BooleanProperty needsFlightLinesTab = new SimpleBooleanProperty();
    private BooleanProperty needsTransformationTab = new SimpleBooleanProperty();
    private BooleanProperty needsDimensionsTab = new SimpleBooleanProperty();

    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @Override
    protected void initializeViewModel(AreaOfInterest aoi) {
        super.initializeViewModel(aoi);
        this.areaOfInterest = aoi;
        needsFlightLinesTab.bind(Bindings.createBooleanBinding(() -> aoi.getType().hasWaypoints(), aoi.typeProperty()));

        needsDimensionsTab.bind(Bindings.createBooleanBinding(() -> aoi.getType().hasDimensions(), aoi.typeProperty()));

        needsTransformationTab.bind(
            aoi.modelSourceProperty()
                .isEqualTo(CPicArea.ModelSourceTypes.MODEL_FILE)
                .and(Bindings.createBooleanBinding(() -> aoi.getType().needsTranformation(), aoi.typeProperty())));

        propertyPathStore
            .from(applicationContext.currentLegacyMissionProperty())
            .selectReadOnlyObject(Mission::currentFlightPlanProperty)
            .addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        getCloseCommand().execute();
                    }
                });
        applicationContext
            .currentLegacyMissionProperty()
            .addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        getCloseCommand().execute();
                    }
                });
    }

    public AreaOfInterest getAreaOfInterest() {
        return areaOfInterest;
    }

    public MainScope getMainScope() {
        return mainScope;
    }

    public PlanningScope getPlanningScope() {
        return planningScope;
    }

    private IPlatformDescription getPlatformDescription() {
        return planningScope.getSelectedHardwareConfiguration().getPlatformDescription();
    }

    public AirplaneType getAirplaneType() {
        IPlatformDescription platformDescription = getPlatformDescription();

        if (platformDescription == null) {
            return null;
        }

        return platformDescription.getAirplaneType();
    }

    public Drone getUav() {
        if (mainScope == null) {
            return null;
        }

        Mission currentMission = applicationContext.getCurrentLegacyMission();
        if (currentMission == null) {
            return null;
        }

        return currentMission.droneProperty().get();
    }

    public void saveAsDefaults() {
        FlightPlan currentFlightplan = applicationContext.getCurrentLegacyMission().getCurrentFlightPlan();

        if (currentFlightplan.getLegacyFlightplan().getFile().exists()) {
            flightPlanService.updateTemplateAoi(currentFlightplan, areaOfInterest);
        }

        if (currentFlightplan.basedOnTemplateProperty().get() != null) {
            flightPlanService.updateTemplateAoi(
                currentFlightplan.basedOnTemplateProperty().get().getFlightPlan(), areaOfInterest);
            notificationCenter.publish(
                StartPlanningViewModel.FLIGHT_PLAN_TEMPLATE_EVENT,
                StartPlanningViewModel.REFRESH_ACTION,
                currentFlightplan.basedOnTemplateProperty().get());
        }
    }

    public void restoreDefaults() {
        areaOfInterest.restoreFromDefaults();
    }

    public BooleanProperty needsFlightLinesTabProperty() {
        return needsFlightLinesTab;
    }

    public BooleanProperty needsTransformationTabProperty() {
        return needsTransformationTab;
    }
    public BooleanProperty needsDimensionsTabProperty() {
        return needsDimensionsTab;
    }
}
