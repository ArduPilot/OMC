/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import com.intel.missioncontrol.collections.RingQueue;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.map.worldwind.WWLayerSettings;
import eu.mavinci.desktop.gui.wwext.WWFactory;
import eu.mavinci.desktop.helper.MathHelper;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.terrain.BasicElevationModel;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.util.TileUrlBuilder;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.imageio.ImageIO;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class MapboxElevationModel extends BasicElevationModel {
    private static final int EXTREMES_MAXIMUM_LEVEL = 11;
    private static final int NUM_CHANNELS = 4;
    private static final int CHANNEL_BYTE_SIZE = 1;
    private static boolean useLargeTiles = true;
    private ArrayList<MercatorElevationTile> topLevels;
    private Queue<TileKey> requested = new RingQueue(100);
    private static final String CACHE_NAME = "Earth/Mapbox/terrain-rgb";

    private static final int expireTimeSec = 27 * 24 * 60 * 60; // 27 days (1 day less then contract to be safe)
    private static final long expireTimeMSec = expireTimeSec * 1000L;

    private static final int redownloadTimeSec = 14 * 24 * 60 * 60; // 14 days (rough 1/2 of expirey time)
    private static final long redownloadTimeMSec = redownloadTimeSec * 1000L;

    private static boolean cacheDropDone;

    private final String mapBoxKey;

    public MapboxElevationModel(Element domElement, AVList params, String mapBoxKey) {
        super(createDefaultAVListParams(domElement, mapBoxKey));
        this.mapBoxKey = mapBoxKey;
        setExpiryTime(System.currentTimeMillis() - redownloadTimeMSec);
        // Mapbox signals oceans with -1m  ... even everyone knows that ocean levels are rapidly rising ;-(
        // BUT: -1 also occurs normally... which is misguiding IMC at the moment to make holes into the model by putting
        // the missing data replacement which is then at the moment NOT shifted by EGM model, so we end up with holes in
        // terrain next to the coasts.... so lets ignore -1 for now!
        setMissingDataSignal(-10000);
        setMissingDataReplacement(0);

        // without this, I would be afraid that temporary values end up in the cache (e.g. while not all nessesary tiles
        // are loaded)
        setExtremesCachingEnabled(false);
        cacheDropDone = false;
    }

    /**
     * Default AVList parameters for the elevation model For comparison with the legacy ones see
     * MapboxElevationTest.checkElevationAtLatLon
     *
     * @param domElement
     * @return
     */
    public static AVList createDefaultAVListParams(Element domElement, String mapBoxKey) {
        AVList params = new AVListImpl();

        if (domElement != null) {
            DataConfigurationUtils.getWMSLayerConfigParams(domElement, params);
            BasicElevationModel.getBasicElevationModelConfigParams(domElement, params);
        }

        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder(mapBoxKey));
        if (useLargeTiles) {
            params.setValue(AVKey.TILE_WIDTH, 512);
            params.setValue(AVKey.TILE_HEIGHT, 512);
            params.setValue(AVKey.NUM_LEVELS, 14);
        } else {
            params.setValue(AVKey.TILE_WIDTH, 256);
            params.setValue(AVKey.TILE_HEIGHT, 256);
            params.setValue(AVKey.NUM_LEVELS, 15);
        }

        params.setValue(AVKey.DATA_CACHE_NAME, CACHE_NAME);
        params.setValue(AVKey.SERVICE, "https://api.mapbox.com/v4/mapbox.terrain-rgb");
        params.setValue(AVKey.DATASET_NAME, "*");
        params.setValue(AVKey.FORMAT_SUFFIX, ".png");
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(180d), Angle.fromDegrees(360d)));
        params.setValue(AVKey.SECTOR, new MercatorSector(-1, 1, Angle.NEG180, Angle.POS180));
        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder(mapBoxKey));
        params.setValue(AVKey.DATA_TYPE, AVKey.FLOAT64);
        params.setValue(AVKey.BYTE_ORDER, AVKey.BIG_ENDIAN);

        params.setValue(AVKey.DETAIL_HINT, 0.2);
        params.setValue(AVKey.GET_CAPABILITIES_URL, "");

        // this would otherwise trigger a loading of a SRTM extreme elevation lookup
        params.setValue(AVKey.ELEVATION_EXTREMES_FILE, null);

        return params;
    }

    /**
     * Only used for the test!!!!!
     */
    public void createTopLevelTiles() {
        MercatorSector sector = (MercatorSector) this.levels.getSector();

        Level level = levels.getFirstLevel();
        Angle dLat = level.getTileDelta().getLatitude();
        Angle dLon = level.getTileDelta().getLongitude();

        Angle latOrigin = this.levels.getTileOrigin().getLatitude();
        Angle lonOrigin = this.levels.getTileOrigin().getLongitude();

        // Determine the row and column offset from the common World Wind global tiling origin.
        // TODO FIXME: the row computation smells... actually it should be MercatorSecotor... but since this is only the
        // top level tiles, it might work this way accidentially
        int firstRow = Tile.computeRow(dLat, sector.getMinLatitude(), latOrigin);
        int firstCol = Tile.computeColumn(dLon, sector.getMinLongitude(), lonOrigin);
        int lastRow = Tile.computeRow(dLat, sector.getMaxLatitude(), latOrigin);
        int lastCol = Tile.computeColumn(dLon, sector.getMaxLongitude(), lonOrigin);

        int nLatTiles = lastRow - firstRow + 1;
        int nLonTiles = lastCol - firstCol + 1;

        this.topLevels = new ArrayList<>(nLatTiles * nLonTiles);

        // Angle p1 = Tile.computeRowLatitude(firstRow, dLat);
        double deltaLat = dLat.degrees / 90;
        double d1 = -1.0 + deltaLat * firstRow;
        for (int row = firstRow; row <= lastRow; row++) {
            // Angle p2;
            // p2 = p1.add(dLat);
            double d2 = d1 + deltaLat;

            Angle t1 = Tile.computeColumnLongitude(firstCol, dLon, lonOrigin);
            for (int col = firstCol; col <= lastCol; col++) {
                Angle t2;
                t2 = t1.add(dLon);

                this.topLevels.add(new MercatorElevationTile(new MercatorSector(d1, d2, t1, t2), level, row, col,mapBoxKey));
                t1 = t2;
            }

            d1 = d2;
        }
    }

    /**
     * Creating MercatorElevationTile tiles with Mercator sector inside
     */
    // Create the tile corresponding to a specified key.
    public ElevationTile createTile(TileKey key) {
        Level level = this.levels.getLevel(key.getLevelNumber());

        // Compute the tile's SW lat/lon based on its row/col in the level's data set.
        double dLat = Math.pow(2, 1 - key.getLevelNumber());
        Angle dLon = level.getTileDelta().getLongitude();
        // Angle latOrigin = this.levels.getTileOrigin().getLatitude();
        Angle lonOrigin = this.levels.getTileOrigin().getLongitude();

        int row = key.getRow();
        int col = key.getColumn();
        // Angle minLatitude = ElevationTile.computeRowLatitude(row, dLat, latOrigin);

        double d1 = -1.0 + dLat * row;
        double d2 = d1 + dLat;

        Angle t1 = Tile.computeColumnLongitude(col, dLon, lonOrigin);
        Angle t2 = t1.add(dLon);

        return new MercatorElevationTile(new MercatorSector(d1, d2, t1, t2), level, row, col, mapBoxKey);
    }

    public List<MercatorElevationTile> getTopLevelTiles() {
        return topLevels;
    }

    public void addTileOrDescendants(MercatorElevationTile tile, List<MercatorElevationTile> tiles) {
        if (tile.getLevel().getLevelNumber() >= 5) {
            return;
        }

        MercatorElevationTile[] subTiles = tile.createSubTiles(this.levels.getLevel(tile.getLevelNumber() + 1));
        for (MercatorElevationTile child : subTiles) {
            this.addTileOrDescendants(child, tiles);
            tiles.add(child);
        }
    }

    public class MercatorElevationTile extends ElevationTile {
        private MercatorSector mercatorSector;

        public MercatorSector getMercatorSector() {
            return mercatorSector;
        }

        String mapBoxKey;

        public MercatorElevationTile(MercatorSector mercatorSector, Level level, int row, int column, String mapBoxKey) {
            super(mercatorSector, level, row, column); // row and column aren't used and need to signal that
            this.mercatorSector = mercatorSector;
            this.mapBoxKey = mapBoxKey;
        }

        @Override
        public URL getResourceURL() {
            return tileToUrl(this, mapBoxKey);
        }

        public MercatorElevationTile[] createSubTiles(Level nextLevel) {
            if (nextLevel == null) {
                String msg = Logging.getMessage("nullValue.LevelIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            double d0 = this.getMercatorSector().getMinLatPercent();
            double d2 = this.getMercatorSector().getMaxLatPercent();
            double d1 = d0 + (d2 - d0) / 2.0;

            Angle t0 = this.getSector().getMinLongitude();
            Angle t2 = this.getSector().getMaxLongitude();
            Angle t1 = Angle.midAngle(t0, t2);

            String nextLevelCacheName = nextLevel.getCacheName();
            int nextLevelNum = nextLevel.getLevelNumber();
            int row = this.getRow();
            int col = this.getColumn();

            MercatorElevationTile[] subTiles = new MercatorElevationTile[4];

            TileKey key = new TileKey(nextLevelNum, 2 * row, 2 * col, nextLevelCacheName);
            MercatorElevationTile subTile = (MercatorElevationTile) MapboxElevationModel.this.getTileFromMemory(key);
            if (subTile != null) subTiles[0] = subTile;
            else
                subTiles[0] =
                        new MercatorElevationTile(new MercatorSector(d0, d1, t0, t1), nextLevel, 2 * row, 2 * col, mapBoxKey);

            key = new TileKey(nextLevelNum, 2 * row, 2 * col + 1, nextLevelCacheName);
            subTile = (MercatorElevationTile) MapboxElevationModel.this.getTileFromMemory(key);
            if (subTile != null) subTiles[1] = subTile;
            else
                subTiles[1] =
                        new MercatorElevationTile(new MercatorSector(d0, d1, t1, t2), nextLevel, 2 * row, 2 * col + 1,mapBoxKey);

            key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col, nextLevelCacheName);
            subTile = (MercatorElevationTile) MapboxElevationModel.this.getTileFromMemory(key);
            if (subTile != null) subTiles[2] = subTile;
            else
                subTiles[2] =
                        new MercatorElevationTile(new MercatorSector(d1, d2, t0, t1), nextLevel, 2 * row + 1, 2 * col, mapBoxKey);

            key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col + 1, nextLevelCacheName);
            subTile = (MercatorElevationTile) MapboxElevationModel.this.getTileFromMemory(key);
            if (subTile != null) subTiles[3] = subTile;
            else
                subTiles[3] =
                        new MercatorElevationTile(new MercatorSector(d1, d2, t1, t2), nextLevel, 2 * row + 1, 2 * col + 1, mapBoxKey);

            return subTiles;
        }

        @Override
        public int computeElevationIndex(LatLon location) {
            // has to be reimplemented in order to support mercator projection properly
            final int tileHeight = this.getHeight();
            final int tileWidth = this.getWidth();

            final double sectorDeltaLat = mercatorSector.getDeltaLatPercent();
            final double sectorDeltaLon = mercatorSector.getDeltaLon().radians;

            final double dLat =
                    mercatorSector.getMaxLatPercent() - MercatorSector.gudermannianInverse(location.getLatitude());
            final double dLon = location.getLongitude().radians - mercatorSector.getMinLongitude().radians;

            final double sLat = dLat / sectorDeltaLat;
            final double sLon = dLon / sectorDeltaLon;

            int j = (int) ((tileHeight - 1) * sLat);
            int i = (int) ((tileWidth - 1) * sLon);

            return j * tileWidth + i;
        }
    }

    protected static class URLBuilder implements TileUrlBuilder {
        URLBuilder(String mapBoxKey) {
            mapBoxKey = mapBoxKey;
        }

        String mapBoxKey;

        public URL getURL(Tile tile, String altImageFormat) {
            return MapboxElevationModel.tileToUrl(tile, mapBoxKey);
        }
    }

    // copied from MapboxLayer
    private static URL tileToUrl(Tile tile, String mapBoxKey) {
        final long z = tile.getLevelNumber();
        final long x = tile.getColumn();
        final long y = (1L << z) - tile.getRow() - 1L;
        String link =
                "https://api.mapbox.com/v4/mapbox.terrain-rgb/"
                        + z
                        + "/"
                        + x
                        + "/"
                        + y
                        + "@2x"
                        + ".pngraw?access_token="
                        + mapBoxKey;

        try {
            return new URL(link);
        } catch (MalformedURLException e) {
            LoggerFactory.getLogger(MapboxElevationModel.class).error(e.getMessage(), e);
        }

        return null;
    }

    @Override
    public double lookupElevation(Angle latitude, Angle longitude, final ElevationTile tile) {
        if (tile == null || !tile.getSector().contains(latitude, longitude)) {
            return getMissingDataReplacement();
        }

        BufferWrapper elevations = tile.getElevations();
        if (elevations == null) {
            return getMissingDataReplacement();
        }

        MercatorSector sector = (MercatorSector) tile.getSector();
        final int tileHeight = tile.getHeight();
        final int tileWidth = tile.getWidth();
        final double sectorDeltaLat = sector.getDeltaLatPercent();
        final double sectorDeltaLon = sector.getDeltaLon().radians;
        final double dLat = sector.getMaxLatPercent() - MercatorSector.gudermannianInverse(latitude);
        final double dLon = longitude.radians - sector.getMinLongitude().radians;
        final double sLat = dLat / sectorDeltaLat;
        final double sLon = dLon / sectorDeltaLon;

        int j = (int) ((tileHeight - 1) * sLat);
        int i = (int) ((tileWidth - 1) * sLon);
        int k = j * tileWidth + i;

        double eLeft = elevations.getDouble(k);
        double eRight = i < (tileWidth - 1) ? elevations.getDouble(k + 1) : eLeft;

        if (this.getMissingDataSignal() == eLeft || this.getMissingDataSignal() == eRight) {
            return this.getMissingDataSignal();
        }

        double dw = sectorDeltaLon / (tileWidth - 1);
        double dh = sectorDeltaLat / (tileHeight - 1);
        double ssLon = (dLon - i * dw) / dw;
        double ssLat = (dLat - j * dh) / dh;

        double eTop = eLeft + ssLon * (eRight - eLeft);

        if (j < tileHeight - 1 && i < tileWidth - 1) {
            eLeft = elevations.getDouble(k + tileWidth);
            eRight = elevations.getDouble(k + tileWidth + 1);

            if (this.getMissingDataSignal() == eLeft || this.getMissingDataSignal() == eRight) {
                return this.getMissingDataSignal();
            }
        }

        double eBot = eLeft + ssLon * (eRight - eLeft);
        double ret = eTop + ssLat * (eBot - eTop);
        return ret;
    }

    @Override
    protected double[] computeExtremeElevations(Sector sector) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        // without the following code we would always download for EACH sampled sector ALL tiles globally...
        // including the 30 days cache dropping this would be quite some traffic
        double ratioCovered = 2 * Math.PI * Math.PI / (sector.getDeltaLatRadians() * sector.getDeltaLonRadians());
        int levelEst =
                (int)
                        Math.round(
                                Math.log(ratioCovered) / Math.log(4)); // in everey level 4 times less area is covered by a tile
        int maxLevel = MathHelper.intoRange(levelEst, 0, EXTREMES_MAXIMUM_LEVEL);

        Elevations elevations = getElevations(sector, getLevels(), maxLevel);
        if (elevations != null) {
            // this code samples TOO MUCH, since the tiles checked here are covering AT LEAST the sector, typically more
            // additionally this code is way faster then sampling all tiles again, since the extreme values for all
            // tiles exist already in memory
            double[] tilesExtremes = elevations.getTileExtremes();

            // this is way too slow:
            // double[] tilesExtremes = elevations.getExtremes(sector);
            min = tilesExtremes[0];
            max = tilesExtremes[1];
        }
        // Set to model's limits if for some reason a limit wasn't determined
        if (min == Double.MAX_VALUE) min = this.getMinElevation();
        if (max == -Double.MAX_VALUE) max = this.getMaxElevation();

        return new double[]{min, max};
    }

    public double[] getExtremeElevations(Sector sector) {
        // cache disabling is ALSO disabling entire computation....
        return this.computeExtremeElevations(sector);
    }

    public double[] getExtremeElevations(Angle latitude, Angle longitude) {
        return computeExtremeElevations(new Sector(latitude, latitude, longitude, longitude));
    }

    @Override
    public double getLocalDataAvailability(Sector requestedSector, Double targetResolution) {
        if (requestedSector == null) {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute intersection of the requested sector and the sector covered by the elevation model.
        LevelSet levelSet = this.getLevels();
        Sector sector = requestedSector.intersection(levelSet.getSector());

        // If there is no intersection there is no data to retrieve
        if (sector == null) return 1d;

        Level targetLevel =
                targetResolution != null ? this.getTargetLevel(sector, targetResolution) : levelSet.getLastLevel();

        // Count all the tiles intersecting the input sector.
        long numLocalTiles = 0;
        long numMissingTiles = 0;
        LatLon delta = targetLevel.getTileDelta();
        LatLon origin = levelSet.getTileOrigin();
        final int nwRow = MercatorSector.computeRow(targetLevel.getLevelNumber(), sector.getMaxLatitude());
        final int nwCol = Tile.computeColumn(delta.getLongitude(), sector.getMinLongitude(), origin.getLongitude());
        final int seRow = MercatorSector.computeRow(targetLevel.getLevelNumber(), sector.getMinLatitude());
        final int seCol = Tile.computeColumn(delta.getLongitude(), sector.getMaxLongitude(), origin.getLongitude());

        for (int row = nwRow; row >= seRow; row--) {
            for (int col = nwCol; col <= seCol; col++) {
                TileKey key = new TileKey(targetLevel.getLevelNumber(), row, col, targetLevel.getCacheName());
                Sector tileSector = levelSet.computeSectorForKey(key);
                Tile tile = new Tile(tileSector, targetLevel, row, col);
                if (!this.isTileLocalOrAbsent(tile)) ++numMissingTiles;
                else ++numLocalTiles;
            }
        }

        return numLocalTiles > 0 ? numLocalTiles / (double) (numLocalTiles + numMissingTiles) : 0d;
    }

    /**
     * The search logic was copied from MercatorTiledImageLayer
     *
     * @param requestedSector
     * @param levelSet
     * @param targetLevelNumber - the target resolution level which depends on the camera tilt angle and height
     * @return
     */
    protected Elevations getElevations(Sector requestedSector, LevelSet levelSet, int targetLevelNumber) {
        // Compute the intersection of the requested sector with the LevelSet's sector.
        // The intersection will be used to determine which Tiles in the LevelSet are in the requested sector.
        Sector sector = requestedSector.intersection(levelSet.getSector());

        Level targetLevel = levelSet.getLevel(targetLevelNumber);
        LatLon delta = targetLevel.getTileDelta();
        LatLon origin = levelSet.getTileOrigin();
        final int nwRow = MercatorSector.computeRow(targetLevel.getLevelNumber(), sector.getMaxLatitude());
        final int nwCol = Tile.computeColumn(delta.getLongitude(), sector.getMinLongitude(), origin.getLongitude());
        final int seRow = MercatorSector.computeRow(targetLevel.getLevelNumber(), sector.getMinLatitude());
        final int seCol = Tile.computeColumn(delta.getLongitude(), sector.getMaxLongitude(), origin.getLongitude());

        java.util.TreeSet<ElevationTile> tiles =
                new java.util.TreeSet<>(
                        (t1, t2) -> {
                            if (t2.getLevelNumber() == t1.getLevelNumber()
                                    && t2.getRow() == t1.getRow()
                                    && t2.getColumn() == t1.getColumn()) return 0;

                            // Higher-res levels compare lower than lower-res
                            return t1.getLevelNumber() > t2.getLevelNumber() ? -1 : 1;
                        });
        ArrayList<TileKey> requested = new ArrayList<>();

        boolean missingTargetTiles = false;
        boolean missingLevelZeroTiles = false;
        for (int row = seRow; row <= nwRow; row++) {
            for (int col = nwCol; col <= seCol; col++) {
                TileKey key = new TileKey(targetLevel.getLevelNumber(), row, col, targetLevel.getCacheName());
                ElevationTile tile = this.getTileFromMemory(key);
                if (tile != null) {
                    tiles.add(tile);
                    continue;
                }

                missingTargetTiles = true;
                this.requestTile(key);

                // Determine the fallback to use. Simultaneously determine a fallback to request that is
                // the next resolution higher than the fallback chosen, if any. This will progressively
                // refine the display until the desired resolution tile arrives.
                TileKey fallbackToRequest = null;
                TileKey fallbackKey;
                int fallbackRow = row;
                int fallbackCol = col;
                for (int fallbackLevelNum = key.getLevelNumber() - 1; fallbackLevelNum >= 0; fallbackLevelNum--) {
                    fallbackRow /= 2;
                    fallbackCol /= 2;
                    fallbackKey =
                            new TileKey(
                                    fallbackLevelNum,
                                    fallbackRow,
                                    fallbackCol,
                                    this.levels.getLevel(fallbackLevelNum).getCacheName());

                    tile = this.getTileFromMemory(fallbackKey);
                    if (tile != null) {
                        if (!tiles.contains(tile)) {
                            tiles.add(tile);
                        }

                        break;
                    } else {
                        if (fallbackLevelNum == 0) missingLevelZeroTiles = true;
                        fallbackToRequest = fallbackKey; // keep track of lowest level to request
                    }
                }

                if (fallbackToRequest != null) {
                    if (!requested.contains(fallbackToRequest)) {
                        this.requestTile(fallbackToRequest);
                        requested.add(fallbackToRequest); // keep track to avoid overhead of duplicte requests
                    }
                }
            }
        }

        Elevations elevations;

        if (missingLevelZeroTiles || tiles.isEmpty()) {
            // Double.MAX_VALUE is a signal for no in-memory tile for a given region of the sector.
            elevations = new Elevations(this, Double.MAX_VALUE);
            elevations.setTiles(tiles);
        } else if (missingTargetTiles) {
            // Use the level of the the lowest resolution found to denote the resolution of this elevation set.
            // The list of tiles is sorted first by level, so use the level of the list's last entry.
            elevations = new Elevations(this, tiles.last().getLevel().getTexelSize());
            elevations.setTiles(tiles);
        } else {
            elevations = new Elevations(this, tiles.last().getLevel().getTexelSize());

            // Compute the elevation extremes now that the sector is fully resolved
            if (tiles.size() > 0) {
                elevations.setTiles(tiles);
                double[] extremes = elevations.getTileExtremes();
                if (extremes != null && this.isExtremesCachingEnabled()) {
                    // Cache the newly computed extremes if they're different from the currently cached ones.
                    double[] currentExtremes = (double[]) this.getExtremesLookupCache().getObject(requestedSector);
                    if (currentExtremes == null
                            || currentExtremes[0] != extremes[0]
                            || currentExtremes[1] != extremes[1])
                        this.getExtremesLookupCache().add(requestedSector, extremes, 64);
                }
            }
        }

        // Check tile expiration. Memory-cached tiles are checked for expiration only when an explicit, non-zero expiry
        // time has been set for the elevation model. If none has been set, the expiry times of the model's individual
        // levels are used, but only for tiles in the local file cache, not tiles in memory. This is to avoid incurring
        // the overhead of checking expiration of in-memory tiles, a very rarely used feature.
        if (this.getExpiryTime() > 0 && this.getExpiryTime() < System.currentTimeMillis())
            this.checkElevationExpiration(tiles);

        if (tiles.size() > 0) {
            this.checkElevationExpiration();
        }

        return elevations;
    }

    public double getUnmappedElevation(Angle latitude, Angle longitude) {
        if (latitude == null || longitude == null) {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.contains(latitude, longitude)) return this.getMissingDataSignal();

        Level lastLevel = this.levels.getLastLevel();

        LatLon delta = lastLevel.getTileDelta();
        LatLon origin = this.levels.getTileOrigin();
        final int row = MercatorSector.computeRow(lastLevel.getLevelNumber(), latitude);
        final int col = Tile.computeColumn(delta.getLongitude(), longitude, origin.getLongitude());

        final TileKey tileKey = new TileKey(lastLevel.getLevelNumber(), row, col, lastLevel.getCacheName());
        ElevationTile tile = this.getTileFromMemory(tileKey);
        if (tile == null) {
            int fallbackRow = tileKey.getRow();
            int fallbackCol = tileKey.getColumn();
            for (int fallbackLevelNum = tileKey.getLevelNumber() - 1; fallbackLevelNum >= 0; fallbackLevelNum--) {
                fallbackRow /= 2;
                fallbackCol /= 2;

                if (this.levels.getLevel(fallbackLevelNum).isEmpty()) {
                    // everything lower res is empty
                    // return this.getExtremeElevations(latitude, longitude)[0];
                    break; // extreme elevation wont work for mapbox as fallback
                }

                TileKey fallbackKey =
                        new TileKey(
                                fallbackLevelNum,
                                fallbackRow,
                                fallbackCol,
                                this.levels.getLevel(fallbackLevelNum).getCacheName());
                tile = this.getTileFromMemory(fallbackKey);
                if (tile != null) break;
            }
        }

        // the following code wont work for mapbox, since the exteme elevation is computed form the same tile data, so
        // it will fail as well!
        /*
        if (tile == null && !this.levels.getFirstLevel().isEmpty()) {
            // Request the level-zero tile since it's not in memory
            Level firstLevel = this.levels.getFirstLevel();
            final TileKey zeroKey = new TileKey(latitude, longitude, this.levels, firstLevel.getLevelNumber());
            this.requestTile(zeroKey);
            // Return the best we know about the location's elevation
            return this.getExtremeElevations(latitude, longitude)[0];
        }*/

        // Check tile expiration. Memory-cached tiles are checked for expiration only when an explicit, non-zero expiry
        // time has been set for the elevation model. If none has been set, the expiry times of the model's individual
        // levels are used, but only for tiles in the local file cache, not tiles in memory. This is to avoid incurring
        // the overhead of checking expiration of in-memory tiles, a very rarely used feature.
        if (this.getExpiryTime() > 0 && this.getExpiryTime() < System.currentTimeMillis()) {
            // Normally getUnmappedElevations() does not request elevation tiles, except for first level tiles. However
            // if the tile is already in memory but has expired, we must issue a request to replace the tile data. This
            // will not fetch new tiles into the cache, but rather will force a refresh of the expired tile's resources
            // in the file cache and the memory cache.
            if (tile != null) this.checkElevationExpiration(tile);
        }

        if (tile != null) {
            checkElevationExpiration();
        }

        // The containing tile is non-null, so look up the elevation and return.
        return this.lookupElevation(latitude, longitude, tile);
    }

    Object mutexCacheCheck = new Object();

    private void checkElevationExpiration() {
        synchronized (mutexCacheCheck) {
            if (!cacheDropDone) {
                cacheDropDone = true;
                Dispatcher.post(
                        () ->
                                WWFactory.dropDataCache(
                                        CACHE_NAME,
                                        expireTimeSec,
                                        null)); // eather put here a fully authorized URL, or leave it empty to get it working
            }
        }
    }

    /**
     * Reads images from the file and converts RGB values to the height values according to the formula from here
     * getHeightInPixel()
     *
     * @param url
     * @return
     * @throws Exception
     */
    @Override
    public BufferWrapper readElevations(URL url, Tile tile) throws Exception {
        try (InputStream tmpStream = url.openStream();
             InputStream stream = new BufferedInputStream(tmpStream)) {
            BufferedImage image = ImageIO.read(stream);
            WritableRaster raster = image.getRaster();
            DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();

            byte[] data = dataBuffer.getData();
            int step = NUM_CHANNELS * CHANNEL_BYTE_SIZE;

            // each color was 32 byte - now it is a 64 bit double - -amount of pixels multiplied 8 bytes per pixel
            ByteBuffer elevationsWrapper = ByteBuffer.allocate(data.length * 2);

            for (int p = 0; p < data.length; p += step) {
                // BufferedImage.TYPE_4BYTE_ABGR
                int blue = data[p + 1] & 0xff;
                int green = data[p + 2] & 0xff;
                int red = data[p + 3] & 0xff;
                double h = getHeightInPixel(red, green, blue);
                elevationsWrapper.putDouble(h);
            }
            // necessary to reset position otherwise BufferWrapper.wrap messes everything up
            elevationsWrapper.position(0);

            // Setup parameters to instruct BufferWrapper on how to interpret the ByteBuffer.
            AVList bufferParams = createDefaultAVListParams(null, mapBoxKey);
            BufferWrapper bufferWrapper = BufferWrapper.wrap(elevationsWrapper, bufferParams);
            return bufferWrapper;
        }
    }

    private double getHeightInPixel(int R, int G, int B) {
        return -10000 + ((R * 256 * 256 + G * 256 + B) * 0.1);
    }

    @Override
    protected boolean needsConfigurationFile(
            FileStore fileStore, String fileName, AVList params, boolean removeIfExpired) {
        return false;
    }

    @Override
    public void requestTile(TileKey key) {
        // if (!requested.contains(key) || key.getLevelNumber() == 0) {
        if (WorldWind.isOfflineMode()) return;
        if (WorldWind.getTaskService().isFull()) return;
        if (this.getLevels().isResourceAbsent(key)) return;

        requested.add(key);
        RequestTask request = new RequestTask(key, this);
        WorldWind.getTaskService().addTask(request);
        // }
    }
}
