/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkInterceptor implements INetworkInterceptor {

    private final List<NetworkListener> listeners = new ArrayList<>();
    private final AtomicInteger activeRequestCount = new AtomicInteger(0);

    public NetworkInterceptor() {}

    public NetworkInterceptor(NetworkListener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean addListener(NetworkListener listener) {
        synchronized (listeners) {
            return listeners.add(listener);
        }
    }

    @Override
    public boolean removeListener(NetworkListener listener) {
        synchronized (listeners) {
            return listeners.remove(listener);
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Object tag = request.tag();
        if (activeRequestCount.getAndIncrement() == 0) {
            synchronized (listeners) {
                for (NetworkListener listener : listeners) {
                    listener.networkActivityStarted(tag);
                }
            }
        }

        Response response;
        try {
            response = chain.proceed(request);
        } finally {
            if (activeRequestCount.decrementAndGet() == 0) {
                synchronized (listeners) {
                    for (NetworkListener listener : listeners) {
                        listener.networkActivityStopped(tag);
                    }
                }
            }
        }

        return response;
    }

}
