/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navigation.ConnectionPage;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.view.widget.AirplaneSpeedComponent;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.LogFilePlayerViewModel;
import com.intel.missioncontrol.utils.DurationConverter;
import com.intel.missioncontrol.utils.DurationTimeSpinnerValidator;
import com.intel.missioncontrol.utils.IntegerValidator;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Pane;

/** @author Vladimir Iordanov */
public class LogFilePlayerView extends ViewBase<LogFilePlayerViewModel> {

    @InjectViewModel
    private LogFilePlayerViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private Hyperlink logFileFileName;

    @FXML
    private Button logPlayerBack;

    @FXML
    private Button logPlayerPlay;

    @FXML
    private Button logPlayerPause;

    @FXML
    private Button logPlayerFwd;

    @FXML
    private Spinner<Integer> logPlayerTime;

    @FXML
    private Label logPlayerTimeTotal;

    @FXML
    private Label estimatedLogPlayerSpeed;

    @FXML
    private Spinner<Integer> logPlayerKeyframe;

    @FXML
    private Label logPlayerKeyframeTotal;

    @FXML
    private AirplaneSpeedComponent airplaneSpeedComponent;

    @FXML
    private Button logPlayerAddMarker;

    @FXML
    private Button logPlayerOpenPlot;

    @FXML
    private Button logPlayerSupportRequest;

    @FXML
    private Button logPlayerClearTrack;

    @FXML
    private Button stopReplay;

    private final DurationConverter durationConverter = new DurationConverter();

    @Override
    public void initializeView() {
        super.initializeView();
        initBindings();
    }

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    public LogFilePlayerViewModel getViewModel() {
        return viewModel;
    }

    private void initBindings() {
        logFileFileName.textProperty().bind(viewModel.logFileNameProperty());
        logFileFileName.setOnAction(event -> viewModel.getFileNameClickCommand().execute());

        logPlayerBack.setOnMousePressed(event -> viewModel.getRewindPlayCommand().execute());
        logPlayerBack.disableProperty().bind(viewModel.getRewindPlayCommand().executableProperty().not());

        logPlayerPlay.setOnMousePressed(event -> viewModel.getPlayCommand().execute());
        logPlayerPlay.disableProperty().bind(viewModel.getPlayCommand().executableProperty().not());

        logPlayerPause.setOnMousePressed(event -> viewModel.getPauseCommand().execute());
        logPlayerPause.disableProperty().bind(viewModel.getPauseCommand().executableProperty().not());

        logPlayerFwd.setOnMousePressed(event -> viewModel.getFastForwardCommand().execute());
        logPlayerFwd.disableProperty().bind(viewModel.getFastForwardCommand().executableProperty().not());

        initKeyframeSpinner();
        initTimeSpinner();

        viewModel
            .totalTimeProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    logPlayerTimeTotal.setText(durationConverter.formatToString(newValue));
                });
        logPlayerKeyframeTotal.textProperty().bind(viewModel.totalFramesProperty().asString());

        estimatedLogPlayerSpeed.textProperty().bind(viewModel.estimatedSimulationSpeedProperty().asString());
        //airplaneSpeedComponent.simulationSpeedProperty().bindBidirectional(viewModel.simulationSpeedProperty());

        // Set max value for spinners according to max values (time and frames)
        viewModel.totalTimeProperty().addListener((observable, oldValue, newValue) -> setTimeSpinnerMaxValue(newValue));
        viewModel
            .totalFramesProperty()
            .addListener((observable, oldValue, newValue) -> setFramesSpinnerMaxValue(newValue));

        logPlayerClearTrack.setOnMousePressed(event -> viewModel.getClearTrackCommand().execute());

        stopReplay.setOnMousePressed(event -> viewModel.getStopPlayCommand().execute());

        viewModel
            .isConnectedToLogProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    boolean isConnected = newValue;

                    ConnectionPage connectionPage = isConnected ? ConnectionPage.LOG_FILE : null;
                    ConnectionState connectionState =
                        isConnected ? ConnectionState.CONNECTED : ConnectionState.NOT_CONNECTED;
                    viewModel.connectedPageProperty().setValue(connectionPage);
                    viewModel.connectionStateProperty().setValue(connectionState);
                });
    }

    private void initKeyframeSpinner() {
        IntegerValidator validator = new IntegerValidator(0, 0);

        SpinnerValueFactory<Integer> valueFactory = validator.getValueFactory();
        valueFactory.valueProperty().bindBidirectional(viewModel.keyframeProperty());

        logPlayerKeyframe.setValueFactory(valueFactory);
        logPlayerKeyframe.getEditor().setTextFormatter(validator.getTextFormatter());
        logPlayerKeyframe.disableProperty().bind(viewModel.canEditKeyFrame().not());
    }

    private void initTimeSpinner() {
        DurationTimeSpinnerValidator validator = new DurationTimeSpinnerValidator(0, 100);

        SpinnerValueFactory<Integer> valueFactory = validator.getValueFactory();
        valueFactory.valueProperty().bindBidirectional(viewModel.currentTimeProperty());

        logPlayerTime.setValueFactory(valueFactory);
        logPlayerTime.getEditor().setTextFormatter(validator.getTextFormatter());
        // Disable forever because backend doesn't have ability tu jump in time
        logPlayerTime.setDisable(true);
    }

    private void setFramesSpinnerMaxValue(Integer maxValue) {
        ((SpinnerValueFactory.IntegerSpinnerValueFactory)logPlayerKeyframe.getValueFactory()).setMax(maxValue);
    }

    private void setTimeSpinnerMaxValue(Integer maxValue) {
        ((SpinnerValueFactory.IntegerSpinnerValueFactory)logPlayerTime.getValueFactory()).setMax(maxValue);
    }
}
