/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.preflightchecks;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.ui.controls.ItemsView;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import com.intel.missioncontrol.ui.sidepane.flight.checklist.ChecklistView;
import com.intel.missioncontrol.ui.sidepane.flight.checklist.ChecklistViewModel;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class ManualChecklistDialogView extends DialogView<ManualChecklistDialogViewModel> {

    @InjectViewModel
    private ManualChecklistDialogViewModel viewModel;

    @FXML
    private VBox checklistrootlayout;

    @FXML
    private ItemsView<ChecklistViewModel> checklists;

    private LanguageHelper languageHelper;

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.preflightchecks.ManualChecklistDialogView.title"));
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
    public ManualChecklistDialogView(LanguageHelper languageHelper) {
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

    public void onCloseClicked(ActionEvent actionEvent) {
        viewModel.getCloseCommand().execute();
    }
}
