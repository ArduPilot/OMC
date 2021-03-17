/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import eu.mavinci.desktop.gui.wwext.MapboxElevationModel;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.terrain.BasicElevationModel;
import gov.nasa.worldwind.terrain.WMSBasicElevationModel;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.util.WWXML;
import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

public class MapboxElevationTest {

    static final int TARGET_LEVEL = 13;

    @Test
    void decompileMapboxElevations() throws Exception {
        MapboxElevationModel elevationModel = new MapboxElevationModel(null, null);
        AVList parameters = elevationModel.createDefaultAVListParams(null);
        LevelSet levels = new LevelSet(parameters);

        String tilePath = ClassLoader.getSystemResource("mapbox/5304_4343.png").getFile().replace("%20", " ");
        File tileFile = new File(tilePath);
        if (!tileFile.exists()) {
            System.out.println("Tile does not exist");
            return;
        }

        Level level = levels.getLevel(TARGET_LEVEL);
        int row = 5304;
        int column = 4344;

        // (46.8000594467873°, 10.8984375°), (46.83013364044739°, 10.9423828125°)

        MercatorSector mercatorSector =
            new MercatorSector(
                0.294921875, 0.295166015625, Angle.fromDegrees(10.8984375), Angle.fromDegrees(10.9423828125));

        Tile tile = elevationModel.new MercatorElevationTile(mercatorSector, level, row, column);

        BufferWrapper result = elevationModel.readElevations(tileFile.toURI().toURL(), tile);

        // validates that Swiss mountains are high
        Assertions.assertTrue(result.getDouble(0) > 2400);
    }

    @Test
    void decompileMapboxElevations3() throws Exception {
        MapboxElevationModel elevationModel = new MapboxElevationModel(null, null);
        AVList parameters = elevationModel.createDefaultAVListParams(null);
        LevelSet levels = new LevelSet(parameters);

        String tilePath = ClassLoader.getSystemResource("mapbox/5490_3901.png").getFile().replace("%20", " ");
        File tileFile = new File(tilePath);
        if (!tileFile.exists()) {
            System.out.println("Tile does not exist");
            return;
        }

        Level level = levels.getLevel(TARGET_LEVEL);
        int row = 5490;
        int column = 3901;

        MercatorSector mercatorSector =
            new MercatorSector(
                0.34033203125, 0.340576171875, Angle.fromDegrees(-8.5693359375), Angle.fromDegrees(-8.525390625));

        Tile tile = elevationModel.new MercatorElevationTile(mercatorSector, level, row, column);

        BufferWrapper result = elevationModel.readElevations(tileFile.toURI().toURL(), tile);

        Assertions.assertTrue(result.getDouble(0) > 100);
    }

    @Test
    void decompileMapboxElevations4() throws Exception {
        MapboxElevationModel elevationModel = new MapboxElevationModel(null, null);
        AVList parameters = elevationModel.createDefaultAVListParams(null);
        LevelSet levels = new LevelSet(parameters);

        String tilePath = ClassLoader.getSystemResource("mapbox/6434_7543.png").getFile().replace("%20", " ");
        File tileFile = new File(tilePath);
        if (!tileFile.exists()) {
            System.out.println("Tile does not exist");
            return;
        }

        Level level = levels.getLevel(TARGET_LEVEL);
        int row = 6434;
        int column = 7543;

        MercatorSector mercatorSector =
            new MercatorSector(
                0.57080078125, 0.571044921875, Angle.fromDegrees(151.435546875), Angle.fromDegrees(151.4794921875));

        Tile tile = elevationModel.new MercatorElevationTile(mercatorSector, level, row, column);

        BufferWrapper result = elevationModel.readElevations(tileFile.toURI().toURL(), tile);

        Assertions.assertTrue(result.getDouble(0) > 0 && result.getDouble(0) < 10);
    }

    /**
     * Validates that tiles created by MapboxElevationModel are equal to the tiles created by the method analogical to
     * one in the MercatorImageryLayer
     */
    @Test
    void sectorToTileCheck() {

        // compare high level mercator tiles with subtiles tree with creating tile on request by sector (lat/lon)
        MapboxElevationModel mapboxElevationModel = new MapboxElevationModel(null, null);
        mapboxElevationModel.createTopLevelTiles();

        List<MapboxElevationModel.MercatorElevationTile> tiles = new ArrayList<>();
        for (MapboxElevationModel.MercatorElevationTile tile : mapboxElevationModel.getTopLevelTiles()) {
            mapboxElevationModel.addTileOrDescendants(tile, tiles);
        }

        Collections.sort(tiles, Comparator.comparingInt(o -> o.getLevel().getLevelNumber()));
        // create a Mapbox Elevation tile - correspondent to the Mapbox Texture Tile - with the same sector and x y z
        // use method getElevation()  or get UnmappedElevation (Lat, Lon) on the tile and compare with the elevations
        // from the current WW WMS elevation model
        // use tiles on several levels
        AVList parameters = mapboxElevationModel.createDefaultAVListParams(null);
        LevelSet levels = new LevelSet(parameters);
        //////////////// ----- level 0
        /*
        level 0
        row 0 col 0
        minLatPercent = -1.0
        maxLatPercent = 1.0
        minLatitude = {Angle@6899} "-85.0511287798066°"
        maxLatitude = {Angle@6900} "85.0511287798066°"
        minLongitude = {Angle@6901} "-180.0°"
        maxLongitude = {Angle@6902} "180.0°"
        deltaLat = {Angle@6903} "170.1022575596132°"
        deltaLon = {Angle@6904} "360.0°"
         */
        TileKey tileKey = new TileKey(0, 0, 0, levels.getLevel(0).getCacheName());
        Tile tile = mapboxElevationModel.createTile(tileKey);
        // compare with values in the comment above and with valus in the top level tiles from the mercator elevation
        // model
        Assertions.assertTrue(
            tile.getSector().getMinLongitude().degrees == (-180.0)
                && tile.getSector().getMinLatitude().degrees == (-85.0511287798066)
                && tile.getSector().getMaxLongitude().degrees == (180.0)
                && tile.getSector().getMaxLatitude().degrees == (85.0511287798066));

        /////////////////////// ------level 3
        /*
        level 3
        row 5 column 5
        minLatPercent = 0.25
        maxLatPercent = 0.5
        minLatitude = {Angle@17112} "40.97989806962013°"
        maxLatitude = {Angle@17113} "66.51326044311186°"
        minLongitude = {Angle@17114} "45.0°"
        maxLongitude = {Angle@17115} "90.0°"
        deltaLat = {Angle@17116} "25.533362373491734°"
        deltaLon = {Angle@17117} "45.0°"
         */
        tileKey = new TileKey(3, 5, 5, levels.getLevel(3).getCacheName());
        tile = mapboxElevationModel.createTile(tileKey);

        MapboxElevationModel.MercatorElevationTile tileX =
            tiles.stream()
                .filter(t -> t.getLevelNumber() == 3 && t.getRow() == 5 && t.getColumn() == 5)
                .collect(Collectors.toList())
                .get(0);

        Assertions.assertTrue(
            tile.getSector().getMinLongitude().degrees == (tileX.getSector().getMinLongitude().degrees)
                && tile.getSector().getMinLatitude().degrees == (tileX.getSector().getMinLatitude().degrees)
                && tile.getSector().getMaxLongitude().degrees == (tileX.getSector().getMaxLongitude().degrees)
                && tile.getSector().getMaxLatitude().degrees == (tileX.getSector().getMaxLatitude().degrees));

        /////////////// ------level 5
        /*
        level 5
        row 20 column 17
        minLatPercent = 0.25
        maxLatPercent = 0.3125
        minLatitude = {Angle@17174} "40.97989806962013°"
        maxLatitude = {Angle@17175} "48.92249926375824°"
        minLongitude = {Angle@17176} "11.25°"
        maxLongitude = {Angle@17177} "22.5°"
        deltaLat = {Angle@17178} "7.942601194138113°"
        deltaLon = {Angle@17179} "11.25°"
         */

        tileKey = new TileKey(5, 20, 17, levels.getLevel(5).getCacheName());
        tile = mapboxElevationModel.createTile(tileKey);

        tileX =
            tiles.stream()
                .filter(t -> t.getLevelNumber() == 5 && t.getRow() == 20 && t.getColumn() == 17)
                .collect(Collectors.toList())
                .get(0);

        Assertions.assertTrue(
            tile.getSector().getMinLongitude().degrees == (tileX.getSector().getMinLongitude().degrees)
                && tile.getSector().getMinLatitude().degrees == (tileX.getSector().getMinLatitude().degrees)
                && tile.getSector().getMaxLongitude().degrees == (tileX.getSector().getMaxLongitude().degrees)
                && tile.getSector().getMaxLatitude().degrees == (tileX.getSector().getMaxLatitude().degrees));

        /////////////// -----level 13
        tileKey = new TileKey(TARGET_LEVEL, 5302, 4342, levels.getLevel(5).getCacheName());
        tile = mapboxElevationModel.createTile(tileKey);

        Assertions.assertTrue(
            tile.getSector().getMinLongitude().degrees == (10.810546875)
                && tile.getSector().getMinLatitude().degrees == (46.739860599692676)
                && tile.getSector().getMaxLongitude().degrees == (10.8544921875)
                && tile.getSector().getMaxLatitude().degrees == (46.769968433569815));
    }

    @Test
    void checkElevationAtLatLonWithDownloading() {
        /*
        Ground truth using WW WMS server:

        vel row column Lat Lon Elevation: 7 875 1222 46.845703125° 10.94970703125° 3027.46777343784
        Level row column Lat Lon Elevation: 7 875 1222 46.845703125° 10.951171875° 3075.6140625003973
        Level row column Lat Lon Elevation: 7 875 1222 46.845703125° 10.95263671875° 3178.9412109377126
        Level row column Lat Lon Elevation: 7 875 1222 46.845703125° 10.9541015625° 3229.9433593753383
        Level row column Lat Lon Elevation: 7 875 1222 46.845703125° 10.95556640625° 3239.963281250528
        Level row column Lat Lon Elevation: 7 875 1222 46.845703125° 10.95703125° 3292.7656250005384
        Level row column Lat Lon Elevation: 7 875 1222 46.845703125° 10.95703125° 3292.7656250005384
        Level row column Lat Lon Elevation: 8 1751 2443 46.808378950492596° 10.892857241909116° 2439.887557535013
        Level row column Lat Lon Elevation: 9 3502 4887 46.80132904612222° 10.905738664934745° 2480.1554877183326
        Level row column Lat Lon Elevation: 11 14008 19548 46.80036555089751° 10.908097839716675° 2441.1658873825195
        Level row column Lat Lon Elevation: 9 3502 4887 46.798219410378174° 10.913198526213108° 2601.4883287844455
        Level row column Lat Lon Elevation: 7 875 1221 46.79585407777738° 10.918171350939895° 2929.253764656295
        Level row column Lat Lon Elevation: 11 14007 19550 46.79217419921993° 10.925438825196254° 2819.7475186677075
        Level row column Lat Lon Elevation: 8 1750 2443 46.78976461194706° 10.929390942944229° 2783.3901426679777
        Level row column Lat Lon Elevation: 8 1750 2443 46.78739893452799° 10.932259015601135° 2823.309865342479
        Level row column Lat Lon Elevation: 8 1750 2443 46.78534003599657° 10.934043233883456° 2878.8439585766273
        Level row column Lat Lon Elevation: 11 14006 19551 46.783149598513376° 10.935954860205564° 2955.01860017119
        Level row column Lat Lon Elevation: 11 14006 19551 46.782423010032325° 10.937241840490667° 2998.0611054704
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.48828125° 1419.1294140624943
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.494140625° 1282.3689257812575
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.5° 1078.7525000000817
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.505859375° 948.180957031283
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.51171875° 996.0175390624382
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.517578125° 1235.0924023437105
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.5234375° 1468.8328906249358
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.529296875° 1578.4440234374688
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.53515625° 1774.669335937408
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.541015625° 1858.8371484374927
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.546875° 1769.5240625000174
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.552734375° 1663.8742773438153
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.55859375° 1671.9385546875974
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.564453125° 1622.545312500085
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.5703125° 1440.1884375000361
        Level row column Lat Lon Elevation: 5 219 309 47.33203125° 13.576171875° 1232.4467187500532
        Level row column Lat Lon Elevation: 2 27 38 48.84375° 12.46875° 356.60972656249885
        Level row column Lat Lon Elevation: 2 27 38 48.84375° 12.515625° 346.437499999999
        Level row column Lat Lon Elevation: 2 27 38 48.84375° 12.5625° 341.9230468749993
        Level row column Lat Lon Elevation: 2 27 38 48.84375° 12.609375° 335.80978515624963
        Level row column Lat Lon Elevation: 2 27 38 48.84375° 12.65625° 325.8136718749996
        Level row column Lat Lon Elevation: 2 27 38 48.84375° 12.703125° 318.51570312499956
        Level row column Lat Lon Elevation: 2 27 38 48.84375° 12.75° 315.3496874999997
        Level row column Lat Lon Elevation: 2 27 38 48.84375° 12.796875° 309.1229882812497
         */

        // compare high level mercator tiles with subtiles tree with creating tile on request by sector (lat/lon)

        System.out.println("Mapbox");

        MapboxElevationModel mapboxElevationModel = new MapboxElevationModel(null, null);
        AVList parameters = mapboxElevationModel.createDefaultAVListParams(null);
        LevelSet levels = new LevelSet(parameters);
        TileKey tileKey = new TileKey(TARGET_LEVEL, 5302, 4342, levels.getLevel(TARGET_LEVEL).getCacheName());
        BasicElevationModel.ElevationTile tile = mapboxElevationModel.createTile(tileKey);
        //
        mapboxElevationModel.retrieveRemoteElevations(
            tile,
            new BasicElevationModel.DownloadPostProcessor(tile, mapboxElevationModel) {
                @Override
                protected ByteBuffer handleSuccessfulRetrieval() {
                    final URL url = this.elevationModel.getDataFileStore().findFile(tile.getPath(), false);
                    try {
                        this.elevationModel.loadElevations((BasicElevationModel.ElevationTile)tile, url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    double elev =
                        mapboxElevationModel.getElevation(
                            Angle.fromDegrees(46.739860599692676), Angle.fromDegrees(10.810546875));
                    Assertions.assertTrue(elev > 2000);
                    return super.handleSuccessfulRetrieval();
                }
            });

        try {
            // wait while some tiles are downloaded
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // TODO make a working test out of it for comparison with legacy elevation model
    public void wmsElevationModelTest() {
        System.out.println("WMS");

        // String configSource = new String("EarthElevations2.xml");
        String configSource =
            new File("..\\WorldWindJava\\src\\main\\resources\\config\\Earth\\EarthElevations2.xml").getAbsolutePath();
        Document doc = WWXML.openDocument(configSource);

        AVList params2 = new AVListImpl();
        params2.setValue("gov.nasa.worldwind.avkey.ElevationMaxKey", 8850.0);
        params2.setValue("gov.nasa.worldwind.avkey.ServiceName", "OGC:WMS");
        params2.setValue("gov.nasa.worldwind.avkey.WMSVersion", "1.3");
        params2.setValue("gov.nasa.worldwind.avkey.FormatSuffixKey", ".bil");
        params2.setValue("gov.nasa.worldwind.avkey.RetrievePropertiesFromService", true);
        params2.setValue("gov.nasa.worldwind.avkey.ImageFormat", "application/bil16");
        params2.setValue("gov.nasa.worldwind.avkey.ByteOrder", ByteOrder.LITTLE_ENDIAN);
        params2.setValue("gov.nasa.worldwind.avkey.DisplayName", "USA 10m, World 30m, Ocean 900m");
        params2.setValue("gov.nasa.worldwind.avkey.ExpiryTime", 1389902400000l);
        params2.setValue("gov.nasa.worldwind.avkey.TileHeightKey", 150);
        params2.setValue("gov.nasa.worldwind.avkey.GetMapURL", "http://wms.mavinci.de/26/elev");

        params2.setValue("gov.nasa.worldwind.avkey.DetailHint", 0.2);
        params2.setValue("gov.nasa.worldwind.avkey.GetCapabilitiesURL", "http://wms.mavinci.de/26/elev");
        params2.setValue("gov.nasa.worldwind.avkey.DatasetNameKey", "NASA_SRTM30_900m_Tiled,aster_v2,USGS-NED");
        params2.setValue(
            "gov.nasa.worldwind.avkey.TileOrigin",
            new LatLon(Angle.fromDegrees(-90.0000), Angle.fromDegrees(-180.0000)));
        params2.setValue("gov.nasa.worldwind.avkey.ElevationMinKey", -14380.0);
        params2.setValue(
            "gov.nasa.worldwind.avkey.ElevationExtremesFileKey", "config/SRTM30Plus_ExtremeElevations_5.bil");
        params2.setValue("gov.nasa.worldwind.avkey.ServiceURLKey", "http://wms.mavinci.de/26/elev?");
        params2.setValue("gov.nasa.worldwind.avkey.LayerNames", "NASA_SRTM30_900m_Tiled,aster_v2,USGS-NED");
        params2.setValue(
            "gov.nasa.worldwind.avKey.Sector",
            new Sector(
                Angle.fromDegrees(-90.0000),
                Angle.fromDegrees(90.0000),
                Angle.fromDegrees(-180.0000),
                Angle.fromDegrees(180.0000)));
        params2.setValue("gov.nasa.worldwind.avkey.TileWidthKey", 150);
        params2.setValue("gov.nasa.worldwind.avkey.DataType", "gov.nasa.worldwind.avkey.Int16");
        params2.setValue("gov.nasa.worldwind.avkey.NumEmptyLevels", 0);
        params2.setValue("gov.nasa.worldwind.avkey.MissingDataFlag", -9999.0);
        params2.setValue(
            "gov.nasa.worldwind.avkey.LevelZeroTileDelta",
            new LatLon(Angle.fromDegrees(20.0000), Angle.fromDegrees(20.0000)));
        params2.setValue("gov.nasa.worldwind.avkey.NumLevels", 12);
        params2.setValue("gov.nasa.worldwind.avkey.DataCacheNameKey", "Earth/EarthElevations2");
        params2.setValue(
            AVKey.TILE_URL_BUILDER,
            new WMSBasicElevationModel.URLBuilder(params2.getStringValue(AVKey.WMS_VERSION), params2));
        WMSBasicElevationModel wmsElevationModel = new WMSBasicElevationModel(doc.getDocumentElement(), params2);
        TileKey tileKey =
            new TileKey(TARGET_LEVEL, 5302, 4342, wmsElevationModel.getLevels().getLevel(TARGET_LEVEL).getCacheName());

        BasicElevationModel.ElevationTile wmsTile = wmsElevationModel.createTile(tileKey);
        // wmsElevationModel.requestTile(tileKey);
        double elev = wmsElevationModel.getElevation(Angle.fromDegrees(48.84375), Angle.fromDegrees(12.796875));
        System.out.println(elev);
        // final URL url = wmsElevationModel.getDataFileStore().findFile(wmsTile.getPath(), false);
        // if (url != null)
        {
            try {
                wmsElevationModel.downloadElevations(wmsTile);
                URL url = wmsElevationModel.getDataFileStore().findFile(wmsTile.getPath(), false);
                wmsElevationModel.loadElevations(wmsTile, url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        double elev2 =
            wmsElevationModel.lookupElevation(Angle.fromDegrees(48.84375), Angle.fromDegrees(12.796875), wmsTile);
        System.out.println(elev2);
    }

    @Test
    void runComparison() {
        // 46.978043 11.249893 2968.09m
        // 46.978074 11.250006 2929.38m

        // 42.831328 -0.000012 1012.0m
        // 42.831328 0.000009 1004.0m

        // test 1//compareTwoNeighbourElevations(46.978043, 11.249893, 46.978074, 11.250006, TARGET_LEVEL,
        // TARGET_LEVEL);
        // test 2// compareTwoNeighbourElevations(42.831328, -0.000012, 42.831328, 0.000009);
        // test 3// Earth/Mapbox/terrain-rgb/13/5310/5310_4350.png --- 2450.83

        // test 4//
        // 46.92025531537452, 11.07421875, 2172.3999999990197
        // 46.92025531537452, 11.07421875 , 2185.4000000000015
        // tile1 "Earth/Mapbox/terrain-rgb/10/663/663_543.png"
        // tile2 "Earth/Mapbox/terrain-rgb/11/1327/1327_1087.png"
        compareTwoNeighbourElevations(46.988986, 11.191773, 46.988986, 11.191773, 10, 11);
    }

    void compareTwoNeighbourElevations(
            double lat1, double lon1, double lat2, double lon2, int targetLevel1, int targetLevel2) {
        MapboxElevationModel elevationModel = new MapboxElevationModel(null, null);
        LevelSet levels = elevationModel.getLevels();
        Sector fakeSector1 =
            new Sector(
                Angle.fromDegrees(lat1), Angle.fromDegrees(lat1), Angle.fromDegrees(lon1), Angle.fromDegrees(lon1));

        Sector fakeSector2 =
            new Sector(
                Angle.fromDegrees(lat2), Angle.fromDegrees(lat2), Angle.fromDegrees(lon2), Angle.fromDegrees(lon2));

        BasicElevationModel.Elevations elevations1 = elevationModel.getElevations(fakeSector1, levels, targetLevel1);
        BasicElevationModel.Elevations elevations2 = elevationModel.getElevations(fakeSector2, levels, targetLevel2);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        elevations1 = elevationModel.getElevations(fakeSector1, levels, targetLevel1);
        elevations2 = elevationModel.getElevations(fakeSector2, levels, targetLevel2);

        if (elevations1.getTiles().size() == 0 || elevations2.getTiles().size() == 0) {
            return;
        }

        // public double getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[]
        // buffer)
        BasicElevationModel.ElevationTile tile1 = new ArrayList<>(elevations1.getTiles()).get(0);
        BasicElevationModel.ElevationTile tile2 = new ArrayList<>(elevations2.getTiles()).get(0);

        // double elevTile1 = elevationModel.lookupElevation(Angle.fromDegrees(lat1), Angle.fromDegrees(lon1), tile1);
        // double elevTile2 = elevationModel.lookupElevation( Angle.fromDegrees(lat2), Angle.fromDegrees(lon2), tile2);

        // System.out.println("Elev1 lvl: " + elevTile1 + " : " + targetLevel1);
        // System.out.println("Elev2 lvl: " + elevTile2 + " : " + targetLevel2);

        ArrayList<LatLon> latlons1 = computeLocations(tile1);
        ArrayList<LatLon> latlons2 = computeLocations(tile2);

        if (targetLevel1 < targetLevel2) {
            latlons1 = latlons2;
        } else if (targetLevel2 < targetLevel1) {
            latlons2 = latlons1;
        }

        double[] elevationsArr1 = new double[latlons1.size()];
        elevationModel.getElevations(
            tile1.getSector(), latlons1, levels.getLevel(targetLevel1).getTexelSize(), elevationsArr1);

        double[] elevationsArr2 = new double[latlons2.size()];
        elevationModel.getElevations(
            tile2.getSector(), latlons2, levels.getLevel(targetLevel2).getTexelSize(), elevationsArr2);

        System.out.println("Beginning. First tile");
        for (int i = 0; i < latlons1.size(); i++) {
            System.out.println(
                latlons1.get(i).latitude.degrees + ", " + latlons1.get(i).longitude.degrees + ", " + elevationsArr1[i]);
        }

        System.out.println("Second tile");
        System.out.println("\n\n\n");

        for (int i = 0; i < latlons2.size(); i++) {
            System.out.println(
                latlons2.get(i).latitude.degrees
                    + ", "
                    + latlons2.get(i).longitude.degrees
                    + " , "
                    + elevationsArr2[i]);
        }
    }

    protected ArrayList<LatLon> computeLocations(BasicElevationModel.ElevationTile tile) {
        int density = 20;
        int numVertices = (density + 3) * (density + 3);

        Angle latMax = tile.getSector().getMaxLatitude();
        Angle dLat = tile.getSector().getDeltaLat().divide(density);
        Angle lat = tile.getSector().getMinLatitude();

        Angle lonMin = tile.getSector().getMinLongitude();
        Angle lonMax = tile.getSector().getMaxLongitude();
        Angle dLon = tile.getSector().getDeltaLon().divide(density);

        ArrayList<LatLon> latlons = new ArrayList<>(numVertices);
        for (int j = 0; j <= density + 2; j++) {
            Angle lon = lonMin;
            for (int i = 0; i <= density + 2; i++) {
                latlons.add(new LatLon(lat, lon));

                if (i > density) lon = lonMax;
                else if (i != 0) lon = lon.add(dLon);

                if (lon.degrees < -180) lon = Angle.NEG180;
                else if (lon.degrees > 180) lon = Angle.POS180;
            }

            if (j > density) lat = latMax;
            else if (j != 0) lat = lat.add(dLat);
        }

        return latlons;
    }
}
