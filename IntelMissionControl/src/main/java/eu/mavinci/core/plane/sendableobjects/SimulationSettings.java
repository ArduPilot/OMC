/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

public class SimulationSettings extends MObject {

    private static final long serialVersionUID = -4882416338844458801L;

    /** wind direction in degrees */
    public float windDir = 0f;

    /** wind speed in km/h */
    public float windSpeed = 0f;

    /** turbulence gain (strength) TODO: Don't know units */
    public float turbGain = 0f;

    /** turbulence rate (how many disturbances per second) */
    public float turbRate = 0f;

    /** max X,Y,Z components of gusting wind TODO: Don't know units */
    public float gustX = 0f;

    public float gustY = 0f;
    public float gustZ = 0f;
}
