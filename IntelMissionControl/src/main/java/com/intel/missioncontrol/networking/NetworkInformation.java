/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncSetProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncSetWrapper;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;

public class NetworkInformation implements INetworkInformation {

    private interface Wininet extends StdCallLibrary {
        Wininet INSTANCE = (Wininet)Native.loadLibrary("Wininet", Wininet.class, W32APIOptions.UNICODE_OPTIONS);

        boolean InternetGetConnectedState(IntByReference lpdwFlags, WinDef.DWORD dwReserved);
    }

    private static final Duration MIN_INTERVAL = Duration.ofSeconds(1);
    private static final Duration MAX_INTERVAL = Duration.ofSeconds(60);

    private OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).build();
    private CancellationSource cancellationSource = new CancellationSource();

    private final AsyncBooleanProperty networkAvailable =
        new SimpleAsyncBooleanProperty(this) {
            @Override
            protected void invalidated() {
                if (get()) {
                    invalidate();
                } else {
                    cancellationSource.cancel();
                }
            }
        };
    private final AsyncBooleanProperty internetAvailable =
        new SimpleAsyncBooleanProperty(this) {
            @Override
            protected void invalidated() {
                if (get()) {
                    invalidate();
                } else {
                    cancellationSource.cancel();
                }
            }
        };
    private final ReadOnlyAsyncSetWrapper<String> unreachableHosts =
        new ReadOnlyAsyncSetWrapper<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableSet<String>>()
                .initialValue(FXAsyncCollections.observableArraySet())
                .create());

    private final AsyncObservableList<HostInfo> hosts = FXAsyncCollections.observableArrayList();

    @Inject
    public NetworkInformation(IApplicationContext applicationContext) {
        applicationContext.addClosingListener(() -> cancellationSource.cancel());
        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.runLaterAsync(
            () -> networkAvailable.set(isNetworkConnected()), Duration.ZERO, Duration.ofMillis(500));
        dispatcher.runLaterAsync(
            () -> internetAvailable.set(isInternetConnected()), Duration.ZERO, Duration.ofMillis(500));
    }

    @Override
    public ReadOnlyAsyncBooleanProperty networkAvailableProperty() {
        return networkAvailable;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty internetAvailableProperty() {
        return internetAvailable;
    }

    @Override
    public ReadOnlyAsyncSetProperty<String> unreachableHostsProperty() {
        return unreachableHosts.getReadOnlyProperty();
    }

    @Override
    public boolean isHostReachable(URL url) {
        try (LockedList<HostInfo> hostsView = hosts.lock()) {
            for (HostInfo host : hostsView) {
                if (host.getHostName().equals(url.getHost())) {
                    return host.isReachable();
                }
            }
        }

        HostInfo hostInfo = new HostInfo(url);
        hosts.add(hostInfo);
        probeHost(hostInfo, Duration.ZERO, cancellationSource);
        return true;
    }

    @Override
    public void invalidate() {
        cancellationSource.cancel();
        cancellationSource = new CancellationSource();

        try (LockedList<HostInfo> hostsView = hosts.lock()) {
            for (HostInfo host : hostsView) {
                probeHost(host, Duration.ZERO, cancellationSource);
            }
        }
    }

    private void probeHost(HostInfo host, Duration delay, CancellationSource cancellationSource) {
        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.runLaterAsync(
            cancellationToken -> {
                if (cancellationToken.isCancellationRequested() || !networkAvailable.get()) {
                    return;
                }

                if (!internetAvailable.get()) {
                    host.setReachable(false);
                    unreachableHosts.add(host.getHostName());
                    probeHost(host, MIN_INTERVAL, cancellationSource);
                    return;
                }

                boolean reachable = isUrlReachable(host.getTestSite());
                host.setReachable(reachable);
                if (reachable) {
                    unreachableHosts.remove(host.getHostName());
                    probeHost(host, MAX_INTERVAL, cancellationSource);
                } else {
                    unreachableHosts.add(host.getHostName());
                    probeHost(host, MIN_INTERVAL, cancellationSource);
                }
            },
            delay,
            cancellationSource);
    }

    private boolean isInternetConnected() {
        try {
            URL url = new URL("http://www.msftconnecttest.com/redirect");
            Request request = new Request.Builder().cacheControl(CacheControl.FORCE_NETWORK).url(url).head().build();

            Response response = httpClient.newCall(request).execute();

            if (response.code() != 504) { // 504 means not cached
                response.close();
            }

            if (response.code() != 200) {
                // 400 => no internet
                return false;
            }

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    private boolean isNetworkConnected() {
        IntByReference lpdwFlags = new IntByReference();
        WinDef.DWORD value = new WinDef.DWORD(0);
        Boolean connected = Wininet.INSTANCE.InternetGetConnectedState(lpdwFlags, value);
        if (!connected) { // sometime it is false, but internet is available
            return internetAvailable.get();
        }

        return connected;
    }

    private boolean isUrlReachable(URL url) {
        try {
            Request request = new Request.Builder().cacheControl(CacheControl.FORCE_NETWORK).url(url).head().build();
            httpClient.newCall(request).execute();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
