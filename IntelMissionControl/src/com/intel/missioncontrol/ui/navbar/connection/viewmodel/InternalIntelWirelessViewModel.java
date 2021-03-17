/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.scope.RtkConnectionScope;
import de.saxsys.mvvmfx.InjectScope;
import javafx.fxml.FXML;

/** @author Vladimir Iordanov */
public class InternalIntelWirelessViewModel extends ViewModelBase {
    @InjectScope
    private RtkConnectionScope RtkConnectionScope;

}
