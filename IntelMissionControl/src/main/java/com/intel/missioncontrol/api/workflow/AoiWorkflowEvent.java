/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api.workflow;

import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.settings.DisplaySettings;
import org.jetbrains.annotations.NotNull;

public class AoiWorkflowEvent implements WorkflowEvent {

    private AreaOfInterest aoi;

    public AoiWorkflowEvent(@NotNull AreaOfInterest aoi) {
        this.aoi = aoi;
    }

    @Override
    public boolean allowedToShow(DisplaySettings.WorkflowHints hintsSettings) {
        return hintsSettings.addAreaOfInterestProperty().get();
    }

    @Override
    public void dontShowAgain(DisplaySettings.WorkflowHints hintsSettings) {
        hintsSettings.addAreaOfInterestProperty().set(false);
    }

    public AreaOfInterest getAoi() {
        return aoi;
    }
}
