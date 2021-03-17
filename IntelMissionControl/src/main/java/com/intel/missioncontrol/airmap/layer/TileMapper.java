/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.layer;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import java.util.ArrayList;
import java.util.Collection;

/** Maps sectors to Tiles */
public class TileMapper {
    private final LevelSet levels;
    private final Level lastLevel;
    private final LatLon tileOrigin;

    public TileMapper(LevelSet tileLevels) {
        this.levels = tileLevels;
        this.lastLevel = levels.getLastLevel();
        this.tileOrigin = levels.getTileOrigin();
    }

    /**
     * Get a list tiles that cover a region
     *
     * @param sector to fetch tiles for
     * @return list of tiles
     */
    public Collection<Tile> computeTilesForSector(Sector sector) {
        ArrayList<Tile> tileList = new ArrayList<>();
        computeTilesForSector(sector, tileList);
        return tileList;
    }

    /**
     * Find the tiles that are needed to cover a sector, add tile to passed in lis
     *
     * @param sector to fetch tiles for
     * @param tiles (out) list to put tiles into
     */
    public void computeTilesForSector(Sector sector, Collection<Tile> tiles) {
        computeTilesForSector(lastLevel, sector, tiles);
    }

    private void computeTilesForSector(Level level, Sector sector, Collection<Tile> tiles) {
        Angle sectorMinLat = sector.getMinLatitude();
        Angle sectorMinLon = sector.getMinLongitude();

        /*
                 x──x──x───x
                 │╔═╪══╪══╗│
                 x╫─x──x──x│
                 │╚═╪══╪══╝│
                 m──x──x───x
        */

        Angle tileDeltaLat = level.getTileDelta().getLatitude();
        Angle tileDeltaLon = level.getTileDelta().getLongitude();

        Angle originLat = tileOrigin.getLatitude();
        Angle originLon = tileOrigin.getLongitude();

        int rowLowest = Tile.computeRow(tileDeltaLat, sectorMinLat, originLat);
        Angle lowerLat = Tile.computeRowLatitude(rowLowest, tileDeltaLat, originLat);
        int colLowest = Tile.computeColumn(tileDeltaLon, sectorMinLon, originLon);
        Angle lowerLon = Tile.computeColumnLongitude(colLowest, tileDeltaLon, originLon);
        int numRows = (int) Math.ceil(sector.getMaxLatitude().subtract(lowerLat).divide(tileDeltaLat));
        int numCols = (int) Math.ceil(sector.getMaxLongitude().subtract(lowerLon).divide(tileDeltaLon));

        for (int y = 0; y < numRows; y++) {
            for (int x = 0; x < numCols; x++) {
                Sector tileSector = Sector.fromDegrees(
                        lowerLat.degrees + y*tileDeltaLat.degrees,
                        lowerLat.degrees + (y+1)*tileDeltaLat.degrees,
                        lowerLon.degrees + x*tileDeltaLon.degrees,
                        lowerLon.degrees + (x+1)*tileDeltaLon.degrees
                );
                tiles.add(new Tile(tileSector, level, rowLowest + y, colLowest + x));
            }
        }
    }
}
