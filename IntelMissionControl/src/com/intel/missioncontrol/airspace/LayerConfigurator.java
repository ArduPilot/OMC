/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspace;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.LevelSet;

public class LayerConfigurator {
    private LayerConfigurator() {}
    //
    //    public static class AirSpaceLayerHolder {
    //        public AirMapTileLoader2 tileLoader;
    //        public TiledRenderableLayer layer;
    //        public AirMapConfig2 airMapConfig;
    //    }

    //    @Deprecated
    //    public static class Builder {
    //        AirSpaceLayerHolder holder = new AirSpaceLayerHolder();
    //        LevelSet levelSet;
    //
    //        public Builder() {
    //            holder.airMapConfig = new AirMapConfig2(null, null);
    //        }
    //
    //        public Builder setCacheSize(int size) {
    //            holder.airMapConfig.cacheSize = size;
    //            return this;
    //        }
    //
    //        public Builder setCacheDirectory(File cacheDirectory) {
    //            holder.airMapConfig.cacheDir = cacheDirectory;
    //            return this;
    //        }
    //
    //        public Builder setApiKey(String apiKey) {
    //            holder.airMapConfig.apiKey = apiKey;
    //            return this;
    //        }
    //
    //        public Builder setLevelSet(LevelSet levelSet) {
    //            this.levelSet = levelSet;
    //            return this;
    //        }
    //
    //
    //        public Builder setDefaultAirmapCacheDir() {
    //            String currentDir = Paths.get(".").toAbsolutePath().normalize().toString();
    //            File cacheFile = new File(currentDir, "airmap-testcache");
    //            holder.airMapConfig.cacheDir = cacheFile;
    //            return this;
    //        }
    //
    //        public AirSpaceLayerHolder build() {
    //            // setup airmap
    //            if (holder.airMapConfig.apiKey == null) {
    //                holder.airMapConfig.apiKey = getDefaultAirmapKey();
    //            }
    //            AirMap.init(holder.airMapConfig);
    //
    //            // setup render layer
    //            if (levelSet == null) {
    //                levelSet = createDefaultLevelSet();
    //            }
    //
    //            holder.tileLoader = new AirMapTileLoader2();
    //            holder.layer = new TiledRenderableLayer(holder.tileLoader, levelSet);
    //
    //            return holder;
    //        }
    //    }

    public static LevelSet createDefaultLevelSet() {
        AVList params = new AVListImpl();

        // important
        params.setValue(AVKey.NUM_LEVELS, 9);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(85.0 / 2.0), Angle.fromDegrees(45d)));
        params.setValue(
            AVKey.SECTOR, new Sector(Angle.fromDegrees(-85.0), Angle.fromDegrees(85.0), Angle.NEG180, Angle.POS180));

        // not important
        params.setValue(AVKey.TILE_WIDTH, 256);
        params.setValue(AVKey.TILE_HEIGHT, 256);
        params.setValue(AVKey.DATA_CACHE_NAME, "cache name");
        params.setValue(AVKey.SERVICE, "http://foo.bar");
        params.setValue(AVKey.DATASET_NAME, "*");
        params.setValue(AVKey.FORMAT_SUFFIX, ".png");

        return new LevelSet(params);
    }

}
