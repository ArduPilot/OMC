/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking;

import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncSetProperty;
import java.net.URL;

public interface INetworkInformation {

    void invalidate();

    ReadOnlyAsyncBooleanProperty networkAvailableProperty();

    ReadOnlyAsyncBooleanProperty internetAvailableProperty();

    ReadOnlyAsyncSetProperty<String> unreachableHostsProperty();

    boolean isHostReachable(URL url);

    default boolean isNetworkAvailable() {
        return networkAvailableProperty().get();
    }

}
