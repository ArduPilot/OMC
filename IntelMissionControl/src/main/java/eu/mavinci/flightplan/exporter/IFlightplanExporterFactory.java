package eu.mavinci.flightplan.exporter;

import eu.mavinci.desktop.gui.doublepanel.planemain.FlightplanExportTypes;

public interface IFlightplanExporterFactory {
    IFlightplanExporter createExporter(FlightplanExportTypes type);
}
