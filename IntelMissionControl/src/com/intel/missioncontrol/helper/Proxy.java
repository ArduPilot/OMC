/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

public class Proxy {

    private String host;
    private int port;
    private String exceptions;

    public Proxy(String host, int port, String exceptions) {
        this.host = host;
        this.port = port;
        this.exceptions = exceptions;
    }

    public Proxy() {
        this.host = "";
        this.port = 0;
        this.exceptions = "";
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getExceptions() {
        return exceptions;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setExceptions(String exceptions) {
        this.exceptions = exceptions;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Proxy{");
        sb.append("host='").append(host).append('\'');
        sb.append(", port=").append(port);
        sb.append(", exceptions='").append(exceptions).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
