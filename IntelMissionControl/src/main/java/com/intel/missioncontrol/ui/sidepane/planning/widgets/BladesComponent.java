/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import de.saxsys.mvvmfx.Context;
import javafx.beans.NamedArg;

/** This class can be used as FXML tag */
public class BladesComponent extends GridComponentBase<BladesWidgetView, BladesWidgetViewModel> {

    public BladesComponent(@NamedArg("context") Context context) {
        super(BladesWidgetView.class, context);
    }

}
