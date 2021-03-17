/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Deprecated(forRemoval = true)
public final class OkHttpUtil {
    private OkHttpUtil() {}

    public interface NetworkActivityListener {
        void onNetworkActivityStarted(Object tag);
        void onNetworkActivityStopped(Object tag);
    }

    /**
     * Keeps track of network activity
     * <p>
     * To use, add as a Network Interceptor via
     * {@link okhttp3.OkHttpClient.Builder#addNetworkInterceptor(Interceptor)}
     */
    public static class NetworkActivityMonitor implements Interceptor {
        final NetworkActivityListener listener;
        final AtomicInteger activeRequestCount = new AtomicInteger(0);

        public NetworkActivityMonitor(NetworkActivityListener listener) {
            this.listener = listener;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            Object tag = request.tag();
            if (activeRequestCount.getAndIncrement() == 0) {
                listener.onNetworkActivityStarted(tag);
            }

            Response response = null;
            try {
                response = chain.proceed(request);
            } finally {
                if (activeRequestCount.decrementAndGet() == 0) {
                    listener.onNetworkActivityStopped(tag);
                }
            }

            return response;
        }
    }


}
