/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navigation;

import com.intel.missioncontrol.IApplicationContext;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.PropertyPathStore;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.sidepane.analysis.AnalysisSettings;

public class NavigationRules {

    private final PropertyPathStore propertyPathStore = new PropertyPathStore();
    private AsyncBooleanProperty dataTransferPopupEnabled;

    public void apply(
            INavigationService navigationService,
            IApplicationContext applicationContext,
            ISettingsManager settingsManager) {
        dataTransferPopupEnabled =
            settingsManager.getSection(AnalysisSettings.class).dataTransferPopupEnabledProperty();

        // If we switch to another dataset, we potentially trigger a navigation request on the data preview tab.
        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .selectReadOnlyObject(Mission::currentMatchingProperty)
            .addListener(
                (observable, oldValue, newValue) ->
                    switchCurrentDataset(
                        navigationService, newValue != null ? newValue.getStatus() : null, applicationContext));

        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .select(Mission::currentMatchingProperty)
            .selectReadOnlyObject(Matching::statusProperty)
            .addListener(
                (observable, oldValue, newValue) ->
                    switchCurrentDataset(navigationService, newValue, applicationContext));
    }


    private void switchCurrentDataset(
            INavigationService navigationService,
            MatchingStatus matchingStatus,
            IApplicationContext applicationContext) {
        if (applicationContext.getCurrentMission() == null
                || navigationService.workflowStepProperty().get() != WorkflowStep.DATA_PREVIEW) {
            return;
        }
        navigationService.navigateTo(SidePanePage.VIEW_DATASET);
    }

}
