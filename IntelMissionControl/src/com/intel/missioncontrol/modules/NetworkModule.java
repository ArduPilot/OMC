/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.networking.INetworkInterceptor;
import com.intel.missioncontrol.networking.NetworkInformation;
import com.intel.missioncontrol.networking.NetworkInterceptor;
import com.intel.missioncontrol.networking.proxy.ProxyManager;

public class NetworkModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(INetworkInformation.class).to(NetworkInformation.class).in(Singleton.class);
        bind(INetworkInterceptor.class).to(NetworkInterceptor.class).in(Singleton.class);

        // we need to create an instance it managing the proxies in the background
        bind(ProxyManager.class).asEagerSingleton();
    }

}
