/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;


import gov.nasa.worldwind.geom.LatLon;

public class LowestAirspace {

	IAirspace relevant = null;
	
	double min = Double.MAX_VALUE;
	double groundLevelElevationEGM;
	
	public LowestAirspace(double groundLevelElevationEGM){
		this.groundLevelElevationEGM = groundLevelElevationEGM;
	}
	
	public double getGroundElevation(){
		return groundLevelElevationEGM;
	}
	
	
	public void computeOther(IAirspace other, LatLon ll){
		computeOther(other, ll, groundLevelElevationEGM);
	}
	
	public void computeOther(IAirspace other, LatLon ll, double groundLevelElevationEGM){
		double next = other.floorMeters(ll,groundLevelElevationEGM);
		if (next < min) {
			relevant = other;
			min = next;
		}
	}
	
	public IAirspace getMinimalAirspace(){
		return relevant;
	}
	public double getMinimalAltEGM(){
		return min;
	}
	public double getMinimalAltOverGround(){
		return min-groundLevelElevationEGM;
	}
	
	@Override
	public String toString() {
		return relevant + " at Level " + min;
	}
}
