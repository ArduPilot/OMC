/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.persistence.insight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intel.missioncontrol.config.CloudSync;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.asyncfx.concurrent.RunAsyncDebouncer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public final class InsightContext {

    public static final String NONE = "None";
    private final Logger LOGGER = LoggerFactory.getLogger(InsightContext.class);
    private final String DXAUTH_OAUTH2_TOKEN = "dxauth/oauth2/token";
    private final Gson gson = new GsonBuilder().create();
    private final AsyncObjectProperty<HashMap> headers = new SimpleAsyncObjectProperty<>(this);
    private final RunAsyncDebouncer loginDebouncer = new RunAsyncDebouncer(this::logInAsyncDebounced, true);

    private final AsyncStringProperty insightUsername =
        new SimpleAsyncStringProperty(this, new PropertyMetadata.Builder<String>().create());

    public InsightContext() {
        this.headers.set(prepareHeaders(NONE));
    }

    public Future<Void> logInAsync(){
        return loginDebouncer.runAsync();
    }

    private Future<Void> logInAsyncDebounced() {
        if (CloudSync.getInsightConfig().insightLoggedInProperty().get()) {
            return Futures.successful();
        }

        return Dispatcher.background().runLaterAsync(
            () -> {
                var insightSettings = CloudSync.getInsightConfig();
                if (insightSettings.insightLoggedInProperty().get()) {
                    return;
                }
                    OAuthResponse oAuthResponse = null;
                    try {
                        oAuthResponse =
                            authWithInsight(
                                insightSettings.insightUsernameProperty().get(),
                                insightSettings.insightPasswordProperty().get(),
                                insightSettings.insightHostProperty().get() + this.DXAUTH_OAUTH2_TOKEN);
                        insightUsername.set(insightSettings.insightUsernameProperty().get());
                    } catch (IllegalStateException var8) {
                        insightSettings.insightLoggedInProperty().set(false);
                        LOGGER.warn(
                            "Insight login didn't work for: " + insightSettings.insightUsernameProperty().get());
                    }

                    if (oAuthResponse != null && oAuthResponse.getAccessToken() != null) {
                        this.headers.set(prepareHeaders(oAuthResponse.getAccessToken()));

                        insightSettings.insightLoggedInProperty().set(true);
                        LOGGER.warn("Insight login worked for: " + insightSettings.insightUsernameProperty().get());
                    }

            });
    }

    @NotNull
    private final HashMap prepareHeaders(@NotNull String token) {
        HashMap headers = new HashMap();
        headers.put("Content-Type", "application/json");
        if (!token.equals("None")) {
            headers.put("Authorization", "Bearer " + token);
        }

        headers.put("Pragma", "no-cache");
        var insightSettings = CloudSync.getInsightConfig();
        headers.put("Referer", "" + insightSettings.getInsightHost() + "/login");
        headers.put("Origin", "" + insightSettings.getInsightHost());
        headers.put("host", "dev.ixstack.net");
        headers.put("Cache-Control", "no-cache");
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Expires", "Sat, 01 Jan 2000 00:00:00 GMT");
        headers.put("Accept-Language", "en-US,en;q=0.9");
        headers.put("Connection", "keep-alive");
        headers.put(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        return headers;
    }

    @Nullable
    private final HttpClientBuilder createHttpClientBuilder() {
        TrustManager[] trustAllCerts =
            new TrustManager[] {
                (TrustManager)
                    (new X509TrustManager() {
                        @NotNull
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(@NotNull X509Certificate[] certs, @NotNull String authType) {}

                        public void checkServerTrusted(@NotNull X509Certificate[] certs, @NotNull String authType) {}

                    })
            };
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try {
            sc.init((KeyManager[])null, trustAllCerts, new SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslFactory =
            new SSLConnectionSocketFactory(
                sc, new String[] {"TLSv1.2"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        clientBuilder.setSSLSocketFactory(sslFactory);
        return clientBuilder;
    }

    private final OAuthResponse authWithInsight(
            @NotNull String userName, @NotNull String password, @NotNull final String target) {
        HttpClientBuilder clientBuilder = this.createHttpClientBuilder();
        CloseableHttpClient httpClient = clientBuilder != null ? clientBuilder.build() : null;
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

        this.headers.set(prepareHeaders(NONE));
        Unirest.config().reset();
        Unirest.config().httpClient(httpClient);

        final OAuthRequest oauthRequest = new OAuthRequest();
        oauthRequest.setPassword(password);
        oauthRequest.setUsername(userName);

        HttpResponse jsonResponse =
            Unirest.post(target).headers(headers.get()).body(gson.toJson(oauthRequest)).asString();

        return gson.fromJson((String)jsonResponse.getBody(), OAuthResponse.class);
    }

    public AsyncObjectProperty<HashMap> headersProperty() {
        return headers;
    }

    public HashMap getHeaders() {
        return headers.get();
    }

    public Future<Void> logOutAsync() {
        return Dispatcher.background().runLaterAsync(
            () -> {
                this.headers.set(prepareHeaders(NONE));
                var insightSettings = CloudSync.getInsightConfig();
                insightSettings.insightUsernameProperty().set(null);
                insightSettings.insightPasswordProperty().set(null);
                insightSettings.insightLoggedInProperty().set(false);
            });
    }
}
