/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.scope;

import com.google.inject.Inject;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.rtk.RtkSettings;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.InternalStationType;
import com.intel.missioncontrol.ui.navbar.connection.RtkConnectionSetupState;
import com.intel.missioncontrol.ui.navbar.connection.RtkStatistic;
import com.intel.missioncontrol.ui.navbar.connection.RtkType;
import com.intel.missioncontrol.ui.navbar.connection.model.RtkStatisticsData;
import com.intel.missioncontrol.ui.navbar.connection.view.StatisticData;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.RtkStatisticData;
import de.saxsys.mvvmfx.Scope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkStatisticListener;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionSettings;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtkClient;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

public class RtkConnectionScope implements Scope, RtkStatisticsData {

    public static final String RTK_SETTINGS_KEY = RtkConnectionScope.class.getName() + ".rtkSettings";
    private static final DelegateCommand NO_OP_COMMAND =
        new DelegateCommand(
            () ->
                new Action() {
                    @Override
                    protected void action() throws Exception {}
                });

    private final Property<InternalStationType> currentInternalStationStatus = new SimpleObjectProperty<>();
    private final ISettingsManager settingsManager;
    private final AsyncObjectProperty<RtkSettings> currentSettings = new SimpleAsyncObjectProperty<>(this);

    private final ObjectProperty<ConnectionState> connectedState =
        new SimpleObjectProperty<>(ConnectionState.NOT_CONNECTED);
    private final ObjectProperty<RtkType> rtkSource = new SimpleObjectProperty<>(RtkType.EXTERNAL_BASE_STATION);
    private final ObjectProperty<NtripConnectionSettings> selectedNtripConnection = new SimpleObjectProperty<>();
    private final ObjectProperty<RtkConnectionSetupState> rtkNtripConnectionSetupState = new SimpleObjectProperty<>();
    private final Map<RtkType, Command> connectionCommands = new EnumMap<>(RtkType.class);
    private final Map<RtkType, Command> disconnectionCommands = new EnumMap<>(RtkType.class);
    private final ObjectProperty<RtkClient> currentClient = new SimpleObjectProperty<>();

    public ObjectProperty<RtkStatisticData> dataProperty() {
        return data;
    }

    private final ObjectProperty<RtkStatisticData> data = new SimpleObjectProperty<>();

    @Inject
    public RtkConnectionScope(ISettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        currentSettings.set(settingsManager.getSection(RtkSettings.class));
    }

    @Override
    public void setData(RtkStatisticData data) {
        this.data.set(data);
    }

    @Override
    public RtkStatisticData getData() {
        return data.get();
    }

    private final BooleanBinding isConnected =
        connectedState
            .isEqualTo(ConnectionState.CONNECTED)
            .or(connectedState.isEqualTo(ConnectionState.CONNECTED_WARNING));

    private final ListProperty<RtkStatistic> detailedStatisticsItems =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<StatisticData> statisticData =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ObjectProperty<IRtkStatisticListener> statisticsListener = new SimpleObjectProperty<>();

    public void registerConnectionCommand(RtkType rtkType, Command connectionCommand) {
        connectionCommands.put(rtkType, connectionCommand);
    }

    public Command getCurrentConnectionCommand() {
        return connectionCommands.getOrDefault(rtkSource.get(), NO_OP_COMMAND);
    }

    public void registerDisconnectionCommand(RtkType rtkType, Command disconnectionCommand) {
        disconnectionCommands.put(rtkType, disconnectionCommand);
    }

    public Command getCurrentDisconnectionCommand() {
        return disconnectionCommands.getOrDefault(rtkSource.get(), NO_OP_COMMAND);
    }

    public InternalStationType getCurrentInternalStationStatus() {
        return currentInternalStationStatus.getValue();
    }

    public Property<InternalStationType> currentInternalStationStatusProperty() {
        return currentInternalStationStatus;
    }

    public boolean getIsConnected() {
        return isConnected.get();
    }

    public BooleanBinding isConnectedBinding() {
        return isConnected;
    }

    public AsyncObjectProperty<RtkSettings> currentSettingsProperty() {
        return currentSettings;
    }

    public ConnectionState getConnectedState() {
        return connectedState.get();
    }

    public ObjectProperty<ConnectionState> connectedStateProperty() {
        return connectedState;
    }

    public RtkType getRtkSource() {
        return rtkSource.get();
    }

    public ObjectProperty<RtkType> rtkSourceProperty() {
        return rtkSource;
    }

    public ObjectProperty<NtripConnectionSettings> selectedNtripConnectionProperty() {
        return selectedNtripConnection;
    }

    public ObjectProperty<RtkConnectionSetupState> rtkNtripConnectionSetupStateProperty() {
        return rtkNtripConnectionSetupState;
    }

    public RtkClient getCurrentClient() {
        return currentClient.get();
    }

    public ObjectProperty<RtkClient> currentClientProperty() {
        return currentClient;
    }

    public ObjectProperty<IRtkStatisticListener> getStatisticsListenerProperty() {
        return statisticsListener;
    }

    public IRtkStatisticListener getStatisticsListener() {
        return statisticsListener.get();
    }

    @Override
    public void updateConnectionState(ConnectionState connectionState) {
        connectedStateProperty().set(connectionState);
    }

    public ListProperty<StatisticData> statisticDataProperty() {
        return statisticData;
    }

    @Override
    public List<StatisticData> getStatisticsDataItems() {
        return statisticDataProperty();
    }

    public ListProperty<RtkStatistic> detailedStatisticsItemsProperty() {
        return detailedStatisticsItems;
    }

    @Override
    public List<RtkStatistic> getDetailedStatisticsItems() {
        return detailedStatisticsItemsProperty();
    }
}
