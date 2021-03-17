/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspace.render;

import com.google.common.collect.Sets;
import com.intel.missioncontrol.airmap.layer.TileMapper;
import com.jogamp.opengl.GL2;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.Locatable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.event.MessageListener;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PreRenderable;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.SphereAirspace;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.asyncfx.concurrent.SynchronizationRoot;

public class TiledRenderableLayer extends AbstractLayer {
    protected final PickSupport pickSupport = new PickSupport();

    private TileMapper tileMapper;
    private final Set<Tile> visibleTiles = new HashSet<Tile>();

    private TileRenderableManager renderableManager;
    private double geometrySegmentDistanceThreshold;

    Collection<Renderable> debugRenderable;
    boolean debug;

    SynchronizationRoot syncRoot;

    public interface TileRenderableManager {
        void onTilesWillBecomeVisible(TiledRenderableLayer layer, Set<Tile> tiles);

        Iterable<? extends Renderable> onPreRender(TiledRenderableLayer layer);

        Iterable<? extends Renderable> onRender(TiledRenderableLayer layer);

        Iterable<? extends Renderable> onPick(TiledRenderableLayer layer);

        Iterable<? extends Renderable> onMessage(TiledRenderableLayer layer);

        void registerRedrawCallback(Runnable callback);

        default void dispose() {}
    }

    public static interface ISource {
        TileRenderableManager getTileLoader();

        TileMapper getTileMapper();
    }

    ISource source;

    public TiledRenderableLayer(ISource source, SynchronizationRoot syncRoot) {
        this(source.getTileLoader(), source.getTileMapper());
        this.source = source;
        this.syncRoot = syncRoot;
    }

    public ISource getSource() {
        return source;
    }

    public TiledRenderableLayer(TileRenderableManager manager, TileMapper tileMapper) {
        this.renderableManager = manager;
        this.tileMapper = tileMapper;
        // setPickEnabled(false);
        renderableManager.registerRedrawCallback(
            () -> {
                if (syncRoot != null) {
                    syncRoot.runAsync(() -> firePropertyChange(AVKey.LAYER, null, TiledRenderableLayer.this));
                }
            });
        setMaxActiveAltitude(100_000);
        setMinActiveAltitude(1_000);

        // todo: expose debug mode as constructor param or method, move this to where TiledRenderableLayer is created,
        // todo: otherwise this breaks AirspaceTestApp
        //        BooleanBinding debugBind =
        //            DependencyInjector.getInjector() == null ? null : DependencyInjector.getInjector()
        //                .getInstance(ISettingsManager.class)
        //                .getSection(GeneralSettings.class)
        //                .operationLevelProperty()
        //                .isEqualTo(OperationLevel.DEBUG);
        //
        //        if (debugBind != null) {
        //            debugBind.addListener(
        //                    (observable, oldValue, newValue) ->
        //                            debug =
        //                                    newValue); // TODO FIXME this layer seems not to take debug flag changes
        // into account jet... even
        //            // when the flag is set
        //            debug = debugBind.get();
        //        }
    }

    public TiledRenderableLayer(TileRenderableManager manager, LevelSet tileLevels) {
        this(manager, new TileMapper(tileLevels));
    }

    @Override
    public void setMaxActiveAltitude(double maxActiveAltitude) {
        super.setMaxActiveAltitude(maxActiveAltitude);
        geometrySegmentDistanceThreshold = maxActiveAltitude * 1.5;
    }

    @Override
    public void dispose() {
        super.dispose();

        renderableManager.dispose();
    }

    private static boolean isTileVisible(DrawContext dc, Tile tile) {
        Box tileExtent = Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), tile.getSector());
        return tileExtent.intersects(dc.getView().getFrustumInModelCoordinates())
            && (dc.getVisibleSector() == null || dc.getVisibleSector().intersects(tile.getSector()));
    }

    private void getVisibleTiles(Collection<Tile> tiles, DrawContext dc, double distanceThreshold) {
        SectorGeometryList sgList = dc.getSurfaceGeometry();
        if (sgList == null || sgList.size() == 0) return;

        if (debug) {
            Sector visibleSector = dc.getVisibleSector();
            if (debugRenderable == null) debugRenderable = new ArrayList<>();

            debugRenderable.clear();
            LatLon centroid1 = visibleSector.getCentroid();
            SphereAirspace airspace = new SphereAirspace(centroid1, 500);
            airspace.setAltitude(0);
            AirspaceAttributes a = new BasicAirspaceAttributes();
            a.setOutlineMaterial(Material.PINK);
            a.setInteriorMaterial(Material.PINK);
            airspace.setAttributes(a);
            airspace.setTerrainConforming(true, true);
            debugRenderable.add(airspace);
        }

        Sector sector = sgList.getSector();
        HashSet<Tile> inRange = new HashSet<>();
        tileMapper.computeTilesForSector(sector, inRange);
        final Vec4 eyePoint = dc.getView().getEyePoint();

        inRange.removeIf(
            tile -> {
                boolean notVisible = !isTileVisible(dc, tile);

                Vec4 tileCentroid = sgList.getSurfacePoint(tile.getSector().getCentroid());
                boolean outOfRange = tileCentroid != null && eyePoint.distanceTo3(tileCentroid) > distanceThreshold;

                return notVisible || outOfRange;
            });
        tiles.addAll(inRange);
    }
    /**
     * Register renderable to receive property change events
     *
     * @param renderable
     */
    public void registerRenderable(Renderable renderable) {
        if (renderable == null) {
            String msg = Logging.getMessage("nullValue.RenderableIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (renderable instanceof AVList) ((AVList)renderable).addPropertyChangeListener(this);
    }

    // from RenderableLayer#dispose

    /**
     * remove renderagle from receiving property change events and call dispose if necessary
     *
     * @param renderable
     */
    public void disposeRenderable(Renderable renderable) {
        try {
            // Remove the layer as a property change listener of the renderable. This prevents the renderable
            // from keeping a dangling reference to the layer.
            if (renderable instanceof AVList) ((AVList)renderable).removePropertyChangeListener(this);

            if (renderable instanceof Disposable) ((Disposable)renderable).dispose();
        } catch (Exception e) {
            String msg = Logging.getMessage("generic.ExceptionAttemptingToDisposeRenderable");
            Logging.logger().severe(msg);
            // continue to next renderable
        }
    }

    @Override
    protected void doPreRender(DrawContext dc) {
        // what tiles are visible
        calculateVisibleTiles(dc);

        // generate tiles
        renderableManager.onTilesWillBecomeVisible(this, visibleTiles);

        // load renderables
        final Iterable<? extends Renderable> renderables = renderableManager.onPreRender(this);
        for (Renderable renderable : renderables) {
            try {
                // If the caller has specified their own Iterable,
                // then we cannot make any guarantees about its contents.
                if (renderable != null && renderable instanceof PreRenderable)
                    ((PreRenderable)renderable).preRender(dc);
            } catch (Exception e) {
                String msg = Logging.getMessage("generic.ExceptionWhilePrerenderingRenderable");
                Logging.logger().severe(msg);
                // continue to next renderable
            }
        }

        if (debug && debugRenderable != null) {
            for (Renderable renderable : debugRenderable) {
                if (renderable instanceof PreRenderable) ((PreRenderable)renderable).preRender(dc);
            }
        }
    }

    @Override
    protected void doPick(DrawContext dc, java.awt.Point pickPoint) {
        this.doPick(dc, renderableManager.onPick(this), pickPoint);
    }

    @Override
    protected void doRender(DrawContext dc) {
        if (debug && debugRenderable != null) {
            for (Renderable renderable : debugRenderable) {
                if (renderable != null) renderable.render(dc);
            }
        }

        this.doRender(dc, renderableManager.onRender(this));
    }

    @Override
    public void onMessage(Message message) {
        this.onMessage(message, renderableManager.onMessage(this));
    }

    private void calculateVisibleTiles(DrawContext dc) {
        Set<Tile> nextVisible = new HashSet<>();
        getVisibleTiles(nextVisible, dc, getDistanceThreshold());

        Set<Tile> tilesAdded = Sets.difference(nextVisible, visibleTiles);
        Set<Tile> tilesRemoved = Sets.difference(visibleTiles, nextVisible);

        int added = tilesAdded.size();
        int removed = tilesRemoved.size();
        if (added > 0 || removed > 0) {
            Debug.getLog()
                .info(
                    String.format(
                        "Total airmap tiles: %d (added %d, removed %d)\n", nextVisible.size(), added, removed));
        }

        visibleTiles.clear();
        visibleTiles.addAll(nextVisible);
    }

    protected void doPick(DrawContext dc, Iterable<? extends Renderable> renderables, java.awt.Point pickPoint) {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        this.pickSupport.clearPickList();
        this.pickSupport.beginPicking(dc);

        try {
            for (Renderable renderable : renderables) {
                // If the caller has specified their own Iterable,
                // then we cannot make any guarantees about its contents.
                if (renderable != null) {
                    //                    float[] inColor = new float[4];
                    //                    gl.glGetFloatv(GL.GL_CURRENT_COLOR, inColor, 0);
                    java.awt.Color color = dc.getUniquePickColor();
                    gl.glColor3ub((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue());

                    try {
                        renderable.render(dc);
                    } catch (Exception e) {
                        String msg = Logging.getMessage("generic.ExceptionWhilePickingRenderable");
                        Logging.logger().severe(msg);
                        Logging.logger().log(java.util.logging.Level.FINER, msg, e); // show exception for this level
                        continue; // go on to next renderable
                    }
                    //
                    //                    gl.glColor4fv(inColor, 0);

                    if (renderable instanceof Locatable) {
                        this.pickSupport.addPickableObject(
                            color.getRGB(), renderable, ((Locatable)renderable).getPosition(), false);
                    } else {
                        this.pickSupport.addPickableObject(color.getRGB(), renderable);
                    }
                }
            }

            this.pickSupport.resolvePick(dc, pickPoint, this);
        } finally {
            this.pickSupport.endPicking(dc);
        }
    }

    protected void doRender(DrawContext dc, Iterable<? extends Renderable> renderables) {
        for (Renderable renderable : renderables) {
            try {
                // If the caller has specified their own Iterable,
                // then we cannot make any guarantees about its contents.
                if (renderable != null) renderable.render(dc);
            } catch (Exception e) {
                String msg = Logging.getMessage("generic.ExceptionWhileRenderingRenderable");
                Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
                // continue to next renderable
            }
        }
    }

    protected void onMessage(Message message, Iterable<? extends Renderable> renderables) {
        for (Renderable renderable : renderableManager.onMessage(this)) {
            try {
                if (renderable instanceof MessageListener) ((MessageListener)renderable).onMessage(message);
            } catch (Exception e) {
                String msg = Logging.getMessage("generic.ExceptionInvokingMessageListener");
                Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
                // continue to next renderable
            }
        }
    }

    public double getDistanceThreshold() {
        return geometrySegmentDistanceThreshold;
    }
}
