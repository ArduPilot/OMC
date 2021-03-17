/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.UIAsyncObjectProperty;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.layers.flightplan.FlightplanLayerGroup;
import com.intel.missioncontrol.map.worldwind.layers.flightplan.FlightplanLayerVisibilitySettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.OperationLevel;

public class FlightPlanLayerGroupViewModel extends LayerGroupViewModel {

    private final ILanguageHelper languageHelper;
    private final UIAsyncObjectProperty<OperationLevel> operationLevelObjectProperty =
        new UIAsyncObjectProperty<>(this);
    private final String prefix = FlightplanLayerGroup.class.getName();

    public FlightPlanLayerGroupViewModel(
            FlightplanLayerGroup flightplanLayerGroup,
            ILanguageHelper languageHelper,
            GeneralSettings generalSettings) {
        super(flightplanLayerGroup, languageHelper, false, true);
        this.languageHelper = languageHelper;
        operationLevelObjectProperty.bind(generalSettings.operationLevelProperty());
        FlightplanLayerVisibilitySettings flightPlanVisibility =
            flightplanLayerGroup.getFlightplanLayerVisibilitySettings();

        addSublayer(flightPlanVisibility.aoiVisibleProperty(), false);
        addSublayer(flightPlanVisibility.waypointVisibleProperty(), false);
        addSublayer(flightPlanVisibility.flightLineVisibleProperty(), false);
        addSublayer(flightPlanVisibility.startLandVisibleProperty(), false);
        addSublayer(flightPlanVisibility.coveragePreviewVisibleProperty(), false);
        addSublayer(flightPlanVisibility.showCamPreviewProperty(), true);
        addSublayer(flightPlanVisibility.showVoxelsDilatedProperty(), true);
        addSublayer(flightPlanVisibility.showVoxelsProperty(), true);
        addSublayer(flightPlanVisibility.showCurrentFlightplanProperty(), false);
        addSublayer(flightPlanVisibility.showOtherFlightplansProperty(), false);
    }

    private void addSublayer(AsyncBooleanProperty property, boolean onlyDebugMode) {
        SimpleLayerViewModel viewModel;
        if (onlyDebugMode) {
            viewModel =
                new SimpleLayerViewModel(
                    languageHelper.getString(prefix + "." + property.getName()),
                    operationLevelObjectProperty.isEqualTo(OperationLevel.DEBUG));
        } else {
            viewModel = new SimpleLayerViewModel(languageHelper.getString(prefix + "." + property.getName()));
        }

        viewModel.selected.bindBidirectional(property);
        subLayerItems.add(viewModel);
    }

}
