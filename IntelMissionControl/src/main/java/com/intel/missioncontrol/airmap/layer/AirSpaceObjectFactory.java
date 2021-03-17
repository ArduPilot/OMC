/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.layer;

import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.ControlledAirspace;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.Park;

import com.google.common.collect.ImmutableList;
import com.intel.missioncontrol.airmap.data.AirSpaceObject;
import com.intel.missioncontrol.airmap.data.AirportAirspaceObject;
import com.intel.missioncontrol.airmap.data.ControlledAirspaceObject;
import com.intel.missioncontrol.airmap.data.GeoJson;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.Polygon;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Create renderables from AirMap Objects */
public class AirSpaceObjectFactory {
    public static final String KEY_AIRSPACE_OBJECT = AirSpaceObject.class.getName();
    private static final double AIRPORT_FENCE_HEIGHT = 30.0;

    // all my factories
    private AirportFactory airportFactory;
    private GenericFactory factory;
    private ControlledAirspaceAndOtherAiryTypeObjectFactory controlledAirSpaceFactory;

    // I guess this is a FactoryFactory...
    public AirSpaceObjectFactory() {
        airportFactory = new AirportFactory();
        factory = new GenericFactory();
        controlledAirSpaceFactory = new ControlledAirspaceAndOtherAiryTypeObjectFactory();
    }

    static Color changeBrightness(Color color, float lightnessDelta) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        // brightness
        float b = hsb[2] + lightnessDelta;
        hsb[2] = Math.max(0.f, Math.min(1.f, b));
        int c = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return new Color((c & 0x00ffffff) | (color.getAlpha() << 24));
    }

    // Every factory needs a factory method...
    public Collection<Renderable> createRenderableForAirspace(AirSpaceObject obj) {
        List<Renderable> renderables = null;
        switch (obj.getType()) {

            // these are normally a no-fly
        case Airport:
            renderables = airportFactory.createForAirSpace(obj);
            break;
        case Heliport:
            renderables = controlledAirSpaceFactory.createForHeliport(obj);
            break;
            // NFZ in DE
        case Emergencies:
        case PowerPlant:
        case Hospitals:
        case Prison:
            renderables = controlledAirSpaceFactory.createForNFZ(obj);
            break;
            // require authorisation and big
        case SpecialUse:
            renderables = controlledAirSpaceFactory.createForSpecialUse(obj);
            break;
        case ControlledAirspace:
            renderables = controlledAirSpaceFactory.createForAirSpace(obj);
            break;
            // designated but flyable
        case Custom:
        case Park:
            renderables = factory.createForAirSpace(obj);
            break;
        }

        if (renderables != null) {
            Collections.reverse(renderables);
        }

        return renderables;
    }

    private enum ColorSets {
        NO(new Color(208, 2, 27), new Color(255, 95, 95)),

        AUTH(new Color(243, 213, 78), new Color(255, 163, 0)),

        AIRSPACE(new Color(0, 113, 197), new Color(0, 113, 197)),

        HOVER(
            new Color(255, 255, 255), // currently not used by red/yellow airspaces
            new Color(255, 255, 255)),
        ;

        private final Color fillColor;
        private final Color outlineColor;

        ColorSets(Color fillColor, Color outlineColor) {
            this.fillColor = fillColor;
            this.outlineColor = outlineColor;
        }
    }

    private enum AirSpaceColors {
        // red

        NFZ(ColorSets.NO, 0.7, 0.2, 1),
        GENERIC(ColorSets.NO, 0.7, 0.2, 1), // parks

        // yellow
        AIRPORTS(ColorSets.AUTH, 0.9, 0.2, 2), // airport fence zone
        CONTROLLED_WEIRD(ColorSets.AUTH, 0.5, 0.1, 1),
        CONTROLLED_HELIPORT(ColorSets.AUTH, 0.6, 0.2, 1),

        // blue
        CONTROLLED_SPECIAL(ColorSets.AIRSPACE, 0.5, 0.2, 1),
        CONTROLLED_CLASS_B(ColorSets.AIRSPACE, 0.5, 0.2, 1),
        CONTROLLED_CLASS_C(ColorSets.AIRSPACE, 0.5, 0.2, 1),
        CONTROLLED_CLASS_D(ColorSets.AIRSPACE, 0.5, 0.2, 1),
        CONTROLLED_CLASS_E(ColorSets.AIRSPACE, 0.5, 0.2, 1),

        // hovers
        AIRSPACE_HIGHLIGHT(ColorSets.AIRSPACE, 1, 0.2, 2), // For all controlled airspaces when highlighted
        GENERIC_HIGHLIGHT(ColorSets.HOVER, 1, 0.4, 2);

        private final ColorSets airspaceColor;
        private final double opacityOutline;
        private final double opacityInterior;
        private final double lineWidth;

        AirSpaceColors(ColorSets airspaceColor, double opacityOutline, double opacityInterior, double lineWidth) {
            this.airspaceColor = airspaceColor;
            this.opacityOutline = opacityOutline > 1.0 ? 1.0 : opacityOutline;
            this.opacityInterior = opacityInterior > 1.0 ? 1.0 : opacityInterior;
            this.lineWidth = lineWidth;
        }

        AirspaceAttributes toAttributes(AirspaceAttributes base) {
            BasicAirspaceAttributes attr = new BasicAirspaceAttributes(base);
            attr.setOutlineWidth(lineWidth);
            attr.setInteriorOpacity(opacityInterior);
            attr.setOutlineOpacity(opacityOutline);
            attr.setOutlineMaterial(new Material(airspaceColor.outlineColor));
            attr.setInteriorMaterial(new Material(airspaceColor.fillColor));
            return attr;
        }
    }

    // Hold highlight and base attributes
    static class AirStyle {
        AirspaceAttributes base;
        AirspaceAttributes highlight;

        static AirStyle createAirStyle(AirSpaceColors normal, AirSpaceColors hover, AirspaceAttributes base_) {
            AirStyle style = new AirStyle();
            style.base = normal.toAttributes(base_);
            style.highlight = hover.toAttributes(style.base);
            return style;
        }
    }

    static class ControlledAirspaceAndOtherAiryTypeObjectFactory {
        AirStyle classB;
        AirStyle classC;
        AirStyle classD;
        AirStyle classE;

        AirStyle special;
        AirStyle heliport;
        AirStyle weirdAirport;
        AirStyle nfz;

        ControlledAirspaceAndOtherAiryTypeObjectFactory() {
            BasicAirspaceAttributes template = new BasicAirspaceAttributes();
            template.setDrawOutline(true);
            template.setEnableLighting(false);

            classB =
                AirStyle.createAirStyle(AirSpaceColors.CONTROLLED_CLASS_B, AirSpaceColors.AIRSPACE_HIGHLIGHT, template);
            classC =
                AirStyle.createAirStyle(AirSpaceColors.CONTROLLED_CLASS_C, AirSpaceColors.AIRSPACE_HIGHLIGHT, template);

            {
                BasicAirspaceAttributes attr = new BasicAirspaceAttributes(template);
                attr.setOutlineStipplePattern((short)0x07FF);
                attr.setOutlineStippleFactor(1);
                classD =
                    AirStyle.createAirStyle(AirSpaceColors.CONTROLLED_CLASS_D, AirSpaceColors.AIRSPACE_HIGHLIGHT, attr);
            }

            {
                BasicAirspaceAttributes attr = new BasicAirspaceAttributes(template);
                attr.setOutlineStipplePattern((short)0x03FF);
                attr.setOutlineStippleFactor(1);
                classE =
                    AirStyle.createAirStyle(AirSpaceColors.CONTROLLED_CLASS_E, AirSpaceColors.AIRSPACE_HIGHLIGHT, attr);
            }

            weirdAirport =
                AirStyle.createAirStyle(AirSpaceColors.CONTROLLED_WEIRD, AirSpaceColors.GENERIC_HIGHLIGHT, template);

            heliport =
                AirStyle.createAirStyle(AirSpaceColors.CONTROLLED_HELIPORT, AirSpaceColors.GENERIC_HIGHLIGHT, template);

            special =
                AirStyle.createAirStyle(AirSpaceColors.CONTROLLED_SPECIAL, AirSpaceColors.AIRSPACE_HIGHLIGHT, template);

            nfz = AirStyle.createAirStyle(AirSpaceColors.NFZ, AirSpaceColors.GENERIC_HIGHLIGHT, template);
        }

        AirStyle getAttrForAirspace(ControlledAirspaceObject object) {
            String airspaceClass = object.getAirspaceClass();
            switch (airspaceClass) {
            case "B":
                return classB;
            case "C":
                return classC;
            case "D":
                return classD;
            case "E":
                return classE;
            default:
                return classE;
            }
        }

        public List<Renderable> createForHeliport(AirSpaceObject object) {
            GeoJson.PolygonGeom geom = (GeoJson.PolygonGeom)object.geometry;
            SurfacePolygon circle = new SurfacePolygon(heliport.base, geom.coordinates.get(0));
            circle.setAttributes(heliport.base);
            circle.setHighlightAttributes(heliport.highlight);

            circle.setValue(AVKey.DISPLAY_NAME, object.type + ": " + object.name);
            circle.setValue(KEY_AIRSPACE_OBJECT, object);
            return ImmutableList.of(circle);
        }

        // for little airports without property border
        public List<Renderable> createForWeirdAirports(GeoJson.Geometry geometry, AirportAirspaceObject object) {
            if (!(geometry instanceof GeoJson.GeometryExtended)) {
                return Collections.emptyList();
            }

            var geoms = ((GeoJson.GeometryExtended)geometry).getOutsidePolys();
            var renderables = new ArrayList<Renderable>();

            for (List<LatLon> geom : geoms) {
                var poly = new SurfacePolygon(weirdAirport.base);
                poly.setHighlightAttributes(weirdAirport.highlight);
                poly.setValue(AVKey.DISPLAY_NAME, object.name);
                poly.setValue(KEY_AIRSPACE_OBJECT, object);
                poly.setOuterBoundary(geom);
                renderables.add(poly);
            }

            return renderables;
        }

        public List<Renderable> createForSpecialUse(AirSpaceObject object) {
            var geoms = object.geometry.getOutsidePolys();
            var renderables = new ArrayList<Renderable>();

            for (List<LatLon> geom : geoms) {
                var poly = new SurfacePolygon(special.base);
                poly.setHighlightAttributes(special.highlight);
                poly.setValue(AVKey.DISPLAY_NAME, object.name);
                poly.setValue(KEY_AIRSPACE_OBJECT, object);
                poly.setOuterBoundary(geom);
                renderables.add(poly);
            }

            return renderables;
        }

        public List<Renderable> createForNFZ(AirSpaceObject object) {
            var geoms = object.geometry.getOutsidePolys();
            var renderables = new ArrayList<Renderable>();

            for (List<LatLon> geom : geoms) {
                var poly = new SurfacePolygon(nfz.base);
                poly.setHighlightAttributes(nfz.highlight);
                poly.setValue(AVKey.DISPLAY_NAME, object.name);
                poly.setValue(KEY_AIRSPACE_OBJECT, object);
                poly.setOuterBoundary(geom);
                renderables.add(poly);
            }

            return renderables;
        }

        public List<Renderable> createForAirSpace(AirSpaceObject object_) {
            if (object_.getType() != ControlledAirspace) {
                return null;
            }

            ControlledAirspaceObject object = (ControlledAirspaceObject)object_;
            List<List<LatLon>> geoms = object.geometry.getOutsidePolys();
            AirStyle attrForAirspace = getAttrForAirspace(object);

            var renderables = new ArrayList<Renderable>();
            for (List<LatLon> geom : geoms) {
                SurfacePolygon poly = new SurfacePolygon(attrForAirspace.base);
                poly.setHighlightAttributes(attrForAirspace.highlight);
                poly.setValue(AVKey.DISPLAY_NAME, object.name);
                poly.setValue(KEY_AIRSPACE_OBJECT, object);
                poly.setOuterBoundary(geom);
                renderables.add(poly);
            }

            return renderables;
        }

    }

    static class GenericFactory {
        // private final RandomShapeAttributes attrFactory;
        private final BasicShapeAttributes attr;
        private final BasicShapeAttributes attrHighlight;

        GenericFactory() {
            // attrFactory = new RandomShapeAttributes();
            attr = new BasicShapeAttributes();
            attr.setOutlineMaterial(new Material(AirSpaceColors.GENERIC.airspaceColor.outlineColor));
            attr.setOutlineOpacity(AirSpaceColors.GENERIC.opacityOutline);
            attr.setOutlineWidth(AirSpaceColors.GENERIC.lineWidth); // Default value was 3.
            attr.setDrawInterior(true);
            //            attr.setInteriorOpacity(0.4);  //HARDCODED?
            attr.setInteriorMaterial(new Material(AirSpaceColors.GENERIC.airspaceColor.fillColor));
            attr.setInteriorOpacity(AirSpaceColors.GENERIC.opacityInterior);

            attrHighlight = (BasicShapeAttributes)attr.copy();
            attrHighlight.setOutlineMaterial(new Material(AirSpaceColors.GENERIC_HIGHLIGHT.airspaceColor.outlineColor));
            attrHighlight.setInteriorMaterial(new Material(AirSpaceColors.GENERIC_HIGHLIGHT.airspaceColor.fillColor));
            attrHighlight.setOutlineWidth(AirSpaceColors.GENERIC_HIGHLIGHT.lineWidth);
        }

        public List<Renderable> createForOtherMultiPolygonParkAirspaces(AirSpaceObject object) {
            var geoms = object.geometry.getOutsidePolys();
            ArrayList<Renderable> renderables = new ArrayList<>();
            for (List<LatLon> geom : geoms) {
                SurfacePolygon poly = new SurfacePolygon(attr);
                poly.setHighlightAttributes(attrHighlight);
                poly.setValue(AVKey.DISPLAY_NAME, object.name);
                poly.setValue(KEY_AIRSPACE_OBJECT, object);
                poly.setOuterBoundary(geom);
                renderables.add(poly);
            }

            return renderables;
        }

        public List<Renderable> createForAirSpace(AirSpaceObject object) {
            GeoJson.PolygonGeom geom;
            if (object.geometry instanceof GeoJson.MultiPolygonGeom && object.getType() == Park) {
                return createForOtherMultiPolygonParkAirspaces(object);
            }

            if (!(object.geometry instanceof GeoJson.PolygonGeom)) {
                return null;
            } else {
                geom = (GeoJson.PolygonGeom)object.geometry;
            }

            ArrayList<Renderable> renderables = new ArrayList<>();
            for (List<LatLon> list : geom.coordinates) {
                SurfacePolygon poly = new SurfacePolygon(attr);
                poly.setHighlightAttributes(attrHighlight);
                poly.setValue(AVKey.DISPLAY_NAME, object.name);
                poly.setValue(KEY_AIRSPACE_OBJECT, object);
                poly.setOuterBoundary(list);
                renderables.add(poly);
            }

            return renderables;
        }
    }

    class AirportFactory {

        AirspaceAttributes attrs;

        AirportFactory() {
            attrs = new BasicAirspaceAttributes();
            attrs.setOutlineMaterial(new Material(AirSpaceColors.AIRPORTS.airspaceColor.outlineColor));
            attrs.setInteriorMaterial(new Material(AirSpaceColors.AIRPORTS.airspaceColor.outlineColor));
            attrs.setInteriorOpacity(AirSpaceColors.AIRPORTS.opacityInterior);
            attrs.setOutlineOpacity(AirSpaceColors.AIRPORTS.opacityOutline);
            attrs.setOutlineWidth(AirSpaceColors.AIRPORTS.lineWidth);
            attrs.setDrawOutline(true);
            attrs.setEnableAntialiasing(true);
            attrs.setEnableLighting(false);
        }

        public List<Renderable> createForAirSpace(AirSpaceObject object) {
            //            assert object.getType() == Airport;
            ArrayList<Renderable> renderables = new ArrayList<>();

            AirportAirspaceObject airportObject = ((AirportAirspaceObject)object);

            renderables.addAll(
                controlledAirSpaceFactory.createForWeirdAirports(airportObject.getAirspaceRule(), airportObject));
            // deal with Airports and their weird geometry possibilities
            GeoJson.Geometry geom = airportObject.getPropertyBoundary();
            if (geom == null) geom = object.geometry;
            if (!(geom instanceof GeoJson.PolygonGeom)) {
                // System.err.println("Airport  " + object.id + " has unexpected boundary type " + geom.getClass());
                return renderables;
            }

            for (List<LatLon> list : ((GeoJson.PolygonGeom)geom).coordinates) {
                gov.nasa.worldwind.render.airspaces.Polygon poly = new Polygon(attrs);
                poly.setEnableCaps(false);
                poly.setLocations(list);
                poly.setAltitude(AIRPORT_FENCE_HEIGHT);
                poly.setTerrainConforming(false, true);
                poly.setValue(AVKey.DISPLAY_NAME, object.name);
                poly.setValue(KEY_AIRSPACE_OBJECT, object);
                renderables.add(poly);
            }

            return renderables;
        }

    }
}
