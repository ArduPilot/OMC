/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap;

import com.airmap.airmapsdk.networking.services.MappingService;
import com.google.common.io.Files;
import com.intel.missioncontrol.airmap.data.AirSpaceObject;
import com.intel.missioncontrol.airmap.network.AirMapConfig2;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.Airport;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.ControlledAirspace;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.Heliport;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.Hospitals;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.Park;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.School;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.SpecialUse;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.TFR;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType.Wildfires;
import static com.intel.missioncontrol.airmap.AirMap.makeSector;
import static com.intel.missioncontrol.airmap.TestUtils.getAirmapTestKey;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Disabled("BROKEN TEST, but ignored to get testing in build system")
public class AirMapExtensionTest {

    @BeforeAll
    public static void setup() {


    }


    static List<AirMap.Coordinate> BAYLANDS = makeSector(37.233442685843016, 37.59317082004017,
            -121.72677386705263, -122.17967594289738);

    static List<MappingService.AirMapAirspaceType> whitelist = Arrays.asList(
            Airport, Heliport, ControlledAirspace, SpecialUse, TFR, Wildfires
    );
    List<MappingService.AirMapAirspaceType> blacklist = Arrays.asList(
            Park, Hospitals, School
    );

    @Test
    public void testCache() throws IOException {
        String currentDir = Paths.get(".").toAbsolutePath().normalize().toString();
        File cacheFile = new File(currentDir, "testcache");

        Cache cache = new Cache(cacheFile, 1000_000);
        //cache.evictAll();


        final OkHttpClient client = new OkHttpClient.Builder()
                .cache(cache)
                .build();

        // The below URL will produce the response with cache in 20 seconds
        Request request = new Request.Builder().url("https://httpbin.org/cache/2000").build();

        Response response1 = client.newCall(request).execute();
        if (!response1.isSuccessful()) {
            throw new IOException("Unexpected code " + response1);
        }
        response1.close();
//        assertNull(response1.cacheResponse());
//        assertNotNull(response1.networkResponse());

        Response response2 = client.newCall(request).execute();
        if (!response2.isSuccessful()) {
            throw new IOException("Unexpected code " + response2);
        }
        response2.close();
        Assertions.assertNotNull(response2.cacheResponse());
        Assertions.assertNull(response2.networkResponse());

        cache.urls().forEachRemaining(url -> System.out.println(url));


    }

    public static File getCacheDir() {
        String currentDir = Paths.get(".").toAbsolutePath().normalize().toString();
        File cacheFile = new File(currentDir, "testcache");
        return cacheFile;
    }

    /**
     * Switch to offline mode and AirMap should only use cache
     */
    @Test
    public void testOffline() {
        AirMapConfig2 config2 = new AirMapConfig2(null, getAirmapTestKey());
        config2.cacheDir = Files.createTempDir();

        final AtomicBoolean forceCache = new AtomicBoolean(true);

        config2.setHttpRequestInterceptor(new Function<Request.Builder, Request.Builder>() {
            @Override
            public Request.Builder apply(Request.Builder builder) {
                if (forceCache.get()) {
                    return builder.cacheControl(CacheControl.FORCE_CACHE);
                } else {
                    return builder;
                }
            }
        });

        AirMap.init(config2);

        System.out.println("Cache dir: " + config2.cacheDir);

        Cache cache = AirMap.getClient().getOkHttpClient().cache();

        try {
            CompletableFuture<Collection<AirSpaceObject>> response = null;

            // load from network, should succeed
            forceCache.set(false);
            response = AirMap.searchAirspace(BAYLANDS, whitelist, null, true, null);
            {
                Collection<AirSpaceObject> airSpaceObjects = response.get();
                Assertions.assertNotNull(airSpaceObjects);
            }

            // load from cache, should succeed
            forceCache.set(true);
            response = AirMap.searchAirspace(BAYLANDS, whitelist, null, true, null);
            {
                Collection<AirSpaceObject> airSpaceObjects = response.get();
                Assertions.assertNotNull(airSpaceObjects);
            }

            // clear cache,
            cache.evictAll();

            // should fail quickly and return null
            response = AirMap.searchAirspace(BAYLANDS, whitelist, null, true, null);
            {
                Collection<AirSpaceObject> airSpaceObjects = response.get();
                Assertions.assertNull(airSpaceObjects);
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FileUtils.deleteDirectory(config2.cacheDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void searchAirspaceTest2() {
        AirMapConfig2 config2 = new AirMapConfig2(null, getAirmapTestKey());
        config2.cacheDir = getCacheDir();

        System.out.println("Cache dir: "+config2.cacheDir);

        AirMap.init(config2);

        for (int i = 0; i < 2; i++) {
            CompletableFuture<Collection<AirSpaceObject>> response =
                    AirMap.searchAirspace(BAYLANDS, whitelist, null, true, null);

            AirMap.AirMapResponseFuture resp1 = (AirMap.AirMapResponseFuture) response;

            try {


                Collection<AirSpaceObject> airSpaceObjects = response.get(5000, MILLISECONDS);
                int size = airSpaceObjects.size();

                //System.out.println("got "+size+"objects in "+ (resp1.received - resp1.sent) + "ms");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    @Disabled("BROKEN TEST, but ignored to get testing in build system")
    @Test
    public void searchAirspaceTest() {

        AirMap.init(getAirmapTestKey());

        CompletableFuture<Collection<AirSpaceObject>> response =
                AirMap.searchAirspace(BAYLANDS, whitelist, null, true, null);

        try {
            Collection<AirSpaceObject> airSpaceObjects = response.get(5000, MILLISECONDS);

            int size = airSpaceObjects.size();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            Assertions.fail();
            e.printStackTrace();
        }


    }
}