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
        double minLatitude = sector.getMinLatitude().degrees,
            minLongitude = sector.getMinLongitude().degrees,
            dLatitude = sector.getDeltaLatDegrees(),
            dLongitude = sector.getDeltaLonDegrees();

        // Compute the tile's SW lat/lon based on its row/col in the level's data set.
        Angle dLat = level.getTileDelta().getLatitude().divide(2);
        Angle dLon = level.getTileDelta().getLongitude().divide(2);

        if (dLat.degrees <= 0 || dLon.degrees <= 0) {
            return;
        }

        Angle latOrigin = tileOrigin.getLatitude();
        Angle lonOrigin = tileOrigin.getLongitude();

        /*
                 x──x──x──x
                 │╔═╪══╪══╗
                 x╫─x──x──x
                 │╚═╪══╪══╝
                 m──x──x──x
        */

        double lat = Angle.normalizedDegreesLatitude(minLatitude);
        int row = Tile.computeRow(dLat, Angle.fromDegreesLatitude(lat), latOrigin);
        Angle rowLat = Tile.computeRowLatitude(row, dLat, latOrigin);
        lat = rowLat.degrees;

        int numLat = (int)Math.ceil(dLatitude / dLat.degrees);
        int numLon = (int)Math.ceil(dLongitude / dLon.degrees);

        for (int i = 0; i != numLat; i++) {
            double lon = Angle.normalizedDegreesLongitude(minLongitude);
            int col = Tile.computeColumn(dLon, Angle.fromDegreesLongitude(lon), lonOrigin);
            Angle rowLon = Tile.computeColumnLongitude(col, dLon, lonOrigin);
            lon = rowLon.degrees;
            for (int n = 0; n != numLon; n++) {
                Sector tileSector = Sector.fromDegrees(lat, lat + dLat.degrees, lon, lon + dLon.degrees);
                tiles.add(new Tile(tileSector, level, row, col));
                col++;
                lon = lon + dLon.degrees;
            }

            row++;
            lat = lat + dLat.degrees;
        }
    }

}
