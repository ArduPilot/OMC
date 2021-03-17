/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.search.browser.ie.IEProxyConfig;
import com.github.markusbernhardt.proxy.search.browser.ie.IEProxySearchStrategy;
import com.github.markusbernhardt.proxy.selector.misc.BufferedProxySelector;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.google.common.collect.Lists;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncSetProperty;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.settings.InternetConnectivitySettings;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ProxyManagerTest {

    private INetworkInformation networkInformation =
        new INetworkInformation() {
            @Override
            public void invalidate() {}

            @Override
            public ReadOnlyAsyncBooleanProperty networkAvailableProperty() {
                throw new UnsupportedOperationException();
            }

            @Override
            public ReadOnlyAsyncSetProperty<String> unreachableHostsProperty() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isHostReachable(URL url) {
                throw new UnsupportedOperationException();
            }
        };

    @BeforeClass
    public static void init() {
        ProxyManager.install();
    }

    static class TestUris {
        static final URI http = URI.create("http://www.example.com");
        static final URI httpPort = URI.create("http://www.example.com:8080");
        static final URI https = URI.create("https://www.example.com");
        static final URI ftp = URI.create("ftp://example.com");

        static final Collection<URI> all = List.of(http, httpPort, https, ftp);
    }

    @Test
    @Ignore
    public void testNoProxy() {
        InternetConnectivitySettings settings = new InternetConnectivitySettings();
        ProxyManager proxyManager = new ProxyManager(settings, networkInformation);

        settings.proxyTypeProperty().set(InternetConnectivitySettings.ProxyType.NONE);

        ProxySelector ps = proxyManager.getProxySelectorForSettings();
        assertNotNull(ps);

        for (URI uri : TestUris.all) {
            List<Proxy> select = ps.select(uri);
            assertEquals(Proxy.Type.DIRECT, select.get(0).type());
        }
    }

    // obviously requires proxy
    @Test
    @Ignore
    public void testAutoProxy() {
        InternetConnectivitySettings settings = new InternetConnectivitySettings();
        settings.proxyTypeProperty().set(InternetConnectivitySettings.ProxyType.AUTO);
        ProxyManager proxyManager = new ProxyManager(settings, networkInformation);

        ProxySelector ps = proxyManager.getProxySelectorForSettings();
        assertNotNull(ps);

        for (URI uri : TestUris.all) {
            List<Proxy> select = ps.select(uri);
            assertEquals(Proxy.Type.HTTP, select.get(0).type());
        }
    }

    @Test
    @Ignore
    public void testManualProxyNoHttps() {
        doTestManualProxy(false);
    }

    @Test
    @Ignore
    public void testManualProxy() {
        doTestManualProxy(true);
    }

    public void doTestManualProxy(boolean setHttps) {
        InternetConnectivitySettings settings = new InternetConnectivitySettings();
        settings.proxyTypeProperty().set(InternetConnectivitySettings.ProxyType.MANUAL);

        final String proxyHost = "proxy-chain.intel.com";
        settings.httpHostProperty().set(proxyHost);
        settings.httpPortProperty().setValue(911);
        if (setHttps) {
            settings.httpsHostProperty().set(proxyHost);
            settings.httpsPortProperty().setValue(912);
        }

        ProxyManager proxyManager = new ProxyManager(settings, networkInformation);

        ProxySelector ps = proxyManager.getProxySelectorForSettings();
        assertNotNull(ps);

        for (URI uri : TestUris.all) {
            List<Proxy> select = ps.select(uri);
            Proxy proxy = select.get(0);
            assertEquals("uri failed " + uri, Proxy.Type.HTTP, proxy.type());
        }
    }

    @Test
    @Ignore
    public void testManualProxySocks() {
        InternetConnectivitySettings settings = new InternetConnectivitySettings();
        settings.proxyTypeProperty().set(InternetConnectivitySettings.ProxyType.MANUAL);

        // only socks set
        final String proxyHost = "proxy-us.intel.com";
        settings.socksHostProperty().set(proxyHost);
        settings.socksPortProperty().set(1080);

        ProxyManager proxyManager = new ProxyManager(settings, networkInformation);

        ProxySelector ps = proxyManager.getProxySelectorForSettings();
        assertNotNull(ps);

        for (URI uri : TestUris.all) {
            List<Proxy> select = ps.select(uri);
            Proxy proxy = select.get(0);
            assertEquals("uri failed " + uri, Proxy.Type.SOCKS, proxy.type());
        }

        // proxy for http and use socks for everything else
        settings.httpHostProperty().set(proxyHost);
        settings.httpPortProperty().setValue(911);

        ps = proxyManager.getProxySelectorForSettings();
        assertNotNull(ps);

        for (URI uri : TestUris.all) {
            List<Proxy> select = ps.select(uri);
            Proxy proxy = select.get(0);
            if (uri.getScheme().equals("http")) {
                assertEquals("uri failed " + uri, Proxy.Type.HTTP, proxy.type());
            } else {
                assertEquals("uri failed " + uri, Proxy.Type.SOCKS, proxy.type());
            }
        }
    }

    public static int testSpeed(ProxySelector proxySelector, List<URI> uris, int iterations) {
        long t0 = System.nanoTime();

        int hits = 0;
        ArrayList<URI> testSet = new ArrayList<>(uris);
        int size = testSet.size();
        Random random = new Random();

        for (int i = 0; i < iterations; i++) {
            URI uri = testSet.get(random.nextInt(size));
            List<Proxy> select = proxySelector.select(uri);
            if (!select.isEmpty()) hits++;
        }

        long elapsed = System.nanoTime() - t0;
        System.out.printf(
            "Proxy %s uris=%d iter=%d:\n time: % 3.1fms,\t time/iter: %3.3fms\n",
            proxySelector, uris.size(), iterations, elapsed / 1e6, (elapsed / 1e6) / iterations);

        return hits;
    }

    public static List<URI> generateTestUris(int domains, int numberUris) throws URISyntaxException {
        Random random = new Random();
        List<String> hosts = new ArrayList<>();
        List<URI> uris = new ArrayList<>(numberUris);
        for (int i = 0; i < numberUris; i++) {
            String host;
            if (hosts.size() < domains) {
                host = "host" + Long.toString(random.nextLong(), 36) + ".com";
                hosts.add(host);
            } else {
                host = hosts.get(random.nextInt(hosts.size()));
            }

            URI uri = new URI("http", host, "/path/" + Long.toString(Math.abs(random.nextLong()), 36), "");
            uris.add(uri);
        }

        return uris;
    }

    public static void main(String[] args) {
        //        try {
        //            testProxySpeed2();
        //        } catch (ProxyException e) {
        //            e.printStackTrace();
        //        }
    }

    @Test
    @Ignore
    public void testProxySpeed2() throws ProxyException {
        IEProxySearchStrategy serach = new IEProxySearchStrategy();
        IEProxyConfig ieProxyConfig = serach.readIEProxyConfig();

        ProxySelector proxySelector = serach.getProxySelector();

        List<URI> uris = List.of(URI.create("http://google.com"), URI.create("http://circuit.intel.com"));

        System.out.println("proxySelector" + proxySelector);
        // this is really slow
        testSpeed(proxySelector, uris, 5);
    }

    @Test
    @Ignore
    public void testAutoProxySpeed() throws URISyntaxException, InterruptedException {
        ProxySearch proxySearch = new ProxySearch();
        proxySearch.addStrategy(ProxySearch.Strategy.OS_DEFAULT);
        proxySearch.addStrategy(ProxySearch.Strategy.BROWSER);

        ProxySelector selector = proxySearch.getProxySelector();

        assertNotNull(selector);

        ProxySelector bufferedProxySelector =
            new BufferedProxySelector(
                1000, 6000 * 1000L, (ProxySelector)selector, BufferedProxySelector.CacheScope.CACHE_SCOPE_HOST);

        ProxySelector otherSelector =
            new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    return Lists.newArrayList(Proxy.NO_PROXY);
                }

                @Override
                public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {}

            };

        List<URI> uris = generateTestUris(400, 50);

        int iters = 100;
        testSpeed(otherSelector, uris, iters);
        testSpeed(selector, uris, iters);
        testSpeed(bufferedProxySelector, uris, iters);

        testSpeed(otherSelector, uris, 10000);
        testSpeed(bufferedProxySelector, uris, 10000);
    }

    @Test
    @Ignore
    public void none() {
        assertNotNull(new Double(3));
    }
}
