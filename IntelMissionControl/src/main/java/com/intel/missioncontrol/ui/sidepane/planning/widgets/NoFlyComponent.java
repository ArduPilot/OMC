/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import de.saxsys.mvvmfx.Context;
import javafx.beans.NamedArg;

public class NoFlyComponent
        extends GridComponentBase<NoFlyWidgetView, NoFlyWidgetViewModel> {

    public NoFlyComponent(@NamedArg("context") Context context) {
        super(NoFlyWidgetView.class, context);
    }

}
