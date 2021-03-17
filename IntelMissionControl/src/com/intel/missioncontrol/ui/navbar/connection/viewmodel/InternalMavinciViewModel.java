/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.InternalStationType;
import com.intel.missioncontrol.ui.navbar.connection.scope.RtkConnectionScope;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;

/** @author Vladimir Iordanov */
public class InternalMavinciViewModel extends ViewModelBase {
    public static final String URL = "http://192.168.5.21:7000";

    private StringProperty currentUrl = new SimpleStringProperty();
    private BooleanProperty isFailedConnection = new SimpleBooleanProperty();

    @InjectScope
    private RtkConnectionScope rtkConnectionScope;

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        rtkConnectionScope
            .isConnectedBinding()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue
                            && rtkConnectionScope.getCurrentInternalStationStatus() == InternalStationType.MAVINCI) {
                        currentUrl.setValue(URL);
                    } else {
                        currentUrl.setValue(null);
                    }
                });
        isFailedConnection.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue) {
                    rtkConnectionScope.connectedStateProperty().setValue(ConnectionState.CONNECTED_WARNING);
                }
            });
    }

    public String getCurrentUrl() {
        return currentUrl.get();
    }

    public StringProperty currentUrlProperty() {
        return currentUrl;
    }

    public BooleanBinding isConnectedBinding() {
        return rtkConnectionScope.isConnectedBinding();
    }

    public boolean getIsFailedConnection() {
        return isFailedConnection.get();
    }

    public BooleanProperty isFailedConnectionProperty() {
        return isFailedConnection;
    }
}
