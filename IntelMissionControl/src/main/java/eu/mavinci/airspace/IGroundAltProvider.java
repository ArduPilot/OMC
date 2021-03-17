/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

public interface IGroundAltProvider {
	/**
	 * Get ground elevation from some topography model relative to EGM
	 * @param latitude in degrees
	 * @param lonngitude in degrees
	 * @return the ground elevation in meters at this point
	 */
	public double groundElevationMetersEGM(double lat_degrees, double lon_degrees);
}
