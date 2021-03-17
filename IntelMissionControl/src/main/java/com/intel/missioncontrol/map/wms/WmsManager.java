/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.wms;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.IElevationLayer;
import com.intel.missioncontrol.map.elevation.IElevationModelsManager;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.networking.proxy.ProxyManager;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.WmsServerSettings;
import com.intel.missioncontrol.settings.WmsServersSettings;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import org.asyncfx.beans.binding.LifecycleValueConverter;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Strand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WmsManager implements IWmsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WmsManager.class);
    public static final int RELOAD_ON_FAIL_DELAY = 5000;

    private final Dispatcher dispatcher;
    private final ISettingsManager settingsManager;
    private final IPathProvider pathProvider;
    private final ILanguageHelper languageHelper;
    private final IElevationModelsManager elevationModelsManager;
    private final IApplicationContext applicationContext;
    private final ProxyManager proxyManager;
    private final Strand strand = new Strand();
    private final Timer timer = new Timer();

    private final AsyncListProperty<WmsServer> wmsServers =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<WmsServer>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());
    private final AsyncListProperty<IElevationLayer> elevationLayers =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<IElevationLayer>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final AsyncListProperty<WmsServerLayer> wmsServerLayers =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<WmsServerLayer>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    @Inject
    public WmsManager(
            @Named(MapModule.DISPATCHER) Dispatcher dispatcher,
            ISettingsManager settingsManager,
            IPathProvider pathProvider,
            ILanguageHelper languageHelper,
            IElevationModelsManager elevationModelsManager,
            IApplicationContext applicationContext,
            INetworkInformation networkInformation,
            ProxyManager proxyManager) {
        this.dispatcher = dispatcher;
        this.settingsManager = settingsManager;
        this.pathProvider = pathProvider;
        this.languageHelper = languageHelper;
        this.elevationModelsManager = elevationModelsManager;
        this.applicationContext = applicationContext;
        this.proxyManager = proxyManager;
        WmsServersSettings wmsServersSettings = settingsManager.getSection(WmsServersSettings.class);

        // creating WMSServers from existing WmsSettings
        try (LockedList<WmsServerSettings> settingsList = wmsServersSettings.wmssProperty().lock()) {
            for (WmsServerSettings setting : settingsList) {
                addWmsServer(setting);
            }
        }

        // adding default WMS server if was not added before
        if (!this.containsWmsServer(WmsServersSettings.SENTINEL_URL)) {
            addWmsServer(WmsServersSettings.SENTINEL_URL);
        }

        wmsServerLayers.bindContent(
            wmsServers,
            new LifecycleValueConverter<>() {
                @Override
                public WmsServerLayer convert(WmsServer value) {
                    return new WmsServerLayer(dispatcher, value);
                }

                @Override
                public void update(WmsServer sourceValue, WmsServerLayer targetValue) {}

                @Override
                public void remove(WmsServerLayer value) {
                    Dispatcher.background().run(() -> value.dropCache());
                }
            });

        wmsServersSettings.wmssProperty().bindContent(wmsServersProperty(), (value -> value.getWmsSettings()));

        // TODO what does it do, why
        wmsServerLayers.addListener(
            (InvalidationListener)
                observable -> {
                    try (LockedList<WmsServerLayer> tmpWms = wmsServerLayers.lock()) {
                        LinkedList<IElevationLayer> elevationLayerNew = new LinkedList<>();
                        for (WmsServerLayer wmsServer : tmpWms) {
                            try (LockedList<IElevationLayer> tmpElev = wmsServer.elevationsLayersProperty().lock()) {
                                elevationLayerNew.addAll(tmpElev);
                            }
                        }

                        elevationLayers.setAll(elevationLayerNew);
                    }
                });

        wmsServers.addListener(
            (ListChangeListener<? super WmsServer>)
                c -> {
                    while (c.next()) {
                        for (WmsServer removedWms : c.getRemoved()) {
                            Dispatcher.background().run(() -> dropCache(removedWms));
                        }
                    }
                });

        elevationModelsManager.register(elevationLayers);

        // refreshing failed wms servers if network status changes to online
        networkInformation
            .internetAvailableProperty()
            .addListener(
                (object, oldVal, newVal) -> {
                    if (newVal) {
                        refreshAllServers();
                    }
                });

        // refreshing failed wms servers if proxy init finished
        proxyManager
            .proxyInitializedProperty()
            .addListener(
                (object, oldVal, newVal) -> {
                    if (newVal) {
                        refreshAllServers();
                    }
                });
    }

    private void refreshAllServers() {
        try (LockedList<WmsServer> servers = wmsServers.lock()) {
            servers.stream().forEach((server) -> refreshWmsCapabilities(server, false));
        }
    }

    @Override
    public AsyncObservableList<WmsServer> wmsServersProperty() {
        return wmsServers;
    }

    @Override
    public boolean containsWmsServer(String wmsUrl) {
        return getWmsServer(wmsUrl) != null;
    }

    @Override
    public WmsServer getWmsServer(String wmsUrl) {
        try (LockedList<WmsServer> servers = wmsServers.lock()) {
            for (WmsServer wmsServer : servers) {
                if (wmsServer.getWmsSettings().urlProperty().getValue().equals(wmsUrl)) {
                    return wmsServer;
                }
            }
        }

        return null;
    }

    @Override
    public void addWmsServer(String wmsUrl) {
        try {
            URI serverURI = new URI(wmsUrl);
            WmsServer server = new WmsServer(serverURI, pathProvider);
            wmsServers.add(server);
            refreshWmsCapabilities(server, true);

        } catch (URISyntaxException e1) {
            applicationContext.addToast(
                Toast.of(ToastType.ALERT)
                    .setShowIcon(true)
                    .setText(
                        languageHelper.getString("com.intel.missioncontrol.map.wms.wmsServer.malFormedURL", wmsUrl))
                    .create());
            LOGGER.error("WMS Server \"" + wmsUrl + "\" has not well-formed URL", e1);
        }
    }

    @Override
    public void addWmsServer(WmsServerSettings settings) {
        WmsServer server = new WmsServer(settings, pathProvider);
        wmsServers.add(server);
        refreshWmsCapabilities(server, true);
    }

    @Override
    public void loadWmsServer(WmsServer wmsServer) {
        refreshWmsCapabilities(wmsServer, false);
    }

    private void refreshWmsCapabilities(WmsServer wmsServer, boolean rescheduleOnFail) {
        strand.runLater(
            (() -> {
                if (!proxyManager.proxyInitializedProperty().get()) {
                    timer.schedule(
                        new TimerTask() {

                            @Override
                            public void run() {
                                refreshWmsCapabilities(wmsServer, true);
                            }
                        },
                        RELOAD_ON_FAIL_DELAY);
                    return;
                }

                try {
                    PropertyHelper.setValueSafe(wmsServer.hasWarningProperty(), false);
                    PropertyHelper.setValueSafe(wmsServer.warningProperty(), null);

                    wmsServer.retrieveAndLoadCapabilities();

                } catch (Exception e) {
                    PropertyHelper.setValueSafe(wmsServer.hasWarningProperty(), true);
                    String error =
                        languageHelper.getString(
                            "com.intel.missioncontrol.map.wms.wmsServer.wmsServerNotReachable", wmsServer.getUrl());
                    String warningAndRefresh =
                        languageHelper.getString(
                            "com.intel.missioncontrol.map.wms.wmsServer.wmsServerNotReachable.refresh");
                    PropertyHelper.setValueSafe(wmsServer.warningProperty(), warningAndRefresh);

                    LOGGER.error(error, e);

                    // best effort - will try to reload a bit later
                    if (rescheduleOnFail) {
                        timer.schedule(
                            new TimerTask() {

                                @Override
                                public void run() {
                                    refreshWmsCapabilities(wmsServer, false);
                                }
                            },
                            RELOAD_ON_FAIL_DELAY);
                    } else {
                        applicationContext.addToast(
                            Toast.of(ToastType.ALERT).setShowIcon(true).setText(error).create());
                    }
                    // TODO refactor
                    if (e instanceof SocketTimeoutException) {
                        // TODO add some info to the warning
                    } else {
                        // TODO add some other info to the warning
                    }
                }
            }));
    }

    @Override
    public void deleteWmsServer(WmsServer wmsServer) {
        wmsServers.remove(wmsServer);
    }

    @Override
    public boolean isDefaultServer(WmsServer wmsServer) {
        return WmsServersSettings.SENTINEL_URL.equals(wmsServer.getUrl());
    }

    private void dropCache(WmsServer wmsServer) {
        wmsServer.deleteFiles();
    }

    public AsyncListProperty<WmsServerLayer> wmsServerLayersProperty() {
        return wmsServerLayers;
    }
}
