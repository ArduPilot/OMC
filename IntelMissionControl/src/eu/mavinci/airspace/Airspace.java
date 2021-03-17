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
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

public class Airspace implements IAirspace, Serializable {

	public static final double NM_TO_METER = 1852.0;
	
	public static final double SAFETY_MARGIN_IN_METER = 20;
	public static final double MIN_FLY_ALT_IN_METER = 150;
	private String id;

	private String name;
	private double floor_meters_ground;
	private boolean floor_reference_is_ground_active = false;

	private double floor_meters_seaLevel;
	private boolean floor_reference_is_seaLevel_active = false;
	
	private boolean ceiling_reference_is_ground_active = false;
	private double ceiling_meters_ground;
	
	private boolean ceiling_reference_is_seaLevel_active = false;
	private double ceiling_meters_seaLevel;

	
	private AirspaceTypes type;
	protected List<LatLon> vertices = new LinkedList<LatLon>();
    private LocalDateTime expirationDateTime;

    public Airspace(Airspace airspace) {
		name = airspace.name;
		floor_meters_ground = airspace.floor_meters_ground;
		floor_reference_is_ground_active = airspace.floor_reference_is_ground_active;
		floor_meters_seaLevel = airspace.floor_meters_seaLevel;
		floor_reference_is_seaLevel_active = airspace.floor_reference_is_seaLevel_active;
		ceiling_reference_is_ground_active = airspace.ceiling_reference_is_ground_active;
		ceiling_meters_ground = airspace.ceiling_meters_ground;
		ceiling_reference_is_seaLevel_active = airspace.ceiling_reference_is_seaLevel_active;
		ceiling_meters_seaLevel = airspace.ceiling_meters_seaLevel;
		type = airspace.type;
		countryIso2 = airspace.countryIso2;
	}

	public Airspace(Airspace airspace, String id) {
		this(airspace);
		this.id = id;
	}

	public Airspace(String name, AirspaceTypes type) {
		this.name = name;
		this.type = type;
		this.floor_reference_is_ground_active = false;
	}

    public Airspace(String name, AirspaceTypes type, String id) {
        this(name, type);
        this.id = id;
    }

	public double floorMeters(LatLon ref, double groundElevationMeters) {
		
		
		if (!insidePolygon(ref)) {
			return Double.POSITIVE_INFINITY;
		}

		double ret = Double.POSITIVE_INFINITY;
//		System.out.println(countryIso2);
//		if ( AirspaceTypes.ClassB==type && "US".equalsIgnoreCase(countryIso2)){
//			if (floor_reference_is_ground_active){
//				ret = groundElevationMeters;
//			}
//			if (floor_reference_is_seaLevel_active){
//				ret = Math.min(ret, 0);
//			}
//			System.out.println("ret0");
//		} else {
		if (floor_reference_is_ground_active){
			if (groundElevationMeters == Double.NEGATIVE_INFINITY) groundElevationMeters = AirspaceManager.groundProvider.groundElevationMetersEGM(ref.getLatitude().getDegrees(), ref.getLongitude().getDegrees());
			ret = Math.min(ret, groundElevationMeters +floor_meters_ground);
		}
		if (floor_reference_is_seaLevel_active){
			ret = Math.min(ret, floor_meters_seaLevel);
		}
//		}
		return ret;
	}

	@Override
	public String getId() {
		return id == null ? name.trim().replaceAll(" ", "_").toLowerCase() : id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double floorMeters(LatLon ref) {
		return floorMeters(ref, Double.NEGATIVE_INFINITY);
	}

	public boolean withinAirspace(LatLon pos, double altitudeAbsoluteMeters) {
		if (insidePolygon(pos))
			if ((floorMeters(pos) < altitudeAbsoluteMeters)
					&& (altitudeAbsoluteMeters < ceilingMeters(pos)))
				return true;
		return false;
	}

	public List<LatLon> getPolygon() {
		return vertices;
//		return new ArrayList<LatLon>();
	}
	
	Sector bb = null;
	
	// not optimized version by iterating over polygon
	// TODO will not work at north/south pole and time line
	public Sector getBoundingBox() {
		if (bb==null){
			double minLat = Double.POSITIVE_INFINITY;
			double maxLat = Double.NEGATIVE_INFINITY;
			double minLon = Double.POSITIVE_INFINITY;
			double maxLon = Double.NEGATIVE_INFINITY;
			for (LatLon l : getPolygon()) {
				minLat = Math.min(minLat, l.getLatitude().getDegrees());
				maxLat = Math.max(maxLat, l.getLatitude().getDegrees());
				minLon = Math.min(minLon, l.getLongitude().getDegrees());
				maxLon = Math.max(maxLon, l.getLongitude().getDegrees());
			}
			bb = Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
//			System.out.println();
//			System.out.println(bb);
//			System.out.println(getPolygon());
		}
		return bb;
	}

	public String getName() {
		return name;
	}
	
	@Override //todo more fields to display
	public String toString() {
		if (title != null) return title;
		String str = getName() + "(" +getType() + ")";
		str += " - Lower: ";
		if (floor_reference_is_ground_active){
			str +=" " + floor_meters_ground + "m over Ground";
		}
		if (floor_reference_is_seaLevel_active){
			str +=" " + floor_meters_seaLevel + "m over Sealevel";
		}
		str += " - Upper: ";
		if (ceiling_reference_is_ground_active){
			str +=" " + ceiling_meters_ground + "m over Ground";
		}
		if (ceiling_reference_is_seaLevel_active){
			str +=" " + ceiling_meters_seaLevel + "m over Sealevel";
		}
		return str;
	}
	
	String title = null;
	
	public void setTitle(String str) {
		title = str;
	}
	

	public AirspaceTypes getType() {
		return type;
	}

    public void updateExpiration(LocalDateTime expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }

    @Override
    public boolean isExpired() {
        return expirationDateTime != null && LocalDateTime.now().until(expirationDateTime, ChronoUnit.HOURS) < 0;
    }

	public double ceilingMeters(LatLon ref) {
		return ceilingMeters(ref, Double.NEGATIVE_INFINITY);
	}
	
	public double ceilingMeters(LatLon ref, double groundElevationMeters) {
		if (!insidePolygon(ref))
			return Double.NEGATIVE_INFINITY;
		double ret = Double.NEGATIVE_INFINITY;
		if (ceiling_reference_is_ground_active){
			if (groundElevationMeters == Double.NEGATIVE_INFINITY) groundElevationMeters = AirspaceManager.groundProvider.groundElevationMetersEGM(ref.getLatitude().getDegrees(), ref.getLongitude().getDegrees());
			
			ret = Math.max(ret, groundElevationMeters + ceiling_meters_ground);
		}
		if (ceiling_reference_is_seaLevel_active){
			ret = Math.max(ret, ceiling_meters_seaLevel);
		}
		if (ceiling_reference_is_ground_active)
			return groundElevationMeters
					+ ceiling_meters_ground;
		return ceiling_meters_ground;
	}

	//fixme was 'protected' before
	public void setFloor(double meters, boolean reference_is_ground) {
		if (reference_is_ground){
			floor_reference_is_ground_active = true;
			floor_meters_ground = meters;
		} else {
			floor_reference_is_seaLevel_active = true;
			floor_meters_seaLevel = meters;
		}
	}
	
	
  //fixme was 'protected' before
	public void setCeiling(double meters, boolean reference_is_ground) {
		if (reference_is_ground){
			ceiling_reference_is_ground_active = true;
			ceiling_meters_ground = meters;
		} else {
			ceiling_reference_is_seaLevel_active = true;
			ceiling_meters_seaLevel = meters;
		}
	}

	//fixme was 'protected' before
	public void addVertex(LatLon vertex) {
		vertices.add(vertex);
	}

	public boolean insidePolygon(LatLon ref) {
		Sector bb= getBoundingBox();
		if (!bb.contains(ref)){
			return false;
		}
		//Special case: we are sitting exactly on a vertex: by definition, we are then inside the airspace
		for (LatLon v: vertices)
			if (Math.abs(ref.getLatitude().getDegrees() - v.getLatitude().getDegrees()) < 1/36000.0) 
				if (Math.abs(ref.getLongitude().getDegrees() - v.getLongitude().getDegrees()) < 1/36000.0)
					return true;
		
		double ref_lat_radians = ref.getLatitude().getRadians();
		double ref_lon_radians = ref.getLongitude().getRadians();
		List<Point> xy_vertices = getXYVertices(ref_lat_radians,
				ref_lon_radians);
		double x[] = new double[vertices.size() + 1];
		double y[] = new double[vertices.size() + 1];
		getCoordinateArray(xy_vertices, x, y);
		
		double c, s; // Ray direction
		// First try: Ray directly upwards:
		c = 0;
		s = 1;
		// The point to probe is ref, the center of our coordinate system, hence
		// xa=ya=0
		boolean special_case = false;
		do {
			special_case = false;
			for (int i = 0; i < vertices.size(); i++) {
				// Does the ray hit a vertex, then select another ray
				if (c * (y[i]) == s * (x[i]))
					special_case = true;
			}
			if (special_case) {
				// if we hit a vertex, randomly select another ray direction
				// in first quadrant until we not hit a vertex
//				System.out.println("Hit vertex");
				c = Math.random();
			}
		} while (special_case);

		int hits = 0;
		for (int i = 0; i < vertices.size(); i++) {
			double d = c * (y[i] - y[i + 1]) - s * (x[i] - x[i + 1]);
			double a = x[i] * (y[i] - y[i + 1]) - y[i] * (x[i] - x[i + 1]);
			double m = c * y[i] - s * x[i];
			//System.out.println("Checking ("+(int)c+","+(int)s+") ray bewteen ("+(int)x[i]+","+(int)y[i]+") and ("+(int)x[i+1]+","+(int)y[i+1]+")");
			if (((d > 0) && (a > 0) && (m > 0) && (m < d))
					|| ((d < 0) && (a < 0) && (m < 0) && (m > d))) {
				hits++;
			}
		}
		if (hits % 2 == 0)
			return false;
		else
			return true;
	}

	private List<Point> getXYVertices(double ref_lat_radians,
			double ref_lon_radians) {
		List<LatLon> pol_vertices = getPolygon();
		List<Point> xy_vertices = new LinkedList<Point>();
		for (LatLon p : pol_vertices) {
			double angular_dist_lat = p.getLatitude().getRadians()
					- ref_lat_radians;
			double angular_dist_lon = p.getLongitude().getRadians()
					- ref_lon_radians;
			double x_meters = angular_dist_lon * 180 / Math.PI * 60
					* NM_TO_METER * Math.cos(ref_lat_radians);
			double y_meters = angular_dist_lat * 180 / Math.PI * 60
			* NM_TO_METER;
			xy_vertices.add(new Point(x_meters, y_meters));
		}
		return xy_vertices;
	}

	private void getCoordinateArray(List<Point> xy_vertices, double[] x,
			double[] y) {
		int k = 0;
		for (Point v : xy_vertices) {
			x[k] = v.x;
			y[k] = v.y;
			k++;
		}
		// close the polygon by adding the first point again at the last
		// position
		x[k] = xy_vertices.get(0).x;
		y[k] = xy_vertices.get(0).y;
	}

//	public int getFloorMeters() {
//		return floor_meters_ground;
//	}

//	public boolean isFloorReferenceGround() {
//		return floor_reference_is_ground_active;
//	}
	
	public Double getFloorReferenceGround() {
		if (floor_reference_is_ground_active)
			return floor_meters_ground;
		else
			return null;
	}

	public Double getFloorReferenceSeaLevel() {
		if (floor_reference_is_seaLevel_active)
			return floor_meters_seaLevel;
		else
			return null;
	}
	
	public double getFloorReferenceGroundOrSeaLevel() {
		if (floor_reference_is_ground_active)
			return floor_meters_ground;
		else 
			return floor_meters_seaLevel;
	}
	
	public double getCeilingReferenceGroundOrSeaLevel() {
		if (ceiling_reference_is_ground_active)
			return ceiling_meters_ground;
		else 
			return ceiling_meters_seaLevel;
	}
	
	String countryIso2;
	
	public void setCountry(String iso2) {
		countryIso2 = iso2;
		if (type==AirspaceTypes.ClassB && "US".equalsIgnoreCase(countryIso2)){
			floor_meters_ground=0;
			floor_meters_seaLevel=0;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Airspace airspace = (Airspace) o;

		if (Double.compare(airspace.floor_meters_ground, floor_meters_ground) != 0) return false;
		if (floor_reference_is_ground_active != airspace.floor_reference_is_ground_active) return false;
		if (Double.compare(airspace.floor_meters_seaLevel, floor_meters_seaLevel) != 0) return false;
		if (floor_reference_is_seaLevel_active != airspace.floor_reference_is_seaLevel_active) return false;
		if (ceiling_reference_is_ground_active != airspace.ceiling_reference_is_ground_active) return false;
		if (Double.compare(airspace.ceiling_meters_ground, ceiling_meters_ground) != 0) return false;
		if (ceiling_reference_is_seaLevel_active != airspace.ceiling_reference_is_seaLevel_active) return false;
		if (Double.compare(airspace.ceiling_meters_seaLevel, ceiling_meters_seaLevel) != 0) return false;
		if (id != null ? !id.equals(airspace.id) : airspace.id != null) return false;
		if (name != null ? !name.equals(airspace.name) : airspace.name != null) return false;
		if (type != airspace.type) return false;
		return vertices != null ? vertices.equals(airspace.vertices) : airspace.vertices == null;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = id != null ? id.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		temp = Double.doubleToLongBits(floor_meters_ground);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (floor_reference_is_ground_active ? 1 : 0);
		temp = Double.doubleToLongBits(floor_meters_seaLevel);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (floor_reference_is_seaLevel_active ? 1 : 0);
		result = 31 * result + (ceiling_reference_is_ground_active ? 1 : 0);
		temp = Double.doubleToLongBits(ceiling_meters_ground);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (ceiling_reference_is_seaLevel_active ? 1 : 0);
		temp = Double.doubleToLongBits(ceiling_meters_seaLevel);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (vertices != null ? vertices.hashCode() : 0);
		return result;
	}
}
