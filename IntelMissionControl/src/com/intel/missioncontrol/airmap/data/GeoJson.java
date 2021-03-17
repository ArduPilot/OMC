/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import gov.nasa.worldwind.geom.LatLon;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoJson {

    public interface Geometry {}

    public interface GeometryExtended extends Geometry {
        public List<List<LatLon>> getOutsidePolys();
    }

    public static class Feature implements GeometryExtended {
        public Geometry geometry;

        public List<List<LatLon>> getOutsidePolys() {
            if (geometry instanceof GeometryExtended) {
                return ((GeometryExtended)geometry).getOutsidePolys();
            }

            return Collections.emptyList();
        }
    }

    public static class PointGeom implements GeometryExtended {
        public LatLon coordinates;

        public List<List<LatLon>> getOutsidePolys() {
            return Collections.emptyList();
        }
    }

    public static class LineStringGeom implements GeometryExtended {
        public List<LatLon> coordinates;

        public List<List<LatLon>> getOutsidePolys() {
            return Collections.emptyList();
        }
    }

    public static class PolygonGeom implements GeometryExtended {
        public List<List<LatLon>> coordinates;

        /** get list of coords representing outside of polygon */
        public List<LatLon> getOutside() {
            // return coordinates != null ? coordinates.get(0) : null;
            return coordinates.get(0);
        }

        public List<List<LatLon>> getOutsidePolys() {
            return Arrays.asList(this.getOutside());
        }
    }

    public static class MultiPolygonGeom implements GeometryExtended {
        public List<List<List<LatLon>>> coordinates;

        /** get outside coordinates of all polygons */
        public List<List<LatLon>> getOutsides() {
            // if (coordinates == null) return null;

            List<List<LatLon>> list = new ArrayList<>(coordinates.size());
            for (List<List<LatLon>> c : coordinates) {
                list.add(c.get(0));
            }

            return list;
        }

        public List<List<LatLon>> getOutsidePolys() {
            return this.getOutsides();
        }
    }

    static class LatLonAdapter extends TypeAdapter<LatLon> {
        @Override
        public void write(JsonWriter jsonWriter, LatLon latLon) throws IOException {
            throw new UnsupportedEncodingException();
        }

        @Override
        public LatLon read(JsonReader reader) throws IOException {
            double lat = 0.0;
            double lon = 0.0;

            reader.beginArray();
            lon = reader.nextDouble();
            lat = reader.nextDouble();
            reader.endArray();

            return LatLon.fromDegrees(lat, lon);
        }
    }

    static class TypedObjectAdapter<T> implements JsonDeserializer<T> {
        Map<String, Type> typeMap;
        Type defaultType;

        TypedObjectAdapter(Map<String, Type> typeMap) {
            this(typeMap, null);
        }

        TypedObjectAdapter(Map<String, Type> typeMap, Type defaultType) {
            this.typeMap = Collections.unmodifiableMap(new HashMap<>(typeMap));
            this.defaultType = defaultType;
        }

        @Override
        public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {
            if (!jsonElement.isJsonObject()) {
                return null;
            }

            JsonPrimitive typeName = jsonElement.getAsJsonObject().getAsJsonPrimitive("type");
            if (typeName == null) {
                return null;
            }

            Type objectType = typeMap.get(typeName.getAsString());

            if (objectType == type) {
                // avoid infinite recursion
                throw new JsonParseException("TypedObjectAdapter: types are same, can't downclass");
            }

            if (objectType == null) {
                if (defaultType != null) {
                    objectType = defaultType; // use default
                } else {
                    System.err.println("can't find JSON type: '" + typeName + "' for " + type);
                    new Exception().printStackTrace();
                    return null;
                }
            }

            return jsonDeserializationContext.deserialize(jsonElement, objectType);
        }
        //
        //        private Type typeForName(final JsonElement typeElem) {
        //            try {
        //                return geometryTypeMap.get(typeElem.getAsString());
        //            } catch (ClassNotFoundException e) {
        //                throw new JsonParseException(e);
        //            }
        //        }
    }

}
