/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import eu.mavinci.core.plane.protocol.Base64;

import java.net.URL;
import java.util.Optional;

public class NtripConnectionSettings {

    private final int port;
    private final String host;
    private final String user;
    private final String password;
    private final boolean https;
    private final NtripSourceStr stream;

    public static NtripConnectionSettings fromUrl(URL url) {
        return new NtripConnectionSettings(
            url.getHost(),
            url.getPort(),
            "https".equals(url.getProtocol()),
            new NtripSourceStr(new String[] {"STR", getNtripStream(url)}, ""),
            getUser(url),
            getPassword(url));
    }

    private static String getUser(URL url) {
        String user;
        String userInfo = url.getUserInfo();
        if (userInfo != null && !userInfo.isEmpty()) {
            int pos = userInfo.indexOf(":");
            user = userInfo.substring(0, pos);
        } else {
            user = null;
        }

        return user;
    }

    private static String getPassword(URL url) {
        String password;
        String userInfo = url.getUserInfo();
        if (userInfo != null && !userInfo.isEmpty()) {
            int pos = userInfo.indexOf(":");
            password = Base64.encodeString(userInfo.substring(pos + 1));
        } else {
            password = "";
        }

        return password;
    }

    private static String getNtripStream(URL url) {
        String stream = url.getPath();
        if (stream != null && !stream.isEmpty()) {
            stream = stream.substring(1);
        }

        return stream;
    }

    private NtripConnectionSettings(
            String host, int port, boolean https, NtripSourceStr stream, String user, String password) {
        this.host = host;
        this.port = port;
        this.https = https;
        this.stream = stream;
        this.user = user;
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean isHttps() {
        return https;
    }

    public NtripSourceStr getStream() {
        return stream;
    }

    public String getStreamAsString() {
        return Optional.ofNullable(stream).map(NtripSourceStr::getMountPoint).orElse("");
    }

    @Override
    public String toString() {
        return "http" + (https ? "s" : "") + "://" + host + ":" + port + "/" + getStreamAsString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getHost() == null) ? 0 : getHost().hashCode());
        result = prime * result + (isHttps() ? 1231 : 1237);
        result = prime * result + getPort();
        result = prime * result + getStreamAsString().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        NtripConnectionSettings other = (NtripConnectionSettings)obj;
        return getHost().equals(other.getHost())
            && isHttps() == other.isHttps()
            && getPort() == other.getPort()
            && getStreamAsString().equals(other.getStreamAsString());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String host;
        private int port;
        private NtripSourceStr stream;
        private boolean isHttps;
        private String username;
        private String password;

        private Builder() {}

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withStream(NtripSourceStr stream) {
            this.stream = stream;
            return this;
        }

        public Builder withHttps() {
            this.isHttps = true;
            return this;
        }

        public Builder withUser(String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public NtripConnectionSettings build() {
            return new NtripConnectionSettings(
                host != null ? host : "",
                port != 0 ? port : -1,
                isHttps,
                stream,
                username,
                password != null ? Base64.encodeString(password) : "");
        }
    }
}
