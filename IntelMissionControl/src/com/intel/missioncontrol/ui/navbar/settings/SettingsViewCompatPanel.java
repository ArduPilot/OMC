/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings;

import com.intel.missioncontrol.ui.MainScope;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

public class SettingsViewCompatPanel extends JFXPanel {

    private static final long serialVersionUID = -2849512077949139913L;

    public SettingsViewCompatPanel(MainScope mainScope) {
        ViewTuple<SettingsView, SettingsViewModel> viewTuple =
            FluentViewLoader.fxmlView(SettingsView.class).providedScopes(mainScope).load();
        setScene(new Scene(viewTuple.getView()));
    }

}
