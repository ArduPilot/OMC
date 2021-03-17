/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.scene.layout.VBox;

public class GpsGlonass extends VBox implements AlertAwareComponent {

    private final ViewTuple<GpsGlonassView, GpsGlonassViewModel> viewModelTuple;

    public GpsGlonass() {
        viewModelTuple = FluentViewLoader.fxmlView(GpsGlonassView.class).root(this).load();
    }

    @Override
    public GpsGlonassViewModel getViewModel() {
        return viewModelTuple.getViewModel();
    }

    public AlertLevel getAlert() {
        return getViewModel().alertPropery().getValue();
    }

    public void setAlert(AlertLevel alert) {
        getViewModel().alertPropery().setValue(alert);
    }

}
