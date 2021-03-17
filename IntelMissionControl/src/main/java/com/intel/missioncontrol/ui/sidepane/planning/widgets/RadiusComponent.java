/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import de.saxsys.mvvmfx.Context;
import javafx.beans.NamedArg;

public class RadiusComponent extends GridComponentBase<RadiusWidgetView, RadiusWidgetViewModel> {

    public RadiusComponent(@NamedArg("context") Context context) {
        super(RadiusWidgetView.class, context);
    }

}
