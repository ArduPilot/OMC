/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.beans.NamedArg;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class GsdComponent extends VBox {

    private final ViewTuple<GsdWidgetView, GsdWidgetViewModel> vieModelTuple;

    public GsdComponent() {
        vieModelTuple = FluentViewLoader.fxmlView(GsdWidgetView.class).root(this).load();
    }

    public GsdComponent(@NamedArg("context") Context context) {
        vieModelTuple = FluentViewLoader.fxmlView(GsdWidgetView.class).root(this).context(context).load();
    }

    public GsdWidgetViewModel getViewModel() {
        return vieModelTuple.getViewModel();
    }
}
