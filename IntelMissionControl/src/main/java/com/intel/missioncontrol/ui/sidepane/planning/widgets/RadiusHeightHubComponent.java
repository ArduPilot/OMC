/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import de.saxsys.mvvmfx.Context;
import javafx.beans.NamedArg;

/** This class can be used as FXML tag */
public class RadiusHeightHubComponent extends GridComponentBase<RadiusHeightHubWidgetView, RadiusHeightHubWidgetViewModel> {

    public RadiusHeightHubComponent(@NamedArg("context") Context context) {
        super(RadiusHeightHubWidgetView.class, context);
    }

}
