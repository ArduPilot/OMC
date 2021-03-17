/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.services;

import com.intel.missioncontrol.airmap.AirMap;
import com.intel.missioncontrol.airmap.AirMap2Source;
import com.intel.missioncontrol.airmap.network.AirMapConfig2;
import com.intel.missioncontrol.airspace.LayerConfigurator;
import com.intel.missioncontrol.airspaces.sources.AirspaceSource;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.networking.OkHttpUtil;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.networking.MapTileDownloadStatusNotifier;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import eu.mavinci.airspace.IAirspace;
import eu.mavinci.desktop.helper.MathHelper;
import gov.nasa.worldwind.geom.Sector;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import okhttp3.CacheControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Airmap2AirspaceService implements LocationAwareAirspaceService, SourceAwareAirspaceService {
    private static final Logger LOG = LoggerFactory.getLogger(Airmap2AirspaceService.class);
    private static final String AIRMAP2_CACHE_DIR = "airmap2-cache";

    private AirMap2Source source;

    private static final AtomicBoolean forceCache = new AtomicBoolean();

    // at least this limits the contagion to this class...
    static MapTileDownloadStatusNotifier notifier = null;

    // todo: should be injected
    public static void initDownloadNotifier(MapTileDownloadStatusNotifier downloadStatusNotifier) {
        notifier = downloadStatusNotifier;
    }

    @Inject
    public Airmap2AirspaceService(
            AirspacesProvidersSettings settings, IPathProvider pathProvider, INetworkInformation networkInformation) {
        configAirmap(settings, pathProvider, networkInformation);
        source = AirMap2Source.getInstance();
    }

    public static void setOfflineMode(boolean offline) {
        forceCache.set(offline);
    }

    private void configAirmap(
            AirspacesProvidersSettings settings, IPathProvider pathProvider, INetworkInformation networkInformation) {
        AirMapConfig2 config = new AirMapConfig2();
        config.proxySelector = null;
        config.apiKey = LayerConfigurator.getDefaultAirmapKey();
        config.cacheSize = settings.airmapAirspacesPersistentCacheSizeProperty().get() /* in megabytes */ * 1024 * 1024;
        config.networkListener =
            new OkHttpUtil.NetworkActivityListener() {
                String active = null;

                @Override
                public void onNetworkActivityStarted(Object tag) {
                    SwingUtilities.invokeLater(
                        () -> {
                            if (active == null) {
                                active = notifier.downloadStarted();
                            }
                        });
                }

                @Override
                public void onNetworkActivityStopped(Object tag) {
                    SwingUtilities.invokeLater(
                        () -> {
                            if (active != null) {
                                notifier.downloadFinished(active);

                                active = null;
                            }
                        });
                }
            };

        // forceCache.set(!networkInformation.networkAvailableProperty().get());
        forceCache.set(!networkInformation.internetAvailableProperty().get());

        // if offline, then we only load from cache
        config.setHttpRequestInterceptor(
            builder -> {
                if (forceCache.get()) {
                    return builder.cacheControl(CacheControl.FORCE_CACHE);
                } else {
                    return builder;
                }
            });

        // getWwjCacheDirectory returns cache
        File cacheDir = new File(pathProvider.getCacheDirectory().toFile().getAbsolutePath(), AIRMAP2_CACHE_DIR);

        config.cacheDir = cacheDir;
        LOG.info("Using cache directory: {}", cacheDir);

        AirMap.init(config);
    }

    @Override
    public List<IAirspace> getAirspacesWithin(Sector boundingBox, int bufferInMeters) {
        Sector bb =
            Math.abs(bufferInMeters) < 0.0001 ? boundingBox : MathHelper.extendSector(boundingBox, bufferInMeters);

        return source.getAirspacesWithin(bb);
    }

    // wtf is the point of AirspaceSource vs AirspaceService?
    @Override
    public AirspaceSource getSource() {
        return source;
    }

}
