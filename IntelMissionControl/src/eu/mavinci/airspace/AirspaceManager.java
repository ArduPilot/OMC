/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

public class AirspaceManager {
	/**
	 * For airspaces that have their floor or ceiling defined relative to the
	 * ground elevation, this elevation is needed for calculating whether the
	 * plane is inside the airspace
	 * 
	 * @param provider
	 *            someone who can tell ground elevation
	 */
	public static void setGroundProvider(IGroundAltProvider provider){
		groundProvider = provider;
	}
	static protected IGroundAltProvider groundProvider = null;
}
