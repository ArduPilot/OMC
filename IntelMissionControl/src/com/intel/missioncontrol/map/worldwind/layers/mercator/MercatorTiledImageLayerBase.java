/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.intel.missioncontrol.map.worldwind.layers.mercator;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.javafx.IScreenshotView;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.layers.mercator.MercatorTextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.TextRenderer;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLTextRenderer;
import gov.nasa.worldwind.util.PerformanceStatistic;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWMath;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import javax.imageio.ImageIO;

/**
 * TiledImageLayer modified 2009-02-03 to add support for Mercator projections.
 *
 * <p>msteinx: Modified slightly from {@link gov.nasa.worldwind.layers.TiledImageLayer} to make public split scale and
 * change a few other things.
 *
 * @author tag
 * @version $Id: MercatorTiledImageLayer.java 2053 2014-06-10 20:16:57Z tgaskins $
 */
public abstract class MercatorTiledImageLayerBase extends AbstractLayer {
    // Infrastructure
    private static final LevelComparer levelComparer = new LevelComparer();
    private final LevelSet levels;
    private ArrayList<MercatorTextureTile> topLevels;
    private boolean forceLevelZeroLoads = false;
    private boolean levelZeroLoaded = false;
    private boolean retainLevelZeroTiles = false;
    private String tileCountName;
    private final double splitScale; // TODO: Make configurable

    private boolean useMipMaps = false;
    private ArrayList<String> supportedImageFormats = new ArrayList<String>();

    // Diagnostic flags
    private boolean showImageTileOutlines = false;
    private boolean drawTileBoundaries = false;
    private boolean useTransparentTextures = false;
    private boolean drawTileIDs = false;
    private boolean drawBoundingVolumes = false;

    // Stuff computed each frame
    private ArrayList<MercatorTextureTile> currentTiles = new ArrayList<MercatorTextureTile>();
    private MercatorTextureTile currentResourceTile;
    private Vec4 referencePoint;
    private boolean atMaxResolution = false;
    private PriorityBlockingQueue<Runnable> requestQ = new PriorityBlockingQueue<Runnable>(200);
    protected double detailHint = 0.0;
    protected final double detailHintOrigin = 2.8;
    private int minimumDisplayLevel = 0;

    protected abstract void requestTexture(DrawContext dc, MercatorTextureTile tile);

    protected abstract void forceTextureLoad(MercatorTextureTile tile);

    public MercatorTiledImageLayerBase(LevelSet levelSet) {
        this(levelSet, 0.9);
    }

    public MercatorTiledImageLayerBase(LevelSet levelSet, double splitScale) {
        this.splitScale = splitScale;

        if (levelSet == null) {
            String message = Logging.getMessage("nullValue.LevelSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.levels = new LevelSet(levelSet); // the caller's levelSet may change internally, so we copy it.
        this.createTopLevelTiles();

        this.setPickEnabled(false); // textures are assumed to be terrain unless specifically indicated otherwise.
        this.tileCountName = this.getName() + " Tiles";
    }

    /**
     * Set minimum tile level that will be displayed (default is 0)
     *
     * @param level
     */
    public void setMinimumDisplayLevel(int level) {
        minimumDisplayLevel = level;
    }

    public int getMinimumDisplayLevel() {
        return minimumDisplayLevel;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        this.tileCountName = this.getName() + " Tiles";
    }

    public boolean isUseTransparentTextures() {
        return this.useTransparentTextures;
    }

    public void setUseTransparentTextures(boolean useTransparentTextures) {
        this.useTransparentTextures = useTransparentTextures;
    }

    public boolean isForceLevelZeroLoads() {
        return this.forceLevelZeroLoads;
    }

    public void setForceLevelZeroLoads(boolean forceLevelZeroLoads) {
        this.forceLevelZeroLoads = forceLevelZeroLoads;
    }

    public boolean isRetainLevelZeroTiles() {
        return retainLevelZeroTiles;
    }

    public void setRetainLevelZeroTiles(boolean retainLevelZeroTiles) {
        this.retainLevelZeroTiles = retainLevelZeroTiles;
    }

    public boolean isDrawTileIDs() {
        return drawTileIDs;
    }

    public void setDrawTileIDs(boolean drawTileIDs) {
        this.drawTileIDs = drawTileIDs;
    }

    public boolean isDrawTileBoundaries() {
        return drawTileBoundaries;
    }

    public void setDrawTileBoundaries(boolean drawTileBoundaries) {
        this.drawTileBoundaries = drawTileBoundaries;
    }

    public boolean isShowImageTileOutlines() {
        return showImageTileOutlines;
    }

    public void setShowImageTileOutlines(boolean showImageTileOutlines) {
        this.showImageTileOutlines = showImageTileOutlines;
    }

    public boolean isDrawBoundingVolumes() {
        return drawBoundingVolumes;
    }

    public void setDrawBoundingVolumes(boolean drawBoundingVolumes) {
        this.drawBoundingVolumes = drawBoundingVolumes;
    }

    protected LevelSet getLevels() {
        return levels;
    }

    protected PriorityBlockingQueue<Runnable> getRequestQ() {
        return requestQ;
    }

    public boolean isMultiResolution() {
        return this.getLevels() != null && this.getLevels().getNumLevels() > 1;
    }

    public boolean isAtMaxResolution() {
        return this.atMaxResolution;
    }

    public boolean isUseMipMaps() {
        return useMipMaps;
    }

    public void setUseMipMaps(boolean useMipMaps) {
        this.useMipMaps = useMipMaps;
    }

    private void createTopLevelTiles() {
        MercatorSector sector = (MercatorSector)this.levels.getSector();

        Level level = levels.getFirstLevel();
        Angle dLat = level.getTileDelta().getLatitude();
        Angle dLon = level.getTileDelta().getLongitude();

        Angle latOrigin = this.levels.getTileOrigin().getLatitude();
        Angle lonOrigin = this.levels.getTileOrigin().getLongitude();

        // Determine the row and column offset from the common World Wind global tiling origin.
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

                this.topLevels.add(new MercatorTextureTile(new MercatorSector(d1, d2, t1, t2), level, row, col));
                t1 = t2;
            }

            d1 = d2;
        }
    }

    private void loadAllTopLevelTextures(DrawContext dc) {
        for (MercatorTextureTile tile : this.topLevels) {
            if (!tile.isTextureInMemory(dc.getTextureCache())) this.forceTextureLoad(tile);
        }

        this.levelZeroLoaded = true;
    }

    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //

    private void assembleTiles(DrawContext dc) {
        this.currentTiles.clear();

        for (MercatorTextureTile tile : this.topLevels) {
            if (this.isTileVisible(dc, tile)) {
                this.currentResourceTile = null;
                this.addTileOrDescendants(dc, tile);
            }
        }

        onTilesAssembledWillDraw(dc, this.currentTiles);
    }

    protected void onTilesAssembledWillDraw(DrawContext dc, ArrayList<MercatorTextureTile> tiles) {}

    private void addTileOrDescendants(DrawContext dc, MercatorTextureTile tile) {
        if (this.meetsRenderCriteria(dc, tile) && tile.getLevelNumber() >= minimumDisplayLevel) {
            this.addTile(dc, tile);
            return;
        }

        // The incoming tile does not meet the rendering criteria, so it must be subdivided and those
        // subdivisions tested against the criteria.

        // All tiles that meet the selection criteria are drawn, but some of those tiles will not have
        // textures associated with them either because their texture isn't loaded yet or because they
        // are finer grain than the layer has textures for. In these cases the tiles use the texture of
        // the closest ancestor that has a texture loaded. This ancestor is called the currentResourceTile.
        // A texture transform is applied during rendering to align the sector's texture coordinates with the
        // appropriate region of the ancestor's texture.

        MercatorTextureTile ancestorResource = null;

        try {
            // TODO: Revise this to reflect that the parent layer is only requested while the algorithm continues
            // to search for the layer matching the criteria.
            // At this point the tile does not meet the render criteria but it may have its texture in memory.
            // If so, register this tile as the resource tile. If not, then this tile will be the next level
            // below a tile with texture in memory. So to provide progressive resolution increase, add this tile
            // to the draw list. That will cause the tile to be drawn using its parent tile's texture, and it will
            // cause it's texture to be requested. At some future call to this method the tile's texture will be in
            // memory, it will not meet the render criteria, but will serve as the parent to a tile that goes
            // through this same process as this method recurses. The result of all this is that a tile isn't rendered
            // with its own texture unless all its parents have their textures loaded. In addition to causing
            // progressive resolution increase, this ensures that the parents are available as the user zooms out, and
            // therefore the layer remains visible until the user is zoomed out to the point the layer is no longer
            // active.
            if (tile.isTextureInMemory(dc.getTextureCache()) || tile.getLevelNumber() == 0) {
                ancestorResource = this.currentResourceTile;
                this.currentResourceTile = tile;
            } else if (!tile.getLevel().isEmpty()) {
                //                this.addTile(dc, tile);
                //                return;

                // Issue a request for the parent before descending to the children.
                if (tile.getLevelNumber() < this.levels.getNumLevels()) {
                    // Request only tiles with data associated at this level
                    if (!this.levels.isResourceAbsent(tile)) this.requestTexture(dc, tile);
                }
            }

            MercatorTextureTile[] subTiles = tile.createSubTiles(this.levels.getLevel(tile.getLevelNumber() + 1));
            for (MercatorTextureTile child : subTiles) {
                if (this.isTileVisible(dc, child)) this.addTileOrDescendants(dc, child);
            }
        } finally {
            if (ancestorResource != null) // Pop this tile as the currentResource ancestor
            this.currentResourceTile = ancestorResource;
        }
    }

    private void addTile(DrawContext dc, MercatorTextureTile tile) {
        tile.setFallbackTile(null);

        if (tile.isTextureInMemory(dc.getTextureCache())) {
            //            System.out.printf("Sector %s, min = %f, max = %f\n", tile.getSector(),
            //                dc.getGlobe().getMinElevation(tile.getSector()),
            // dc.getGlobe().getMaxElevation(tile.getSector()));
            this.addTileToCurrent(tile);
            return;
        }

        // Level 0 loads may be forced
        if (tile.getLevelNumber() == 0 && this.forceLevelZeroLoads && !tile.isTextureInMemory(dc.getTextureCache())) {
            this.forceTextureLoad(tile);
            if (tile.isTextureInMemory(dc.getTextureCache())) {
                this.addTileToCurrent(tile);
                return;
            }
        }

        // Tile's texture isn't available, so request it
        if (tile.getLevelNumber() < this.levels.getNumLevels()) {
            // Request only tiles with data associated at this level
            if (!this.levels.isResourceAbsent(tile)) this.requestTexture(dc, tile);
        }

        // Set up to use the currentResource tile's texture
        if (this.currentResourceTile != null) {
            if (this.currentResourceTile.getLevelNumber() == 0
                    && this.forceLevelZeroLoads
                    && !this.currentResourceTile.isTextureInMemory(dc.getTextureCache())
                    && !this.currentResourceTile.isTextureInMemory(dc.getTextureCache()))
                this.forceTextureLoad(this.currentResourceTile);

            if (this.currentResourceTile.isTextureInMemory(dc.getTextureCache())) {
                tile.setFallbackTile(currentResourceTile);
                this.addTileToCurrent(tile);
            }
        }
    }

    private void addTileToCurrent(MercatorTextureTile tile) {
        this.currentTiles.add(tile);
    }

    private boolean isTileVisible(DrawContext dc, MercatorTextureTile tile) {
        //        if (!(tile.getExtent(dc).intersects(dc.getView().getFrustumInModelCoordinates())
        //            && (dc.getVisibleSector() == null || dc.getVisibleSector().intersects(tile.getSector()))))
        //            return false;
        //
        //        Position eyePos = dc.getView().getEyePosition();
        //        LatLon centroid = tile.getSector().getCentroid();
        //        Angle d = LatLon.greatCircleDistance(eyePos.getLatLon(), centroid);
        //        if ((!tile.getLevelName().equals("0")) && d.compareTo(tile.getSector().getDeltaLat().multiply(2.5)) ==
        // 1)
        //            return false;
        //
        //        return true;
        //
        return tile.getExtent(dc).intersects(dc.getView().getFrustumInModelCoordinates())
            && (dc.getVisibleSector() == null || dc.getVisibleSector().intersects(tile.getSector()));
    }

    //
    //    private boolean meetsRenderCriteria2(DrawContext dc, TextureTile tile)
    //    {
    //        if (this.levels.isFinalLevel(tile.getLevelNumber()))
    //            return true;
    //
    //        Sector sector = tile.getSector();
    //        Vec4[] corners = sector.computeCornerPoints(dc.getGlobe());
    //        Vec4 centerPoint = sector.computeCenterPoint(dc.getGlobe());
    //
    //        View view = dc.getView();
    //        double d1 = view.getEyePoint().distanceTo3(corners[0]);
    //        double d2 = view.getEyePoint().distanceTo3(corners[1]);
    //        double d3 = view.getEyePoint().distanceTo3(corners[2]);
    //        double d4 = view.getEyePoint().distanceTo3(corners[3]);
    //        double d5 = view.getEyePoint().distanceTo3(centerPoint);
    //
    //        double minDistance = d1;
    //        if (d2 < minDistance)
    //            minDistance = d2;
    //        if (d3 < minDistance)
    //            minDistance = d3;
    //        if (d4 < minDistance)
    //            minDistance = d4;
    //        if (d5 < minDistance)
    //            minDistance = d5;
    //
    //        double r = 0;
    //        if (minDistance == d1)
    //            r = corners[0].getLength3();
    //        if (minDistance == d2)
    //            r = corners[1].getLength3();
    //        if (minDistance == d3)
    //            r = corners[2].getLength3();
    //        if (minDistance == d4)
    //            r = corners[3].getLength3();
    //        if (minDistance == d5)
    //            r = centerPoint.getLength3();
    //
    //        double texelSize = tile.getLevel().getTexelSize(r);
    //        double pixelSize = dc.getView().computePixelSizeAtDistance(minDistance);
    //
    //        return 2 * pixelSize >= texelSize;
    //    }

    private boolean meetsRenderCriteria(DrawContext dc, MercatorTextureTile tile) {
        //        boolean needToSplit3 = needToSplit3(dc, tile);
        //        boolean needToSplit2 = needToSplit2(dc, tile.getSector(), tile.getLevel());
        // if (!this.levels.getLevel(tile.getLevelNumber()).isActive()) return false;

        return this.levels.isFinalLevel(tile.getLevelNumber()) || !needToSplit(dc, tile.getSector());
    }

    /**
     * Indicates the layer's detail hint, which is described in {@link #setDetailHint(double)}.
     *
     * @return the detail hint
     * @see #setDetailHint(double)
     */
    public double getDetailHint() {
        return this.detailHint;
    }

    /**
     * Modifies the default relationship of image resolution to screen resolution as the viewing altitude changes.
     * Values greater than 0 cause imagery to appear at higher resolution at greater altitudes than normal, but at an
     * increased performance cost. Values less than 0 decrease the default resolution at any given altitude. The default
     * value is 0. Values typically range between -0.5 and 0.5.
     *
     * <p>Note: The resolution-to-height relationship is defined by a scale factor that specifies the approximate size
     * of discernible lengths in the image relative to eye distance. The scale is specified as a power of 10. A value of
     * 3, for example, specifies that 1 meter on the surface should be distinguishable from an altitude of 10^3 meters
     * (1000 meters). The default scale is 1/10^2.8, (1 over 10 raised to the power 2.8). The detail hint specifies
     * deviations from that default. A detail hint of 0.2 specifies a scale of 1/1000, i.e., 1/10^(2.8 + .2) = 1/10^3.
     * Scales much larger than 3 typically cause the applied resolution to be higher than discernible for the altitude.
     * Such scales significantly decrease performance.
     *
     * @param detailHint the degree to modify the default relationship of image resolution to screen resolution with
     *     changing view altitudes. Values greater than 1 increase the resolution. Values less than zero decrease the
     *     resolution. The default value is 0.
     */
    public void setDetailHint(double detailHint) {
        this.detailHint = detailHint;
    }

    private double getDetailFactor() {
        // default
        return detailHintOrigin + getDetailHint();
    }

    protected boolean needToSplit3(DrawContext dc, MercatorTextureTile tile) {
        //        View view = dc.getView();
        //        List<? extends LatLon> corners = tile.getCorners();
        //
        //        double[] minAndMaxElevations = dc.getGlobe().getMinAndMaxElevations(tile.getSector());
        //
        //        double alt = minAndMaxElevations[1];
        //
        //        Vec4[] verts = new Vec4[4];
        //        Matrix viewModelMat = dc.getView().getModelviewMatrix();
        //
        //        int i =0;
        //        for (LatLon corner : corners) {
        //            Vec4 v = dc.getGlobe().computePointFromPosition(corner, alt);
        //            Vec4 project = view.project(v);
        //        }
        //      // todo: get corners of flattend bounding box and calculate area
        dc.getView().getViewport();
        dc.getView().getFrustum();

        Extent extent = tile.getExtent(dc);
        if (extent == null) return false;
        double numPixels = extent.getProjectedArea(dc.getView());

        double tileSize = tile.getWidth();

        if (numPixels != Double.POSITIVE_INFINITY) numPixels = Math.sqrt(numPixels);

        return numPixels > tileSize * 1.5;
    }

    /** Taken from {@link gov.nasa.worldwind.layers.TiledImageLayer#needToSplit(DrawContext, Sector, Level)} */
    protected boolean needToSplit2(DrawContext dc, Sector sector, Level level) {
        // Compute the height in meters of a texel from the specified level. Take care to convert from the radians to
        // meters by multiplying by the globe's radius, not the length of a Cartesian point. Using the length of a
        // Cartesian point is incorrect when the globe is flat.
        double texelSizeRadians = level.getTexelSize();
        double texelSizeMeters = dc.getGlobe().getRadius() * texelSizeRadians;

        // Compute the level of detail scale and the field of view scale. These scales are multiplied by the eye
        // distance to derive a scaled distance that is then compared to the texel size. The level of detail scale is
        // specified as a power of 10. For example, a detail factor of 3 means split when the cell size becomes more
        // than one thousandth of the eye distance. The field of view scale is specified as a ratio between the current
        // field of view and a the default field of view. In a perspective projection, decreasing the field of view by
        // 50% has the same effect on object size as decreasing the distance between the eye and the object by 50%.
        // The detail hint is reduced for tiles above 75 degrees north and below 75 degrees south.
        double s = this.getDetailFactor();
        if (sector.getMinLatitude().degrees >= 75 || sector.getMaxLatitude().degrees <= -75) s *= 0.9;
        double detailScale = Math.pow(10, -s);
        double fieldOfViewScale = dc.getView().getFieldOfView().tanHalfAngle() / Angle.fromDegrees(45).tanHalfAngle();
        fieldOfViewScale = WWMath.clamp(fieldOfViewScale, 0, 1);

        // Compute the distance between the eye point and the sector in meters, and compute a fraction of that distance
        // by multiplying the actual distance by the level of detail scale and the field of view scale.
        double eyeDistanceMeters = sector.distanceTo(dc, dc.getView().getEyePoint());
        double scaledEyeDistanceMeters = eyeDistanceMeters * detailScale * fieldOfViewScale;

        // Split when the texel size in meters becomes greater than the specified fraction of the eye distance, also in
        // meters. Another way to say it is, use the current tile if its texel size is less than the specified fraction
        // of the eye distance.
        //
        // NOTE: It's tempting to instead compare a screen pixel size to the texel size, but that calculation is
        // window-size dependent and results in selecting an excessive number of tiles when the window is large.
        return texelSizeMeters > scaledEyeDistanceMeters;
    }

    private boolean needToSplit(DrawContext dc, Sector sector) {
        View view = dc.getView();

        // for the screenshot we need all the tiles
        if (view instanceof IScreenshotView) {
            return true;
        }

        Vec4[] corners = sector.computeCornerPoints(dc.getGlobe(), dc.getVerticalExaggeration());
        Vec4 centerPoint = sector.computeCenterPoint(dc.getGlobe(), dc.getVerticalExaggeration());

        double d1 = view.getEyePoint().distanceTo3(corners[0]);
        double d2 = view.getEyePoint().distanceTo3(corners[1]);
        double d3 = view.getEyePoint().distanceTo3(corners[2]);
        double d4 = view.getEyePoint().distanceTo3(corners[3]);
        double d5 = view.getEyePoint().distanceTo3(centerPoint);

        double minDistance = d1;
        if (d2 < minDistance) minDistance = d2;
        if (d3 < minDistance) minDistance = d3;
        if (d4 < minDistance) minDistance = d4;
        if (d5 < minDistance) minDistance = d5;

        double cellSize = (Math.PI * sector.getDeltaLatRadians() * dc.getGlobe().getRadius()) / 20; // TODO

        return !(Math.log10(cellSize) <= (Math.log10(minDistance) - this.splitScale));
    }

    private boolean atMaxLevel(DrawContext dc) {
        Position vpc = dc.getViewportCenterPosition();
        if (dc.getView() == null || this.getLevels() == null || vpc == null) return false;

        if (!this.getLevels().getSector().contains(vpc.getLatitude(), vpc.getLongitude())) return true;

        Level nextToLast = this.getLevels().getNextToLastLevel();
        if (nextToLast == null) return true;

        Sector centerSector =
            nextToLast.computeSectorForPosition(
                vpc.getLatitude(), vpc.getLongitude(), this.getLevels().getTileOrigin());
        return this.needToSplit(dc, centerSector);
    }

    // ============== Rendering ======================= //
    // ============== Rendering ======================= //
    // ============== Rendering ======================= //

    @Override
    public void render(DrawContext dc) {
        this.atMaxResolution = this.atMaxLevel(dc);
        super.render(dc);
    }

    @Override
    protected final void doRender(DrawContext dc) {
        if (this.forceLevelZeroLoads && !this.levelZeroLoaded) this.loadAllTopLevelTextures(dc);
        if (dc.getSurfaceGeometry() == null || dc.getSurfaceGeometry().size() < 1) return;

        dc.getGeographicSurfaceTileRenderer().setShowImageTileOutlines(this.showImageTileOutlines);

        draw(dc);
    }

    private void draw(DrawContext dc) {
        this.referencePoint = this.computeReferencePoint(dc);

        this.assembleTiles(dc); // Determine the tiles to draw.

        if (this.currentTiles.size() >= 1) {
            MercatorTextureTile[] sortedTiles = new MercatorTextureTile[this.currentTiles.size()];
            sortedTiles = this.currentTiles.toArray(sortedTiles);
            Arrays.sort(sortedTiles, levelComparer);

            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            if (this.isUseTransparentTextures() || this.getOpacity() < 1) {
                gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_POLYGON_BIT | GL2.GL_CURRENT_BIT);
                gl.glColor4d(1d, 1d, 1d, this.getOpacity());
                gl.glEnable(GL.GL_BLEND);
                gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            } else {
                gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_POLYGON_BIT);
            }

            gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);

            dc.setPerFrameStatistic(
                PerformanceStatistic.IMAGE_TILE_COUNT, this.tileCountName, this.currentTiles.size());
            dc.getGeographicSurfaceTileRenderer().renderTiles(dc, this.currentTiles);

            gl.glPopAttrib();

            if (this.drawTileIDs) this.drawTileIDs(dc, this.currentTiles);

            if (this.drawBoundingVolumes) this.drawBoundingVolumes(dc, this.currentTiles);

            this.currentTiles.clear();
        }

        this.sendRequests();
        this.requestQ.clear();
    }

    private void sendRequests() {
        Runnable task = this.requestQ.poll();
        while (task != null) {
            if (!WorldWind.getTaskService().isFull()) {
                WorldWind.getTaskService().addTask(task);
            }

            task = this.requestQ.poll();
        }
    }

    public boolean isLayerInView(DrawContext dc) {
        if (dc == null) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (dc.getView() == null) {
            String message = Logging.getMessage("layers.AbstractLayer.NoViewSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        return !(dc.getVisibleSector() != null && !this.levels.getSector().intersects(dc.getVisibleSector()));
    }

    private Vec4 computeReferencePoint(DrawContext dc) {
        if (dc.getViewportCenterPosition() != null)
            return dc.getGlobe().computePointFromPosition(dc.getViewportCenterPosition());

        Rectangle2D viewport = dc.getView().getViewport();
        int x = (int)viewport.getWidth() / 2;
        for (int y = (int)(0.5 * viewport.getHeight()); y >= 0; y--) {
            Position pos = dc.getView().computePositionFromScreenPoint(x, y);
            if (pos == null) continue;

            return dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(), 0d);
        }

        return null;
    }

    protected Vec4 getReferencePoint() {
        return this.referencePoint;
    }

    private static class LevelComparer implements Comparator<MercatorTextureTile> {
        public int compare(MercatorTextureTile ta, MercatorTextureTile tb) {
            int la = ta.getFallbackTile() == null ? ta.getLevelNumber() : ta.getFallbackTile().getLevelNumber();
            int lb = tb.getFallbackTile() == null ? tb.getLevelNumber() : tb.getFallbackTile().getLevelNumber();

            return la < lb ? -1 : la == lb ? 0 : 1;
        }
    }

    private void drawTileIDs(DrawContext dc, ArrayList<MercatorTextureTile> tiles) {
        Rectangle viewport = dc.getView().getViewport();
        TextRenderer textRenderer =
            OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), Font.decode("Arial-Plain-13"));

        dc.getGL().glDisable(GL.GL_DEPTH_TEST);
        dc.getGL().glDisable(GL.GL_BLEND);
        dc.getGL().glDisable(GL.GL_TEXTURE_2D);

        textRenderer.setColor(Color.YELLOW);
        textRenderer.beginRendering(viewport.width, viewport.height);
        for (MercatorTextureTile tile : tiles) {
            String tileLabel = tile.getLabel();

            if (tile.getFallbackTile() != null) tileLabel += "/" + tile.getFallbackTile().getLabel();

            LatLon ll = tile.getSector().getCentroid();
            Vec4 pt =
                dc.getGlobe()
                    .computePointFromPosition(
                        ll.getLatitude(),
                        ll.getLongitude(),
                        dc.getGlobe().getElevation(ll.getLatitude(), ll.getLongitude()));
            pt = dc.getView().project(pt);
            textRenderer.draw(tileLabel, (int)pt.x, (int)pt.y);
        }

        textRenderer.endRendering();
    }

    private void drawBoundingVolumes(DrawContext dc, ArrayList<MercatorTextureTile> tiles) {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        float[] previousColor = new float[4];
        gl.glGetFloatv(GL2.GL_CURRENT_COLOR, previousColor, 0);
        gl.glColor3d(0, 1, 0);

        for (TextureTile tile : tiles) {
            if (tile.getExtent(dc) instanceof Renderable) ((Renderable)tile.getExtent(dc)).render(dc);
        }

        Box c = Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), this.levels.getSector());
        gl.glColor3d(1, 1, 0);
        c.render(dc);

        gl.glColor4fv(previousColor, 0);
    }

    // ============== Image Composition ======================= //
    // ============== Image Composition ======================= //
    // ============== Image Composition ======================= //

    public List<String> getAvailableImageFormats() {
        return new ArrayList<String>(this.supportedImageFormats);
    }

    public boolean isImageFormatAvailable(String imageFormat) {
        return imageFormat != null && this.supportedImageFormats.contains(imageFormat);
    }

    public String getDefaultImageFormat() {
        return this.supportedImageFormats.size() > 0 ? this.supportedImageFormats.get(0) : null;
    }

    protected void setAvailableImageFormats(String[] formats) {
        this.supportedImageFormats.clear();

        if (formats != null) {
            this.supportedImageFormats.addAll(Arrays.asList(formats));
        }
    }

    private BufferedImage requestImage(MercatorTextureTile tile, String mimeType) throws URISyntaxException {
        String pathBase = tile.getPath().substring(0, tile.getPath().lastIndexOf("."));
        String suffix = WWIO.makeSuffixForMimeType(mimeType);
        String path = pathBase + suffix;
        URL url = this.getDataFileStore().findFile(path, false);

        if (url == null) // image is not local
        return null;

        if (WWIO.isFileOutOfDate(url, tile.getLevel().getExpiryTime())) {
            // The file has expired. Delete it.
            this.getDataFileStore().removeFile(url);
            String message = Logging.getMessage("generic.DataFileExpired", url);
            Logging.logger().fine(message);
        } else {
            try {
                File imageFile = new File(url.toURI());
                BufferedImage image = ImageIO.read(imageFile);
                if (image == null) {
                    String message = Logging.getMessage("generic.ImageReadFailed", imageFile);
                    throw new RuntimeException(message);
                }

                this.levels.unmarkResourceAbsent(tile);
                return image;
            } catch (IOException e) {
                // Assume that something's wrong with the file and delete it.
                this.getDataFileStore().removeFile(url);
                this.levels.markResourceAbsent(tile);
                String message = Logging.getMessage("generic.DeletedCorruptDataFile", url);
                Logging.logger().info(message);
            }
        }

        return null;
    }

    private void downloadImage(final MercatorTextureTile tile, String mimeType) throws Exception {
        //        System.out.println(tile.getPath());
        final URL resourceURL = tile.getResourceURL(mimeType);
        Retriever retriever;

        String protocol = resourceURL.getProtocol();

        if ("http".equalsIgnoreCase(protocol)) {
            retriever = new HTTPRetriever(resourceURL, new HttpRetrievalPostProcessor(tile));
            retriever.setValue(URLRetriever.EXTRACT_ZIP_ENTRY, "true"); // supports legacy layers
        } else {
            String message = Logging.getMessage("layers.TextureLayer.UnknownRetrievalProtocol", resourceURL);
            throw new RuntimeException(message);
        }

        retriever.setConnectTimeout(10000);
        retriever.setReadTimeout(20000);
        retriever.call();
    }

    public int computeLevelForResolution(Sector sector, Globe globe, double resolution) {
        if (sector == null) {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (globe == null) {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        double texelSize = 0;
        Level targetLevel = this.levels.getLastLevel();
        for (int i = 0; i < this.getLevels().getLastLevel().getLevelNumber(); i++) {
            if (this.levels.isLevelEmpty(i)) continue;

            texelSize = this.levels.getLevel(i).getTexelSize();
            if (texelSize > resolution) continue;

            targetLevel = this.levels.getLevel(i);
            break;
        }

        Logging.logger()
            .info(Logging.getMessage("layers.TiledImageLayer.LevelSelection", targetLevel.getLevelNumber(), texelSize));
        return targetLevel.getLevelNumber();
    }

    private class HttpRetrievalPostProcessor implements RetrievalPostProcessor {
        private MercatorTextureTile tile;

        public HttpRetrievalPostProcessor(MercatorTextureTile tile) {
            this.tile = tile;
        }

        public ByteBuffer run(Retriever retriever) {
            if (!retriever.getState().equals(Retriever.RETRIEVER_STATE_SUCCESSFUL)) return null;

            HTTPRetriever htr = (HTTPRetriever)retriever;
            if (htr.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                // Mark tile as missing to avoid excessive attempts
                MercatorTiledImageLayerBase.this.levels.markResourceAbsent(tile);
                return null;
            }

            if (htr.getResponseCode() != HttpURLConnection.HTTP_OK) return null;

            URLRetriever r = (URLRetriever)retriever;
            ByteBuffer buffer = r.getBuffer();

            String suffix = WWIO.makeSuffixForMimeType(htr.getContentType());
            if (suffix == null) {
                return null; // TODO: log error
            }

            String path = tile.getPath().substring(0, tile.getPath().lastIndexOf("."));
            path += suffix;

            final File outFile = getDataFileStore().newFile(path);
            if (outFile == null) return null;

            try {
                WWIO.saveBuffer(buffer, outFile);
                return buffer;
            } catch (IOException e) {
                e.printStackTrace(); // TODO: log error
                return null;
            }
        }
    }
}
