/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.data;

import com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gov.nasa.worldwind.geom.LatLon;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Loader {


    public static Collection<AirSpaceObject> loadFromAirmapJson(Object object) {
        Gson loader = createLoader();

        Type listType = new TypeToken<AirMapResponse<Collection<AirSpaceObject>>>() {
        }.getType();
        AirMapResponse<Collection<AirSpaceObject>> resp = null;
        if (object instanceof Reader) {
            resp = loader.fromJson((Reader) object, listType);
        } else if (object instanceof String) {
            resp = loader.fromJson((String) object, listType);
        }

        if(resp == null) {
            return new ArrayList<AirSpaceObject>();
        }

        return resp.data;
    }


    public static void registerAirspaceAdapter(GsonBuilder builder) {
        Map<String, Type> typeMap = new HashMap<>();
        typeMap.put(AirMapAirspaceType.ControlledAirspace.toString(), ControlledAirspaceObject.class);
        typeMap.put(AirMapAirspaceType.Airport.toString(), AirportAirspaceObject.class);

        GeoJson.TypedObjectAdapter<AirSpaceObject> adapter = new GeoJson.TypedObjectAdapter<>(typeMap, AirSpaceBase.class);
        builder.registerTypeAdapter(AirSpaceObject.class, adapter);
    }

    public static void registerGeoJsonAdapter(GsonBuilder builder) {
        builder.registerTypeAdapter(LatLon.class, new GeoJson.LatLonAdapter());

        Map<String, Type> typeMap = new HashMap<>();
        typeMap.put("Point", GeoJson.PointGeom.class);
        typeMap.put("LineString", GeoJson.LineStringGeom.class);
        typeMap.put("Polygon", GeoJson.PolygonGeom.class);
        typeMap.put("MultiPolygon", GeoJson.MultiPolygonGeom.class);
        typeMap.put("Feature", GeoJson.Feature.class);
        typeMap.put("geometry",GeoJson.Geometry.class);

        GeoJson.TypedObjectAdapter<GeoJson.Geometry> adapter = new GeoJson.TypedObjectAdapter<>(typeMap);
        builder.registerTypeAdapter(GeoJson.Geometry.class, adapter);

        GeoJson.TypedObjectAdapter<GeoJson.GeometryExtended> adapter2 = new GeoJson.TypedObjectAdapter<>(typeMap);
        builder.registerTypeAdapter(GeoJson.GeometryExtended.class, adapter2);

    }


    public static Gson createLoader() {
        GsonBuilder builder = new GsonBuilder();
        registerGeoJsonAdapter(builder);
        registerAirspaceAdapter(builder);

        return builder.create();
    }
}
