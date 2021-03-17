/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.intel.missioncontrol.ui.MainScope;
import de.saxsys.mvvmfx.Context;
import javafx.beans.NamedArg;

public class FlightDirectionComponent
        extends GridComponentBase<FlightDirectionWidgetView, FlightDirectionWidgetViewModel> {

    public FlightDirectionComponent(@NamedArg("context") Context context) {
        super(FlightDirectionWidgetView.class, context);
    }

}
