/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.summary;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.Button;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class WarningsView extends ViewBase<WarningsViewModel> {

    @InjectViewModel
    private WarningsViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private Button editWaypointsButton;

    private final IDialogContextProvider dialogContextProvider;

    @InjectContext
    private Context context;

    @Inject
    public WarningsView(IDialogContextProvider dialogContextProvider) {
        this.dialogContextProvider = dialogContextProvider;
    }

    @Inject
    private ILanguageHelper languageHelper;

    @Override
    public void initializeView() {
        super.initializeView();
        editWaypointsButton.setOnAction((actionEvent) -> viewModel.getShowEditWayointsDialogCommand().execute());
        dialogContextProvider.setContext(viewModel, context);
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public WarningsViewModel getViewModel() {
        return viewModel;
    }

}
