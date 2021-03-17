/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import de.saxsys.mvvmfx.Context;
import javafx.beans.NamedArg;

/** This class can be used as FXML tag */
public class RadiusHeightComponent extends GridComponentBase<RadiusHeightWidgetView, RadiusHeightWidgetViewModel> {

    public RadiusHeightComponent(@NamedArg("context") Context context) {
        super(RadiusHeightWidgetView.class, context);
    }

}
