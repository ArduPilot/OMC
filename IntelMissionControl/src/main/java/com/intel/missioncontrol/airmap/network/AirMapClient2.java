/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.network;

import com.intel.missioncontrol.airmap.TextUtils;
import com.intel.missioncontrol.networking.OkHttpUtil;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.Map;
import java.util.function.Function;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirMapClient2 {
    private static final Logger LOG = LoggerFactory.getLogger(AirMapClient2.class);

    private OkHttpClient client;
    private AirMapConfig2 config;
    private Function<Request.Builder, Request.Builder> interceptor;

    public AirMapClient2(AirMapConfig2 config) {
        this.config = config;
        resetClient();
    }

    public void setConfig(AirMapConfig2 config) {
        this.config = config;
        resetClient();
    }

    /** only use this if you know what you're doing */
    public OkHttpClient getOkHttpClient() {
        return client;
    }

    public void resetClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (ProxySelector.getDefault().equals(ProxySelector.of(null)) || config.getProxySelector() == null) {
            builder.proxySelector(ProxySelector.getDefault());
        } else {
            builder.proxySelector(config.getProxySelector());
        }

        if (config.getHttpRequestInterceptor() != null) {
            interceptor = config.getHttpRequestInterceptor();
        } else {
            interceptor = (f) -> f;
        }

        if (config.networkListener != null) {
            builder.addNetworkInterceptor(new OkHttpUtil.NetworkActivityMonitor(config.networkListener));
        }

        boolean forceRewriteCacheControl = true;

        if (forceRewriteCacheControl) {
            builder.addNetworkInterceptor(
                new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        // TODO: need to re-validate if online
                        Response originalResponse = chain.proceed(chain.request());
                        // rewrite response headers
                        return originalResponse
                            .newBuilder()
                            .removeHeader("Pragma") // allow us to use without revalidating if offline
                            .removeHeader("Expires")
                            .header("Cache-Control", "max-age=36000")
                            .build();
                    }
                });
        }

        if (config.cacheDir != null) {
            LOG.info("Cache configuration: dir=" + config.cacheDir + " size=" + config.cacheSize);
            builder.cache(new Cache(config.cacheDir, config.cacheSize));
        }

        // This interceptor adds the api and auth headers
        builder.addInterceptor(
            new Interceptor() {

                final String xApiKey = config.getApiKey();

                @Override
                public Response intercept(Chain chain) throws IOException {
                    // Only attach our API Key and Auth token if we're going to AirMap
                    if (chain.request().url().host().equals("api.airmap.com")) {
                        Request.Builder newRequest = chain.request().newBuilder();
                        if (!TextUtils.isEmpty(xApiKey)) {
                            newRequest.header("x-Api-Key", xApiKey);
                        }

                        return chain.proceed(newRequest.build());
                    }

                    return chain.proceed(chain.request());
                }
            });

        client = builder.build();
    }

    /**
     * Make a GET call with params
     *
     * @param url The full url to GET
     * @param params The params to add to the request
     * @param callback An OkHttp Callback
     */
    public Call get(String url, Map<String, String> params, Callback callback) {
        Request.Builder requestBuilder = new Request.Builder().url(urlBodyFromMap(url, params)).get().tag(url);
        Request request = interceptor.apply(requestBuilder).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * Make a GET call
     *
     * @param url The full url to GET
     * @param callback An OkHttp Callback
     */
    private Call get(String url, Callback callback) {
        Request request = new Request.Builder().url(url).get().tag(url).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * Make a blocking GET call
     *
     * @param url The full url to GET
     * @return the string contents of the response body
     */
    private String get(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().tag(url).build();
        Call call = client.newCall(request);
        Response response = call.execute();
        String responseString = response.body().string();
        response.body().close();
        return responseString;
    }

    /**
     * Creates url based on map of params
     *
     * @param base The base url
     * @param map The parameters to add to the url
     * @return The url with parameters embedded
     */
    private HttpUrl urlBodyFromMap(String base, Map<String, String> map) {
        HttpUrl.Builder builder = HttpUrl.parse(base).newBuilder(base);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                builder.addEncodedQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    /**
     * Creates a Request Body from a map of params
     *
     * @param map The parameters to add to the body
     * @return The request body
     */
    private FormBody bodyFromMap(Map<String, String> map) {
        FormBody.Builder formBody = new FormBody.Builder();
        if (map != null) {
            for (final Map.Entry<String, String> entrySet : map.entrySet()) {
                if (entrySet.getValue() != null) {
                    formBody.add(entrySet.getKey(), entrySet.getValue());
                }
            }
        }

        return formBody.build();
    }
}
