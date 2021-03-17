/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api.workflow;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.geometry.AreaOfInterestCorner;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.DisplaySettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import eu.mavinci.core.flightplan.PlanType;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

public class AoiWorkflowHints {

    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final DisplaySettings.WorkflowHints workflowHints;

    private final ChangeListener<ObservableList<AreaOfInterestCorner>> cornerListChangedListener =
        new ChangeListener<>() {
            @Override
            public void changed(
                    ObservableValue<? extends ObservableList<AreaOfInterestCorner>> observableValue,
                    ObservableList<AreaOfInterestCorner> oldCorners,
                    ObservableList<AreaOfInterestCorner> newCorners) {
                if (newCorners != null && newCorners.size() > 1) {
                    if (toast != null) {
                        toast.dismiss();
                        toast = null;
                    }
                }
            }
        };

    private Toast toast;

    @Inject
    public AoiWorkflowHints(
            ISettingsManager settingsManager, IApplicationContext applicationContext, ILanguageHelper languageHelper) {
        this.workflowHints = settingsManager.getSection(DisplaySettings.class).getWorkflowHints();
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
    }

    public void reportEvent(AoiWorkflowEvent aoiWorkflowEvent) {
        if (aoiWorkflowEvent.allowedToShow(workflowHints)) {
            AreaOfInterest aoi = aoiWorkflowEvent.getAoi();
            if (aoi.isInitialAddingProperty().get()) {
                String textForAoi = getTextForAoi(aoi.getType());
                if (textForAoi != null) {
                    toast = simpleToast(aoiWorkflowEvent, textForAoi);
                    toast.isShowingProperty()
                        .addListener(
                            (observable, wasShowing, isShowing) -> {
                                if (!isShowing) {
                                    aoi.cornerListProperty().removeListener(cornerListChangedListener);
                                }
                            });
                    applicationContext.addToast(toast);
                    // first remove it, to make sure its not added twice
                    aoi.cornerListProperty().removeListener(cornerListChangedListener);

                    // eventuall dismiss the toast when corner list is changed and toast got obsolete
                    aoi.cornerListProperty().addListener(cornerListChangedListener);
                }
            }
        }
    }

    public DisplaySettings.WorkflowHints getWorkflowHints() {
        return workflowHints;
    }

    private String getTextForAoi(PlanType aoiType) {
        switch (aoiType) {
        case BUILDING:
            return languageHelper.getString("com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.building");
        case POLYGON:
            return languageHelper.getString("com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.polygon");
        case SPIRAL:
            return languageHelper.getString("com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.spiral");
        case SEARCH:
            return languageHelper.getString("com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.search");
        case CORRIDOR:
            return languageHelper.getString("com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.corridor");
        case TOWER:
            return languageHelper.getString("com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.tower");
        case WINDMILL:
            return languageHelper.getString("com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.tower");
        case FACADE:
            return languageHelper.getString("com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.facade");
        case CITY:
            return languageHelper.getString("com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.citymapping");
        case POINT_OF_INTEREST:
            return languageHelper.getString("com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.poi");
        case PANORAMA:
            return languageHelper.getString("com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.panorama");
        default:
            return null;
        }
    }

    private Toast simpleToast(WorkflowEvent workflowEvent, String textForAoi) {
        return Toast.of(ToastType.INFO)
            .setShowIcon(true)
            .setText(textForAoi)
            .setCloseable(true)
            .setAction(
                languageHelper.getString("com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.hintGotIt"),
                true,
                true,
                () -> {
                    workflowEvent.dontShowAgain(getWorkflowHints());
                    toast = null;
                },
                Platform::runLater)
            .create();
    }

}
