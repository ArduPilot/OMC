/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncSetProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncSetWrapper;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.AsyncObservableSet;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.collections.LockedList;
import com.intel.missioncontrol.concurrent.CancellationToken;
import com.intel.missioncontrol.concurrent.CancellationTokenSource;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import javafx.util.Duration;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NetworkInformation implements INetworkInformation {

    private interface Wininet extends StdCallLibrary {
        Wininet INSTANCE = (Wininet)Native.loadLibrary("Wininet", Wininet.class, W32APIOptions.UNICODE_OPTIONS);

        boolean InternetGetConnectedState(IntByReference lpdwFlags, WinDef.DWORD dwReserved);
    }

    private static final Duration MIN_INTERVAL = Duration.seconds(1);
    private static final Duration MAX_INTERVAL = Duration.seconds(60);

    private OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).build();
    private CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

    private final AsyncBooleanProperty networkAvailable =
        new SimpleAsyncBooleanProperty(this) {
            @Override
            protected void invalidated() {
                if (get()) {
                    invalidate();
                } else {
                    cancellationTokenSource.close();
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
        applicationContext.addClosingListener(() -> cancellationTokenSource.close());
        Dispatcher.schedule(() -> networkAvailable.set(isNetworkConnected()), Duration.ZERO, Duration.millis(500));
    }

    @Override
    public ReadOnlyAsyncBooleanProperty networkAvailableProperty() {
        return networkAvailable;
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
        probeHost(hostInfo, Duration.ZERO, cancellationTokenSource.createToken());
        return true;
    }

    @Override
    public void invalidate() {
        cancellationTokenSource.close();
        cancellationTokenSource = new CancellationTokenSource();

        try (LockedList<HostInfo> hostsView = hosts.lock()) {
            for (HostInfo host : hostsView) {
                probeHost(host, Duration.ZERO, cancellationTokenSource.createToken());
            }
        }
    }

    private void probeHost(HostInfo host, Duration delay, CancellationToken cancellationToken) {
        Dispatcher.schedule(
            () -> {
                if (cancellationToken.isCancellationRequested() || !networkAvailable.get()) {
                    return;
                }

                boolean reachable = isUrlReachable(host.getTestSite());
                host.setReachable(reachable);
                if (reachable) {
                    unreachableHosts.remove(host.getHostName());
                    probeHost(host, MAX_INTERVAL, cancellationToken);
                } else {
                    unreachableHosts.add(host.getHostName());
                    probeHost(host, MIN_INTERVAL, cancellationToken);
                }
            },
            delay,
            cancellationToken);
    }

    private boolean isNetworkConnected() {
        IntByReference lpdwFlags = new IntByReference();
        WinDef.DWORD value = new WinDef.DWORD(0);
        return Wininet.INSTANCE.InternetGetConnectedState(lpdwFlags, value);
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
