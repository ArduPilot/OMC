/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.map.worldwind.layers.mercator.FastMercatorTiledImageLayer;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.networking.INetworkInterceptor;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.layers.mercator.MercatorTextureTile;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.asyncfx.concurrent.Dispatcher;

public class MapboxLayer extends FastMercatorTiledImageLayer {

    public static class Keys {
        static final String defMapID_pureSat = "TODO-mapID";
        public static final String defAccessToken =
                "TODO Access token";

        static final String defMapID_hybrid = "TODO-mapID";
        static final String defAccessTokenHybrid =
                "TODO-MapBox Access token";

        static final String defMapID_pureStreets = "TODO-mapID";
        static final String defAccessTokenPureStreets =
                "TODO-MapBox Access token";
    }

    @LayerDefaults(name = "%com.intel.missioncontrol.map.worldwind.layers.Streets")
    public static final class Streets extends WWLayerWrapper {

        @Inject
        public Streets(
                @Named(MapModule.DISPATCHER) Dispatcher dispatcher,
                INetworkInterceptor networkInterceptor,
                IPathProvider pathProvider,
                INetworkInformation networkInformation) {
            super(
                new MapboxLayer(
                    MapType.STREETS,
                    new Options(pathProvider.getCacheDirectory().resolve("mapbox2").toFile(), networkInterceptor),
                    networkInformation),
                dispatcher);
        }
    }

    @LayerDefaults(name = "%com.intel.missioncontrol.map.worldwind.layers.Satellite")
    public static final class Satellite extends WWLayerWrapper {
        @Inject
        public Satellite(
                @Named(MapModule.DISPATCHER) Dispatcher dispatcher,
                INetworkInterceptor networkInterceptor,
                IPathProvider pathProvider,
                INetworkInformation networkInformation) {
            super(
                new MapboxLayer(
                    MapType.SATELLITE,
                    new Options(pathProvider.getCacheDirectory().resolve("mapbox2").toFile(), networkInterceptor),
                    networkInformation),
                dispatcher);
        }
    }

    @LayerDefaults(name = "%com.intel.missioncontrol.map.worldwind.layers.Hybrid")
    public static final class Hybrid extends WWLayerWrapper {
        @Inject
        public Hybrid(
                @Named(MapModule.DISPATCHER) Dispatcher dispatcher,
                INetworkInterceptor networkInterceptor,
                IPathProvider pathProvider,
                INetworkInformation networkInformation) {
            super(
                new MapboxLayer(
                    MapType.HYBRID,
                    new Options(pathProvider.getCacheDirectory().resolve("mapbox2").toFile(), networkInterceptor),
                    networkInformation),
                dispatcher);
        }
    }

    private static final Logger LOG = Logger.getLogger(MapboxLayer.class.getName());

    private final boolean disableBlankTileCheck;
    private final boolean largeTiles;
    private final MapType mapType;
    private INetworkInformation networkInformation;

    public enum MapType {
        STREETS(Keys.defMapID_pureStreets, Keys.defAccessTokenPureStreets),
        SATELLITE(Keys.defMapID_pureSat, Keys.defAccessToken),
        HYBRID(Keys.defMapID_hybrid, Keys.defAccessTokenHybrid);

        public final String id;
        public final String token;

        MapType(String mapId, String accessToken) {
            this.id = mapId;
            this.token = accessToken;
        }
    }

    public static class Options {
        public int maxRequests;
        public File cacheDirectory;
        public long cacheSize;
        public boolean useLargeTiles;
        public boolean disableBlankTileCheck = false;
        public FastMercatorTiledImageLayer.TileDownloadQueue tileQueue;
        // listen for network becoming active @Nullable
        public INetworkInterceptor networkInterceptor;

        public Options(File cacheDirectory, INetworkInterceptor networkInterceptor) {
            maxRequests = 6;
            useLargeTiles = true;
            cacheSize = 500 * 1024 * 1024;
            tileQueue = null;
            this.cacheDirectory = cacheDirectory;
            this.networkInterceptor = networkInterceptor;
        }
    }

    private static HttpOptions makeHttpOptions(Options options) {
        return new HttpOptions(
            options.cacheDirectory,
            options.cacheSize,
            options.maxRequests,
            Duration.ofDays(14),
            options.networkInterceptor);
    }

    public MapboxLayer(MapType type, Options options, INetworkInformation networkInformation) {
        super(
            makeLevels(options.useLargeTiles, type.id, type.token),
            options.useLargeTiles ? 1.1 : 1.4,
            options.tileQueue,
            makeHttpOptions(options));

        this.disableBlankTileCheck = options.disableBlankTileCheck;
        this.largeTiles = options.useLargeTiles;
        this.mapType = type;
        this.networkInformation = networkInformation;

        setDetailHint(0.3);
        // avoid big textel for high altitude
        if (largeTiles) {
            setMinimumDisplayLevel(2);
        }

        tryDropOldCache();
    }

    private long getLastPurgeMillis() {
        return 0;
    }

    private void setLastPurgeMillis(long lastPurgeMillis) {}

    @Override
    protected boolean isTileValid(MercatorTextureTile tile, BufferedImage image, String type, long encodedImageSize) {
        if (disableBlankTileCheck) {
            return true;
        }

        if (mapType == MapType.HYBRID) {
            return !isBlackTileHybrid(largeTiles, tile, image, encodedImageSize);
        } else {
            return !isEmptyTileSatStreet(largeTiles, tile, image, encodedImageSize);
        }
    }

    private static LevelSet makeLevels(boolean largeTiles, String mapID, String accessToken) {
        if (mapID == null || accessToken == null) {
            throw new IllegalArgumentException("must supply mapId and accessToken");
        }

        AVList params = new AVListImpl();
        if (largeTiles) {
            params.setValue(AVKey.TILE_WIDTH, 512);
            params.setValue(AVKey.TILE_HEIGHT, 512);
            params.setValue(AVKey.NUM_LEVELS, 19);
        } else {
            params.setValue(AVKey.TILE_WIDTH, 256);
            params.setValue(AVKey.TILE_HEIGHT, 256);
            params.setValue(AVKey.NUM_LEVELS, 20);
        }

        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/Mapbox/" + mapID);
        params.setValue(AVKey.SERVICE, "https://api.mapbox.com/styles/v1/");
        params.setValue(AVKey.DATASET_NAME, "*");
        params.setValue(AVKey.FORMAT_SUFFIX, ".jpg");
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(180d), Angle.fromDegrees(360d)));
        params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));
        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder(mapID, accessToken, largeTiles));

        return new LevelSet(params);
    }

    private static class URLBuilder implements TileUrlBuilder {
        final String mapTilesId;
        final String accessToken;

        URLBuilder(String mapID, String accessToken, boolean largeTiles) {
            this.accessToken = accessToken;
            this.mapTilesId = mapID + (largeTiles ? "/tiles/" : "/tiles/256/");
        }

        public URL getURL(Tile tile, String imageFormat) throws MalformedURLException {
            // see https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
            final long z = tile.getLevelNumber();
            final long x = tile.getColumn();
            final long y = (1L << z) - tile.getRow() - 1L;

            // these are massive strings
            String url =
                tile.getLevel().getService() + mapTilesId + z + "/" + x + "/" + y + "?access_token=" + accessToken;

            return new URL(url);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // black tile detection stuff
    ///////////////////////////////////////////////////////////////////////////

    /** black tile 'black' color is exactly rbg(4, 7, 14) */
    public static final Color BLACKTILE_COLOR = new Color(4, 7, 14);

    /** these heuristics generated by running MapboxOrtho2BlackTileTest against 3000 map tiles and looking */
    private static final int BLACKTILE_512_BLANK_SIZE = 4724 + 2;

    private static final int BLACKTILE_512_MAX_SIZE = 33000;
    private static final int BLACKTILE_512_MIN_ZOOM = 11;

    private static final int BLACKTILE_256_BLANK_SIZE = 1652 + 2;
    private static final int BLACKTILE_256_MAX_SIZE = 12000;
    private static final int BLACKTILE_256_MIN_ZOOM = 12;

    private static final int BLACKTILE_PIXEL_PCT_THRESH = 40;

    private static final int H_SAMPLE_FREQ = 3;

    public static class ImageInfo {
        public float pctUnderMatch;
        public float pctMatch;
    }

    /** pure Satellite and street tiles will either be entirely blank (might be white in the case of street) or not; */
    private static boolean isEmptyTileSatStreet(
            boolean largeTiles, MercatorTextureTile tile, BufferedImage image, long encodedImageSize) {
        int blankSize = largeTiles ? BLACKTILE_512_BLANK_SIZE : BLACKTILE_256_BLANK_SIZE;
        return encodedImageSize <= blankSize;
    }

    /** Hybrid tiles can be totally blank, or have labels but no satellite imagery */
    private static boolean isBlackTileHybrid(
            boolean largeTiles, MercatorTextureTile tile, BufferedImage image, long encodedImageSize) {
        int blankSize, maxSize, minZoom;

        if (largeTiles) {
            blankSize = BLACKTILE_512_BLANK_SIZE;
            maxSize = BLACKTILE_512_MAX_SIZE;
            minZoom = BLACKTILE_512_MIN_ZOOM;
        } else {
            blankSize = BLACKTILE_256_BLANK_SIZE;
            maxSize = BLACKTILE_256_MAX_SIZE;
            minZoom = BLACKTILE_256_MIN_ZOOM;
        }

        // totally blank or high zoom
        if (tile.getLevelNumber() < minZoom || encodedImageSize > maxSize) {
            return false;
        }

        if (encodedImageSize <= blankSize) {
            return true;
        }

        // could be black;

        ImageInfo imageThreshold = getImageColorMatch(image, BLACKTILE_COLOR);
        return imageThreshold.pctMatch >= BLACKTILE_PIXEL_PCT_THRESH;
    }

    public static ImageInfo getImageColorMatch(BufferedImage image, final Color color) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        final int type = image.getType();
        final int r_thresh = color.getRed();
        final int g_thresh = color.getGreen();
        final int b_thresh = color.getBlue();

        int countUnderColor = 0;
        int countMatching = 0;

        if (type == BufferedImage.TYPE_3BYTE_BGR) {
            final byte[] data = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();

            for (int p = 0; p < data.length; p += 3 * H_SAMPLE_FREQ) {
                int b = ((int)data[p] & 0xff); // blue
                int g = ((int)data[p + 1] & 0xff); // green
                int r = ((int)data[p + 2] & 0xff); // red

                if (r == r_thresh && g == g_thresh && b == b_thresh) {
                    countMatching++;
                } else if (r <= r_thresh && g <= g_thresh && b <= b_thresh) {
                    countUnderColor++;
                }
            }
        } else {
            // fallback for non BGR images
            // warning: this is very slow! A 512px tile takes about 5000us vs 100us for fast path above
            LOG.warning("getImageColorMatch(), using very slow path for BufferedImage type: " + type);

            int[] pix = new int[width * height];
            image.getRGB(0, 0, width, height, pix, 0, width);

            for (int p = 0; p < pix.length; p += H_SAMPLE_FREQ) {
                int b = pix[p] & 0xff;
                int g = (pix[p] & 0xff00) >> 8;
                int r = (pix[p] & 0xff0000) >> 16;

                if (r == r_thresh && g == g_thresh && b == b_thresh) {
                    countMatching++;
                } else if (r <= r_thresh && g <= g_thresh && b <= b_thresh) {
                    countUnderColor++;
                }
            }
        }

        ImageInfo info = new ImageInfo();
        info.pctUnderMatch = ((float)countUnderColor * H_SAMPLE_FREQ) * 100.0f / (float)(width * height);
        info.pctMatch = ((float)countMatching * H_SAMPLE_FREQ) * 100.0f / (float)(width * height);
        return info;
    }

    private static boolean chacheDropTriedOnce;

    private static final Semaphore cacheDropSemaphore = new Semaphore(1);

    private static final Duration MAPBOX_CONTRACT_MAX_AGE_BUT_ALSO_GIVE_OURSELVES_A_SAFETY_MARGIN =
        Duration.ofDays(28 - 1);

    private static final long MAPBOX2_PURGE_INTERVAL = Duration.ofHours(4).toMillis();
    private static final Object cachePurgelock = new Object();

    private void tryDropOldCache() {
        tryDropOldCache(getHttpClient());
    }

    private void tryDropOldCache(OkHttpClient client) {
        synchronized (cachePurgelock) {
            if (!chacheDropTriedOnce) {
                chacheDropTriedOnce = true;
                LOG.info("MapLayerMapBox: MapBoxOrtho2 cache purge check scheduling in 120 seconds");
                Dispatcher dispatcher = Dispatcher.background();
                dispatcher.runLater(
                    () -> {
                        if (!WorldWind.isOfflineMode()
                                && !WorldWind.getNetworkStatus().isNetworkUnavailable()
                                && networkInformation.networkAvailableProperty().get()
                                && networkInformation.internetAvailableProperty().get()) {
                            long last = getLastPurgeMillis();
                            long now = System.currentTimeMillis();

                            LOG.info(
                                "MapLayerMapBox: last MapBoxOrtho2 cache purge "
                                    + java.util.Date.from(Instant.ofEpochMilli(last)));

                            if (now - last > MAPBOX2_PURGE_INTERVAL) {
                                LOG.info("MapLayerMapBox: MapBoxOrtho2 cache purge is due -> start purging");
                                doDropOldCache(client, MAPBOX_CONTRACT_MAX_AGE_BUT_ALSO_GIVE_OURSELVES_A_SAFETY_MARGIN);
                            } else {
                                LOG.info("MapLayerMapBox: MapBoxOrtho2 cache purge is NOT due -> skipping");
                            }
                        } else {
                            LOG.info("MapLayerMapBox: MapBoxOrtho2 cache purge skipped due to no network connection");
                        }
                    },
                    Duration.ofSeconds(120));
            }
        }
    }

    // this is stupid and slow, but contract with mapbox forces us to do something like this
    private static void doDropOldCache(OkHttpClient client, Duration threshold) {
        synchronized (client) {
            Cache cache = client.cache();
            try {
                long startSize = cache.size();
                long currentTime = System.currentTimeMillis();
                long thresholdAgeMillis = threshold.toMillis();

                int cacheCount = 0;
                int dropCount = 0;

                LOG.info(
                    "MapBoxOrtho2 - DropOldCache: attempting to drop cache older than "
                        + threshold.toSeconds()
                        + " seconds");

                Iterator<String> urls = cache.urls();
                while (urls.hasNext()) {
                    cacheCount++;
                    String url = urls.next();

                    Request request = new Request.Builder().cacheControl(CacheControl.FORCE_CACHE).url(url).build();
                    Response response = null;
                    try {
                        response = client.newCall(request).execute();
                    } catch (IOException e) {
                    }

                    if (response == null) {
                        continue;
                    }

                    if (response.code() != 504) { // 504 means not cached
                        long age = currentTime - response.receivedResponseAtMillis();

                        // must both close response and close before dropping or else cache behaves weirdly
                        response.close();

                        if (age > thresholdAgeMillis) {
                            urls.remove();
                            dropCount++;
                        }
                    } else {
                        response.close();
                    }
                }

                cache.flush();
                long endSize = cache.size();
                long endTime = System.currentTimeMillis();

                LOG.info(
                    "MapBoxOrtho2 - DropOldCache: finished after "
                        + (endTime - currentTime)
                        + " ms: dropped "
                        + dropCount
                        + " of "
                        + cacheCount
                        + " cache items, dropped "
                        + (startSize - endSize) / 1024
                        + "KiB, size now "
                        + endSize / 1024
                        + "KiB");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
