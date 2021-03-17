/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import kong.unirest.Unirest;
import org.junit.jupiter.api.Test;

public class TestUnirest {
    @Test
    public void testUnirest(){
       /* System.setProperty("http.proxyHost", "http://proxy-chain.intel.com");
        System.setProperty("http.proxyPort", "911");
        System.setProperty("https.proxyHost", "https://proxy-chain.intel.com");
        System.setProperty("https.proxyPort", "912");*/
        var resp = Unirest.get("https://imc-insight-dev.tm.intel.com/").asJson();
        System.out.println();
    }
}
