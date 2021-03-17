/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.util.ArrayList;
import java.util.List;

public class GolfUpperBoundAirspace implements IAirspace {

	@Override
	public String getId() {
		return "GolfUpperBoundAirspace";
	}

	public double floorMeters(LatLon pos) {
		return floorMeters(pos,ground_alt_provider.groundElevationMetersEGM(pos.getLatitude()
						.getDegrees(), pos.getLongitude().getDegrees()));
	}

	public  static double GOLF_FLOOR_METERS_REL =2500* OpenAirspaceParser.FT_TO_METER;
	public static double getGOLF_FLOOR_METERS_REL(){
		return GOLF_FLOOR_METERS_REL;
	}
	public static double getGOLF_FLOOR_METERS_ABS(){
		return GOLF_FLOOR_METERS_ABS;
	}
	
	
	public static double GOLF_FLOOR_METERS_ABS = 2000; //typical upper UAV limit
	
	public double floorMeters(LatLon ref, double groundElevationMeters) {
		double limit_relToAbs = GOLF_FLOOR_METERS_REL + groundElevationMeters;
		return (int) Math.min(GOLF_FLOOR_METERS_ABS, limit_relToAbs);
	}

	public Sector getBoundingBox() {
		return bbox;
	}

	public String getName() {
		return "Upper bound of Golf";
	}


	public List<LatLon> getPolygon() {
		return list;
	}

	public AirspaceTypes getType() {
		return AirspaceTypes.ClassE;
	}

	@Override
	public boolean isExpired() {
		return false;
	}

	public boolean insidePolygon(LatLon ref) {
		return true;
	}

	public void setGroundProvider(IGroundAltProvider provider) {
		ground_alt_provider = provider;
	}

	public boolean withinAirspace(LatLon pos, double altitudeAbsoluteMeters) {
		return (altitudeAbsoluteMeters >= floorMeters(pos));
	}

	private IGroundAltProvider ground_alt_provider;
	private List<LatLon> list = new ArrayList<LatLon>();
	private Sector bbox = Sector.fromDegrees(-90, 90, 0, 360);

	public Double getFloorReferenceGround() {
		return GOLF_FLOOR_METERS_REL;
	}
	
	public Double getFloorReferenceSeaLevel() {
		return GOLF_FLOOR_METERS_ABS;
	}
	
	public double getFloorReferenceGroundOrSeaLevel() {
		return GOLF_FLOOR_METERS_REL;
	}
	
	public double getCeilingReferenceGroundOrSeaLevel() {
		return Integer.MAX_VALUE;
	}
	
	String title = null;
	
	public void setTitle(String str) {
		title = str;
	}
	
	@Override
	public String toString() {
		if (title != null) return title;
		return super.toString();
	}

	
	public void setCountry(String iso2) {
	}
}
