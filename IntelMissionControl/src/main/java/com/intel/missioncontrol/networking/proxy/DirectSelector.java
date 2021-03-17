/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking.proxy;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

class DirectSelector extends ProxySelector {

    private final List<Proxy> list = Lists.newArrayList(Proxy.NO_PROXY);

    @Override
    public List<Proxy> select(URI uri) {
        return list;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {}

}
