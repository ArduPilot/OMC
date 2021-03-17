/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic;

import org.junit.Test;

public class AirAvionicsClientTest {

    private static boolean INTELPROXY = true;

    private static void useIntelProxy() {
        System.setProperty("socksProxyHost", "proxy-us.intel.com");
        System.setProperty("socksProxyPort", "1080");
    }

    @Test
    public void Test_Default_Token() {
        var c = new AirAvionicsClient();
        c.refreshDefaultToken();
        c.refreshTokenIfNeeded();
    }

    @Test
    public void Test_Around_Endpoint() {
        var c = new AirAvionicsClient();
        //Frankfurt airport: 50.0379° N, 8.5622° E
        c.refreshDefaultToken();
        var res = c.getTraffic("https://endpoints-esp-dqgoqs6jea-ew.a.run.app/api/v1/traffic/around", 50.0379, 8.5622, 10000, 5);
        System.out.println(res);
    }

    @Test
    public void Test_If_Invalid_Timestamp_Throws_Exception() {
        var c = new AirAvionicsClient();

        c.publishUavPosition(10, 10, 10, 10, 1, "foo", true, 10, "f", 0, 0, 10);
    }

    @Test
    public void Test_If_Publishing_Uav_Position_Works() {
        if (INTELPROXY) {
            useIntelProxy();
        }

        var c = new AirAvionicsClient();

        var foo = c.publishUavPosition(10, 10, 10, 10, 1, "imc_test", true, 10, "", 1, 0, 10);
    }
}