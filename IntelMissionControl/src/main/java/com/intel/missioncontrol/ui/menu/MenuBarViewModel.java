/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.menu;

import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;

public class MenuBarViewModel extends ViewModelBase {

    @InjectScope
    private MainScope mainScope;

    public ReadOnlyObjectProperty<MenuModel> menuModelProperty() {
        return mainScope.mainMenuModelProperty();
    }

    public StringProperty statusTextProperty() {
        return mainScope.mainMenuStatusTextProperty();
    }

}
