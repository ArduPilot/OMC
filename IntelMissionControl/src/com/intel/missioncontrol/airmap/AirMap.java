/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap;

import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.util.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intel.missioncontrol.airmap.data.AirMapResponse;
import com.intel.missioncontrol.airmap.data.AirSpaceObject;
import com.intel.missioncontrol.airmap.data.Loader;
import com.intel.missioncontrol.airmap.network.AirMapClient2;
import com.intel.missioncontrol.airmap.network.AirMapConfig2;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.ProxySelector;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Add AirMap APIs missing from {@link com.airmap.airmapsdk.networking.services.AirMap }
 *
 */
public class AirMap {

    private static AirMapClient2 client;

    // Intialization-on-demand holder
    private static class GsonHolder {
        private static Gson INSTANCE = Loader.createLoader();
    }

    private static Gson gsonInstance() {
        return GsonHolder.INSTANCE;
    }

    public static final TypeToken<AirMapResponse<Collection<AirSpaceObject>>> airmapResponseType
            = new TypeToken<>() {
    };

    public static final String AIRMAP_SEARCH_URL = "https://api.airmap.com/airspace/v2/search";

    private AirMap() {
    }

    public enum AirMapGeometryFormat implements Serializable {
        WKT("wkt"),
        GeoJSON("geojson");

        private final String text;

        private AirMapGeometryFormat(String text) {
            this.text = text;
        }

        public String toString() {
            return this.text;
        }
    }

    public static void init(String apiKey) {
        AirMapConfig2 config = new AirMapConfig2(null, apiKey);
        init(config);
    }

    public static void init(String apiKey, ProxySelector selector) {
        AirMapConfig2 config = new AirMapConfig2(selector, apiKey);
        init(config);
    }

    public static void init(AirMapConfig2 config) {
        AirMapClient2 c = new AirMapClient2(config);
        client = c;
    }

    public static AirMapClient2 getClient() {
        if (client == null) {
            throw new IllegalStateException("client not initialized, forgot to call init()");
        }
        return client;
    }

    public static class Coordinate {
        public final double latitude;
        public final double longitude;

        public Coordinate(double lat, double lng) {
            latitude = lat;
            longitude = lng;
        }

        @Override
        public String toString() {
            return String.format("%3.6f %3.6f", longitude, latitude);
        }
    }

    private static String makeGeoString(List<Coordinate> coordinates) {
        return (String) coordinates.stream().map(Coordinate::toString).collect(Collectors.joining(","));
    }

    public static List<Coordinate> makeSector(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
        return Arrays.asList(
                new Coordinate(minLatitude, maxLongitude), // NW
                new Coordinate(maxLatitude, maxLongitude), // NE
                new Coordinate(maxLatitude, minLongitude), // SE
                new Coordinate(minLatitude, minLongitude), // SW
                new Coordinate(minLatitude, maxLongitude)  // NW
        );
    }

    private static Map<String, String> getParamsForSearch(int buffer, List<MappingService.AirMapAirspaceType> types, List<MappingService.AirMapAirspaceType> ignoredTypes, boolean full, int limit, int offset, AirMapGeometryFormat airMapGeometryFormat, Date datetime) {
        Map<String, String> params = new HashMap<>();
        if (types != null && types.size() > 0) {
            params.put("types", types.stream().map(MappingService.AirMapAirspaceType::toString).collect(Collectors.joining(",")));
        }

        if (ignoredTypes != null && ignoredTypes.size() > 0) {
            params.put("ignored_types", ignoredTypes.stream().map(MappingService.AirMapAirspaceType::toString).collect(Collectors.joining(",")));
        }

        params.put("full", full ? "true" : "false");
        //params.put("buffer", String.valueOf(buffer));
        if (limit > 0) {
            params.put("limit", String.valueOf(limit));
        }

        if (offset > 0) {
            params.put("offset", String.valueOf(offset));
        }

        if (airMapGeometryFormat != null) {
            params.put("geometry_format", airMapGeometryFormat.toString());
        }

        if (datetime != null) {
            params.put("datetime", Utils.getIso8601StringFromDate(datetime));
        }

        return params;
    }

    public static final class AirMapResponseFuture<T> extends CompletableFuture<T> {
        private Call call;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            call.cancel();

            // OkHTTP doesn't like to be interrupted:
            // see https://github.com/square/okhttp/issues/1903
            super.cancel(false);
            //call.isCanceled();
            // return super.cancel(mayInterruptIfRunning);
            return call.isCanceled();
        }
    }

    public static <T> T decodeAirmapResponse(ResponseBody body, Type type) throws Exception {
        Gson gson = gsonInstance();
        return gson.fromJson(body.charStream(), type);
    }

    public static AirMapResponseFuture<Collection<AirSpaceObject>> searchAirspace(List<Coordinate> geometry, List<MappingService.AirMapAirspaceType> types,
                                                                                  List<MappingService.AirMapAirspaceType> ignoredTypes,
                                                                                  boolean full, Date date) /*throws AirMapException*/ {
        Map<String, String> params = getParamsForSearch(0, types, ignoredTypes, full, 0, 0,
                AirMapGeometryFormat.GeoJSON, null);
        params.put("geometry", "POLYGON(" + makeGeoString(geometry) + ")");

        final AirMapResponseFuture<Collection<AirSpaceObject>> future = new AirMapResponseFuture<>();
        final Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    AirMapResponse<Collection<AirSpaceObject>> t = null;
                    try (ResponseBody responseBody = response.body()) {
                        t = decodeAirmapResponse(responseBody, airmapResponseType.getType());
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                        return;
                    }
                    future.complete(t.getData());
                } else if (response.networkResponse() == null) { // forced cache load that missed (code 504)
                    future.complete(null);
                } else {
                    // todo: better messages
                    future.completeExceptionally(new RuntimeException("AirMap Request failed: " + response.code() + " " + response.message()
                            + " : " + response.body().string()));
                }
            }
        };


        future.call = getClient().get(AIRMAP_SEARCH_URL, params, callback);
        return future;
    }

    public static Call searchAirspace2(List<Coordinate> geometry, List<MappingService.AirMapAirspaceType> types,
                                       List<MappingService.AirMapAirspaceType> ignoredTypes,
                                       boolean full, Date date, Callback callback) /*throws AirMapException*/ {
        Map<String, String> params = getParamsForSearch(0, types, ignoredTypes, full, 0, 0,
                AirMapGeometryFormat.GeoJSON, null);
        params.put("geometry", "POLYGON(" + makeGeoString(geometry) + ")");

        return getClient().get(AIRMAP_SEARCH_URL, params, callback);
    }
}
