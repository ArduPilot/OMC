/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.update;

import com.google.inject.Inject;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.update.EnumUpdateTargets;
import java.util.Map;

/** Created by bulatnikov on 7/16/17. */
@SuppressLinter(value = "IllegalViewModelMethod", reviewer = "mstrauss", justification = "legacy file")
public class UpdateViewModel extends DialogViewModel {

    @Inject
    private IUpdateManager updateManager;

    @Inject
    private ILicenceManager licenceManager;

    void runUpdate(EnumUpdateTargets target) {
        updateManager.runUpdate(target);
    }

    Map<EnumUpdateTargets, AvailableUpdate> getAvailableUpdatesMap() {
        return updateManager.getAvailableUpdatesMap();
    }

    String getCurrentFullVersion() {
        return updateManager.getCurrentFullVersion();
    }

    void onSkipVersion(String target) {
        updateManager.skipVersion(EnumUpdateTargets.valueOf(target));
    }

    public String getCurrentLicenceVersion() {
        return licenceManager.getActiveLicence().getHumanReadableVersion();
    }
}
