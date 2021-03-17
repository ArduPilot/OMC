/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.start;

import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import javafx.scene.control.Label;

public class DateItemView extends Label implements JavaView<DateItemViewModel> {

    @InjectViewModel
    private DateItemViewModel viewModel;

    public void initialize() {
        getStyleClass().add("title");
        setText(viewModel.textProperty().get());
    }

}
