/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain;

public enum FlightplanExportTypes {
    KML("KML"),
    CSV("CSV"),
    RTE("RTE"),
    GPX("GPX"),
    FPL("FPL"),
    ASCTECCSV("AscTec Navigator - Flight plan only"),
    ASCTECJPG("ASCTECJPG"),
    ASCTECCSVJPG("AscTec Navigator - Flight plan + map ( Intel Falcon 8 Format )"),
    ANP("Intel Falcon 8+ Format"),
    ACP("Intel Falcon 8+ Format (Cockpit Project)"),
    LCSV("Litchi CSV"),;

    private String description;

    FlightplanExportTypes(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static FlightplanExportTypes fromDescription(String description) {
        for (FlightplanExportTypes type : FlightplanExportTypes.values()) {
            if (type.getDescription().equalsIgnoreCase(description)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Can't find FP type with description " + description);
    }

}
