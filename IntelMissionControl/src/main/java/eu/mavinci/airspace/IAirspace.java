/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public interface IAirspace extends Serializable {
	/**
	 * Airspace's ID
	 * @return id as defined by implementation
	 */
	String getId();

	/**
	 * Get the floor of this airspaces at the given position
	 * 
	 * @param pos
	 * @return floor altitude in absolute meters above sea level
	 */
	public double floorMeters(LatLon pos);
	
	/**
	 * Get the floor of this airspaces at the given position
	 * 
	 * @param pos
	 * @param groundElevationMetersEGM the ground elevation at the given pos
	 * @return floor altitude in absolute meters above sea level
	 */
	public double floorMeters(LatLon ref, double groundElevationMetersEGM);

	/**
	 * Is this position inside the airspace?
	 * 
	 * @param pos
	 * @param altitude_absolute_meters
	 *            in meters above sea level
	 */
	public boolean withinAirspace(LatLon pos, double altitude_absolute_meters);

	/**
	 * get a polygon that approximates the shape of this airspace. The list
	 * contains all vertices and is left open, so you must connect the last list
	 * entry to the first for a closed polygon
	 * 
	 * @return
	 */
	public List<LatLon> getPolygon();
	
	
	/**
	 * @return name of the airspace
	 */
	public String getName();
	
	/**
	 * Is this position inside of the airspace polygon in 2d, 
	 * regardless of altitude? 
	 * @param 
	 * @return
	 */
	public boolean insidePolygon(LatLon ref);
	
	
	/**
	 * @return
	 */
	public AirspaceTypes getType();

    /**
     * @return true if Airspace's "effective until to" is set and it's already expired
     */
    public boolean isExpired();

	/**
	 * @return an approximated 2d bounding box of the airspace
	 */
	public Sector getBoundingBox();
	

	/**
	 * returns null it it is not referenced like this
	 * 
	 */
	public Double getFloorReferenceGround();
	public Double getFloorReferenceSeaLevel();
	
	/**
	 * returns ground if this is avaliable, otherwise sealevel
	 * @return
	 */
	public double getFloorReferenceGroundOrSeaLevel();
	public double getCeilingReferenceGroundOrSeaLevel();
	
	public void setTitle(String str);
	
	public void setCountry(String iso2);
}
