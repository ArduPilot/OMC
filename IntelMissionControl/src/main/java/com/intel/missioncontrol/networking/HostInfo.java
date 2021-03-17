/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking;

import java.net.URL;

public class HostInfo {

    private final URL testSite;
    private boolean reachable = true;

    public HostInfo(URL testSite) {
        this.testSite = testSite;
    }

    public String getHostName() {
        return testSite.getHost();
    }

    public URL getTestSite() {
        return testSite;
    }

    public boolean isReachable() {
        return reachable;
    }

    @Override
    public int hashCode() {
        return testSite.getHost().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HostInfo)) {
            return false;
        }

        HostInfo other = (HostInfo)obj;
        return testSite.getHost().equals(other.testSite.getHost());
    }

    void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

}
