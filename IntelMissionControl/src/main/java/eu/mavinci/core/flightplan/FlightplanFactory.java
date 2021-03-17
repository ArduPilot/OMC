/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import eu.mavinci.flightplan.FlightplanFactoryBase;

public class FlightplanFactory {

    private static IFlightplanFactory factory; // = new FlightplanFactoryCore();

    public static IFlightplanFactory getFactory() {
        if (factory == null) {
            factory = new FlightplanFactoryBase();
        }

        return factory;
    }

    public static void setFactory(IFlightplanFactory factory) {
        FlightplanFactory.factory = factory;
    }
}
