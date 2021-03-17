/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.exporter;

import eu.mavinci.desktop.gui.doublepanel.planemain.FlightplanExportTypes;

public interface IFlightplanExporterFactory {
    IFlightplanExporter createExporter(FlightplanExportTypes type);
}
