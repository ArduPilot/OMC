/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.options;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.ViewModelBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class AnalysisOptionsViewModel extends ViewModelBase {

    private final SimpleBooleanProperty rtkAvailable = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty rtkVisible = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<Matching> currentMatching = new SimpleObjectProperty();
    private final SimpleObjectProperty<MatchingStatus> matchingStatus = new SimpleObjectProperty();

    @Inject
    public AnalysisOptionsViewModel(IApplicationContext applicationContext, GeneralSettings generalSettings) {
        currentMatching.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .selectReadOnlyObject(Mission::currentMatchingProperty));
        rtkAvailable.bind(PropertyPath.from(currentMatching).selectReadOnlyBoolean(Matching::rtkAvailableProperty));
        matchingStatus.bind(PropertyPath.from(currentMatching).selectReadOnlyObject(Matching::statusProperty));
        rtkVisible.bind(generalSettings.operationLevelProperty().isNotEqualTo(OperationLevel.USER));
    }

    public ReadOnlyBooleanProperty rtkAvailableProperty() {
        return rtkAvailable;
    }

    public SimpleBooleanProperty rtkVisibleProperty() {
        return rtkVisible;
    }

    public ReadOnlyObjectProperty<Matching> currentMatchingProperty() {
        return currentMatching;
    }

    public ReadOnlyObjectProperty<MatchingStatus> matchingStatusProperty() {
        return matchingStatus;
    }
}
