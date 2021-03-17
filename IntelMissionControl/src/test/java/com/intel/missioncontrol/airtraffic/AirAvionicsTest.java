/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic;

import org.junit.Test;

public class AirAvionicsTest {
    private static boolean INTELPROXY = true;

    private static void useIntelProxy() {
        System.setProperty("socksProxyHost", "proxy-us.intel.com");
        System.setProperty("socksProxyPort", "1080");
    }

    @Test
    public void Get_Traffic() {
        if(INTELPROXY) {
            useIntelProxy();
        }
        var t = new AirAvionics();
        //Frankfurt airport: 50.0379° N, 8.5622° E
        var atos = t.getTraffic(50.0379, 8.5622, 500000, 10);
        System.out.println(atos);
        System.out.println("number of aircraft:" + atos.size());
    }

    @Test
    public void Parse_Response() {
        var testString = "{\"features\":[{\"geometry\":{\"coordinates\":[8.56348133087158,50.039966583252],\"type\":\"Point\"},\"id\":1523625876,\"properties\":{\"identifier\":\"3C6758\",\"altitude\":{\"wgs84\":168,\"barometric\":145},\"idType\":1,\"courseOverGround\":278,\"flightId\":\"DLH1453\",\"speedOverGround\":11,\"time\":\"2019-11-29T09:09:00.000Z\",\"type\":0,\"distanceTo\":247.61934721764152}},{\"geometry\":{\"coordinates\":[8.56477069854736,50.041748046875],\"type\":\"Point\"},\"id\":1908817236,\"properties\":{\"identifier\":\"A290D0\",\"altitude\":{\"wgs84\":160,\"barometric\":145},\"idType\":1,\"courseOverGround\":70,\"flightId\":\"UAL58\",\"speedOverGround\":4,\"time\":\"2019-11-29T09:08:59.000Z\",\"type\":5,\"verticalSpeed\":-0.3,\"distanceTo\":466.124466551083}},{\"geometry\":{\"coordinates\":[8.56879425048828,50.0382957458496],\"type\":\"Point\"},\"id\":1550230694,\"properties\":{\"identifier\":\"4A08E6\",\"altitude\":{\"wgs84\":122,\"barometric\":145},\"idType\":1,\"courseOverGround\":68,\"flightId\":\"????????\",\"speedOverGround\":15,\"time\":\"2019-11-29T09:08:57.000Z\",\"type\":0,\"verticalSpeed\":-0.7,\"distanceTo\":473.52715887754357}},{\"geometry\":{\"coordinates\":[8.54654693603516,50.040397644043],\"type\":\"Point\"},\"id\":1523625859,\"properties\":{\"identifier\":\"3C674F\",\"altitude\":{\"barometric\":137},\"idType\":1,\"courseOverGround\":160,\"flightId\":\"DLH3LF\",\"time\":\"2019-11-29T09:08:58.000Z\",\"type\":0,\"distanceTo\":1153.1529788853077}},{\"geometry\":{\"coordinates\":[8.58260440826416,50.0336494445801],\"type\":\"Point\"},\"id\":1523562326,\"properties\":{\"identifier\":\"3C4318\",\"altitude\":{},\"idType\":1,\"courseOverGround\":191,\"flightId\":\"V82\",\"speedOverGround\":6,\"cog\":0,\"time\":\"2019-11-29T09:08:57.000Z\",\"type\":21,\"distanceTo\":1533.74928649207}},{\"geometry\":{\"coordinates\":[8.58610439300537,50.0448303222656],\"type\":\"Point\"},\"id\":1523623518,\"properties\":{\"identifier\":\"3C64F6\",\"altitude\":{\"barometric\":152},\"idType\":1,\"courseOverGround\":217,\"flightId\":\"DLH462\",\"speedOverGround\":3,\"time\":\"2019-11-29T09:08:59.000Z\",\"type\":5,\"distanceTo\":1875.053919346552}},{\"geometry\":{\"coordinates\":[8.59004688262939,50.0389785766602],\"type\":\"Point\"},\"id\":1523562325,\"properties\":{\"identifier\":\"3C4317\",\"altitude\":{},\"idType\":1,\"courseOverGround\":68,\"flightId\":\"V81\",\"speedOverGround\":7,\"cog\":0,\"time\":\"2019-11-29T09:08:57.000Z\",\"type\":21,\"distanceTo\":1994.589681068663}},{\"geometry\":{\"coordinates\":[8.53056812286377,50.030143737793],\"type\":\"Point\"},\"id\":1523559561,\"properties\":{\"identifier\":\"3C4052\",\"altitude\":{},\"idType\":1,\"flightId\":\"RM505\",\"cog\":0,\"time\":\"2019-11-29T09:08:59.000Z\",\"type\":21,\"distanceTo\":2420.9949963813956}},{\"geometry\":{\"coordinates\":[8.52704238891602,50.0342636108398],\"type\":\"Point\"},\"id\":1538216678,\"properties\":{\"identifier\":\"440039\",\"altitude\":{\"barometric\":137},\"idType\":1,\"courseOverGround\":250,\"flightId\":\"AUA1BE\",\"speedOverGround\":2,\"time\":\"2019-11-29T09:08:59.000Z\",\"type\":0,\"distanceTo\":2546.1762979705477}},{\"geometry\":{\"coordinates\":[8.52596855163574,50.0315895080566],\"type\":\"Point\"},\"id\":1523456740,\"properties\":{\"identifier\":\"3C0A58\",\"altitude\":{\"barometric\":137},\"idType\":1,\"courseOverGround\":180,\"flightId\":\"DLH23W\",\"speedOverGround\":11,\"time\":\"2019-11-29T09:08:57.000Z\",\"type\":3,\"distanceTo\":2684.1968060087106}},{\"geometry\":{\"coordinates\":[8.5270528793335,49.9789123535156],\"type\":\"Point\"},\"id\":1514448370,\"properties\":{\"identifier\":\"39856E\",\"altitude\":{\"wgs84\":564,\"barometric\":549},\"idType\":1,\"courseOverGround\":178,\"flightId\":\"AFR1519\",\"speedOverGround\":87,\"time\":\"2019-11-29T09:08:57.000Z\",\"type\":3,\"verticalSpeed\":12,\"distanceTo\":7031.411657432627}},{\"geometry\":{\"coordinates\":[8.65215214093526,50.0080718994141],\"type\":\"Point\"},\"id\":1926138290,\"properties\":{\"identifier\":\"C0A820D0\",\"altitude\":{\"wgs84\":81},\"idType\":4,\"flightId\":\"ATS-2 8\",\"cog\":0,\"time\":\"2019-11-29T09:09:00.277Z\",\"type\":22,\"distanceTo\":7239.735307813987}},{\"geometry\":{\"coordinates\":[8.6525936126709,50.0075429280599],\"type\":\"Point\"},\"id\":1926138288,\"properties\":{\"identifier\":\"C0A820D2\",\"altitude\":{\"wgs84\":120},\"idType\":4,\"flightId\":\"ATS-2 10\",\"cog\":0,\"time\":\"2019-11-29T09:08:58.088Z\",\"type\":22,\"distanceTo\":7294.929235208628}}],\"type\":\"FeatureCollection\"}\n";
        var t = new AirAvionics();
        var atos = t.parseJson(testString);
        System.out.println(atos);
    }
}