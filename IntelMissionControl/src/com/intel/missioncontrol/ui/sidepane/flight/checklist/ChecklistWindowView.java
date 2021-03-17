/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.checklist;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.ItemsView;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ChecklistWindowView extends ViewBase<ChecklistWindowViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    private ScrollPane scroller;

    @InjectViewModel
    private ChecklistWindowViewModel viewModel;

    @FXML
    private ItemsView<ChecklistViewModel> checklists;

    @FXML
    public Button closeDialogButton;

    @Override
    public void initializeView() {
        super.initializeView();

        checklists.addViewFactory(
                ChecklistViewModel.class,
                vm -> FluentViewLoader.fxmlView(ChecklistView.class).viewModel(vm).load().getView());
        checklists.itemsProperty().bind(viewModel.checklistsProperty());

        closeDialogButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            Stage stage = (Stage) closeDialogButton.getScene().getWindow();
            stage.hide();
        });
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public ChecklistWindowViewModel getViewModel() {
        return viewModel;
    }

}
