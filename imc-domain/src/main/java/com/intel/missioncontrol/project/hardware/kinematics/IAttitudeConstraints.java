/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.Arc;

/**
 * Earth frame attitude constraints for the optical axis of a payload.
 * https://en.wikipedia.org/wiki/Flight_dynamics_(fixed-wing_aircraft)
 *
 * <p>These constraints are valid for flight planning and define the range of values that can be sent to a drone. Actual
 * payload attitude might exceed these constraints due to unconstrained orientation of the drone body.
 */
public interface IAttitudeConstraints {

    /**
     * Allowed pitch angle range of the optical axis with respect to the horizon. A 0° angle corresponds to horizontal,
     * -90° to looking downward, 30° to looking slightly upward, etc. Definition according to body frame in
     * https://en.wikipedia.org/wiki/Flight_dynamics_(fixed-wing_aircraft)
     */
    Arc getPitchRange();

    /**
     * Roll angle, in degrees, about the optical axis. A 0° angle corresponds to horizontal ("landscape" orientation if
     * pitch is horizontal), rotating in a clockwise sense. Definition according to body frame in
     * https://en.wikipedia.org/wiki/Flight_dynamics_(fixed-wing_aircraft)
     */
    double getRollDeg();
}
