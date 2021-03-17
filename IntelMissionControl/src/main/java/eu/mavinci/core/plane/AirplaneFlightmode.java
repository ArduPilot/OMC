/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

public enum AirplaneFlightmode {
    // TODO FIXME merge this into here:
    /*
        IMC_FLIGHTMODE_NOT_AVAILABLE("eu.mavinci.desktop.rs232.asctec.IMC_FLIGHTMODE.NOT_AVAILABLE"),
    IMC_FLIGHTMODE_GPS("eu.mavinci.desktop.rs232.asctec.IMC_FLIGHTMODE.GPS"),
    IMC_FLIGHTMODE_HEIGHT("eu.mavinci.desktop.rs232.asctec.IMC_FLIGHTMODE.HEIGHT"),
    IMC_FLIGHTMODE_MANUAL("eu.mavinci.desktop.rs232.asctec.IMC_FLIGHTMODE.MANUAL");
     */
    ManualControl(0, "eu.mavinci.core.plane.AirplaneFlightmode.ManualControl"), // =0
    AutomaticFlight(1, "eu.mavinci.core.plane.AirplaneFlightmode.AutomaticFlight"), // =1
    AssistedFlying(2, "eu.mavinci.core.plane.AirplaneFlightmode.AssistedFlying"), // =2
    MotorShutdownNLocked(3,"eu.mavinci.core.plane.AirplaneFlightmode.MotorShutdownNLocked"), //=3
    Guided(4,"eu.mavinci.core.plane.AirplaneFlightmode.GuidedFlying"),//=4
    Loiter(5,"eu.mavinci.core.plane.AirplaneFlightmode.Loiter"),//=5,
    RTL(6,"eu.mavinci.core.plane.AirplaneFlightmode.Rtl"),//=6,
    Brake(7,"eu.mavinci.core.plane.AirplaneFlightmode.Brake"),//=7,
    Placeholder4(8,"eu.mavinci.desktop.rs232.asctec.IMC_FLIGHTMODE.NOT_AVAILABLE"),//=8
    Land(9,"eu.mavinci.core.plane.AirplaneFlightmode.Landed") //=9
    ;

    private int value;
    private String displayNameKey;

    AirplaneFlightmode(int value, String displayNameKey) {
        this.value = value;
        this.displayNameKey = displayNameKey;
    }

    public String getDisplayNameKey() {
        return displayNameKey;
    }
}
