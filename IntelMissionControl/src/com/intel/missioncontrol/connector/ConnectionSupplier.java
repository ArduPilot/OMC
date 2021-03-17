/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.connector;

import com.google.inject.Inject;
import com.intel.missioncontrol.utils.IVersionProvider;
import eu.mavinci.core.plane.protocol.Base64;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.ConnectionObjects;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionSettings;
import eu.mavinci.plane.nmea.NMEA;
import gov.nasa.worldwind.geom.Position;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionSupplier implements IConnectionSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionSupplier.class);

    IVersionProvider versionProvider;

    @Inject
    public ConnectionSupplier(IVersionProvider versionProvider) {
        this.versionProvider = versionProvider;
    }

    @Override
    public ConnectionObjects getNtripConnection(NtripConnectionSettings connectionSettings) {
        return getConnection(connectionSettings.getStreamAsString(), connectionSettings, null);
    }

    private static final int CONNECT_TIMEOUT = 15 * 1000;
    private static final int READ_TIMEOUT = 5 * 1000;

    private ConnectionObjects getConnection(String stream, NtripConnectionSettings con, Position lastPosWGS84) {
        LOGGER.debug("Get connection for stream {}", stream);
        URL url = null;
        try {
            url =
                new URL(
                    (con.isHttps() ? "https" : "http"),
                    con.getHost(),
                    con.getPort(),
                    (stream != null ? "/" + stream : ""));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        LOGGER.debug("Connection URL {}", url);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            connection.setRequestMethod("GET");
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }

        connection.setRequestProperty("Host", con.getHost());
        connection.setRequestProperty("Ntrip-Version", "Ntrip/2.0");
        connection.setRequestProperty(
            "User-Agent", "NTRIP mavinci-desktop/" + versionProvider.getHumanReadableVersion());
        if (con.getUser() != null && !con.getUser().isEmpty()) {
            String authStr = con.getUser() + ":" + Base64.decodeString(con.getPassword());
            String authEncoded = Base64.encodeString(authStr);
            connection.setRequestProperty("Authorization", "Basic " + authEncoded);
        }

        connection.setRequestProperty("Connection", "close");
        connection.setRequestProperty("Accept-Language", "en,*");
        connection.setRequestProperty("Ntrip-Version", "Ntrip/2.0");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        ConnectionObjects conObj = null;
        try {
            conObj = new ConnectionObjects(connection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (lastPosWGS84 != null) {
            conObj.print("Ntrip-GGA: " + NMEA.createGPGGA(lastPosWGS84) + "\r\n");
        }

        conObj.flush();

        return conObj;
    }
}
