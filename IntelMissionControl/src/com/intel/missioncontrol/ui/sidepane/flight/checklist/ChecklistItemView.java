/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.checklist;

import com.intel.missioncontrol.helper.ScaleHelper;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;

public class ChecklistItemView extends CheckBox implements JavaView<ChecklistItemViewModel> {

    @InjectViewModel
    private ChecklistItemViewModel viewModel;

    public void initialize() {
        this.setWrapText(true);
        this.setWidth(ScaleHelper.emsToPixels(4.0));
        this.setPadding(new Insets(ScaleHelper.emsToPixels(0.2)));
        textProperty().bind(viewModel.textProperty());
        selectedProperty().bindBidirectional(viewModel.checkedProperty());
    }
}
