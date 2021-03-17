/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import gov.nasa.worldwind.geom.Sector;
import java.util.List;

public interface IAirspaceList {

	public abstract List<IAirspace> getAirspaces();

	/**
	 * @param bb
	 * @return the maximum height a mav is allowed to fly in the given bounding box
	 * the bounding box has to be small in comparison to the airspace sizes
	 * altitude above sea level in meter
	 */
	public abstract LowestAirspace getMaxMAVAltitude(Sector bb,
			double groundLevelElevation);

	/**
	 * @param bb
	 * @return all airspaces which overlap with the bounding box
	 */
	public abstract List<IAirspace> getAirspaces(Sector bb);

	/**
	 * @param bb
	 * @return all airspace indices whose bounding box is  overlapping with bb
	 */
	public abstract List<Integer> getAirspaceIndices(Sector bb);

	
	public abstract boolean isActive();

	public abstract void setActive(boolean active);
	
	public abstract int size();

	public void setListener(IAirspaceListener listener);
	
	public String getName();
	
}