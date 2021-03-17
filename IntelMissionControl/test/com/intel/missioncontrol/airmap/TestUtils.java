/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap;

import com.intel.missioncontrol.airspace.LayerConfigurator;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

class TestUtils {

    /**
     * To run with fiddler, set this as appropriate in your VM options <code>
     * <p>
     * -DproxySet=true -Dproxy.httpHost=127.0.0.1 -Dproxy.httpPort=8888
     * -Dproxy.httpsHost=127.0.0.1 -Dproxy.httpsPort=8888
     * -Djavax.net.ssl.trustStore=C:\Users\Max\Dev\Java\Fiddler\FiddlerKeystore
     * -Djavax.net.ssl.trustStorePassword=password
     *
     * </code>
     */
    public static void setUpFiddler() {}

    public static File getTempDir(String name) {
        return new File(System.getProperty("java.io.tmpdir"), name);
    }

    public static String getAirmapTestKey() {
        return "TODO my airmap key goes here";
    }

    static boolean httpsTest(String httpsUrl) {
        try {
            URL url = new URL(httpsUrl);

            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
            // dumpl all cert info
            if (con == null) return false;
            System.out.println("Response Code : " + con.getResponseCode());
            System.out.println(" Cipher Suite : " + con.getCipherSuite());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean checkIfProxy() {
        URI testUri;
        try {
            testUri = new URI("https://www.example.com/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }

        Proxy proxy = ProxySelector.getDefault().select(testUri).iterator().next();

        System.out.println("Proxy type : " + proxy.type());
        InetSocketAddress proxyAddr = (InetSocketAddress)proxy.address();
        if (proxyAddr == null) {
            System.out.println("No Proxy");
            return false;
        } else {
            System.out.println("Proxy hostname : " + proxyAddr.getHostName());
            System.out.println("    Proxy port : " + proxyAddr.getPort());
            return true;
        }
    }

    public static void fixWWJScaling() {
        // JAVA 10 breaks this
        System.setProperty("sun.java2d.uiScale", "1");
    }

    public static double computeZoomForExtent(Sector sector) {
        Angle delta = sector.getDeltaLat();
        if (sector.getDeltaLon().compareTo(delta) > 0) delta = sector.getDeltaLon();
        double arcLength = delta.radians * Earth.WGS84_EQUATORIAL_RADIUS;
        double fieldOfView = Configuration.getDoubleValue(AVKey.FOV, 45.0);
        return arcLength / (2 * Math.tan(fieldOfView / 2.0));
    }

    /** set configuration for WWJ to zoom to initial view */
    public static void setupInitialZoom(Sector area) {
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, computeZoomForExtent(area));
        Configuration.setValue(AVKey.INITIAL_LATITUDE, area.getCentroid().getLatitude().degrees);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, area.getCentroid().getLongitude().degrees);
    }
}
