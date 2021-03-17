/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.utils.IVersionProvider;
import eu.mavinci.core.plane.protocol.Base64;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.ConnectionObjects;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionSettings;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripSourceTableEntry;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class NtripRequesterImpl implements NtripRequester {

    private static final long CONNECT_TIMEOUT = TimeUnit.SECONDS.toMillis(15);
    private static final long READ_TIMEOUT = TimeUnit.SECONDS.toMillis(5);

    IVersionProvider versionProvider;

    @Inject
    public NtripRequesterImpl(IVersionProvider versionProvider) {
        this.versionProvider = versionProvider;
    }

    @Override
    public Stream<NtripSourceTableEntry> requestNtripStreams(URL url) {
        NtripConnectionSettings connectionSettings = NtripConnectionSettings.fromUrl(url);
        return getSources(connectionSettings).stream();
    }

    private List<NtripSourceTableEntry> getSources(NtripConnectionSettings con) {
        List<NtripSourceTableEntry> sources = new ArrayList<>();
        // http://igs.bkg.bund.de/root_ftp/NTRIP/documentation/NtripDocumentation.pdf
        try (ConnectionObjects connection = getConnection(con)) {
            String newLine;
            while ((newLine = connection.readLine()) != null) {
                NtripSourceTableEntry.parse(newLine).ifPresent(sources::add);
            }

            if (sources.isEmpty()) {
                throw new Exception("no entries in source table");
            }

        } catch (SocketException e2) {
            return sources;
        } catch (Exception e1) {
            Debug.getLog().log(Debug.WARNING, "Problem getting Ntrip sources list", e1);
            return sources;
        }

        return sources;
    }

    private ConnectionObjects getConnection(NtripConnectionSettings con) throws IOException {
        URL url = new URL((con.isHttps() ? "https" : "http"), con.getHost(), con.getPort(), "");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
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
        connection.setConnectTimeout((int)CONNECT_TIMEOUT);
        connection.setReadTimeout((int)READ_TIMEOUT);
        ConnectionObjects conObj = new ConnectionObjects(connection);

        conObj.flush();

        return conObj;
    }
}
