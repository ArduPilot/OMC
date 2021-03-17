/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking;

import com.intel.missioncontrol.beans.property.ReadOnlyAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncSetProperty;
import java.net.URL;

public interface INetworkInformation {

    void invalidate();

    ReadOnlyAsyncBooleanProperty networkAvailableProperty();

    ReadOnlyAsyncSetProperty<String> unreachableHostsProperty();

    boolean isHostReachable(URL url);

    default boolean isNetworkAvailable() {
        return networkAvailableProperty().get();
    }

}
