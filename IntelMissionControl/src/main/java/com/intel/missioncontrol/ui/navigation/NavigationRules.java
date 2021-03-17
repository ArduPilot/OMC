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

        // When navigating to the data preview workflow step, choose the sidepane tab depending on the status of the
        // current matching.
        navigationService.addRule(
            WorkflowStep.DATA_PREVIEW,
            () -> {
                Mission mission = applicationContext.getCurrentLegacyMission();
                if (mission == null) {
                    return null;
                }

                Matching currentMatching = mission.getCurrentMatching();
                if (currentMatching != null) {
                    if (currentMatching.getStatus() == MatchingStatus.NEW) {
                        return getImportPage();
                    } else if (currentMatching.getStatus() == MatchingStatus.IMPORTED) {
                        return SidePanePage.VIEW_DATASET;
                    } else if (currentMatching.getStatus() == MatchingStatus.TRANSFERRING) {
                        return SidePanePage.TRANSFERRING_DATA;
                    }
                }

                return null;
            });

        // If we switch to another dataset, we potentially trigger a navigation request on the data preview tab.
        propertyPathStore
            .from(applicationContext.currentLegacyMissionProperty())
            .selectReadOnlyObject(Mission::currentMatchingProperty)
            .addListener(
                (observable, oldValue, newValue) ->
                    switchCurrentDataset(
                        navigationService, newValue != null ? newValue.getStatus() : null, applicationContext));

        propertyPathStore
            .from(applicationContext.currentLegacyMissionProperty())
            .select(Mission::currentMatchingProperty)
            .selectReadOnlyObject(Matching::statusProperty)
            .addListener(
                (observable, oldValue, newValue) ->
                    switchCurrentDataset(navigationService, newValue, applicationContext));
    }

    private SidePanePage getImportPage() {
        return dataTransferPopupEnabled.get() ? SidePanePage.VIEW_DATASET_HELP : SidePanePage.DATA_IMPORT;
    }

    private void switchCurrentDataset(
            INavigationService navigationService,
            MatchingStatus matchingStatus,
            IApplicationContext applicationContext) {
        if (applicationContext.getCurrentLegacyMission() == null
                || navigationService.workflowStepProperty().get() != WorkflowStep.DATA_PREVIEW) {
            return;
        }

        if (matchingStatus == MatchingStatus.NEW) {
            navigationService.navigateTo(getImportPage());
        } else if (matchingStatus == MatchingStatus.IMPORTED) {
            navigationService.navigateTo(SidePanePage.VIEW_DATASET);
        } else if (matchingStatus == MatchingStatus.TRANSFERRING) {
            navigationService.navigateTo(SidePanePage.TRANSFERRING_DATA);
        }
    }


    //TODO add rule based on the changes in the applicationContext.currentMission
    /*
    if (clonedFlightPlan.areasOfInterestProperty().isEmpty() && !clonedFlightPlan.isTemplate()) {
            navigationService.navigateTo(SidePanePage.CHOOSE_AOI);
        } else {
            Optional.ofNullable(navigationService.getWorkflowStep())
                .filter(wfs -> wfs != WorkflowStep.FLIGHT)
                .ifPresent(wfs -> navigationService.navigateTo(SidePanePage.EDIT_FLIGHTPLAN));
        }


        /////same ???
        if (newFlightPlan.areasOfInterestProperty().isEmpty() && !newFlightPlan.isTemplate()) {
                        navigationService.navigateTo(SidePanePage.CHOOSE_AOI);
                    } else {
                        navigationService.navigateTo(SidePanePage.EDIT_FLIGHTPLAN);
                    }
     */
}
