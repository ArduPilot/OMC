/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.summary;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class WarningsView extends ViewBase<WarningsViewModel> {

    @InjectViewModel
    private WarningsViewModel viewModel;

    @FXML
    private Pane rootNode;

    @Inject
    private ILanguageHelper languageHelper;

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public WarningsViewModel getViewModel() {
        return viewModel;
    }

}
