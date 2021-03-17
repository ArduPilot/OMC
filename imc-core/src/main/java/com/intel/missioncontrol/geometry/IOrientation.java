/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

/**
 * Orientation of a drone (or part thereof) in earth frame, see
 * https://en.wikipedia.org/wiki/Flight_dynamics_(fixed-wing_aircraft) Yaw angle: 0°: north, 90° east Pitch Angle: 0°
 * horizontal, 90° vertically up Roll angle: 0° horizontal (for pitch = 0°), 90° right wing vertically down (for pitch =
 * 0°)
 */
public interface IOrientation {

    double getYawDeg();

    double getPitchDeg();

    double getRollDeg();

}
