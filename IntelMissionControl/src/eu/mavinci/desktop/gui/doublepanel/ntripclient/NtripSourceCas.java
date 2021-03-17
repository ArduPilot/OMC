/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.LatLon;

import java.util.Objects;
import java.util.logging.Level;

public class NtripSourceCas extends NtripSourceTableEntry {

    private final String host;
    private final int port;
    private final String identifier;
    private String operator;
    private String country;
    private LatLon latLon;
    private boolean needNmea;
    private String fallbackHost;
    private int fallbackPort = 80;

    NtripSourceCas(String[] all, String line) {
        super(EntryType.CAS);
        host = all[1];
        port = Integer.parseInt(all[2]);
        identifier = all[3];
        try {
            operator = all[4];
            needNmea = "1".equals(all[5]);
            country = all[6];
            latLon = LatLon.fromDegrees(Double.parseDouble(all[7]), Double.parseDouble(all[8]));
            fallbackHost = all[9];
            fallbackPort = Integer.parseInt(all[10]);
        } catch (Exception e) {
            Debug.getLog().log(Level.FINE, "parsing problems " + line, e);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getOperator() {
        return operator;
    }

    public String getCountry() {
        return country;
    }

    public LatLon getLatLon() {
        return latLon;
    }

    public boolean isNeedNmea() {
        return needNmea;
    }

    public String getFallbackHost() {
        return fallbackHost;
    }

    public int getFallbackPort() {
        return fallbackPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NtripSourceCas that = (NtripSourceCas)o;
        return port == that.port
            && needNmea == that.needNmea
            && fallbackPort == that.fallbackPort
            && Objects.equals(host, that.host)
            && Objects.equals(identifier, that.identifier)
            && Objects.equals(operator, that.operator)
            && Objects.equals(country, that.country)
            && Objects.equals(latLon, that.latLon)
            && Objects.equals(fallbackHost, that.fallbackHost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getHost(),
            getPort(),
            getIdentifier(),
            getOperator(),
            getCountry(),
            getLatLon(),
            isNeedNmea(),
            getFallbackHost(),
            getFallbackPort());
    }

    @Override
    public String toString() {
        return type + "," + host + "," + port;
    }
}
