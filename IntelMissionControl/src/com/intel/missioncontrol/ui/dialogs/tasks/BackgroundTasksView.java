/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.tasks;

import com.intel.missioncontrol.ui.RootView;
import com.intel.missioncontrol.ui.controls.ItemsView;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class BackgroundTasksView extends RootView<BackgroundTasksViewModel> {

    @InjectViewModel
    private BackgroundTasksViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private ItemsView<TaskViewModel> itemsView;

    @FXML
    private Label noTasksLabel;

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        itemsView.addViewFactory(
            TaskViewModel.class, vm -> FluentViewLoader.javaView(TaskView.class).viewModel(vm).load().getView());
        itemsView.itemsProperty().bind(viewModel.runningTasksProperty());

        itemsView.visibleProperty().bind(viewModel.runningTasksProperty().emptyProperty().not());
        itemsView.managedProperty().bind(itemsView.visibleProperty());

        noTasksLabel.visibleProperty().bind(viewModel.runningTasksProperty().emptyProperty());
        noTasksLabel.managedProperty().bind(noTasksLabel.visibleProperty());
    }

}
