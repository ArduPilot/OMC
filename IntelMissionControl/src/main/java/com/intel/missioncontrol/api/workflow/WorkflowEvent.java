/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api.workflow;

import com.intel.missioncontrol.settings.DisplaySettings;

public interface WorkflowEvent {
    boolean allowedToShow(DisplaySettings.WorkflowHints hintsSettings);

    void dontShowAgain(DisplaySettings.WorkflowHints hintsSettings);
}
