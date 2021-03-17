/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.LogFileViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/** @author Vladimir Iordanov */
public class LogFileView extends ViewBase<LogFileViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    private Pane logFilePlayerPane;

    @InjectViewModel
    private LogFileViewModel viewModel;

    @Override
    public void initializeView() {
        super.initializeView();
        initPanes();
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public LogFileViewModel getViewModel() {
        return viewModel;
    }

    private void initPanes() {
        TranslateTransition openPlayerPane = new TranslateTransition(new Duration(400), logFilePlayerPane);
        openPlayerPane.setToX(0);
        TranslateTransition closePlayerPane = new TranslateTransition(new Duration(400), logFilePlayerPane);

        viewModel
            .stateProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (logFilePlayerPane.getTranslateX() == 0) {
                        // Adjust X to the current width of the pane
                        closePlayerPane.setToX(logFilePlayerPane.getWidth());
                        closePlayerPane.play();
                    } else {
                        openPlayerPane.play();
                    }
                });

        // We need to initialize TranslateX with the real width of the pane which will be calculated later
        logFilePlayerPane
            .widthProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    logFilePlayerPane.setTranslateX(newValue.doubleValue());
                });
    }
}
