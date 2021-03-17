/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking;

import static org.junit.Assert.*;

import com.github.markusbernhardt.proxy.selector.fixed.FixedProxySelector;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;
import java.net.URLConnection;
import org.junit.Ignore;
import org.junit.Test;

public class ProxyManagerTest {

    public void testUrl(String address) throws IOException {
        URL url = new URL(address);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(2000);
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());
        int contentLength = urlConnection.getContentLength();
        System.out.println("got "+contentLength+" bytes, " + urlConnection);
        reader.close();
    }

    @Test
    @Ignore
    public void testManualProxy() throws IOException {
        ProxySelector.setDefault(new FixedProxySelector(Proxy.NO_PROXY));
        try {
            testUrl("http://example.com");
        } catch (IOException e) {
            e.printStackTrace();
        }

        ProxySelector.setDefault(new FixedProxySelector("proxy-us.intel.com", 911));
        testUrl("http://example.com");

    }

    @Test
    @Ignore
    public void test() throws IOException {

        testUrl("http://example.com");
        System.clearProperty("http.proxyHost");
        System.clearProperty("https.proxyHost");
        testUrl("http://example.com");

    }
}