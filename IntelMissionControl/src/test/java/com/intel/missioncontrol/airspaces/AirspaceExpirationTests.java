/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces;

import eu.mavinci.airspace.Airspace;
import eu.mavinci.airspace.AirspaceTypes;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AirspaceExpirationTests {
    @Test
    public void testNotExpired_ifStillFreshAirspace() throws Exception {
        LocalDateTime dateTimeEffective = LocalDateTime.now().plusHours(8);

        Airspace airspace = new Airspace("ID", AirspaceTypes.TFR);
        airspace.updateExpiration(dateTimeEffective);

        assertThat(airspace.isExpired(), is(false));
    }

    @Test
    public void testExpired_ifExpiredAirspace() throws Exception {
        LocalDateTime dateTimeEffective = LocalDateTime.now().minusHours(24);

        Airspace airspace = new Airspace("ID", AirspaceTypes.TFR);
        airspace.updateExpiration(dateTimeEffective);

        assertThat(airspace.isExpired(), is(true));
    }

    @Test
    public void testNotExpired_ifAirspaceWithoutDateEffective() throws Exception {
        Airspace airspace = new Airspace("ID", AirspaceTypes.TFR);

        assertThat(airspace.isExpired(), is(false));
    }
}
