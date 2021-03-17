/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.checks.automatic;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.controls.ItemsView;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class AutomaticChecksDialogView extends DialogView<AutomaticChecksDialogViewModel> {

    @InjectViewModel
    private AutomaticChecksDialogViewModel viewModel;

    @FXML
    private VBox layoutRoot;

    @FXML
    private ItemsView<AutoCheckItemViewModel> itemsView;

    private final ILanguageHelper languageHelper;

    public void onDoneButtonClicked(ActionEvent actionEvent) {
        viewModel.getCloseCommand().execute();
    }

    @Inject
    public AutomaticChecksDialogView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        itemsView.addViewFactory(
            AutoCheckItemViewModel.class,
            vm -> FluentViewLoader.javaView(AutoCheckItemView.class).viewModel(vm).load().getView());
        itemsView.itemsProperty().bind(viewModel.autoCheckItemsProperty());
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(languageHelper.getString(AutomaticChecksDialogView.class, "title"));
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

}
