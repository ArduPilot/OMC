/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap;

import com.airmap.airmapsdk.models.airspace.AirMapAirspace;
import com.airmap.airmapsdk.networking.services.AirMapClient;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.intel.missioncontrol.airspace.LayerConfigurator;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;

/** Hello world! */
public class App {
    AirMapClient client;

    static JSONObject loadJsonTestData() {
        InputStream is = App.class.getResourceAsStream("/airmap_response.json");
        JSONObject object;

        // load file
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            object = (new JSONObject(result.toString("UTF-8")));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return null;
        }

        return object;
    }
    //
    //    static List<AirMapAirspace> loadData() throws JSONException {
    //        JSONArray arr = loadJsonTestData().getJSONArray("data");
    //
    //
    //            // load airspaces
    //        List<AirMapAirspace> airspaces = new ArrayList<>(object.length());
    //        for (int i = 0; i < object.length(); i++) {
    //            try {
    //                airspaces.add(new AirMapAirspace(object.getJSONObject(i)));
    //            } catch (JSONException| IllegalArgumentException e) {
    //                e.printStackTrace();
    //            }
    //        }
    //
    //        return airspaces;
    //    }

    static List<AirMapAirspace> loadData2() {
        try {
            FileInputStream fos = new FileInputStream(new File(System.getProperty("user.home"), "test.dat"));
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(fos));

            List<AirMapAirspace> as = (List<AirMapAirspace>)ois.readObject();
            ois.close();
            fos.close();

            return as;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void writeData(List<AirMapAirspace> airMapAirspaces) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(System.getProperty("user.home"), "test.dat"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(airMapAirspaces);
            oos.close();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String getSecretKey() {
        return LayerConfigurator.getDefaultAirmapKey();
    }

    static void initAirspace() {
        //        AirMap.init();
    }

    public static List<AirMapAirspace> load() {
        return null;
    }

    public static void main(String[] args) throws IOException {

        //        Airmap
        //        long l = System.nanoTime();
        //        List<AirMapAirspace> airMapAirspaces = loadData();
        //        System.out.println("time: " + (System.nanoTime() - l)/1000);

        long l = System.nanoTime();
        List<AirMapAirspace> airMapAirspaces1 = loadData2();
        System.out.println("time: " + (System.nanoTime() - l) / 1000);

        AirMapAirspace airspace = airMapAirspaces1.get(0);
        airspace.getRules();

        List<AirMapAirspace> collect =
            airMapAirspaces1.stream().filter(a -> a.getAirspaceType() == null).collect(Collectors.toList());

        Map<MappingService.AirMapAirspaceType, List<AirMapAirspace>> m =
            airMapAirspaces1
                .stream()
                .filter(a -> a.getAirspaceType() != null)
                .collect(Collectors.groupingBy(AirMapAirspace::getAirspaceType));

        m.forEach((k, v) -> System.out.println("for " + k + " got: " + v.size()));

        //        System.out.println( "Hello World!" );
    }
}
