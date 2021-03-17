/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.scope.LogFilePlayerScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import javafx.beans.property.ReadOnlyObjectProperty;

/** @author Vladimir Iordanov */
@ScopeProvider(scopes = {LogFilePlayerScope.class})
public class LogFileViewModel extends ViewModelBase {

    @InjectScope
    private LogFilePlayerScope logFilePlayerScope;

    public ReadOnlyObjectProperty<LogFilePlayerScope.State> stateProperty() {
        return logFilePlayerScope.stateProperty();
    }

}
