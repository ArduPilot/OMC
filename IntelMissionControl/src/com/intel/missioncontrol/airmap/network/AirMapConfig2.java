/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.network;

import com.intel.missioncontrol.networking.OkHttpUtil.NetworkActivityListener;

import java.io.File;
import java.net.ProxySelector;
import java.util.function.Function;
import okhttp3.Request;

public class AirMapConfig2 {
    public ProxySelector proxySelector;
    public String apiKey;

    public File cacheDir;
    /** cache size in bytes */
    public int cacheSize = 1024 * 1024 * 5;
    private Function<Request.Builder, Request.Builder> httpRequestInterceptor;
    /** Nullable */
    public NetworkActivityListener networkListener = null;

    public AirMapConfig2() {}

    public AirMapConfig2(ProxySelector proxySelector, String apiKey) {
        this.proxySelector = proxySelector;
        this.apiKey = apiKey;
    }

    public AirMapConfig2(String apiKey) {
        this.apiKey = apiKey;
    }



    public String getApiKey() {
        return apiKey;
    }

    public ProxySelector getProxySelector() {
        return proxySelector;
    }

    public Function<Request.Builder, Request.Builder> getHttpRequestInterceptor() {
        return httpRequestInterceptor;
    }

    public void setHttpRequestInterceptor(Function<Request.Builder, Request.Builder> httpRequestInterceptor) {
        this.httpRequestInterceptor = httpRequestInterceptor;
    }
}
