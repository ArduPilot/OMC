/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import de.saxsys.mvvmfx.Context;
import javafx.beans.NamedArg;

public class WidthComponent extends GridComponentBase<WidthWidgetView, WidthWidgetViewModel> {

    public WidthComponent(@NamedArg("context") Context context) {
        super(WidthWidgetView.class, context);
    }

}
