/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.start;

import com.google.inject.Inject;
import com.intel.missioncontrol.beans.property.UIAsyncBooleanProperty;
import com.intel.missioncontrol.settings.DisplaySettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.menu.MainMenuModel;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;

public class IntroductionViewModel extends ViewModelBase {

    @InjectScope
    private MainScope mainScope;

    private final BooleanProperty visible = new SimpleBooleanProperty();
    private final UIAsyncBooleanProperty introductionEnabled = new UIAsyncBooleanProperty(this);

    @Inject
    public IntroductionViewModel(ISettingsManager settingsManager) {
        DisplaySettings displaySettings = settingsManager.getSection(DisplaySettings.class);
        introductionEnabled.bindBidirectional(displaySettings.introductionEnabledProperty());
    }

    public boolean getVisible() {
        return visible.get();
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }

    void hidePanel() {
        visible.set(false);
    }

    Property<Boolean> introductionEnabledProperty() {
        return introductionEnabled;
    }

    public ICommand getShowQuickStartCommand() {
        return mainScope.mainMenuModelProperty().get().find(MainMenuModel.Help.QUICK_START_GUIDE).getCommand();
    }

    public ICommand getDemoMissionCommand() {
        return mainScope.mainMenuModelProperty().get().find(MainMenuModel.Help.DEMO_MISSION).getCommand();
    }

}
