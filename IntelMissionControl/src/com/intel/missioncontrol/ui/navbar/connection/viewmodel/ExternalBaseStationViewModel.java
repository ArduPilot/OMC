/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.RtkType;
import com.intel.missioncontrol.ui.navbar.connection.scope.ExternalConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.scope.RtkConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.view.ExternalBaseStationView;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.ObjectProperty;

/** @author Vladimir Iordanov */
@ScopeProvider(scopes = ExternalConnectionScope.class)
public class ExternalBaseStationViewModel extends ViewModelBase {

    @InjectScope
    private RtkConnectionScope rtkConnectionScope;

    @InjectScope
    private ExternalConnectionScope externalConnectionScope;

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        rtkConnectionScope.registerConnectionCommand(
            RtkType.EXTERNAL_BASE_STATION,
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            Expect.notNull(externalConnectionScope, "externalConnectionScope");
                            Command connectedCommand = externalConnectionScope.getCurrentConnectCommand();
                            Expect.notNull(connectedCommand, "connectedCommand");
                            connectedCommand.execute();
                        }
                    }));
        rtkConnectionScope.registerDisconnectionCommand(
            RtkType.EXTERNAL_BASE_STATION,
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            Expect.notNull(externalConnectionScope, "externalConnectionScope");
                            Command disconnectedCommand = externalConnectionScope.getCurrentDisconnectCommand();
                            Expect.notNull(disconnectedCommand, "disconnectedCommand");
                            disconnectedCommand.execute();
                        }
                    }));
    }

    public ExternalBaseStationView.ExternalConnetionType getCurrentSection() {
        return externalConnectionScope.getCurrentSection();
    }

    public ObjectProperty<ExternalBaseStationView.ExternalConnetionType> currentSectionProperty() {
        return externalConnectionScope.currentSectionProperty();
    }
}
