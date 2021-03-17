/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.InternalStationType;
import com.intel.missioncontrol.ui.navbar.connection.RtkType;
import com.intel.missioncontrol.ui.navbar.connection.scope.RtkConnectionScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import java.util.EnumSet;

/** @author Vladimir Iordanov */
public class InternalBaseStationViewModel extends ViewModelBase {

    @InjectScope
    private MainScope mainScope;

    @InjectScope
    private RtkConnectionScope rtkConnectionScope;

    private final IApplicationContext applicationContext;

    private final Property<InternalStationType> selectedView = new SimpleObjectProperty<>();
    private final ListProperty<InternalStationType> items =
        new SimpleListProperty<>(FXCollections.observableArrayList(EnumSet.allOf(InternalStationType.class)));

    @Inject
    public InternalBaseStationViewModel(IApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        selectedView.setValue(InternalStationType.getDefault());
        rtkConnectionScope.currentInternalStationStatusProperty().bind(selectedView);
        rtkConnectionScope.registerConnectionCommand(RtkType.INTERNAL_BASE_STATION, getConnectionCommand());
        rtkConnectionScope.registerDisconnectionCommand(RtkType.INTERNAL_BASE_STATION, getDisconnectionCommand());
    }

    private Command getDisconnectionCommand() {
        return new DelegateCommand(
            () ->
                new Action() {
                    @Override
                    protected void action() throws Exception {
                        connectedStateProperty().setValue(ConnectionState.NOT_CONNECTED);
                    }
                },
            isConnectedBinding());
    }

    private Command getConnectionCommand() {
        return new DelegateCommand(
            () ->
                new Action() {
                    @Override
                    protected void action() throws Exception {
                        if (applicationContext.getCurrentMission() != null) {
                            // Save settings before connect
                            //rtkConnectionScope.saveSettings(applicationContext.getCurrentMission());
                        }

                        connectedStateProperty().setValue(ConnectionState.CONNECTED);
                    }
                },
            isConnectedBinding().not());
    }

    public InternalStationType getSelectedView() {
        return selectedView.getValue();
    }

    public Property<InternalStationType> selectedViewProperty() {
        return selectedView;
    }

    public ObservableList<InternalStationType> getItems() {
        return items.get();
    }

    public ListProperty<InternalStationType> itemsProperty() {
        return items;
    }

    public BooleanBinding isConnectedBinding() {
        return rtkConnectionScope.isConnectedBinding();
    }

    public Property<ConnectionState> connectedStateProperty() {
        return rtkConnectionScope.connectedStateProperty();
    }
}
