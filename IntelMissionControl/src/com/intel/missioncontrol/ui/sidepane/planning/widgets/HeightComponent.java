/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.intel.missioncontrol.ui.MainScope;
import de.saxsys.mvvmfx.Context;
import javafx.beans.NamedArg;

public class HeightComponent extends GridComponentBase<HeightWidgetView, HeightWidgetViewModel> {

    public HeightComponent(@NamedArg("context") Context context) {
        super(HeightWidgetView.class, context);
    }

}
