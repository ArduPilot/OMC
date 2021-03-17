/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model.discovery;

import static org.junit.Assert.*;

import org.junit.Test;

public class DiscoveryAdvertisementTest {

    @Test
    public void testSerialize() {
        DiscoveryAdvertisement adv = new DiscoveryAdvertisement();
        adv.connectionType = "udp";
        adv.connectionPort = 2222;
        adv.connectionHost = "foo";
        adv.name = "Drone";

        String serial = adv.serialize();
        assertEquals("MAVLINK_ENDPOINT|udp;foo;2222|Drone", serial);
    }

    @Test
    public void testParse() {
        String string = "MAVLINK_ENDPOINT|udp;foo;2222|Drone";

        DiscoveryAdvertisement adv = DiscoveryAdvertisement.parse(string);
        assertEquals("foo", adv.connectionHost);
        assertEquals(2222, adv.connectionPort);
        assertEquals("udp", adv.connectionType);
    }
}