/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.intel.missioncontrol.airmap.data.AirSpaceObject;
import com.intel.missioncontrol.airmap.data.AirportAirspaceObject;
import com.intel.missioncontrol.airmap.data.Loader;
import com.intel.missioncontrol.airmap.layer.AirSpaceObjectFactory;
import com.intel.missioncontrol.airspace.render.TiledRenderableLayer;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ExtrudedPolygon;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.RandomShapeAttributes;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The purpose of this App is to test loading of AirMap JSON (off of disk or resources) and the conversion into
 * renderables. Without the complexity of using {@link TiledRenderableLayer} with a complicated TileLoader like {@link
 * com.intel.missioncontrol.airmap.layer.AirMapTileLoader2} and without having to deal with loading AirMap data from the
 * network (so no {@link com.intel.missioncontrol.airmap.network.AirMapClient2}
 *
 * <p>This requires no network access and can be run standalone. (unless someone broke worldwind again)
 */
public class AirspaceTestApp2 extends ApplicationTemplate {
    private static final String KEY_AIRSPACE_OBJECT = AirspaceTestApp2.class.getName();

    public static class AirMapLayer {

        private RenderableLayer layer;
        private Collection<AirSpaceObject> airSpaceObjects;

        AirSpaceObjectFactory objectFactory;
        //    private Map<String, AirSpaceRenderableFactory> factories;

        public AirMapLayer(WorldWindow wwd) {
            setupLayer();
        }

        void setupLayer() {

            //        Level l = new Level();

            layer =
                new RenderableLayer() {
                    @Override
                    protected void doPreRender(DrawContext dc) {
                        super.doPreRender(dc);
                        //                SectorGeometryList surfaceGeometry = dc.getSurfaceGeometry();
                        //
                        //                return tile.getExtent(dc).intersects(
                        //                        dc.getView().getFrustumInModelCoordinates())
                        //                        && (dc.getVisibleSector() == null || dc.getVisibleSector()
                        //                        .intersects(tile.getSector()));
                        //
                        if (false) {
                            Sector visibleSector = dc.getVisibleSector();
                            System.out.println("visible sectors: " + visibleSector);
                        }

                        //                System.out.println("other sectors: "+ surfaceGeometry.getSector());
                    }

                    Sector sector;

                    @Override
                    public void render(DrawContext dc) {
                        super.render(dc);
                        //                Sector s = new Sector()
                        //                Extent e = Sector.computeBoundingBox(dc.getGlobe(),
                        // dc.getVerticalExaggeration(), )
                        //                dc.getView().getFrustumInModelCoordinates().get
                    }
                };

            layer.setName("Airspaces");
            layer.setPickEnabled(true);
            layer.setMaxActiveAltitude(40000.0);

            objectFactory = new AirSpaceObjectFactory();
        }

        public RenderableLayer getLayer() {
            return layer;
        }

        public void addAirSpaces(Collection<AirSpaceObject> objs) {
            airSpaceObjects = objs;

            for (AirSpaceObject obj : objs) {
                Collection<Renderable> r = objectFactory.createRenderableForAirspace(obj);
                if (r != null) {
                    layer.addRenderables(r);
                }
            }
        }

    }

    public static class TileLoader implements TiledRenderableLayer.TileRenderableManager {
        Set<Renderable> renderables = new HashSet<>();

        LoadingCache<Tile, Renderable> cache;
        RandomShapeAttributes randAttr = new RandomShapeAttributes();

        TileLoader() {
            cache =
                CacheBuilder.newBuilder()
                    .maximumSize(100)
                    .removalListener(
                        new RemovalListener<Tile, Renderable>() {
                            @Override
                            public void onRemoval(RemovalNotification<Tile, Renderable> removalNotification) {
                                System.out.println("removing tile: " + removalNotification.getKey());
                            }
                        })
                    .build(
                        new CacheLoader<Tile, Renderable>() {
                            @Override
                            public Renderable load(Tile tile) throws Exception {
                                System.out.println("creating tile: " + tile);
                                return createRenderableForTile(tile);
                            }
                        });
        }

        List<LatLon> getPolys(Sector s) {
            Sector sector = s.subdivide()[0];
            List<LatLon> latLons = sector.asList();
            latLons.add(sector.iterator().next());

            return latLons;
        }

        int i = 0;

        private Renderable createRenderableForTile(Tile tile) {
            ShapeAttributes attr = randAttr.nextAttributes().asShapeAttributes();
            SurfacePolygon polygon = new SurfacePolygon(attr, getPolys(tile.getSector()));
            polygon.setValue(
                AVKey.DISPLAY_NAME, String.format("tile %d/%d\n(seq. %d)", tile.getRow(), tile.getColumn(), i++));
            return polygon;
        }

        @Override
        public void onTilesWillBecomeVisible(TiledRenderableLayer layer, Set<Tile> newTiles) {
            //            Sets.SetView<Tile> removed = Sets.difference(tiles.keySet(), newTiles);
            //            Sets.SetView<Tile> added = Sets.difference(newTiles, tiles.keySet());
            //

            for (Renderable r : renderables) {
                layer.disposeRenderable(r);
            }

            renderables.clear();

            for (Tile tile : newTiles) {
                Renderable renderable = cache.getUnchecked(tile);
                if (renderable != null) {
                    layer.registerRenderable(renderable);
                    renderables.add(renderable);
                }
            }
        }

        @Override
        public Iterable<? extends Renderable> onPreRender(TiledRenderableLayer layer) {
            return renderables;
        }

        @Override
        public Iterable<? extends Renderable> onRender(TiledRenderableLayer layer) {
            return renderables;
        }

        @Override
        public Iterable<? extends Renderable> onPick(TiledRenderableLayer layer) {
            return renderables;
        }

        @Override
        public Iterable<? extends Renderable> onMessage(TiledRenderableLayer layer) {
            return renderables;
        }

        @Override
        public void registerRedrawCallback(Runnable callback) {}
    }

    public static class AppFrame extends ApplicationTemplate.AppFrame {
        private static final String DEMO_AIRSPACES_PATH = "./airspaces2.json";

        AirMapLayer airMapLayer;

        public AppFrame() {
            super(true, true, false);

            try {
                setupAirSpaceLayer();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Layer createTestLayer() {
            AVList params = new AVListImpl();

            params.setValue(AVKey.TILE_WIDTH, 256);
            params.setValue(AVKey.TILE_HEIGHT, 256);
            params.setValue(AVKey.DATA_CACHE_NAME, "Earth/OSM-Mercator/OpenStreetMap Cycle");
            params.setValue(AVKey.SERVICE, "http://b.andy.sandbox.cloudmade.com/tiles/cycle/");
            params.setValue(AVKey.DATASET_NAME, "*");
            params.setValue(AVKey.FORMAT_SUFFIX, ".png");
            params.setValue(AVKey.NUM_LEVELS, 9);

            params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
            params.setValue(
                AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(85.0 / 2.0), Angle.fromDegrees(45d)));

            params.setValue(
                AVKey.SECTOR,
                new Sector(Angle.fromDegrees(-85.0), Angle.fromDegrees(85.0), Angle.NEG180, Angle.POS180));
            //            params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0,
            //                    Angle.NEG180, Angle.POS180));
            //            params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder());

            //            params.setValue(AVKey.INACTIVE_LEVELS, "0,1,2,3,4");

            LevelSet l = new LevelSet(params);

            //            TilingLayer layer = new TilingLayer(l);

            TiledRenderableLayer layer = new TiledRenderableLayer(new TileLoader(), l);

            //            layer.setDrawDebug(true);
            //
            //            MercatorTiledImageLayer layer = new BasicMercatorTiledImageLayer(l);
            //            layer.setForceLevelZeroLoads(true);
            //
            //            layer.setNetworkRetrievalEnabled(false);
            //            layer.setDrawTileBoundaries(true);
            //            layer.setDrawTileIDs(true);
            //            layer.setDrawBoundingVolumes(true);
            //            layer.setName("FAIL");

            return layer;
        }

        void setupAirSpaceLayer() throws IOException {
            // Create a layer for the shapes.
            //                RenderableLayer layer = new RenderableLayer();
            //                layer.setName("Extruded Shapes");
            //                layer.setPickEnabled(true);
            //                layer.setMaxActiveAltitude(40000.0);

            LinkedList<AirSpaceObject> airspaces = new LinkedList<>();
            LatLon eyePos = doLoadAirspaces(airspaces);

            airMapLayer = new AirMapLayer(this.getWwd());
            airMapLayer.addAirSpaces(airspaces);

            System.out.printf("Adding %d AirSpaces\n", airspaces.size());

            // Add the layer to the model.
            insertBeforePlacenames(this.getWwd(), airMapLayer.getLayer());

            // Adjust the view so that it looks at the buildings.
            View view = getWwd().getView();
            view.setEyePosition(Position.fromDegrees(eyePos.getLatitude().degrees, eyePos.getLongitude().degrees, 1e3));

            insertBeforePlacenames(this.getWwd(), createTestLayer());

            //            getWwd().addPositionListener(new PositionListener() {
            //                @Override
            //                public void moved(PositionEvent event) {
            //                    Position position = event.getPosition();
            //                    position.l
            //
            //                }
            //            });
            // This is how a select listener would notice that one of the shapes was picked.
            getWwd().addSelectListener(
                    new SelectListener() {
                        public void selected(SelectEvent event) {
                            if (event.getTopObject() instanceof ExtrudedPolygon) {}

                            //                            System.out.println("EXTRUDED POLYGON");
                        }
                    });
        }
    }

    static File airspacesJsonFile = null;

    private static LatLon doLoadAirspaces(Collection<AirSpaceObject> objects) throws IOException {
        BufferedInputStream is;
        if (airspacesJsonFile == null) {
            is = new BufferedInputStream(AirspaceTestApp2.class.getResourceAsStream("/airmap/airmap_response.json"));
        } else {
            is = new BufferedInputStream(new FileInputStream(airspacesJsonFile));
        }

        Reader reader = new InputStreamReader(is, Charset.forName("utf-8"));

        return doLoadAirspaces(objects, reader);
    }

    private static LatLon doLoadAirspaces(Collection<AirSpaceObject> objects, Reader reader) throws IOException {
        Collection<AirSpaceObject> airSpaceObjects = Loader.loadFromAirmapJson(reader);
        objects.addAll(airSpaceObjects);

        List<AirSpaceObject> collect =
            objects.stream().filter(s -> s instanceof AirportAirspaceObject).collect(Collectors.toList());

        LatLon eyePos = LatLon.ZERO;
        if (airSpaceObjects.iterator().hasNext()) {
            AirSpaceObject next = airSpaceObjects.iterator().next();
            eyePos = next.getPosition();
        }

        return eyePos;
    }

    /**
     * pass filename as arguement on disk JSON file, e.g to use something from airmap cache from IMC:
     *
     * <p>'C:\Users\UserName\AppData\Roaming\Intel Mission
     * Control\cache\airmap2-cache\29f0246be47838d41bff86c12e1eae7e.1'
     *
     * <p>otherwise use test resource
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            Path path = Paths.get(args[0]);
            File f = path.toFile();
            if (f == null || !f.exists()) {
                System.err.printf("error: '%s' is not a file \n", args[0]);
                System.err.printf("usage: app [<AIRSPACE_JSON_FILE>]\n");
                System.exit(1);
            } else {
                airspacesJsonFile = f;
            }
        }

        ApplicationTemplate.start("WorldWind Extruded Polygons on Ground", AirspaceTestApp2.AppFrame.class);
    }
}
