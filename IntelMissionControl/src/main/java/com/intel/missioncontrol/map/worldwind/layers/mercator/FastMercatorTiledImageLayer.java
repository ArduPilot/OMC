/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.mercator;

import com.intel.missioncontrol.networking.INetworkInterceptor;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.layers.mercator.MercatorTextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.asyncfx.concurrent.FutureExecutorService;

/**
 * A faster version of {@link gov.nasa.worldwind.layers.mercator.BasicMercatorTiledImageLayer} that has customizable
 * queueing logic.
 *
 * <p>{@see FancyDownloadQueue}
 */
public class FastMercatorTiledImageLayer extends MercatorTiledImageLayerBase {

    private static final Logger LOG = Logger.getLogger(FastMercatorTiledImageLayer.class.getName());

    /** keeps track of all queued and downloaded tiles, to avoid adding twice */
    private final ConcurrentHashMap.KeySetView<Object, Boolean> activeRequests = ConcurrentHashMap.newKeySet();

    private final HttpClient httpClient;
    private final TileDownloadQueue downloadQueue;

    public interface TileDownloadQueue {
        /**
         * <b>Called from WWJ main thread</b> Renderer needs a tile from network/disk (i.e. not in memory cache). A
         * specific tile may be added multiple times, implementation must deduplicate
         *
         * @param tile
         */
        void addTile(MercatorTextureTile tile);

        /**
         * <b>Called from ANY thread</b> Http client is free download next tile
         *
         * @return next tile to download, null if there is nothing in the queue
         */
        MercatorTextureTile getNextTileForDownload();

        void onTilesWillDraw(DrawContext drawContext, ArrayList<MercatorTextureTile> tiles);
    }

    public static class HttpOptions {
        final File cacheDirectory;
        final long cacheSize;
        final int maxRequests;
        /** if null, don't change HTTP headers and use whatever the server gives us */
        final Duration overrideMaxCacheAge;

        final INetworkInterceptor networkInterceptor;

        public HttpOptions(
                File cacheDirectory,
                long cacheSize,
                int maxRequests,
                Duration overrideMaxCacheAge,
                INetworkInterceptor networkInterceptor) {
            this.cacheDirectory = cacheDirectory;
            this.cacheSize = cacheSize;
            this.maxRequests = maxRequests;
            this.overrideMaxCacheAge = overrideMaxCacheAge;
            this.networkInterceptor = networkInterceptor;
        }
    }

    public FastMercatorTiledImageLayer(LevelSet levelSet, HttpOptions options) {
        this(levelSet, 1.4, null, options);
    }

    /** @see gov.nasa.worldwind.layers.mercator.BasicMercatorTiledImageLayer#BasicMercatorTiledImageLayer(LevelSet) */
    public FastMercatorTiledImageLayer(
            LevelSet levelSet, double splitScale, TileDownloadQueue queue, HttpOptions options) {
        super(levelSet, splitScale); // see MMercatorTiledImageLayer#splitScale

        if (!WorldWind.getMemoryCacheSet().containsCache(MercatorTextureTile.class.getName())) {
            long size = Configuration.getLongValue(AVKey.TEXTURE_IMAGE_CACHE_SIZE, 3000000L);
            MemoryCache cache = new BasicMemoryCache((long)(0.85 * size), size);
            cache.setName("Texture Tiles");
            WorldWind.getMemoryCacheSet().addCache(MercatorTextureTile.class.getName(), cache);
        }

        httpClient = new HttpClient(options);
        if (queue == null) {
            downloadQueue = new FancyDownloadQueue();
        } else {
            downloadQueue = queue;
        }
    }

    private class HttpClient implements Callback {
        final OkHttpClient client;
        final Dispatcher dispatcher;
        final int maxSimultaneousRequests;

        private HttpClient(HttpOptions options) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            // use our own dispatcher
            builder.dispatcher(new okhttp3.Dispatcher(FutureExecutorService.getInstance()));

            // setup cache
            if (options.cacheDirectory != null) {
                builder.cache(new Cache(options.cacheDirectory, options.cacheSize));
            } else {
                LOG.warning("not using any cache because cacheDirectory in httpOptions set to null!");
            }

            // network activity callback
            builder.addNetworkInterceptor(options.networkInterceptor);

            if (options.overrideMaxCacheAge != null) {
                final String cacheControl = "max-age=" + options.overrideMaxCacheAge.getSeconds();
                builder.addNetworkInterceptor(
                    new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Response originalResponse = chain.proceed(chain.request());

                            // rewrite response headers to force caching
                            return originalResponse
                                .newBuilder()
                                .removeHeader("Pragma") // allow us to use without re-validating if offline
                                .removeHeader("Expires")
                                .header("Cache-Control", cacheControl)
                                .build();
                        }
                    });
            }

            client = builder.build();

            // setup dispatcher
            dispatcher = client.dispatcher();

            // dump cache when idle
            dispatcher.setMaxRequests(options.maxRequests);
            dispatcher.setMaxRequestsPerHost(options.maxRequests);

            maxSimultaneousRequests = dispatcher.getMaxRequests();
        }

        /// feed the #httpClient from the downloadQueue
        private void pumpTileQueue() {
            while (dispatcher.runningCallsCount() < maxSimultaneousRequests) {
                MercatorTextureTile tile = downloadQueue.getNextTileForDownload();
                if (tile == null) {
                    return;
                }

                makeRequest(tile);
            }
        }

        private void makeRequest(MercatorTextureTile tile) {
            makeRequest(tile, WorldWind.isOfflineMode());
        }

        private void makeRequest(MercatorTextureTile tile, boolean forceCache) {
            URL url;
            try {
                url = tile.getResourceURL();
            } catch (MalformedURLException e) {
                // this should not happen because we already check url validity in #requestTexture()
                e.printStackTrace();
                return;
            }

            Request.Builder builder = new Request.Builder().url(url).get().tag(tile);
            if (forceCache) {
                builder.cacheControl(CacheControl.FORCE_CACHE);
            }

            activeRequests.add(tile);

            Call call = client.newCall(builder.build());
            call.enqueue(this);
        }

        @Override
        public void onFailure(Call call, IOException e) {
            MercatorTextureTile tile = null;
            try {
                tile = (MercatorTextureTile)call.request().tag();
                handleLoadFailure(tile, e, 0);
            } finally {
                if (tile != null) {
                    activeRequests.remove(tile);
                }

                pumpTileQueue();
            }
        }

        @Override
        public void onResponse(Call call, Response response) {
            MercatorTextureTile tile = null;
            try {
                tile = (MercatorTextureTile)response.request().tag();
                ResponseBody body = null;
                if (response.isSuccessful() && (body = response.body()) != null) {
                    String type = response.header("Content-Type", "image/?");
                    handleLoadTexture(tile, type, body.contentLength(), body.byteStream());
                    response.close();
                } else {
                    handleLoadFailure(tile, null, response.code());
                }
            } finally {
                if (tile != null) {
                    activeRequests.remove(tile);
                }

                pumpTileQueue();
            }
        }
    }

    protected OkHttpClient getHttpClient() {
        return httpClient.client;
    }

    @Override
    protected void onTilesAssembledWillDraw(DrawContext dc, ArrayList<MercatorTextureTile> tiles) {
        downloadQueue.onTilesWillDraw(dc, tiles);
        httpClient.pumpTileQueue();
    }

    private void addTileToFetchQueue(URL url, MercatorTextureTile tile) {
        if (activeRequests.contains(tile)) {
            return;
        }

        downloadQueue.addTile(tile);
    }

    // from gov.nasa.worldwind.layers.TiledImageLayer.computeReferencePoint
    // utility to be used by DownloadQueue
    static Vec4 computeReferencePoint(DrawContext dc) {
        if (dc.getViewportCenterPosition() != null) {
            return dc.getGlobe().computePointFromPosition(dc.getViewportCenterPosition());
        }

        java.awt.geom.Rectangle2D viewport = dc.getView().getViewport();
        int x = (int)viewport.getWidth() / 2;
        for (int y = (int)(0.5 * viewport.getHeight()); y >= 0; y--) {
            Position pos = dc.getView().computePositionFromScreenPoint(x, y);
            if (pos == null) {
                continue;
            }

            return dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(), 0d);
        }

        return null;
    }

    private void handleLoadFailure(MercatorTextureTile tile, IOException e, int response_code) {
        getLevels().markResourceAbsent(tile);
        firePropertyChange(AVKey.LAYER, null, this);
    }

    private void handleLoadTexture(
            MercatorTextureTile tile, String type, long inputStreamLength, InputStream inputStream) {
        // see end of gov.nasa.worldwind.layers.mercator.BasicMercatorTiledImageLayer.DownloadPostProcessor.run

        BufferedImage image = convertBufferToImage(inputStream);
        if (image != null) {
            image = modifyImage(image);
            if (isTileValid(tile, image, type, inputStreamLength)) {
                image = transform(image, tile.getMercatorSector());
            } else {
                getLevels().markResourceAbsent(tile);
                return;
            }
        }

        if (loadTexture(tile, image)) {
            getLevels().unmarkResourceAbsent(tile);
            firePropertyChange(AVKey.LAYER, null, this);
        } else {
            // Assume that something's wrong with the file and delete it.
            getLevels().markResourceAbsent(tile);
            firePropertyChange(AVKey.LAYER, null, this);
        }
    }

    /**
     * override in subclass to check image tile
     *
     * @return if false is returned, then tile is marked absent
     */
    protected boolean isTileValid(MercatorTextureTile tile, BufferedImage image, String type, long encodedImageSize) {
        return true;
    }

    /** override in subclass to modify image tile */
    private BufferedImage modifyImage(BufferedImage image) {
        return image;
    }

    private BufferedImage convertBufferToImage(InputStream is) {
        try {
            return ImageIO.read(is);
        } catch (IOException e) {
            return null;
        }
    }

    // from gov.nasa.worldwind.layers.mercator.BasicMercatorTiledImageLayer.loadTexture
    private boolean loadTexture(MercatorTextureTile tile, BufferedImage image) {
        TextureData textureData = readTexture(image, isUseMipMaps());
        if (textureData == null) {
            return false;
        }

        tile.setTextureData(textureData);
        if (tile.getLevelNumber() != 0 || !this.isRetainLevelZeroTiles()) {
            this.addTileToCache(tile);
        }

        return true;
    }

    private void addTileToCache(MercatorTextureTile tile) {
        WorldWind.getMemoryCache(MercatorTextureTile.class.getName()).add(tile.getTileKey(), tile);
    }

    private static TextureData readTexture(BufferedImage img, boolean useMipMaps) {
        try {
            if (img != null) {
                return AWTTextureIO.newTextureData(Configuration.getMaxCompatibleGLProfile(), img, useMipMaps);
            }
        } catch (Exception e) {
            Logging.logger().log(java.util.logging.Level.SEVERE, "Issue reading texture from buffered image", e);
        }

        return null;
    }

    @Override
    protected void requestTexture(DrawContext dc, MercatorTextureTile tile) {
        // gov.nasa.worldwind.layers.mercator.BasicMercatorTiledImageLayer.downloadTexture
        URL url;
        try {
            url = tile.getResourceURL();
            if (url == null) {
                return;
            }
        } catch (MalformedURLException e) {
            Logging.logger()
                .log(
                    java.util.logging.Level.SEVERE,
                    Logging.getMessage("layers.TextureLayer.ExceptionCreatingTextureUrl", tile),
                    e);
            return;
        }

        // TODO: apply overriden timeouts like BasicMercatorTiledImage
        addTileToFetchQueue(url, tile);
    }

    @Override
    protected void forceTextureLoad(MercatorTextureTile tile) {
        // don't need to do anything because requestTexture will also be called
    }

    // TODO: make this faster? Don't use setRGB
    /** @see gov.nasa.worldwind.layers.mercator.BasicMercatorTiledImageLayer#transform(BufferedImage, MercatorSector) */
    private BufferedImage transform(BufferedImage image, MercatorSector sector) {
        int type = image.getType();
        if (type == 0) {
            type = BufferedImage.TYPE_INT_RGB;
        }

        BufferedImage trans = new BufferedImage(image.getWidth(), image.getHeight(), type);
        double miny = sector.getMinLatPercent();
        double maxy = sector.getMaxLatPercent();
        for (int y = 0; y < image.getHeight(); y++) {
            double sy = 1.0 - y / (double)(image.getHeight() - 1);
            Angle lat = Angle.fromRadians(sy * sector.getDeltaLatRadians() + sector.getMinLatitude().radians);
            double dy = 1.0 - (MercatorSector.gudermannianInverse(lat) - miny) / (maxy - miny);
            dy = Math.max(0.0, Math.min(1.0, dy));
            int iy = (int)(dy * (image.getHeight() - 1));

            for (int x = 0; x < image.getWidth(); x++) {
                trans.setRGB(x, y, image.getRGB(x, iy));
            }
        }

        return trans;
    }

}
