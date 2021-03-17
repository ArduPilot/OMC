/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.mavinci.airspace.Airspace;
import eu.mavinci.airspace.AirspaceTypes;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class AirspaceExpirationTests {
    @Test
    public void testNotExpired_ifStillFreshAirspace() throws Exception {
        LocalDateTime dateTimeEffective = LocalDateTime.now().plusHours(8);

        Airspace airspace = new Airspace("ID", AirspaceTypes.TFR);
        airspace.updateExpiration(dateTimeEffective);

        assertFalse(airspace.isExpired());
    }

    @Test
    public void testExpired_ifExpiredAirspace() throws Exception {
        LocalDateTime dateTimeEffective = LocalDateTime.now().minusHours(24);

        Airspace airspace = new Airspace("ID", AirspaceTypes.TFR);
        airspace.updateExpiration(dateTimeEffective);

        assertTrue(airspace.isExpired());
    }

    @Test
    public void testNotExpired_ifAirspaceWithoutDateEffective() throws Exception {
        Airspace airspace = new Airspace("ID", AirspaceTypes.TFR);

        assertFalse(airspace.isExpired());
    }
}
