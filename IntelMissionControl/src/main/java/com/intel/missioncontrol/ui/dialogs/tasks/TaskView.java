/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.tasks;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TaskView extends VBox implements JavaView<TaskViewModel> {

    @InjectViewModel
    private TaskViewModel viewModel;

    @Inject
    private ILanguageHelper languageHelper;

    public void initialize() {
        getStyleClass().add("list-item");

        Label nameLabel = new Label();
        nameLabel.setMaxWidth(Double.POSITIVE_INFINITY);
        nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        nameLabel.textProperty().bind(viewModel.nameProperty());
        nameLabel.setMinWidth(0);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Hyperlink cancelLink = new Hyperlink();
        cancelLink.setPadding(new Insets(0, 0, 0, ScaleHelper.emsToPixels(0.5)));
        cancelLink.setText(languageHelper.getString("com.intel.missioncontrol.ui.dialogs.tasks.TaskView.cancel"));
        cancelLink.setOnAction(event -> viewModel.getCancelCommand().execute());
        cancelLink.disableProperty().bind(viewModel.getCancelCommand().notExecutableProperty());
        HBox.setHgrow(cancelLink, Priority.NEVER);

        BorderPane line = new BorderPane();
        line.setCenter(nameLabel);
        line.setRight(cancelLink);
        line.setMaxWidth(Double.POSITIVE_INFINITY);
        getChildren().add(line);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.POSITIVE_INFINITY);
        progressBar.progressProperty().bind(viewModel.progressProperty());
        getChildren().add(progressBar);
    }

}
