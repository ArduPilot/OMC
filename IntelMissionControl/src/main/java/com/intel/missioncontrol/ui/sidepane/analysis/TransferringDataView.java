/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import org.asyncfx.beans.property.PropertyPath;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.controls.MenuButton;
import com.intel.missioncontrol.ui.sidepane.FancyTabView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import org.controlsfx.control.TaskProgressView;

public class TransferringDataView extends FancyTabView<TransferringDataViewModel> {

    @InjectViewModel
    private TransferringDataViewModel viewModel;

    @FXML
    private TaskProgressView<Task<Void>> progressView;

    @FXML
    private Parent layoutRoot;

    @FXML
    private Label projectNameLabel;

    @FXML
    private MenuButton datasetMenuButton;

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    private final IApplicationContext applicationContext;

    @Inject
    public TransferringDataView(IApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        layoutRoot.setVisible(false);
        viewModel
            .shownTaskProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        progressView.getTasks().add(newValue);
                    }
                });

        projectNameLabel
            .textProperty()
            .bind(
                PropertyPath.from(applicationContext.currentLegacyMissionProperty())
                    .selectReadOnlyString(Mission::nameProperty));

        datasetMenuButton.modelProperty().bind(viewModel.datasetMenuModelProperty());
    }

    @FXML
    public void renameClicked() {
        viewModel.getRenameMissionCommand().execute();
    }
}
