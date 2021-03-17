package eu.mavinci.flightplan.asctec;

/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import eu.mavinci.core.obfuscation.IKeepAll;
import eu.mavinci.core.obfuscation.IKeepAll;

public class ATOS_DEFINES implements IKeepAll {

    // for single wp
    public static final int NAV_FLAG_SINGLE_WAYPOINTS = 0x00200000;
    public static final int WP_FLAG_CAM_PITCH_ACTIVE = 0x00000001;
    public static final int WP_FLAG_HEIGHT_ACTIVE = 0x00000040;
    public static final int WP_FLAG_CAM_YAW_ACTIVE = 0x00000004;

    // For spline trajectory
    public static final int NAV_FLAG_CUBIC_SPLINE = 0x00800000;

    // for matrix wp
    public static final int NAV_FLAG_MATRIX_SPLINE = 0x00400000;
    public static final int NAV_FLAG_ABSOLUTE_ORIGIN = 0x00010000;
    public static final int WP_FLAG_RELATIVE_COORDS = 0x00000080;
    // public static final int NAV_FLAG_HEADING_ALONG_PATH

    public static final int NAV_FLAG_RING_BUFFER = 0x02000000;
    public static final int WP_FLAG_STOP = 0x00000100;
    public static final int NAV_FLAG_SINGLE_WP_SPLINE = 0x40000000;
    public static final int WP_NO_FLAG = 0x00000000;

}
