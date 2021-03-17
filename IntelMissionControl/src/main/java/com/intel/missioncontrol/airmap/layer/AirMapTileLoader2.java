/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.layer;

import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.Airport;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.ControlledAirspace;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.Heliport;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.Hospitals;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.Park;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.PowerPlant;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.Prison;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.SpecialUse;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.TFR;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.Wildfires;

import com.airmap.airmapsdk.networking.services.MappingService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.intel.missioncontrol.airmap.AirMap;
import com.intel.missioncontrol.airmap.data.AirSpaceObject;
import com.intel.missioncontrol.airspace.render.TiledRenderableLayer;
import eu.mavinci.airspace.EAirspaceManager;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.util.Tile;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads Tile data and manages renderable cache, probably needs to be split up into these two responsibilities.
 *
 * <p>This class is complicated mostly to deal with the fact that one airspace object will appear in multiple tiles. We
 * want to keep a one to one mapping between airspaces and visibleRenderableMap, and want duplicate airspace objects in
 * different tiles to actually only be aliases and point to the same object. We also don't want to these airspace
 * objects to get added to the renderable layer multiple times. To do this we keep track of these objects across tile
 * loads/unloads via reference counting {@link AirSpaceRenderablePair} .
 *
 * <p>this class is super gross, but it works...
 */
public class AirMapTileLoader2 implements TiledRenderableLayer.TileRenderableManager {
    private static final Logger LOG = LoggerFactory.getLogger(AirMapTileLoader2.class);
    private static boolean tryReload = false;
    // this scheduler is used to spare warnings logging
    private final ScheduledExecutorService logWarnScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture logWarnFuture;

    public enum Status {
        /** no AirSpaceObjects or visibleRenderableMap available, due to e.g. unloaded stuff */
        Unloaded,
        /** no AirSpaceObjects or visibleRenderableMap available, should loading */
        Loading,
        /** data has AirSpaceObjects, but not renderable (should be in tile cache) */
        Loaded,
        /** data has AirSpaceObjects and Renderables ready */
        Renderable,
        /** something bad happened while loading */
        LoadError
    }

    public static class TileData {
        final Tile tile;
        List<AirSpaceRenderablePair> data = Collections.emptyList();
        CompletableFuture<Void> dataLoad;
        private volatile Status status;
        int failCount = 0;

        TileData(Tile tile) {
            this.status = Status.Unloaded;
            this.tile = tile;
        }

        public List<AirSpaceRenderablePair> getData() {
            return data;
        }

        public boolean isLoaded() {
            return status == Status.Loaded || status == Status.Renderable;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public Status getStatus() {
            return this.status;
        }
    }

    /** Maps an AirSpaceObject to a Renderable, or null */
    public static class AirSpaceRenderablePair {
        final AirSpaceObject airspace;
        Collection<Renderable> renderables;

        public AirSpaceObject getAirspace() {
            return airspace;
        }

        public Collection<Renderable> getRenderables() {
            return renderables;
        }

        int refCount = 0;

        AirSpaceRenderablePair(AirSpaceObject airspace) {
            this.airspace = airspace;
        }

        boolean hasRenderables() {
            return renderables != null;
        }

        /** use AirSpace object's UUID to check equality */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AirSpaceRenderablePair that = (AirSpaceRenderablePair)o;
            return Objects.equals(airspace, that.airspace);
        }

        @Override
        public int hashCode() {
            return Objects.hash(airspace);
        }
    }

    /** holds a list of visibleRenderables Renderables given to TiledRenderableLayer */
    private Collection<Renderable> visibleRenderables = Collections.emptyList();

    /**
     * source for visibleRenderables, keeps track of AirSpaceRenderablePair so they may destroyed when reference count
     * goes to zero
     */
    private final Map<AirSpaceObject, AirSpaceRenderablePair> visibleRenderableMap = new HashMap<>();

    private final Map<Tile, TileData> tileDataMap = new HashMap<>();
    private final AirSpaceObjectFactory renderableFactory = new AirSpaceObjectFactory();

    private final LoadingCache<Tile, TileData> tileCache;

    private static final long NUMBER_OF_TILES_TO_CACHE = 100;
    private static final int MAX_RETRY_COUNT = 2;

    /** the {@link #visibleRenderables} list needs to be recreated because tiles have been added/removed */
    private boolean visibleDirty = false;

    boolean debugTiles = false;
    private Collection<Renderable> debugRenderables = Collections.emptyList();

    /** draw some stuff to show tiles and their load status */
    public void setDrawDebug(boolean debugEnabled) {
        debugTiles = debugEnabled;
    }

    /** airspace types that are visibleRenderables */
    private final AtomicReference<Set<MappingService.AirMapAirspaceType>> visibleAirspaceTypes;

    public AirMapTileLoader2() {
        LOG.info("initializing AirMapTileLoader2 with cache size " + NUMBER_OF_TILES_TO_CACHE);
        tileCache =
            CacheBuilder.newBuilder()
                .maximumSize(NUMBER_OF_TILES_TO_CACHE)
                .build(
                    new CacheLoader<Tile, TileData>() {
                        @Override
                        public TileData load(Tile key) throws Exception {
                            return loadTileData(key);
                        }
                    });
        visibleAirspaceTypes = new AtomicReference<>(Collections.unmodifiableSet(new HashSet<>(AIRMAP_SEARCH_TYPES)));
    }

    // Note: Skipping Schools as the files in cache end up being too big.
    // Also even though Airmap website mentions Fires and Emergencies, including them in the query is giving an error
    // that these values are not supported.
    static final List<MappingService.AirMapAirspaceType> AIRMAP_SEARCH_TYPES =
        Arrays.asList(
            Airport, ControlledAirspace, Heliport, Hospitals, Park, PowerPlant, Prison, SpecialUse, TFR, Wildfires);

    public static List<MappingService.AirMapAirspaceType> getAirmapSearchTypes() {
        return AIRMAP_SEARCH_TYPES;
    }

    private static List<AirMap.Coordinate> toCoordinates(Sector sector) {
        return AirMap.makeSector(
            sector.getMinLatitude().degrees,
            sector.getMaxLatitude().degrees,
            sector.getMinLongitude().degrees,
            sector.getMaxLongitude().degrees);
    }

    /**
     * loads tile data from network (or cache), but doesn't create drawable
     *
     * @throws Exception
     */
    private TileData loadTileData(Tile tile) throws Exception {
        final TileData tileData = new TileData(tile);

        // TODO OPTIMIZE: remove ignore Types / to be evtl. reloaded if settings changed; at the moment: all relevant
        // ones are loaded

        tileData.status = Status.Loading;
        tileData.dataLoad =
            AirMap.searchAirspace(toCoordinates(tileData.tile.getSector()), AIRMAP_SEARCH_TYPES, null, true, null)
                .exceptionally(
                    throwable -> {
                        if (logWarnFuture != null) {
                            logWarnFuture.cancel(true);
                        }

                        logWarnFuture =
                            logWarnScheduler.schedule(
                                () -> LOG.warn("AirMap tile fetch failed, WW status: " + WorldWind.getNetworkStatus()),
                                200l,
                                TimeUnit.SECONDS);

                        tileData.setStatus(Status.LoadError);

                        try {
                            WorldWind.getNetworkStatus().logUnavailableHost(new URL(AirMap.AIRMAP_SEARCH_URL));
                            tryReload = true;
                        } catch (MalformedURLException e) {
                            logWarnFuture =
                                logWarnScheduler.schedule(
                                    () -> LOG.warn("AirMap tile fetch failed", e), 200l, TimeUnit.SECONDS);
                        }

                        return null;
                    })
                .thenAccept(
                    airSpaceObjects -> {
                        // after download
                        if (airSpaceObjects != null) {
                            tileData.data =
                                airSpaceObjects.stream().map(AirSpaceRenderablePair::new).collect(Collectors.toList());
                            if (tileData.getStatus() != Status.LoadError) {
                                tileData.setStatus(Status.Loaded);
                            }
                        } else {
                            // null returned when we've forced cache load and we miss
                            tileData.data = Collections.emptyList();
                            tileData.setStatus(Status.Unloaded);
                        }
                    });

        return tileData;
    }

    /** will return null on cache miss */
    public TileData getCachedTileData(Tile tile) {
        return tileCache.getIfPresent(tile);
    }

    /** will fetch from network on cache miss */
    public TileData fetchTileData(Tile tile) throws ExecutionException {
        TileData tileData = tileCache.get(tile);
        return tileData;
    }

    /**
     * Dispose of a Tile's visibleRenderableMap, visibleRenderableMap shared with another tile will have a refCount that
     * is greater than 1, and so will not be disposed of
     *
     * <p><b>must only be called on from Layer's thread</b>
     */
    private void disposeRenderablesForTileData(TiledRenderableLayer layer, TileData tileData) {
        assert tileData.getStatus() == Status.Renderable;
        if (tileData.getStatus() != Status.Renderable) {
            LOG.error("TileData dispose called with status=" + tileData.getStatus());
            return;
        }

        for (AirSpaceRenderablePair rh : tileData.data) {
            if (!rh.hasRenderables()) continue;

            // release if refcount goes to zero, unrenderable pairs should have negative refcount
            if (rh.renderables != null && --rh.refCount == 0) {
                visibleRenderableMap.remove(rh.airspace);
                for (Renderable renderable : rh.renderables) {
                    layer.disposeRenderable(renderable);
                }

                rh.renderables = null;
            }
        }

        tileData.setStatus(Status.Loaded);
    }

    /**
     * Creates visibleRenderableMap for all the AirSpace objects in a tile. If the same AirSpace object exists in
     * another tile, we will use that one
     *
     * <p><b>must only be called on from Layer's thread</b>
     */
    private void createRenderablesForTileData(TiledRenderableLayer layer, TileData tileData) {
        assert tileData.getStatus() == Status.Loaded;

        ListIterator<AirSpaceRenderablePair> it = tileData.data.listIterator();
        while (it.hasNext()) { // not using for-each loop, because need modification
            AirSpaceRenderablePair pair = it.next();
            AirSpaceRenderablePair existing_rh = visibleRenderableMap.get(pair.airspace);
            if (existing_rh != null) {
                // use existing renderable
                existing_rh.refCount++;
                it.set(existing_rh);
            } else {
                // no existing renderable, so create
                try {
                    pair.renderables = renderableFactory.createRenderableForAirspace(pair.airspace);
                } catch (Exception e) {
                    logWarnScheduler.scheduleAtFixedRate(
                        () -> LOG.warn("error creating renderable for airspace: " + pair.airspace, e),
                        100l,
                        0l,
                        TimeUnit.SECONDS);
                }

                if (pair.renderables != null && !pair.renderables.isEmpty()) {
                    for (Renderable renderable : pair.renderables) {
                        layer.registerRenderable(renderable);
                    }

                    pair.refCount++; // refCount = 1
                    visibleRenderableMap.put(pair.airspace, pair);
                }
            }
        }

        tileData.setStatus(Status.Renderable);
    }

    private static final Comparator<AirSpaceRenderablePair> airspaceRenderingOrder =
        new Comparator<AirSpaceRenderablePair>() {
            @Override
            public int compare(AirSpaceRenderablePair o1, AirSpaceRenderablePair o2) {
                return Double.compare(o1.getAirspace().getType().getzOrder(), o2.getAirspace().getType().getzOrder());
            }
        };

    /**
     * updates the visibleRenderables list, with any new tiles that have been loaded
     *
     * @return was renderable list changed
     */
    private boolean updateRenderableList(TiledRenderableLayer layer) {
        for (TileData tileData : tileDataMap.values()) {
            if (tileData.getStatus() != Status.Loaded) continue;

            visibleDirty = true;
            createRenderablesForTileData(layer, tileData);
        }

        if (visibleDirty) {
            final Set<MappingService.AirMapAirspaceType> visibleTypes = visibleAirspaceTypes.get();
            visibleRenderables =
                visibleRenderableMap
                    .values()
                    .stream()
                    .filter(rp -> visibleTypes.contains(rp.airspace.getType()))
                    .sorted(airspaceRenderingOrder)
                    .flatMap(rh -> rh.renderables.stream())
                    .collect(Collectors.toList());

            visibleDirty = false;
            EAirspaceManager.fireAirspacesChangedStatic();
            return true;
        } else {
            return false;
        }
    }

    public void setVisibleAirspaceLayers(Set<MappingService.AirMapAirspaceType> visibleLayers) {
        visibleDirty = true;
        this.visibleAirspaceTypes.set(new HashSet<>(visibleLayers));
        if (redrawCallback != null) {
            redrawCallback.run();
        }

        EAirspaceManager.fireAirspacesChangedStatic();
    }

    public Set<MappingService.AirMapAirspaceType> getVisibleAirspaceTypes() {
        return visibleAirspaceTypes.get();
    }

    @Override
    public void onTilesWillBecomeVisible(TiledRenderableLayer layer, Set<Tile> tiles) {
        Set<Tile> currentTiles = ImmutableSet.copyOf(tileDataMap.keySet());
        Sets.SetView<Tile> addedTiles = Sets.difference(tiles, currentTiles);
        Sets.SetView<Tile> removedTiles = Sets.difference(currentTiles, tiles);

        // add new tiles to tileDataMap
        for (Tile added : addedTiles) {
            // will load tile from cache or network
            TileData tileData = tileCache.getUnchecked(added);
            if (!WorldWind.isOfflineMode()) {
                tileData.failCount = 0;
            }

            tileDataMap.put(added, tileData);
        }

        // remove old tile from tileDataMap
        for (Tile removed : removedTiles) {
            TileData tileData = tileDataMap.remove(removed);
            if (tileData.getStatus() == Status.Renderable) {
                disposeRenderablesForTileData(layer, tileData);
            } else {
                // cancel loading
                if (!tileData.isLoaded()) {
                    if (tileData.dataLoad != null) {
                        boolean cancel = tileData.dataLoad.cancel(false);
                        if (cancel) {
                            // tile load was cancelled
                            tileData.failCount = 0;
                            tileData.status = Status.Unloaded;
                        }
                    }
                }
            }

            visibleDirty = true;
        }

        // check all current tiles for load errors
        Iterator<Map.Entry<Tile, TileData>> iterator = tileDataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Tile, TileData> next = iterator.next();
            TileData tileData = next.getValue();
            if (tileData.status == Status.Loading
                    || tileData.status == Status.LoadError
                    || tileData.status == Status.Unloaded) {
                if (tryReload) {
                    tileData.failCount = 0;
                }
            }

            if (tileData.status == Status.LoadError || tileData.status == Status.Unloaded) {
                int failCount = tileData.failCount;
                if (failCount < MAX_RETRY_COUNT) {
                    // reload
                    Tile tile = next.getKey();
                    tileCache.refresh(tile);
                    tileData = tileCache.getUnchecked(tile);
                    tileData.failCount = failCount + 1;
                    next.setValue(tileData);
                }
            }
        }

        tryReload = false;
    }

    // For debug use
    void generateDebugTiles() {
        debugRenderables = new ArrayList<>(visibleRenderables.size() + tileDataMap.size());
        debugRenderables.addAll(visibleRenderables);

        for (Map.Entry<Tile, TileData> e : tileDataMap.entrySet()) {
            Sector sector = e.getKey().getSector();

            SurfacePolygon quad = new SurfacePolygon(Arrays.asList(sector.getCorners()));
            Status status = e.getValue().status;
            Material mat = Material.BLUE;
            switch (status) {
            case Renderable:
                mat = Material.CYAN;
                break;
            case Loaded:
                mat = Material.GREEN;
                break;
            case Loading:
                mat = Material.YELLOW;
                break;
            case Unloaded:
                mat = Material.LIGHT_GRAY;
                break;
            case LoadError:
                mat = Material.RED;
                break;
            }

            {
                BasicShapeAttributes attr = new BasicShapeAttributes();
                attr.setOutlineMaterial(mat);
                attr.setOutlineWidth(3);
                attr.setInteriorMaterial(mat);
                attr.setInteriorOpacity(0.2);
                attr.setDrawInterior(true);
                attr.setDrawOutline(true);
                quad.setAttributes(attr);
            }

            debugRenderables.add(quad);

            TileData td = e.getValue();
            boolean loaded = td.isLoaded();
            String size = td.getData() != null ? Integer.toString(td.getData().size()) + " items" : "<N/A>";
            String tileName = "T " + td.tile.getRow() + "/" + td.tile.getColumn();

            PointPlacemark pp = new PointPlacemark(new Position(sector.getCentroid(), 0));
            pp.setLabelText(tileName + " / " + size + " / " + td.status);
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            pp.setLineEnabled(false);
            pp.setAlwaysOnTop(true);
            PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
            attrs.setLabelColor("ffddeedd");
            attrs.setDrawImage(false);
            attrs.setLabelOffset(Offset.TOP_CENTER);
            attrs.setScale(10d);
            pp.setAttributes(attrs);
            debugRenderables.add(pp);
        }
    }

    @Override
    public Iterable<? extends Renderable> onPreRender(TiledRenderableLayer layer) {
        boolean dirty = updateRenderableList(layer);

        if (debugTiles && dirty) {
            generateDebugTiles();
        }

        if (debugTiles) return debugRenderables;
        return visibleRenderables;
    }

    @Override
    public Iterable<? extends Renderable> onRender(TiledRenderableLayer layer) {
        if (debugTiles) return debugRenderables;
        return visibleRenderables;
    }

    @Override
    public Iterable<? extends Renderable> onPick(TiledRenderableLayer layer) {
        return visibleRenderables;
    }

    @Override
    public Iterable<? extends Renderable> onMessage(TiledRenderableLayer layer) {
        return visibleRenderables;
    }

    private Runnable redrawCallback;

    @Override
    public void registerRedrawCallback(Runnable callback) {
        redrawCallback = callback;
    }
}
