/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking.proxy;

import com.github.markusbernhardt.proxy.selector.whitelist.ProxyBypassListSelector;
import com.github.markusbernhardt.proxy.util.UriFilter;
import com.intel.missioncontrol.beans.property.AsyncIntegerProperty;
import com.intel.missioncontrol.beans.property.AsyncStringProperty;
import com.intel.missioncontrol.settings.InternetConnectivitySettings;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO: this class could use some work, to handle edge cases, failover, no proxy, etc
 * see {@link sun.net.spi.DefaultProxySelector} for implementation that  is probably closer to what we want.
  */
class ManualProxySelector extends ProxySelector {

    private final List<Proxy> httpProxy = new ArrayList<>();
    private final List<Proxy> httpsProxy = new ArrayList<>();
    private final List<Proxy> ftpProxy = new ArrayList<>();
    private final List<Proxy> socksProxy = new ArrayList<>();
    private final List<Proxy> noProxy = new ArrayList<>();

    /**
     * create
     *
     * @param settings
     * @param filters filters, e.g. created with DefaultWhiteListParser().parseWhiteList(whiteList) or null
     * @return
     */
    static ProxySelector createManualProxySelector(InternetConnectivitySettings settings, List<UriFilter> filters) {
        ManualProxySelector ps = new ManualProxySelector(settings);

        if (filters == null) {
            return ps;
        } else {
            return new ProxyBypassListSelector(filters, ps);
        }

    }

    static ProxySelector createManualProxySelector(InternetConnectivitySettings settings) {
        return createManualProxySelector(settings, Collections.emptyList());
    }

    private ManualProxySelector(InternetConnectivitySettings settings) {
        InetSocketAddress httpAddr = getSocketAddr(settings.httpHostProperty(), settings.httpPortProperty());
        InetSocketAddress httpsAddr = getSocketAddr(settings.httpsHostProperty(), settings.httpsPortProperty());
        InetSocketAddress ftpAddr = getSocketAddr(settings.ftpHostProperty(), settings.ftpPortProperty());
        InetSocketAddress socksAddr = getSocketAddr(settings.socksHostProperty(), settings.socksPortProperty());

        if (httpAddr != null) {
            httpProxy.add(new Proxy(Proxy.Type.HTTP, httpAddr));
        }

        if (httpsAddr != null) {
            httpsProxy.add(new Proxy(Proxy.Type.HTTP, httpsAddr));
        }

        if (ftpAddr != null) {
            ftpProxy.add(new Proxy(Proxy.Type.HTTP, ftpAddr));
        }

        if (socksAddr != null) {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksAddr);
            httpProxy.add(proxy);
            httpsProxy.add(proxy);
            ftpProxy.add(proxy);
            socksProxy.add(proxy);
        }

        httpProxy.add(Proxy.NO_PROXY);
        httpsProxy.add(Proxy.NO_PROXY);
        ftpProxy.add(Proxy.NO_PROXY);
        socksProxy.add(Proxy.NO_PROXY);
        noProxy.add(Proxy.NO_PROXY);


    }

    /**
     * TODO: handle stuff that should be handled, e.g. {@link sun.net.spi.DefaultProxySelector#select(java.net.URI)}
     */
    @Override
    public List<Proxy> select(URI uri) {
        String scheme = uri.getScheme().toLowerCase();
        if ("http".equals(scheme)) {
            return httpProxy;
        } else if ("https".equals(scheme)) {
            return httpsProxy;
        } else if ("ftp".equals(scheme)) {
            return ftpProxy;
        } else if ("socket".equals(scheme)) {
            ProxySelector.getDefault();
        }

        return socksProxy;
    }

    // TODO: stuff!
    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {}

    private static InetSocketAddress getSocketAddr(AsyncStringProperty host, AsyncIntegerProperty port) {
        String h = host.get() == null ? "" : host.get().trim();
        if (h.isEmpty()) {
            return null;
        }

        try {
            return InetSocketAddress.createUnresolved(h, port.get());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
