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
import com.intel.missioncontrol.airmap.data.Loader;
import com.intel.missioncontrol.airmap.layer.AirSpaceObjectFactory;
import com.intel.missioncontrol.airspace.render.TiledRenderableLayer;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.map.annotation.airspaces.AirspacesToolTipController;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AirspaceTooltipTest extends ApplicationTemplate {
    private static final String KEY_AIRSPACE_OBJECT = AirspaceTooltipTest.class.getName();

    public static class AirMapLayer {
        private RenderableLayer layer;
        private Collection<AirSpaceObject> airSpaceObjects;

        AirSpaceObjectFactory objectFactory;

        public AirMapLayer(WorldWindow wwd) {
            setupLayer();
        }

        void setupLayer() {
            layer = new RenderableLayer() {
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
                        System.out.println("visible sectors: "+visibleSector);

                    }

//                System.out.println("other sectors: "+ surfaceGeometry.getSector());
                }

                Sector sector;

                @Override
                public void render(DrawContext dc) {
                    super.render(dc);
//                Sector s = new Sector()
//                Extent e = Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), )
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


    public static class AppFrame extends ApplicationTemplate.AppFrame {
        private static final String DEMO_AIRSPACES_PATH = "./airspaces2.json";

        AirMapLayer airMapLayer;

        public AppFrame() {
            super(true, true, false);

            setToolTipController(new AirspacesToolTipController(getWwd(), new LanguageHelper()));
            try {
                setupAirSpaceLayer();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        void setupAirSpaceLayer() throws IOException {
            LinkedList<AirSpaceObject> airspaces = new LinkedList<>();
            LatLon eyePos = doLoadAirspaces( airspaces);

            airMapLayer = new AirMapLayer(this.getWwd());
            airMapLayer.addAirSpaces(airspaces);

            System.out.printf("Adding %d AirSpaces\n", airspaces.size());


            // Add the layer to the model.
            insertBeforePlacenames(this.getWwd(), airMapLayer.getLayer());

            // Adjust the view so that it looks at the buildings.
            View view = getWwd().getView();
            view.setEyePosition(Position.fromDegrees(eyePos.getLatitude().degrees, eyePos.getLongitude().degrees, 1e3));


//            getWwd().addPositionListener(new PositionListener() {
//                @Override
//                public void moved(PositionEvent event) {
//                    Position position = event.getPosition();
//                    position.l
//
//                }
//            });
            // This is how a select listener would notice that one of the shapes was picked.
            getWwd().addSelectListener(new SelectListener() {
                public void selected(SelectEvent event) {
                    if (event.getTopObject() instanceof ExtrudedPolygon) {

                    }

//                            System.out.println("EXTRUDED POLYGON");
                }
            });
        }
    }


    private static LatLon doLoadAirspaces(Collection<AirSpaceObject> objects) throws IOException {
        BufferedInputStream is = new BufferedInputStream(AirspaceTooltipTest.class.getResourceAsStream( "/airmap/airmap_response.json"));
        Reader reader = new InputStreamReader(is, Charset.forName("utf-8"));

        Collection<AirSpaceObject> airSpaceObjects = Loader.loadFromAirmapJson(reader);
        objects.addAll(airSpaceObjects);

        LatLon eyePos = LatLon.ZERO;
        if (airSpaceObjects.iterator().hasNext()) {
            AirSpaceObject next = airSpaceObjects.iterator().next();
            eyePos = next.getPosition();
        }
        return eyePos;
    }

    public static void main(String[] args) {
        ApplicationTemplate.start("WorldWind Extruded Polygons on Ground", AirspaceTooltipTest.AppFrame.class);
    }
}
