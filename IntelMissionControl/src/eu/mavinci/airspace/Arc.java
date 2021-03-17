/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import eu.mavinci.desktop.helper.MathHelper;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.Earth;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Arc implements IAirspacePointConnector, Serializable {

	public enum TurnDir {
		LEFT, //-
		RIGHT //+
	}

	public Arc(LatLon center, LatLon p1, LatLon p2, TurnDir turn_dir) {
		this.arc_center = center;
		this.p1 = p1;
		this.p2 = p2;
		this.turn_dir = turn_dir;
	}
	
	public Arc(LatLon center, double radiusNM, double angleStartDeg,
			double angleEndDeg, TurnDir turn_dir) {
		this.arc_center = center;
        this.p1 =
            LatLon.greatCircleEndPosition(
                center, Angle.fromDegrees(angleStartDeg), Angle.fromDegrees(radiusNM * OpenAirspaceParser.MNI_TO_RAD));
		this.p2 = LatLon.greatCircleEndPosition(center, Angle.fromDegrees(angleEndDeg), Angle.fromDegrees(radiusNM * OpenAirspaceParser.MNI_TO_RAD));
//		System.out.println("p1="+p1);
//		System.out.println("p2="+p2);
		this.turn_dir = turn_dir;
	}

	public TurnDir getTurnDir() {
		return turn_dir;
	}

	public List<LatLon> getConnectingPoints(int num_of_sampling_points) {
		List<LatLon> interpolation = new ArrayList<LatLon>();
		double tc1 = MathHelper.getTrueCourseRadians(arc_center, p1);
		double tc2 = MathHelper.getTrueCourseRadians(arc_center, p2);
//		if (tc2 < tc1) tc2+=2*Math.PI;
		
		if (turn_dir == TurnDir.LEFT){ //FIXME, LEFT was originally here
			if (tc2 > tc1) tc2-= 2*Math.PI;
		} else {
			if (tc2 < tc1) tc2+= 2*Math.PI;
		}
//		System.out.println("TC from" + arc_center + " to " + p1 + " tc1= " + tc1* 180 / Math.PI);
		
		
//		System.out.println("TC from" + arc_center + " to " + p2 + " tc2= " + tc2* 180 / Math.PI);
		
		
//		System.out.println((turn_dir==TurnDir.LEFT ? "Dir: -" : "Dir: +") );
		
		double r1 = LatLon.greatCircleDistance(arc_center, p1).radians* Earth.WGS84_POLAR_RADIUS;
		double r2 = LatLon.greatCircleDistance(arc_center, p2).radians* Earth.WGS84_POLAR_RADIUS;
		
//		System.out.println("r from" + arc_center + " to " + p2 + " = " + r2);
//		System.out.println("r from" + arc_center + " to " + p1 + " = " + r1);
//		System.out.println("r from" + arc_center + " r= " + ((r1+r2)/2));
		
		
		double error = Math.abs(r2 - r1)/((Math.abs(r1)+Math.abs(r2))/2); //relative error
		if (error > 0.13)
			throw new RuntimeException("Errornous arc definition:  relative error == "+error);
		
		for (int i = 0; i <= num_of_sampling_points; i++) {
			double tc;
			double r;
			double step = ((double)i) / num_of_sampling_points;
//			if (turn_dir == TurnDir.LEFT){ //FIXME, LEFT was originally here
//				tc = tc1 + (tc2 - tc1) * step;
//				r = r1 + (r1-r2) * step;
//			} else{
				tc = tc1 + (tc2 - tc1) * step;
				r = r1 + (r1-r2) * step;
//			}
			//System.out.println("TC[" + i + "] = " + tc[i] * 180 / Math.PI);
			interpolation.add(LatLon.greatCircleEndPosition(arc_center, Angle.fromRadians(tc), Angle.fromRadians(r / Earth.WGS84_POLAR_RADIUS)));
//			double d = (Math.PI / (180 * 60)) * r1 / 1852.0f;
//			double lat = Math.asin(Math.sin(arc_center.getLatitude()
//					.getRadians())
//					* Math.cos(d)
//					+ Math.cos(arc_center.getLatitude().getRadians())
//					* Math.sin(d) * Math.cos(tc));
//			double lon;
//			if (Math.cos(lat) == 0)
//				lon = arc_center.getLongitude().getRadians(); // endpoint a pole
//			else
//				lon = ((arc_center.getLongitude().getRadians()
//						- Math.asin(Math.sin(tc) * Math.sin(d)
//								/ Math.cos(lat)) + Math.PI) % (2 * Math.PI))
//						- Math.PI;
//
//			LatLon inbetween = new LatLon(lat * 180 / Math.PI, lon * 180
//					/ Math.PI);
//			interpolation.add(inbetween);
		}
//		System.out.println("first" + p1 + " second" + p2 + " => "+ interpolation);
		return interpolation;
	}

	public LatLon center() {
		return arc_center;
	}

	protected void setTurnDirLeft() {
		turn_dir = TurnDir.LEFT;
	}

	protected void setTurnDirRight() {
		turn_dir = TurnDir.RIGHT;
	}

	private LatLon arc_center;
	private TurnDir turn_dir;
	private LatLon p1;
	private LatLon p2;

}
