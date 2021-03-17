/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.checks.preflight;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.controls.ItemsView;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.ChecklistView;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.ChecklistViewModel;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class PreflightChecklistDialogView extends DialogView<PreflightChecklistDialogViewModel> {

    @InjectViewModel
    private PreflightChecklistDialogViewModel viewModel;

    @FXML
    private VBox checklistrootlayout;

    @FXML
    private ItemsView<ChecklistViewModel> checklists;

    private ILanguageHelper languageHelper;

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.sidepane.flight.fly.checks.preflight.PreflightChecklistDialogView.title"));
    }

    @Override
    protected Parent getRootNode() {
        return checklistrootlayout;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Inject
    public PreflightChecklistDialogView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        checklists.addViewFactory(
            ChecklistViewModel.class,
            vm -> FluentViewLoader.fxmlView(ChecklistView.class).viewModel(vm).load().getView());

        checklists.setItems(viewModel.checklistsProperty());
    }

    @FXML
    public void onCloseClicked() {
        viewModel.getCloseCommand().execute();
    }

}
