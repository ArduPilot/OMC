/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.sources;

import com.airmap.airmapsdk.models.Coordinate;
import com.intel.missioncontrol.airspace.LayerConfigurator;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AirmapTestUtils {

    static String getAirmapTestKey() {
        return "TODO my airmap key goes here";
    }

    static List<LatLon> asLatLon(Sector bb) {
        return Arrays.asList(
            new LatLon(bb.getMinLatitude(), bb.getMaxLongitude()), // NW
            new LatLon(bb.getMaxLatitude(), bb.getMaxLongitude()), // NE
            new LatLon(bb.getMaxLatitude(), bb.getMinLongitude()), // SE
            new LatLon(bb.getMinLatitude(), bb.getMinLongitude()), // SW
            new LatLon(bb.getMinLatitude(), bb.getMaxLongitude()) // NW
            );
    }

    static List<Coordinate> asCoordinates(Sector sector) {
        List<gov.nasa.worldwind.geom.LatLon> latLons = sector.asList();
        return Stream.concat(latLons.stream(), Stream.of(latLons.get(0)))
            .map(ll -> new Coordinate(ll.getLatitude().getDegrees(), ll.getLongitude().getDegrees()))
            .collect(Collectors.toList());
    }

    static List<Coordinate> asCoordinates(List<LatLon> latLonCoordinates) {
        return latLonCoordinates
            .stream()
            .map(latLon -> new Coordinate(latLon.getLatitude().getDegrees(), latLon.getLongitude().getDegrees()))
            .collect(Collectors.toList());
    }

    static ProxySelector createProxySelector(String hostname, int port) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port));

        return new ProxySelector() {
            final List<Proxy> list = Arrays.asList(proxy);

            @Override
            public List<Proxy> select(URI uri) {
                return list;
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                System.err.println("connect " + uri + " failed: " + ioe.getMessage());
            }
        };
    }

    static void setDefaultProxySelector(String hostname, int port) {
        ProxySelector.setDefault(createProxySelector(hostname, port));
    }

    static void setJavaTrustStore(String certificatePath, String certPassword) {
        System.setProperty("javax.net.ssl.trustStore", certificatePath);
        System.setProperty("javax.net.ssl.trustStorePassword", certPassword);
    }

}
