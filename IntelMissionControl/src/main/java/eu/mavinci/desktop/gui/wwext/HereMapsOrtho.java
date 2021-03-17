/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;

public class HereMapsOrtho extends MBasicMercatorTiledImageLayer {
    String mapType;

    private static final String defAppID = "TODOappID";
    private static final String defAppCode = "TODOappCode";

    public static final String defMapType_Satellite = "satellite";
    public static final String defMapType_Hybrid = "hybrid";

    public static final String KEY_mapType = "mavinci.heremaps.mapType";
    public static final String KEY_mapID = "mavinci.heremaps.mapID";
    public static final String KEY_accessToken = "mavinci.heremaps.accessToken";

    public static final long expireTime = 2 * 30 * 24 * 60 * 60 * 1000L; // 2 monts

    public HereMapsOrtho(String mapType) {
        super(makeLevels(mapType));
        this.mapType = mapType;

    }

    private static LevelSet makeLevels(String mapType) {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 256);
        params.setValue(AVKey.TILE_HEIGHT, 256);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/HereMaps/" + HereMapsOrtho.defAppID);
        params.setValue(AVKey.SERVICE, "");
        params.setValue(AVKey.DATASET_NAME, "*");
        params.setValue(AVKey.FORMAT_SUFFIX, ".jpg");
        params.setValue(AVKey.NUM_LEVELS, 20);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(180d), Angle.fromDegrees(360d)));
        params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));
        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder());
        params.setValue(AVKey.EXPIRY_TIME, System.currentTimeMillis() - expireTime); // all files older than this will be redownloaded
        params.setValue(KEY_accessToken, HereMapsOrtho.defAppCode);
        params.setValue(KEY_mapID, HereMapsOrtho.defAppID);
        params.setValue(KEY_mapType,mapType);

        return new LevelSet(params);
    }

    private static class URLBuilder implements TileUrlBuilder {
        private java.util.Random rand = new java.util.Random();
        private int randMax = 4;
        private int randMin = 1;
        private String urlTerminator = "/256/" + "jpg" + "?" + "lg=eng" + "&"
                        + "app_id=" + HereMapsOrtho.defAppID
                        + "&" +  "app_code=" + HereMapsOrtho.defAppCode;
        private String urlProtocol = "https://";
        private String urlBase = ".aerial.maps.cit.api.here.com/maptile/2.1/maptile/newest/";

        public URL getURL(Tile tile, String imageFormat) throws MalformedURLException {
            StringBuffer sb = new StringBuffer();
           sb.append(urlProtocol);
            int randInt = rand.nextInt(randMax) + randMin;
            sb.append(randInt);
            sb.append(urlBase);
            sb.append(tile.getLevel().getParams().getValue(KEY_mapType)+ ".day");
            sb.append("/");
            sb.append(tile.getLevelNumber());
            sb.append("/");
            sb.append(tile.getColumn());
            sb.append("/");
            sb.append((int)Math.pow(2, tile.getLevelNumber()) - tile.getRow() - 1);
            sb.append(urlTerminator);
            return new URL(sb.toString());
        }
    }

}
