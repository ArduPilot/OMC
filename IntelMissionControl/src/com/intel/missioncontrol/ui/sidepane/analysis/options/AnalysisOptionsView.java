/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.options;

import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class AnalysisOptionsView extends ViewBase<AnalysisOptionsViewModel> {

    @FXML
    private Tab locationTab;

    @FXML
    private Tab sourceDataTab;

    @FXML
    private TabPane optionsTabPane;

    @InjectViewModel
    private AnalysisOptionsViewModel viewModel;

    @Override
    public void initializeView() {
        super.initializeView();

        if (!viewModel.rtkVisibleProperty().get()) {
            // this way of hiding the RTK location tab requires restart of IMC to response to Level Change
            optionsTabPane.getTabs().remove(locationTab);
        }

        locationTab.disableProperty().bind(viewModel.rtkAvailableProperty().not());

        viewModel
            .currentMatchingProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    sourceDataTab.getTabPane().requestLayout();
                    fixSelectedTab();
                });

        viewModel
            .matchingStatusProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    sourceDataTab.getTabPane().requestLayout();
                    fixSelectedTab();
                });

        viewModel.rtkAvailableProperty().addListener((observable, oldValue, newValue) -> fixSelectedTab());
    }

    @Override
    protected Parent getRootNode() {
        return optionsTabPane;
    }

    @Override
    public AnalysisOptionsViewModel getViewModel() {
        return viewModel;
    }

    private void fixSelectedTab() {
        if (optionsTabPane.getSelectionModel().getSelectedItem() == locationTab
                && viewModel.rtkAvailableProperty().not().get()) {
            optionsTabPane.getSelectionModel().select(sourceDataTab);
        }
    }

}
