/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.options;

import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
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

    @FXML
    private Tab annotationsTab;

    @FXML
    private Tab statisticsTab;

    @InjectViewModel
    private AnalysisOptionsViewModel viewModel;

    @Override
    public void initializeView() {
        super.initializeView();

        locationTab.disableProperty().bind(viewModel.rtkAvailableProperty().not());

        // TODO IMC-3132 Annotations
        // annotationsTab.disableProperty().bind(viewModel.matchingStatusProperty().isEqualTo(MatchingStatus.NEW));
        annotationsTab.disableProperty().set(true);
        statisticsTab.disableProperty().bind(viewModel.matchingStatusProperty().isEqualTo(MatchingStatus.NEW));

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
