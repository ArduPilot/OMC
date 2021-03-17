/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap;

import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType;
import com.intel.missioncontrol.airmap.data.AirMapAirspaceLegacyConverter;
import com.intel.missioncontrol.airmap.layer.AirMapTileLoader2;
import com.intel.missioncontrol.airmap.layer.TileMapper;
import com.intel.missioncontrol.airspace.LayerConfigurator;
import com.intel.missioncontrol.airspace.render.TiledRenderableLayer;
import com.intel.missioncontrol.airspaces.sources.AirspaceSource;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import eu.mavinci.airspace.IAirspace;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Tile;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AirMap2Source implements AirspaceSource, TiledRenderableLayer.ISource {
    private final TileMapper tileMapper;
    private final AirMapTileLoader2 tileLoader;

    // todo(max): this is gross, but WWFactory is not guicified so this is shitty hack
    static AirMap2Source instance;

    @Deprecated
    public static AirMap2Source getInstance() {
        if (instance == null) {
            instance = new AirMap2Source();
        }

        return instance;
    }

    private AirMap2Source() {
        tileLoader = new AirMapTileLoader2();
        tileMapper = new TileMapper(LayerConfigurator.createDefaultLevelSet());
    }

    private AirspacesProvidersSettings settings;

    public void bindToSettings(AirspacesProvidersSettings airspacesProvidersSettings) {
        // bind to changes
        for (MappingService.AirMapAirspaceType entry : AirMapTileLoader2.getAirmapSearchTypes()) {
            airspacesProvidersSettings
                .showAirspaceTypeProperty(entry)
                .addListener(
                    (observable, oldValue, newValue) ->
                        setAirSpaceLayerVisible(getSelectedAirmaps(airspacesProvidersSettings)));
        }

        // init
        setAirSpaceLayerVisible(getSelectedAirmaps(airspacesProvidersSettings));
    }

    public void setAirSpaceLayerVisible(Set<AirMapAirspaceType> visibleTypes) {
        tileLoader.setVisibleAirspaceLayers(visibleTypes);
    }

    public AirMapTileLoader2 getTileLoader() {
        return tileLoader;
    }

    public TileMapper getTileMapper() {
        return tileMapper;
    }

    private List<AirMapTileLoader2.TileData> loadCachedTiles(Collection<Tile> tiles) {
        return tiles.stream()
            .map(tileLoader::getCachedTileData)
            .filter(tileData -> tileData != null && tileData.getStatus() == AirMapTileLoader2.Status.Renderable)
            .collect(Collectors.toList());
    }

    private List<AirMapTileLoader2.TileData> loadTiles(Collection<Tile> tiles, int timeoutMs) {
        List<AirMapTileLoader2.TileData> data =
            tiles.stream()
                .map(
                    (tile) -> {
                        try {
                            return tileLoader.fetchTileData(tile);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // futures to wait for completion
        if (false) {
            //            List<CompletableFuture<Void>> f =
            //                    data.stream().map(tileData -> tileData.isLoaded()).collect(Collectors.toList());
            //
            //            try {
            //                CompletableFuture[] loadFutures = new CompletableFuture[f.size()];
            //                loadFutures = f.toArray(loadFutures);
            //
            //                CompletableFuture.allOf(loadFutures).get(timeoutMs, TimeUnit.MILLISECONDS);
            //            } catch (CancellationException | TimeoutException e) {
            //                // ignore
            //            } catch (Exception e) {
            //                if (e.getCause() instanceof CancellationException || e.getCause() instanceof
            // TimeoutException) {
            //                    // ignore as well ;-)
            //                } else {
            //                    Debug.getLog().log(Level.WARNING, "problem loading airmap data", e);
            //                }
            //            }
        }

        return data.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<IAirspace> getAirspacesWithinTimeout(Sector sector, boolean cacheOnly, int timeoutMs) {
        Collection<Tile> tiles = tileMapper.computeTilesForSector(sector);

        List<AirMapTileLoader2.TileData> data;
        if (cacheOnly) {
            data = loadCachedTiles(tiles);
        } else {
            data = loadTiles(tiles, timeoutMs);
        }

        var visTypes = tileLoader.getVisibleAirspaceTypes();
        List<IAirspace> collect =
            data.stream()
                .flatMap(tileData -> tileData.getData().stream())
                .map(pair -> pair.getAirspace())
                .filter(a -> visTypes.contains(a.getType()))
                .map(AirMapAirspaceLegacyConverter::convertDeep)
                .flatMap(a -> a.stream())
                .collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<IAirspace> getAirspacesWithin(Sector boundingBox) {
        return getAirspacesWithinTimeout(boundingBox, false, 10000);
    }

    @Override
    public List<IAirspace> getCachedAirspacesWithin(Sector boundingBox) {
        return getAirspacesWithinTimeout(boundingBox, true, 0);
    }

    private Set<MappingService.AirMapAirspaceType> getSelectedAirmaps(
            AirspacesProvidersSettings airspacesProvidersSettings) {
        Set<MappingService.AirMapAirspaceType> visibleSet = new HashSet<>();
        for (MappingService.AirMapAirspaceType entry : AirMapTileLoader2.getAirmapSearchTypes()) {
            // airspaces
            if (airspacesProvidersSettings.showAirspaceTypeProperty(entry).get()) {
                visibleSet.add(entry);
            }
        }

        return visibleSet;
    }

}
