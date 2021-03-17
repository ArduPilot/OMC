/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import de.saxsys.mvvmfx.Context;
import javafx.beans.NamedArg;

public class PowerPoleComponent extends GridComponentBase<PowerPoleWidgetView, PowerPoleWidgetViewModel> {

    public PowerPoleComponent(@NamedArg("context") Context context) {
        super(PowerPoleWidgetView.class, context);
    }

}
