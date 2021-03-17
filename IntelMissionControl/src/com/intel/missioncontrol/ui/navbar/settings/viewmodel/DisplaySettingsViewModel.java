/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.beans.property.UIAsyncBooleanProperty;
import com.intel.missioncontrol.settings.DisplaySettings;
import com.intel.missioncontrol.settings.InternetConnectivitySettings;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.sidepane.analysis.AnalysisSettings;
import javafx.beans.property.Property;

public class DisplaySettingsViewModel extends ViewModelBase {

    private final UIAsyncBooleanProperty introductionEnabled = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty addAreaOfInterestWorkflowHintEnabled = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty dataTransferWorkflowHintEnabled = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty srsCheckEnabled = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty showNews = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty demoDatasetHelp = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty showNewsVisible = new UIAsyncBooleanProperty(this);

    @Inject
    public DisplaySettingsViewModel(
            DisplaySettings displaySettings,
            AnalysisSettings analysisSettings,
            InternetConnectivitySettings internetConnectivitySettings) {
        introductionEnabled.bindBidirectional(displaySettings.introductionEnabledProperty());
        showNews.bindBidirectional(displaySettings.showNewsProperty());
        addAreaOfInterestWorkflowHintEnabled.bindBidirectional(
            displaySettings.getWorkflowHints().addAreaOfInterestProperty());
        dataTransferWorkflowHintEnabled.bindBidirectional(displaySettings.getWorkflowHints().dataTransferProperty());
        srsCheckEnabled.bindBidirectional(displaySettings.getWorkflowHints().srsCheckEnabledProperty());
        demoDatasetHelp.bindBidirectional(analysisSettings.dataTransferPopupEnabledProperty());
        showNewsVisible.bind(internetConnectivitySettings.isIntelProxyProperty());
    }

    public Property<Boolean> introductionEnabledProperty() {
        return introductionEnabled;
    }

    public Property<Boolean> addAreaOfInterestWorkflowHintEnabledProperty() {
        return addAreaOfInterestWorkflowHintEnabled;
    }

    public Property<Boolean> dataTransferWorkflowHintEnabledProperty() {
        return dataTransferWorkflowHintEnabled;
    }

    public Property<Boolean> srsCheckEnabledProperty() {
        return srsCheckEnabled;
    }

    public Property<Boolean> showNewsProperty() {
        return showNews;
    }

    public UIAsyncBooleanProperty demoDatasetHelpProperty() {
        return demoDatasetHelp;
    }

    public UIAsyncBooleanProperty showNewsVisibleProperty() {
        return showNewsVisible;
    }
}
