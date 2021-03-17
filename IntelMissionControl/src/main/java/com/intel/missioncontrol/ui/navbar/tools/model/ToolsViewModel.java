/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.navbar.tools.ToolsPage;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.NavBarDialog;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.property.ObjectProperty;

public class ToolsViewModel extends DialogViewModel {

    @InjectScope
    private MainScope mainScope;

    @Inject
    private INavigationService navigationService;

    public ObjectProperty<ToolsPage> currentPageProperty() {
        return mainScope.activeToolsPage();
    }

    @Override
    protected void onClosing() {
        navigationService.navigateTo(NavBarDialog.NONE);
    }
}
