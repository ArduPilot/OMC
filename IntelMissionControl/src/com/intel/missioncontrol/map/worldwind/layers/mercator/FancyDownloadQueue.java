/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.mercator;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.mercator.MercatorTextureTile;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.util.Tile;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Download queue that prioritizes download of currently visible tiles.
 *
 * <p>Core logic in {@link #prioritize}
 */
public class FancyDownloadQueue implements FastMercatorTiledImageLayer.TileDownloadQueue {
    /** maximum number of tiles that are not in the current display set to download */
    private static final int DEFAULT_MAX_NON_PRIORITY_TILES_TO_QUEUE = 3;
    /** maximum tile level above current layer (i.e. smaller tile number) to download */
    private static final int MAX_TILE_LEVEL_DIFF = 1;

    private final AtomicLong requestId = new AtomicLong(0L);
    private final DebugLayer debug;

    private final ArrayDeque<TileRequest> queue = new ArrayDeque<>();
    private final HashSet<MercatorTextureTile> queueSet = new HashSet<>();

    private final Object queueLock = new Object();
    private final int maxNonPriorityTilesToQueue;

    FancyDownloadQueue() {
        this(false, DEFAULT_MAX_NON_PRIORITY_TILES_TO_QUEUE);
    }

    public FancyDownloadQueue(boolean debugLayer, int maxNonPriorityTilesToQueue) {
        if (debugLayer) {
            debug = new DebugLayer();
            debug.setName("Debug Layer");
        } else {
            debug = null;
        }

        this.maxNonPriorityTilesToQueue = maxNonPriorityTilesToQueue;
    }

    public DebugLayer getDebugLayer() {
        return debug;
    }

    final class TileRequest implements Comparable<TileRequest> {
        final long id;
        final MercatorTextureTile tile;
        boolean isPriority;
        double distance;
        boolean isVisible;
        boolean isInTileLevelRange;

        TileRequest(MercatorTextureTile tile) {
            this.tile = tile;
            this.id = requestId.getAndIncrement();
        }

        @Override
        public int compareTo(TileRequest o) {
            return Double.compare(this.tile.getPriority(), o.tile.getPriority());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TileRequest) {
                return Objects.equals(tile, ((TileRequest)obj).tile);
            } else {
                return false;
            }
        }
    }

    @Override
    public void onTilesWillDraw(DrawContext drawContext, ArrayList<MercatorTextureTile> tiles) {
        if (debug != null) {
            debug.onTilesWillDraw(tiles);
        }

        synchronized (queueLock) {
            if (queue.size() == 0) {
                return;
            }
        }

        prioritize(drawContext, tiles);
    }

    @Override
    public void addTile(MercatorTextureTile tile) {
        synchronized (queueLock) {
            if (!queueSet.contains(tile)) {
                queueSet.add(tile);
                queue.add(new TileRequest(tile));
            }
        }
    }

    @Override
    public MercatorTextureTile getNextTileForDownload() {
        synchronized (queueLock) {
            TileRequest next = queue.poll();
            if (next == null) {
                return null;
            } else {
                queueSet.remove(next.tile);
                return next.tile;
            }
        }
    }

    private void prioritize(DrawContext dc, ArrayList<MercatorTextureTile> tiles) {
        HashSet<MercatorTextureTile> prioritySet = new HashSet<>(tiles);

        final Vec4 referencePoint = FastMercatorTiledImageLayer.computeReferencePoint(dc);

        // calculate isVisible, this is expensive
        //        final Frustum frustumInModelCoordinates = dc.getView().getFrustumInModelCoordinates();
        final Sector visibleSector = dc.getVisibleSector(); // == null ? dc.getVisibleSector() : Sector.EMPTY_SECTOR;

        IntSummaryStatistics priorityLevelStats =
            prioritySet.stream().mapToInt(Tile::getLevelNumber).summaryStatistics();

        // select tile range to download
        int minTileLevel, maxTileLevel;
        if (tiles.size() > 0) {
            minTileLevel = Math.max(0, priorityLevelStats.getMin() - MAX_TILE_LEVEL_DIFF);
            maxTileLevel = priorityLevelStats.getMax();
        } else {
            minTileLevel = 0;
            maxTileLevel = 2;
        }

        ArrayList<TileRequest> priorityQueue = new ArrayList<>(tiles.size());

        synchronized (queueLock) {
            for (TileRequest tr : queue) {
                MercatorTextureTile tile = tr.tile;
                tr.isPriority = prioritySet.contains(tile);

                Vec4 centroid = tile.getCentroidPoint(dc.getGlobe());
                double distance = (referencePoint != null) ? centroid.distanceTo3(referencePoint) : 0.0;

                tr.distance = distance;
                tr.isVisible = visibleSector.intersectsInterior(tile.getSector());

                int tileLevel = tr.tile.getLevelNumber();

                tr.isInTileLevelRange = tileLevel <= maxTileLevel && tileLevel >= minTileLevel;

                // set priority by tile distance, giving preference to large tiles (i.e. lower tile levels numbers)
                tr.tile.setPriority(distance * (1.0 - 1.0 / (Math.max(0.0, tileLevel - minTileLevel) + 1.0)));

                priorityQueue.add(tr);
            }

            Collections.sort(priorityQueue);

            queue.clear();
            int nonPrioCount = 0;
            int dropCount = 0;
            for (TileRequest tr : priorityQueue) {
                if (tr.isPriority) {
                    queue.add(tr);
                } else if (tr.isVisible && tr.isInTileLevelRange && (nonPrioCount++ < maxNonPriorityTilesToQueue)) {
                    // limit non priority tile
                    queue.add(tr);
                } else {
                    // drop tile
                    dropCount++;
                    queueSet.remove(tr.tile);
                }
            }

            //            IntSummaryStatistics stats = queue.stream().mapToInt(tr ->
            // tr.tile.getLevelNumber()).summaryStatistics();
            //            System.out.printf("queue stats: %d tiles (%d non-prio), dropped %d\n", queue.size(),
            // nonPrioCount, dropCount);
            //            System.out.printf("           : tile range %d - %d\n", stats.getMin(), stats.getMax());
        }
    }

    static class TR {
        TileRequest tileRequest;
        int order;
    }

    // debug layer to draw tiles loading status, this is expensive because it redraws each frame
    public class DebugLayer extends RenderableLayer {
        ArrayList<TileRequest> requestsList = new ArrayList<>();
        HashMap<MercatorTextureTile, TR> requestMap = new HashMap<>();
        HashSet<MercatorTextureTile> drawTiles = new HashSet<>();

        @Override
        protected void doPreRender(DrawContext dc) {
            Queue<TileRequest> r;
            synchronized (queueLock) {
                r = new ArrayDeque<>(queue);
            }

            onRequestQueue(r);

            removeAllRenderables();

            for (MercatorTextureTile mercTile : requestMap.keySet()) {
                makeSector(mercTile, dc);
                drawTiles.remove(mercTile);
            }

            // existing
            for (MercatorTextureTile existingTiles : drawTiles) {
                makeSector(existingTiles, dc);
            }

            super.doPreRender(dc);
        }

        void onRequestQueue(Queue<TileRequest> queueRequests) {
            this.requestsList.clear();
            this.requestsList.addAll(queueRequests);
            requestMap.clear();

            if (queueRequests.size() > 0) {
                System.out.println(">>> got " + queueRequests.size() + " requests");
            }

            int i = 0;
            for (TileRequest queueRequest : requestsList) {
                TR tr = new TR();
                tr.order = i++;
                tr.tileRequest = queueRequest;
                requestMap.put(queueRequest.tile, tr);
            }
        }

        private void makeSector(MercatorTextureTile tile, DrawContext dc) {
            Sector sector = tile.getMercatorSector();

            SurfacePolygon quad = new SurfacePolygon(Arrays.asList(sector.getCorners()));
            Material mat = Material.CYAN;
            TR tr = requestMap.get(tile);

            Material interior = null;
            float interiorOpacity = 0.0f;
            float size = 1.0f;
            if (tr != null) {
                TileRequest tileRequest = tr.tileRequest;
                size = 2.0f;
                if (tileRequest.isPriority) {
                    mat = Material.RED;
                } else {
                    if (tileRequest.isInTileLevelRange) {
                        mat = Material.YELLOW;
                        size = 5.0f;
                    }
                }

                interiorOpacity = 1.0f / (tr.order + 1.0f);
            } else {
                mat = Material.WHITE;
            }

            interior = mat;

            {
                BasicShapeAttributes attr = new BasicShapeAttributes();
                attr.setOutlineMaterial(mat);
                attr.setOutlineWidth(size);
                attr.setOutlineOpacity(0.8);
                //                attr.setDrawInterior(interior != null);
                //                if (interior != null) {
                attr.setDrawInterior(false);
                //                    attr.setInteriorMaterial(interior);
                //                    attr.setInteriorOpacity(0.1 + 0.5*interiorOpacity);
                ////                }
                attr.setDrawOutline(true);
                quad.setAttributes(attr);
                addRenderable(quad);
            }

            String tileName =
                tile.getLevelNumber()
                    + " "
                    + tile.getColumn()
                    + "/"
                    + ((2 << tile.getLevelNumber()) - tile.getRow() - 1);

            {
                PointPlacemark pp = new PointPlacemark(new Position(sector.getCorners()[0], 0));
                pp.setLabelText(tileName);
                pp.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                pp.setLineEnabled(false);
                pp.setAlwaysOnTop(true);
                PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();

                attrs.setLabelColor("ffffffff");
                attrs.setDrawImage(false);
                attrs.setLabelOffset(Offset.LEFT_CENTER);
                attrs.setScale(14d);
                attrs.setLabelScale(1d);
                pp.setAttributes(attrs);
                addRenderable(pp);
            }

            do {
                Extent extent = tile.getExtent(dc);

                if (extent == null) break;

                double numPixels = extent.getProjectedArea(dc.getView());
                if (numPixels != Double.POSITIVE_INFINITY) numPixels = Math.sqrt(numPixels);

                PointPlacemark pp = new PointPlacemark(new Position(sector.getCentroid(), 0));
                pp.setLabelText(String.format("Size % 6.0f", numPixels));
                pp.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                pp.setLineEnabled(false);
                pp.setAlwaysOnTop(true);
                PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();

                attrs.setLabelColor("ffffff88");
                attrs.setDrawImage(false);
                attrs.setLabelOffset(Offset.RIGHT_CENTER);
                attrs.setScale(14d);
                attrs.setLabelScale(1d);
                pp.setAttributes(attrs);
                addRenderable(pp);
            } while (false);

            if (tr != null) {
                {
                    PointPlacemark pp = new PointPlacemark(new Position(sector.getCentroid(), 0));

                    pp.setLabelText(String.format("% 2d - %2.2g", tr.order, tr.tileRequest.distance));
                    pp.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                    pp.setLineEnabled(false);
                    pp.setAlwaysOnTop(true);
                    PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
                    attrs.setLabelMaterial(mat);
                    //                    attrs.setLabelColor("ffff88ff");
                    attrs.setDrawImage(false);
                    attrs.setLabelOffset(Offset.CENTER);
                    attrs.setScale(22d);
                    attrs.setLabelScale(1d);
                    pp.setAttributes(attrs);
                    addRenderable(pp);
                }
            }
        }

        public void onTilesWillDraw(ArrayList<MercatorTextureTile> tiles) {
            drawTiles.clear();
            drawTiles.addAll(tiles);
        }
    }

}
