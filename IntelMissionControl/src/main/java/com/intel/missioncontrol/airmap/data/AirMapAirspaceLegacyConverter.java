/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.data;

import com.airmap.airmapsdk.networking.services.MappingService;
import eu.mavinci.airspace.Airspace;
import eu.mavinci.airspace.AirspaceTypes;
import eu.mavinci.airspace.IAirspace;
import eu.mavinci.desktop.helper.MathHelper;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.LoggerFactory;

/** Convert from Airmap to IAirspace */
public class AirMapAirspaceLegacyConverter {
    private AirMapAirspaceLegacyConverter() {}

    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AirMapAirspaceLegacyConverter.class);

    /** Airspace for tooltips */
    public static IAirspace convert(AirSpaceObject object) {
        if (object == null) throw new IllegalArgumentException("AirSpaceObject can't be null");
        return new AirspaceFacade(object);
    }

    /** Airspace with Mavinici geometry, for flightplan checking */
    public static List<Airspace> convertDeep(AirSpaceObject object) {
        List<Airspace> airspaces = new ArrayList<>(1);
        convertDeep(object, airspaces);
        return airspaces;
    }

    static void convertDeep(AirSpaceObject object, List<Airspace> airspaces) {
        if (object == null) {
            return; // throw new IllegalArgumentException("AirSpaceObject can't be null");
        }

        convertDeep(object, airspaces, object.geometry);

        if (object instanceof AirSpaceBase) {
            AirSpaceBase airSpaceBase = (AirSpaceBase)object;
            convertDeep(object, airspaces, airSpaceBase.getPropertyBoundary());
        }

        if (object instanceof AirportAirspaceObject) {
            AirportAirspaceObject airportObject = ((AirportAirspaceObject)object);
            convertDeep(object, airspaces, airportObject.getAirspaceRule());
        }

        return;
    }

    private static void convertDeep(AirSpaceObject object, List<Airspace> airspaces, GeoJson.Geometry geometry) {
        List<List<LatLon>> polygons = exteriorGeometries(object, geometry);
        for (int i = 0; i < polygons.size(); i++) {
            final List<LatLon> polygon = polygons.get(i);
            AirspaceWithGeometry a =
                new AirspaceWithGeometry(
                    object.name,
                    convertType(object.getType()),
                    object.id != null ? object.id.toString() : "" + "_" + Integer.toString(i),
                    polygon);

            a.setFloor(0.0, true);
            a.setCeiling(40000, true);
            airspaces.add(a);
        }

        return;
    }

    private static List<List<LatLon>> exteriorGeometries(AirSpaceObject object, GeoJson.Geometry geometry) {
        if (geometry == null) return Collections.emptyList();

        if (geometry instanceof GeoJson.GeometryExtended) {
            return ((GeoJson.GeometryExtended)geometry).getOutsidePolys();
        } else {
            LOGGER.warn("can't create boundary for airspace with this object:" + object + " geometry: " + geometry);
            return Collections.emptyList();
        }
    }

    static AirspaceTypes convertType(MappingService.AirMapAirspaceType type) {
        switch (type) {
        case SpecialUse:
            return AirspaceTypes.SpecialUseAirspace;
        case ControlledAirspace:
            return AirspaceTypes.ControlledAirspace;
        case Airport:
            return AirspaceTypes.Airport;
        case Heliport:
            return AirspaceTypes.Heliport;
        case Prison:
            return AirspaceTypes.Prison;
        case Emergencies:
            return AirspaceTypes.Emergencies;
        case Park:
            return AirspaceTypes.Park;
        case School:
            return AirspaceTypes.School;
        case Hospitals:
            return AirspaceTypes.Hospital;
        case TFR:
            return AirspaceTypes.TFR;
        case PowerPlant:
            return AirspaceTypes.PowerPlant;
        case Wildfires:
            return AirspaceTypes.Wildfires;
        default:
            return AirspaceTypes.UNKNOWN;
        }
    }

    static class AirspaceWithGeometry extends Airspace {
        public AirspaceWithGeometry(String name, AirspaceTypes type, String id, List<LatLon> _verti) {
            super(name, type, id);
            vertices = _verti;
        }
    };

    static class AirspaceFacade implements IAirspace {
        final AirSpaceObject a;
        Sector boundingBox;

        AirspaceFacade(AirSpaceObject object) {
            this.a = object;
        }

        @Override
        public String getId() {
            return a.id.toString();
        }

        @Override
        public double floorMeters(LatLon pos) {
            return 0;
        }

        @Override
        public double floorMeters(LatLon ref, double groundElevationMetersEGM) {
            return 400;
        }

        @Override
        public boolean withinAirspace(LatLon pos, double altitude_absolute_meters) {
            return false;
        }

        @Override
        public List<LatLon> getPolygon() {
            return null;
        }

        @Override
        public String getName() {
            return a.name;
        }

        @Override
        public boolean insidePolygon(LatLon ref) {
            return false;
        }

        @Override
        public AirspaceTypes getType() {
            return convertType(a.getType());
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public Sector getBoundingBox() {
            if (boundingBox == null) {
                boundingBox = Sector.boundingSector(a.getPosition(), a.getPosition());
                if (a.min_circle_radius > 0.01) {
                    boundingBox = MathHelper.extendSector(boundingBox, a.min_circle_radius);
                }
            }

            return boundingBox;
        }

        @Override
        public Double getFloorReferenceGround() {
            return null;
        }

        @Override
        public Double getFloorReferenceSeaLevel() {
            return null;
        }

        @Override
        public double getFloorReferenceGroundOrSeaLevel() {
            return 0;
        }

        @Override
        public double getCeilingReferenceGroundOrSeaLevel() {
            return 0;
        }

        @Override
        public void setTitle(String str) {}

        @Override
        public void setCountry(String iso2) {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AirspaceFacade that = (AirspaceFacade)o;
            return Objects.equals(a, that.a);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a);
        }
    }
}
