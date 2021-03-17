/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings;

import com.google.inject.Inject;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.NavBarDialog;
import com.intel.missioncontrol.ui.navigation.SettingsPage;
import javafx.beans.property.ReadOnlyObjectProperty;

public class SettingsViewModel extends DialogViewModel {

    private final INavigationService navigationService;

    @Inject
    public SettingsViewModel(INavigationService navigationService, GeneralSettings generalSettings) {
        this.navigationService = navigationService;
    }

    public ReadOnlyObjectProperty<SettingsPage> currentPageProperty() {
        return navigationService.settingsPageProperty();
    }

    public void setCurrentPage(SettingsPage page) {
        navigationService.navigateTo(page);
    }

    @Override
    protected void onClosing() {
        navigationService.navigateTo(NavBarDialog.NONE);
    }

}
