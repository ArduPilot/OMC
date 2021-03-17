/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.checklist;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.ItemsView;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class ChecklistView extends ViewBase<ChecklistViewModel> {

    @InjectViewModel
    private ChecklistViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private Label caption;

    @FXML
    private ItemsView<ChecklistItemViewModel> itemsView;

    @Override
    public void initializeView() {
        super.initializeView();

        caption.textProperty().bind(viewModel.captionProperty());
        itemsView.addViewFactory(
                ChecklistItemViewModel.class,
                vm -> FluentViewLoader.javaView(ChecklistItemView.class).viewModel(vm).load().getView());
        itemsView.itemsProperty().bind(viewModel.itemsProperty());
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public ChecklistViewModel getViewModel() {
        return viewModel;
    }

}
