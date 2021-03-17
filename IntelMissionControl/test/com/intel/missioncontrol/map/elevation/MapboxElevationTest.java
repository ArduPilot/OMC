/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

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
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

@Ignore
public class MapboxElevationTest {

    public final String mapBoxKey  ="TODO insert mapbox key here";

    @Test
    public void decompileMapboxElevations() throws Exception {
        MapboxElevationModel elevationModel = new MapboxElevationModel(null, null, mapBoxKey);
        AVList parameters = elevationModel.createDefaultAVListParams(null, mapBoxKey);
        LevelSet levels = new LevelSet(parameters);

        String tilePath = ClassLoader.getSystemResource("5304_4343.png").getFile().replace("%20", " ");
        File tileFile = new File(tilePath);
        if (!tileFile.exists()) {
            System.out.println("Tile does not exist");
            return;
        }

        Level level = levels.getLevel(13);
        int row = 5304;
        int column = 4344;

        // (46.8000594467873°, 10.8984375°), (46.83013364044739°, 10.9423828125°)

        MercatorSector mercatorSector =
                new MercatorSector(
                        0.294921875, 0.295166015625, Angle.fromDegrees(10.8984375), Angle.fromDegrees(10.9423828125));

        Tile tile = elevationModel.new MercatorElevationTile(mercatorSector, level, row, column, mapBoxKey);

        BufferWrapper result = elevationModel.readElevations(tileFile.toURI().toURL(), tile);

        // validates that Swiss mountains are high
        Assert.assertTrue(result.getDouble(0) > 2400);
    }

    @Test
    public void decompileMapboxElevations2() throws Exception {
        MapboxElevationModel elevationModel = new MapboxElevationModel(null, null, mapBoxKey);
        AVList parameters = elevationModel.createDefaultAVListParams(null, mapBoxKey);
        LevelSet levels = new LevelSet(parameters);

        String tilePath = ClassLoader.getSystemResource("5082_3387.png").getFile().replace("%20", " ");
        File tileFile = new File(tilePath);
        if (!tileFile.exists()) {
            System.out.println("Tile does not exist");
            return;
        }

        Level level = levels.getLevel(13);
        int row = 3387;
        int column = 3109;

        /*
        minLatPercent = 0.24072265625
        maxLatPercent = 0.240966796875
        minLatitude = {Angle@20848} "39.70718665682655°"
        maxLatitude = {Angle@20849} "39.740986355883564°"
        minLongitude = {Angle@20850} "-31.201171875°"
        maxLongitude = {Angle@20851} "-31.1572265625°"
        deltaLat = {Angle@20852} "0.0337996990570133°"
        deltaLon = {Angle@20853} "0.0439453125°"
         */

        MercatorSector mercatorSector =
                new MercatorSector(
                        0.24072265625, 0.240966796875, Angle.fromDegrees(-31.201171875), Angle.fromDegrees(-31.1572265625));

        Tile tile = elevationModel.new MercatorElevationTile(mercatorSector, level, row, column, mapBoxKey);

        BufferWrapper result = elevationModel.readElevations(tileFile.toURI().toURL(), tile);

        Assert.assertTrue(result.getDouble(0) > 0);
    }

    @Test
    public void decompileMapboxElevations3() throws Exception {
        MapboxElevationModel elevationModel = new MapboxElevationModel(null, null, mapBoxKey);
        AVList parameters = elevationModel.createDefaultAVListParams(null, mapBoxKey);
        LevelSet levels = new LevelSet(parameters);

        String tilePath = ClassLoader.getSystemResource("5490_3901.png").getFile().replace("%20", " ");
        File tileFile = new File(tilePath);
        if (!tileFile.exists()) {
            System.out.println("Tile does not exist");
            return;
        }

        Level level = levels.getLevel(13);
        int row = 5490;
        int column = 3901;

        MercatorSector mercatorSector =
                new MercatorSector(
                        0.34033203125, 0.340576171875, Angle.fromDegrees(-8.5693359375), Angle.fromDegrees(-8.525390625));

        Tile tile = elevationModel.new MercatorElevationTile(mercatorSector, level, row, column, mapBoxKey);

        BufferWrapper result = elevationModel.readElevations(tileFile.toURI().toURL(), tile);

        Assert.assertTrue(result.getDouble(0) > 100);
    }

    @Test
    public void decompileMapboxElevations4() throws Exception {
        MapboxElevationModel elevationModel = new MapboxElevationModel(null, null, mapBoxKey);
        AVList parameters = elevationModel.createDefaultAVListParams(null, mapBoxKey);
        LevelSet levels = new LevelSet(parameters);

        String tilePath = ClassLoader.getSystemResource("6434_7543.png").getFile().replace("%20", " ");
        File tileFile = new File(tilePath);
        if (!tileFile.exists()) {
            System.out.println("Tile does not exist");
            return;
        }

        Level level = levels.getLevel(13);
        int row = 6434;
        int column = 7543;

        MercatorSector mercatorSector =
                new MercatorSector(
                        0.57080078125, 0.571044921875, Angle.fromDegrees(151.435546875), Angle.fromDegrees(151.4794921875));

        Tile tile = elevationModel.new MercatorElevationTile(mercatorSector, level, row, column, mapBoxKey);

        BufferWrapper result = elevationModel.readElevations(tileFile.toURI().toURL(), tile);

        Assert.assertTrue(result.getDouble(0) > 0 && result.getDouble(0) < 10);
    }

    @Test
    public void sectorToTileCheck() {

        // compare high level mercator tiles with subtiles tree with creating tile on request by sector (lat/lon)
        MapboxElevationModel mapboxElevationModel = new MapboxElevationModel(null, null, mapBoxKey);
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
        AVList parameters = mapboxElevationModel.createDefaultAVListParams(null, mapBoxKey);
        LevelSet levels = new LevelSet(parameters);
        // 1
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
        System.out.println();

        // 2
        /*
        level 1
        row 1 column 0
        minLatPercent = 0.0
        maxLatPercent = 1.0
        minLatitude = {Angle@17284} "0.0°"
        maxLatitude = {Angle@17285} "85.0511287798066°"
        minLongitude = {Angle@6901} "-180.0°"
        maxLongitude = {Angle@17286} "0.0°"
        deltaLat = {Angle@17287} "85.0511287798066°"
        deltaLon = {Angle@17288} "180.0°"
         */
        tileKey = new TileKey(1, 1, 0, levels.getLevel(1).getCacheName());
        tile = mapboxElevationModel.createTile(tileKey);
        // compare with values in the comment above and with valus in the top level tiles from the mercator elevation
        // model
        System.out.println();

        // 3
        /*
        level 2
        row 2 column 2
        minLatPercent = 0.0
        maxLatPercent = 0.5
        minLatitude = {Angle@17369} "0.0°"
        maxLatitude = {Angle@17370} "66.51326044311186°"
        minLongitude = {Angle@17286} "0.0°"
        maxLongitude = {Angle@17371} "90.0°"
        deltaLat = {Angle@17372} "66.51326044311186°"
        deltaLon = {Angle@17373} "90.0°"
         */
        tileKey = new TileKey(2, 2, 2, levels.getLevel(2).getCacheName());
        tile = mapboxElevationModel.createTile(tileKey);
        // compare with values in the comment above and with valus in the top level tiles from the mercator elevation
        // model
        System.out.println();

        // 4
        /*
        level 3
        row 4 column 4
        minLatPercent = 0.0
        maxLatPercent = 0.25
        minLatitude = {Angle@17444} "0.0°"
        maxLatitude = {Angle@17445} "40.97989806962013°"
        minLongitude = {Angle@17286} "0.0°"
        maxLongitude = {Angle@17446} "45.0°"
        deltaLat = {Angle@17447} "40.97989806962013°"
        deltaLon = {Angle@17448} "45.0°"
         */
        tileKey = new TileKey(3, 4, 4, levels.getLevel(3).getCacheName());
        tile = mapboxElevationModel.createTile(tileKey);
        // compare with values in the comment above and with valus in the top level tiles from the mercator elevation
        // model
        System.out.println();

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
        // compare with values in the comment above and with valus in the top level tiles from the mercator elevation
        // model
        System.out.println();

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
        // compare with values in the comment above and with valus in the top level tiles from the mercator elevation
        // model
        System.out.println();

        /*
         level 13
        row 5302 column 4342
        minLatPercent = 0.29443359375
        maxLatPercent = 0.294677734375
        minLatitude = {Angle@17083} "46.739860599692676°"
        maxLatitude = {Angle@17084} "46.769968433569815°"
        minLongitude = {Angle@17085} "10.810546875°"
        maxLongitude = {Angle@17086} "10.8544921875°"
        deltaLat = {Angle@17087} "0.030107833877139°"
        deltaLon = {Angle@17088} "0.0439453125°"
         */
        tileKey = new TileKey(13, 5302, 4342, levels.getLevel(5).getCacheName());
        tile = mapboxElevationModel.createTile(tileKey);
        // compare with values in the comment above and with valus in the top level tiles from the mercator elevation
        // model
        System.out.println();
    }

    @Test
    public void checkElevationAtLatLon() {
        /*
        Ground truth:

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

        MapboxElevationModel mapboxElevationModel = new MapboxElevationModel(null, null, mapBoxKey);
        AVList parameters = mapboxElevationModel.createDefaultAVListParams(null, mapBoxKey);
        LevelSet levels = new LevelSet(parameters);
        TileKey tileKey = new TileKey(2, 27, 38, levels.getLevel(2).getCacheName());
        // BasicElevationModel.ElevationTile tile = mapboxElevationModel.createTile(tileKey);
        // mapboxElevationModel.requestTile(tileKey);
        double elev = mapboxElevationModel.getElevation(Angle.fromDegrees(48.84375), Angle.fromDegrees(12.796875));
        System.out.println(elev);
        // double elev2 = mapboxElevationModel.lookupElevation(Angle.fromDegrees(48.84375),
        // Angle.fromDegrees(12.796875), tile);

        try {
            // wait while some tiles are downloaded
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("WMS");

        String configSource = new String("EarthElevations2.xml");
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
        params2.setValue("gov.nasa.worldwind.avkey.TileOrigin", new LatLon(Angle.fromDegrees(-90.0000), Angle.fromDegrees(-180.0000)));
        params2.setValue("gov.nasa.worldwind.avkey.ElevationMinKey", -14380.0);
        params2.setValue(
                "gov.nasa.worldwind.avkey.ElevationExtremesFileKey", "config/SRTM30Plus_ExtremeElevations_5.bil");
        params2.setValue("gov.nasa.worldwind.avkey.ServiceURLKey", "http://wms.mavinci.de/26/elev?");
        params2.setValue("gov.nasa.worldwind.avkey.LayerNames", "NASA_SRTM30_900m_Tiled,aster_v2,USGS-NED");
        params2.setValue("gov.nasa.worldwind.avKey.Sector", new Sector(Angle.fromDegrees(-90.0000), Angle.fromDegrees(90.0000),
                Angle.fromDegrees(-180.0000), Angle.fromDegrees(180.0000)));
        params2.setValue("gov.nasa.worldwind.avkey.TileWidthKey", 150);
        params2.setValue("gov.nasa.worldwind.avkey.DataType", "gov.nasa.worldwind.avkey.Int16");
        params2.setValue("gov.nasa.worldwind.avkey.NumEmptyLevels", 0);
        params2.setValue("gov.nasa.worldwind.avkey.MissingDataFlag", -9999.0);
        params2.setValue("gov.nasa.worldwind.avkey.LevelZeroTileDelta", new LatLon(Angle.fromDegrees(20.0000), Angle.fromDegrees(20.0000)));
        params2.setValue("gov.nasa.worldwind.avkey.NumLevels", 12);
        params2.setValue("gov.nasa.worldwind.avkey.DataCacheNameKey", "Earth/EarthElevations2");
        params2.setValue(
                AVKey.TILE_URL_BUILDER,
                new WMSBasicElevationModel.URLBuilder(params2.getStringValue(AVKey.WMS_VERSION), params2));
        WMSBasicElevationModel wmsElevationModel = new WMSBasicElevationModel(doc.getDocumentElement(), params2);
        BasicElevationModel.ElevationTile wmsTile = wmsElevationModel.createTile(tileKey);
        // wmsElevationModel.requestTile(tileKey);
        elev = wmsElevationModel.getElevation(Angle.fromDegrees(48.84375), Angle.fromDegrees(12.796875));
        System.out.println(elev);
        //final URL url = wmsElevationModel.getDataFileStore().findFile(wmsTile.getPath(), false);
        //if (url != null)
        {
            try {
                wmsElevationModel.downloadElevations(wmsTile);
                //Thread.sleep(10000);
                URL url = wmsElevationModel.getDataFileStore().findFile(wmsTile.getPath(), false);
                wmsElevationModel.loadElevations(wmsTile, url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        double elev2 = wmsElevationModel.lookupElevation(Angle.fromDegrees(48.84375), Angle.fromDegrees(12.796875),
                wmsTile);
        System.out.println(elev2);
    }


    @Test
    public void downloadTilesForSector() throws InterruptedException {
        Sector sector = new Sector(Angle.fromDegrees(46.7999594467873), Angle.fromDegrees(46.8001594467873),
                Angle.fromDegrees(10.8983375), Angle.fromDegrees(10.8985375));

        MapboxElevationModel mapboxElevationModel = new MapboxElevationModel(null, null, mapBoxKey);
        LevelSet levels = mapboxElevationModel.getLevels();
        BasicElevationModel.Elevations elevations = mapboxElevationModel.getElevations(sector, levels, 13);
        System.out.println();
        Thread.sleep(200000);
    }
}
