/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import com.intel.missioncontrol.ui.menu.MenuModel;
import com.intel.missioncontrol.ui.navbar.tools.ToolsPage;
import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MainScope implements Scope {

    private final ObjectProperty<ToolsPage> activeToolsPage = new SimpleObjectProperty<>();

    private final StringProperty mainMenuStatusText = new SimpleStringProperty();
    private final ObjectProperty<MenuModel> mainMenuModel = new SimpleObjectProperty<>();
    private final ObjectProperty<MenuModel> flightPlanMenuModel = new SimpleObjectProperty<>();
    private final ObjectProperty<MenuModel> datasetMenuModel = new SimpleObjectProperty<>();
    private final ObjectProperty<MenuModel>  flyDroneMenuModel = new SimpleObjectProperty<>();
    public StringProperty mainMenuStatusTextProperty() {
        return mainMenuStatusText;
    }

    public ObjectProperty<MenuModel> mainMenuModelProperty() {
        return mainMenuModel;
    }

    public ObjectProperty<MenuModel> flightPlanMenuModelProperty() {
        return flightPlanMenuModel;
    }

    public ObjectProperty<MenuModel> datasetMenuModelProperty() {
        return datasetMenuModel;
    }

    public ObjectProperty<MenuModel> flyDroneMenuModelProperty() {
        return flyDroneMenuModel;
    }

    public ObjectProperty<ToolsPage> activeToolsPage() {
        return activeToolsPage;
    }

}
