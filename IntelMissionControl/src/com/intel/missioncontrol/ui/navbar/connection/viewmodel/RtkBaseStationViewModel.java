/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.RtkType;
import com.intel.missioncontrol.ui.navbar.connection.model.IRtkStatisticFactory;
import com.intel.missioncontrol.ui.navbar.connection.scope.RtkConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.view.StatisticData;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkStatisticListener;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtkClient;
import eu.mavinci.plane.IAirplane;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RtkBaseStationViewModel extends ViewModelBase {
    private static final String CONNECTED_IMG = "/com/intel/missioncontrol/icons/icon_dot(fill=theme-green).svg";
    private static final String WORLD_MAP_IMG = "/com/intel/missioncontrol/icons/icon_world-map.svg";
    private static final double WORLD_MAP_ICON_SIZE = ScaleHelper.emsToPixels(2);

    private final IApplicationContext applicationContext;
    private final IRtkStatisticFactory rtkStatisticFactory;
    private final ILanguageHelper languageHelper;
    private final IDialogService dialogService;
    private final IMapView mapView;

    @InjectScope
    protected RtkConnectionScope rtkConnectionScope;

    @InjectScope
    protected MainScope mainScope;

    private DelegateCommand connectCommand;
    private DelegateCommand disconnectCommand;
    private Command crossCheckCommand;

    @Inject
    public RtkBaseStationViewModel(
            IApplicationContext applicationContext,
            IRtkStatisticFactory rtkStatisticFactory,
            ILanguageHelper languageHelper,
            IDialogService dialogService,
            IMapView mapView) {
        this.applicationContext = applicationContext;
        this.rtkStatisticFactory = rtkStatisticFactory;
        this.languageHelper = languageHelper;
        this.dialogService = dialogService;
        this.mapView = mapView;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        rtkConnectionScope.statisticDataProperty().set(FXCollections.observableList(createStatisticsContent()));
        connectedStateProperty().setValue(ConnectionState.NOT_CONNECTED);

        connectCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            connect();
                        }
                    },
                isConnectedBinding().not());

        connectCommand.setOnSucceeded(e -> connectedStateProperty().set(ConnectionState.CONNECTED));

        disconnectCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            disconnect();
                        }
                    },
                isConnectedBinding());

        disconnectCommand.setOnSucceeded(e -> connectedStateProperty().set(ConnectionState.NOT_CONNECTED));

        crossCheckCommand =
            new DelegateCommand(
                () -> {
                    return new Action() {
                        @Override
                        protected void action() throws Exception {
                            crossCheck();
                        }
                    };
                },
                isConnectedBinding());

        applicationContext
            .currentMissionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        // Disconnect the current statistic
                        RtkClient currentClient = rtkConnectionScope.currentClientProperty().get();
                        if (currentClient != null) {
                            connectedStateProperty().setValue(ConnectionState.NOT_CONNECTED);

                            IRtkStatisticListener rtkStatisticListener = rtkConnectionScope.getStatisticsListener();
                            if (rtkStatisticListener != null) {
                                currentClient.removeListener(rtkStatisticListener);
                            }
                        }

                        IAirplane airplane = newValue.uavProperty().get().getLegacyPlane();
                        RtkClient newClient = airplane.getRtkClient();
                        IRtkStatisticListener newStatisticListener =
                            rtkStatisticFactory.createStatisticListener(rtkConnectionScope, newClient);
                        newClient.addListener(newStatisticListener);
                        rtkConnectionScope.getStatisticsListenerProperty().set(newStatisticListener);
                        rtkConnectionScope.currentClientProperty().setValue(newClient);

                        // Connect the current
                        // TODO Bind setting fields to controls
                    }
                });
    }

    private void connect() {
        rtkConnectionScope.getCurrentConnectionCommand().execute();
    }

    private void disconnect() {
        RtkClient client = rtkConnectionScope.getCurrentClient();
        RtkType rtkSource = rtkConnectionScope.getRtkSource();
        if ((client != null && client.isConnected())
                || rtkSource == RtkType.NTRIP
                || rtkSource == RtkType.INTERNAL_BASE_STATION) {
            boolean result =
                dialogService.requestConfirmation(
                    languageHelper.getString(getConfirmationWindowTitle()),
                    languageHelper.getString(getConfirmationWindowBody()));

            if (!result) {
                return;
            }

            rtkConnectionScope.getCurrentDisconnectionCommand().execute();

            if ((client != null) && (rtkSource != RtkType.NTRIP && rtkSource != RtkType.INTERNAL_BASE_STATION)) {
                client.disconnect();
            }
        }
    }

    private String getConfirmationWindowTitle() {
        return rtkConnectionScope.getRtkSource() != RtkType.NTRIP
            ? "com.intel.missioncontrol.ui.connection.viewmodel.ExternalRs232SectionViewModel.disconnect.title"
            : "com.intel.missioncontrol.ui.connection.view.NtripConnectionView.disconnectTitle";
    }

    private String getConfirmationWindowBody() {
        return rtkConnectionScope.getRtkSource() != RtkType.NTRIP
            ? "com.intel.missioncontrol.ui.connection.viewmodel.ExternalRs232SectionViewModel.disconnect.text"
            : "com.intel.missioncontrol.ui.connection.view.NtripConnectionView.disconnectMessage";
    }

    private void crossCheck() {
        // TODO Cross check logic
    }

    public ObjectProperty<RtkType> rtkSourceProperty() {
        return rtkConnectionScope.rtkSourceProperty();
    }

    public BooleanBinding isConnectedBinding() {
        return rtkConnectionScope.isConnectedBinding();
    }

    public ObjectProperty<ConnectionState> connectedStateProperty() {
        return rtkConnectionScope.connectedStateProperty();
    }

    public Command getConnectCommand() {
        return connectCommand;
    }

    public Command getDisconnectCommand() {
        return disconnectCommand;
    }

    public Command getCrossCheckCommand() {
        return crossCheckCommand;
    }

    private List<StatisticData> createStatisticsContent() {
        List<StatisticData> content = new ArrayList<>();
        Label statusNode =
            new Label(languageHelper.getString("com.intel.missioncontrol.RtkStatistic.status.notConnected"));
        statusNode.setGraphic(new ImageView(new Image(CONNECTED_IMG)));
        content.add(
            new StatisticData(
                languageHelper.getString("com.intel.missioncontrol.RtkStatistic.status"),
                statusNode,
                statusNode::setText));
        Label packageStatusNode =
            new Label(languageHelper.getString("com.intel.missioncontrol.RtkStatistic.packageStatus.bad"));
        content.add(
            new StatisticData(
                languageHelper.getString("com.intel.missioncontrol.RtkStatistic.packageStatus"),
                packageStatusNode,
                packageStatusNode::setText));
        Label timeNode = new Label("0");
        content.add(
            new StatisticData(
                languageHelper.getString("com.intel.missioncontrol.RtkStatistic.time"),
                timeNode,
                value -> {
                    timeNode.setText(value);
                }));
        Label volumeInNode = new Label("0B");
        content.add(
            new StatisticData(
                languageHelper.getString("com.intel.missioncontrol.RtkStatistic.volumeIn"),
                volumeInNode,
                volumeInNode::setText));
        Label volumeOutNode = new Label("0B");
        content.add(
            new StatisticData(
                languageHelper.getString("com.intel.missioncontrol.RtkStatistic.volumeOut"),
                volumeOutNode,
                volumeOutNode::setText));

        Label skippedNode = new Label("0B");
        content.add(
            new StatisticData(
                languageHelper.getString("com.intel.missioncontrol.RtkStatistic.skipped"),
                skippedNode,
                skippedNode::setText));
        Label baseIdNode = new Label("null");
        content.add(
            new StatisticData(
                languageHelper.getString("com.intel.missioncontrol.RtkStatistic.baseId"),
                baseIdNode,
                baseIdNode::setText));
        Label baseAntennaNode = new Label("-1");
        content.add(
            new StatisticData(
                languageHelper.getString("com.intel.missioncontrol.RtkStatistic.baseAntenna"),
                baseAntennaNode,
                baseAntennaNode::setText));
        Label baselineNode = new Label("-");
        content.add(
            new StatisticData(
                languageHelper.getString("com.intel.missioncontrol.RtkStatistic.baseline"),
                baselineNode,
                baselineNode::setText));
        HBox container = new HBox();
        container.setPrefHeight(ScaleHelper.emsToPixels(4.5));
        Label basePositionNode = new Label("");
        ImageView graphic =
            new ImageView(new Image(WORLD_MAP_IMG, WORLD_MAP_ICON_SIZE, WORLD_MAP_ICON_SIZE, true, false));
        Button locateBasePosition = new Button("", graphic);
        VBox box = new VBox(locateBasePosition);
        box.setStyle("-fx-padding: 1em");

        container.getChildren().addAll(basePositionNode, box);
        StatisticData basePositionStatistic =
            new StatisticData(
                languageHelper.getString("com.intel.missioncontrol.RtkStatistic.basePosition"),
                container,
                basePositionNode::setText);
        locateBasePosition.setOnMouseClicked(
            (e) -> basePositionStatistic.getLocation().ifPresent(mapView::goToPositionAsync));
        content.add(basePositionStatistic);
        return content;
    }
}
