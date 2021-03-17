/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.dialogs.ProgressTask;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.DataTransferFtpViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.widget.ProgressButton;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Pane;

public class DataTransferFtpView extends ViewBase<DataTransferFtpViewModel> {

    @FXML
    private Pane layoutRoot;

    @FXML
    private ProgressButton photologButton;

    @FXML
    private ProgressButton flightLogsButton;

    @FXML
    private ProgressButton gpsRawDataButton;

    @FXML
    private ProgressButton gpsDebuggingButton;

    @FXML
    private Hyperlink connectorUrl;

    @FXML
    private Hyperlink uavUrl;

    @FXML
    private Hyperlink gpsRawDataCancel;

    @FXML
    private Hyperlink gpsDebuggingButtonCancel;

    @FXML
    private Hyperlink flightLogsButtonCancel;

    @FXML
    private Hyperlink photologButtonCancel;

    @InjectViewModel
    private DataTransferFtpViewModel viewModel;

    @Override
    protected void initializeView() {
        super.initializeView();
        connectorUrl.visibleProperty().bind(viewModel.connectorUrlVisibleProperty());
        connectorUrl.textProperty().bind(viewModel.connectorUrlProperty());
        uavUrl.visibleProperty().bind(viewModel.uavUrlVisibleProperty());
        uavUrl.textProperty().bind(viewModel.uavUrlProperty());

        setCancelButtonVisibility(gpsRawDataButton, gpsRawDataCancel);
        setCancelButtonVisibility(gpsDebuggingButton, gpsDebuggingButtonCancel);
        setCancelButtonVisibility(flightLogsButton, flightLogsButtonCancel);
        setCancelButtonVisibility(photologButton, photologButtonCancel);
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected DataTransferFtpViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    private void showConnectionInBrowser() {
        viewModel.showUrlInBrowser(connectorUrl.getText());
    }

    @FXML
    private void showUavInBrowser() {
        viewModel.showUrlInBrowser(uavUrl.getText());
    }

    private void setCancelButtonVisibility(ProgressButton progressButton, Hyperlink cancelLink) {
        progressButton
            .stateProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    cancelLink.visibleProperty().setValue(newValue == ProgressButton.State.IN_PROGRESS);
                });
    }

    @FXML
    public void photologButtonClicked() {
        addTaskFinishListeners(viewModel.downloadPhotolog(), photologButtonCancel, photologButton);
    }

    @FXML
    public void flightLogsButtonClicked() {
        addTaskFinishListeners(viewModel.downloadFlightlog(), flightLogsButtonCancel, flightLogsButton);
    }

    @FXML
    public void gpsRawDataButtonClicked() {
        addTaskFinishListeners(viewModel.downloadGpsRawData(), gpsRawDataCancel, gpsRawDataButton);
    }

    @FXML
    public void gpsDebuggingButtonClicked() {
        addTaskFinishListeners(viewModel.downloadGpsDebugging(), gpsDebuggingButtonCancel, gpsDebuggingButton);
    }

    private void addTaskFinishListeners(
            ProgressTask progressTask, Hyperlink cancelHyperlink, ProgressButton progressButton) {
        progressButton.stateProperty().set(ProgressButton.State.IN_PROGRESS);

        progressTask.setOnCancelled(event -> handleTaskFinish(cancelHyperlink, progressButton));
        progressTask.setOnFailed(event -> handleTaskFinish(cancelHyperlink, progressButton));
        progressTask.setOnSucceeded(event -> handleTaskFinish(cancelHyperlink, progressButton));

        cancelHyperlink.setOnMouseClicked(
            event -> {
                progressTask.cancel();
                cancelHyperlink.setVisible(false);
            });
    }

    private void handleTaskFinish(Hyperlink cancelHyperlink, ProgressButton progressButton) {
        cancelHyperlink.visibleProperty().set(false);
        progressButton.stateProperty().set(ProgressButton.State.PRE_PROGRESS);
    }

}
