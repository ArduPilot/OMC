/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking;

import okhttp3.Interceptor;

public interface INetworkInterceptor extends Interceptor {

    boolean addListener(NetworkListener listener);

    boolean removeListener(NetworkListener listener);

}
