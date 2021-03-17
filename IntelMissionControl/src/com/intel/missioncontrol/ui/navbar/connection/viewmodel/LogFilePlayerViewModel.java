/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.MavinciObjectFactory;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.scope.LogFilePlayerScope;
import com.intel.missioncontrol.ui.navigation.ConnectionPage;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.utils.FileUtils;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.helper.CMathHelper;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackendConnectionLost;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConnectionState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFlightphase;
import eu.mavinci.core.plane.listeners.IAirplaneListenerLogReplay;
import eu.mavinci.core.plane.listeners.IAirplaneListenerSimulationSettings;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.plane.logfile.ALogReader;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Vladimir Iordanov */
public class LogFilePlayerViewModel extends ViewModelBase
        implements IAirplaneListenerSimulationSettings,
            IAirplaneListenerConnectionState,
            IAirplaneListenerLogReplay,
            IAirplaneListenerBackendConnectionLost,
            IAirplaneListenerFlightphase {

    private enum PlayerMode {
        REWIND,
        PLAY,
        PAUSE,
        FAST_FORWARD,
        STOP
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LogFilePlayerViewModel.class);

    public static final String LANG_DIALOG_TITLE_KEY =
        "com.intel.missioncontrol.ui.connection.viewmodel.LogFilePlayerViewModel.dialog.title";
    public static final String LANG_DIALOG_INTERRUPT_KEY =
        "com.intel.missioncontrol.ui.connection.viewmodel.LogFilePlayerViewModel.dialog.interrupt";

    public static final Float DEFAULT_SPEED = 1f;

    @Inject
    private MavinciObjectFactory mavinciObjectFactory;

    @Inject
    private IDialogService dialogService;

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private IApplicationContext applicationContext;

    @Inject
    private INavigationService navigationService;

    @Inject
    private FileUtils fileUtils;

    @Inject
    private IMapModel mapModel;

    @InjectScope
    private LogFilePlayerScope logFilePlayerScope;

    @InjectScope
    private UavConnectionScope uavConnectionScope;

    @InjectScope
    private MainScope mainScope;

    private final StringProperty logFileName = new SimpleStringProperty();
    private final ObjectProperty<Float> simulationSpeed = new SimpleObjectProperty<>();
    private final ObjectProperty<Float> estimatedSimulationSpeed = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> keyframe = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> totalFrames = new SimpleObjectProperty<>();
    private final ObjectProperty<PlayerMode> playerMode = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> currentTime = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> totalTime = new SimpleObjectProperty<>();
    private final BooleanProperty isConnectedToLog = new SimpleBooleanProperty();

    private Command fileNameClickCommand;
    private Command rewindPlayCommand;
    private Command playCommand;
    private Command pauseCommand;
    private Command fastForwardCommand;
    private Command stopPlayCommand;
    private Command clearTrackCommand;

    private ALogReader logReader;
    private IAirplane airplane;

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        initValues();
        initBindings();
    }

    private void initBindings() {
        keyframe.addListener((observable, oldValue, newValue) -> gotoKeyframe(newValue));
        simulationSpeed.addListener((observable, oldValue, newValue) -> setSimulationSpeed(newValue));

        fileNameClickCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            exploreLogFile();
                        }
                    });

        rewindPlayCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            rewind();
                        }
                    },
                playerMode.isEqualTo(PlayerMode.PAUSE).or(playerMode.isEqualTo(PlayerMode.STOP)).not());

        playCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            play();
                            navigationService.navigateTo(WorkflowStep.PLANNING);
                        }
                    },
                playerMode.isEqualTo(PlayerMode.PAUSE).or(playerMode.isEqualTo(PlayerMode.STOP)));

        pauseCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            pause();
                        }
                    },
                playerMode
                    .isEqualTo(PlayerMode.PLAY)
                    .or(playerMode.isEqualTo(PlayerMode.FAST_FORWARD))
                    .or(playerMode.isEqualTo(PlayerMode.REWIND)));

        fastForwardCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            fastForward();
                        }
                    },
                playerMode.isEqualTo(PlayerMode.PAUSE).or(playerMode.isEqualTo(PlayerMode.STOP)).not());

        stopPlayCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            closePlayer();
                        }
                    });

        clearTrackCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            clearTrack();
                        }
                    });

        logFilePlayerScope
            .selectedLogNameProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    logFileName.setValue(fileUtils.extractFilename(newValue));
                });

        applicationContext.currentMissionProperty().addListener((observable, oldValue, newValue) -> breakPlay());

        logFilePlayerScope
            .stateProperty()
            .addListener((observable, oldValue, newValue) -> onLogViewModeChange(newValue));

        isConnectedToLog.bind(logFilePlayerScope.stateProperty().isEqualTo(LogFilePlayerScope.State.PLAYER_VIEW));
    }

    private void initValues() {
        playerMode.setValue(PlayerMode.STOP);
        simulationSpeed.setValue(DEFAULT_SPEED);

        keyframe.setValue(0);
        totalFrames.setValue(0);
        currentTime.setValue(0);
        totalTime.setValue(0);
        estimatedSimulationSpeed.setValue(0f);
    }

    private void onLogViewModeChange(LogFilePlayerScope.State newState) {
        if (newState == LogFilePlayerScope.State.PLAYER_VIEW) {
            initLogPlayer();
        }
    }

    private void initLogPlayer() {
        Mission mission = logFilePlayerScope.getMission();

        airplane = mission.getLegacyPlane();
        String logFileNameStr = logFilePlayerScope.getSelectedLogName();

        logReader = mavinciObjectFactory.createLogReader(airplane, new File(logFileNameStr));
        Ensure.notNull(logReader, "logReader");
        logReader.dispatchEventsInUIthread = false;
        airplane.addListener(this);

        totalFrames.setValue(logReader.getLineCount());
    }

    private void closePlayer() {
        if (logReader == null) {
            return;
        }

        logReader.stopSimulation();
        logReader.close();
        logReader = null;

        airplane.removeListener(this);
        airplane = null;

        playerMode.setValue(PlayerMode.STOP);

        logFilePlayerScope.switchToTableView();

        initValues();
    }

    private void breakPlay() {
        if (logReader != null && logReader.isReadable()) {
            dialogService.showInfoMessage(getLocalized(LANG_DIALOG_TITLE_KEY), getLocalized(LANG_DIALOG_INTERRUPT_KEY));
            closePlayer();
        }
    }

    private void exploreLogFile() {
        String logName = logFilePlayerScope.getSelectedLogName();
        try {
            Path parentFolder = new File(logName).toPath().getParent();
            if (parentFolder != null) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(parentFolder.toFile());
            }
        } catch (IOException e) {
            LOGGER.error("Could not create file from string " + logName);
        }
    }

    private void rewind() {
        logReader.rewindAndStartSimulation();
        playerMode.setValue(PlayerMode.REWIND);
    }

    private void play() {
        if (playerMode.getValue() == PlayerMode.STOP) {
            logReader.startSimulation(simulationSpeed.getValue());
        } else {
            logReader.resumeSimulation();
        }

        playerMode.setValue(PlayerMode.PLAY);
    }

    private void pause() {
        logReader.pauseSimulation();
        playerMode.setValue(PlayerMode.PAUSE);
    }

    private void fastForward() {
        logReader.skipToNextPhase(true);
        playerMode.setValue(PlayerMode.FAST_FORWARD);
    }

    private void setSimulationSpeed(Float speed) {
        if (speed != null && logReader != null && !speed.equals(logReader.getSimulationSpeed())) {
            logReader.setSimulationSpeed(speed);
        }
    }

    private void gotoKeyframe(int keyframeNumber) {
        if (playerMode.getValue() == PlayerMode.PAUSE) {
            // Goto a keyframe
            int fromLine = logReader.getCurrentLine();
            if (Math.abs(keyframeNumber - fromLine) <= 10) {
                int step = (keyframeNumber - fromLine) > 0 ? 1 : -1;
                for (int line = fromLine; line != keyframeNumber + step; line += step) {
                    logReader.jumpToLine(line);
                }
            } else {
                logReader.jumpToLine(keyframeNumber);
            }
        }
    }

    private void clearTrack() {
        // TODO IMPLEMENT ME
    }

    @Override
    public void err_backendConnectionLost(ConnectionLostReasons reason) {
        if (reason == ConnectionLostReasons.LOGFILE_AT_END) {
            rewind();
            keyframe.setValue(0);
            pause();
        }

        connectionStateChange(null);
    }

    @Override
    public void connectionStateChange(AirplaneConnectorState newState) {
        // Probably should skip this event because this is only the place where log reader is creating
    }

    @Override
    public void recv_flightPhase(Integer fp) {
        // For now we don't need to recognize flightPhase. Just skip it.
    }

    @Override
    public void elapsedSimTime(double secs, double secsTotal) {
        Dispatcher.postToUI(
            () -> {
                if (logReader != null) {
                    if (!logReader.isPaused()) {
                        estimatedSimulationSpeed.setValue(
                            (float)CMathHelper.round(logReader.getSimSpeedRealEstim(), 1));
                        keyframe.setValue(logReader.getCurrentLine());
                        totalFrames.setValue(logReader.getLineCount());

                        currentTime.setValue((int)Math.round(secs));
                        totalTime.setValue((int)Math.round(secsTotal));
                    }
                }
            });
    }

    @Override
    public void replayStopped(boolean stopped) {
        PlayerMode newMode = stopped ? PlayerMode.STOP : PlayerMode.REWIND;
        playerMode.setValue(newMode);
    }

    @Override
    public void replayPaused(boolean paused) {
        PlayerMode newMode = paused ? PlayerMode.PAUSE : PlayerMode.PLAY;
        playerMode.setValue(newMode);
    }

    @Override
    public void replaySkipPhase(boolean isSkipping) {
        // Do not process
    }

    @Override
    public void replayFinished() {
        // Do not process
    }

    @Override
    public void recv_simulationSpeed(Float speed) {
        if (speed != null && !speed.equals(simulationSpeed.getValue())) {
            Dispatcher.postToUI(() -> simulationSpeed.setValue(speed));
        }
    }

    @Override
    public void recv_simulationSettings(SimulationSettings settings) {
        // Do not process
    }

    private String getLocalized(String key) {
        return languageHelper.getString(key);
    }

    public Command getRewindPlayCommand() {
        return rewindPlayCommand;
    }

    public Command getPlayCommand() {
        return playCommand;
    }

    public Command getPauseCommand() {
        return pauseCommand;
    }

    public Command getFastForwardCommand() {
        return fastForwardCommand;
    }

    public Command getStopPlayCommand() {
        return stopPlayCommand;
    }

    public Command getFileNameClickCommand() {
        return fileNameClickCommand;
    }

    public Command getClearTrackCommand() {
        return clearTrackCommand;
    }

    public float getSimulationSpeed() {
        return simulationSpeed.get();
    }

    public ObjectProperty<Float> simulationSpeedProperty() {
        return simulationSpeed;
    }

    public Float getEstimatedSimulationSpeed() {
        return estimatedSimulationSpeed.get();
    }

    public ObjectProperty<Float> estimatedSimulationSpeedProperty() {
        return estimatedSimulationSpeed;
    }

    public String getLogFileName() {
        return logFileName.get();
    }

    public StringProperty logFileNameProperty() {
        return logFileName;
    }

    public Integer getTotalTime() {
        return totalTime.get();
    }

    public ObjectProperty<Integer> totalTimeProperty() {
        return totalTime;
    }

    public Integer getCurrentTime() {
        return currentTime.get();
    }

    public ObjectProperty<Integer> currentTimeProperty() {
        return currentTime;
    }

    public int getKeyframe() {
        return keyframe.get();
    }

    public ObjectProperty<Integer> keyframeProperty() {
        return keyframe;
    }

    public int getTotalFrames() {
        return totalFrames.get();
    }

    public ObjectProperty<Integer> totalFramesProperty() {
        return totalFrames;
    }

    public ConnectionPage getConnectedPage() {
        return connectedPageProperty().get();
    }

    public ObjectProperty<ConnectionPage> connectedPageProperty() {
        return uavConnectionScope.connectedPageProperty();
    }

    public ConnectionState getConnectionState() {
        return uavConnectionScope.connectionStateProperty().get();
    }

    public ObjectProperty<ConnectionState> connectionStateProperty() {
        return uavConnectionScope.connectionStateProperty();
    }

    public BooleanBinding canEditKeyFrame() {
        return playerMode.isEqualTo(PlayerMode.PAUSE).or(playerMode.isEqualTo(PlayerMode.STOP));
    }

    public boolean getIsConnectedToLog() {
        return isConnectedToLog.get();
    }

    public BooleanProperty isConnectedToLogProperty() {
        return isConnectedToLog;
    }
}
