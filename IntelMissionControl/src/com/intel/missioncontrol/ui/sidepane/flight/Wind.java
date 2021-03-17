/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.scene.layout.VBox;

public class Wind extends VBox {

    private final ViewTuple<WindView, WindViewModel> viewModelTuple;

    public Wind() {
        viewModelTuple = FluentViewLoader.fxmlView(WindView.class).root(this).load();
    }

    public WindViewModel getViewModel() {
        return viewModelTuple.getViewModel();
    }

}
