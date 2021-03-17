/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.geo;

import eu.mavinci.airspace.Airspace;
import eu.mavinci.airspace.Point;
import eu.mavinci.core.helper.MinMaxPair;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import eu.mavinci.core.helper.MinMaxPair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Country extends GeoRestriction {

    public String name;
    public String iso2;
    public final ArrayList<ArrayList<LatLon>> borders = new ArrayList<ArrayList<LatLon>>();

    static final Pattern regCsv = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");;

    static final Pattern regCoordinates = Pattern.compile("<coordinates>(.+?)</coordinates>");

    public double dataQuality = 10000; // 15000;
    public double SAFETY_MARGIN = (int)(dataQuality * 1.5); // in meter

    public Country(String iso2, String name) {
        this.iso2 = iso2;
        this.name = name;
    }

    private Country() {}

    public static Country fromInternal(String mavinciStringDefinition) {
        String[] parts = mavinciStringDefinition.split("\\|");
        // System.out.println(mavinciStringDefinition);
        Country c = new Country();
        c.iso2 = parts[0];
        c.name = parts[1];

        // System.out.println(c.iso2 + parts.length);
        try {
            for (int i = 2; i < parts.length; i++) {

                // System.out.println(i+ " "+parts[i]);
                ArrayList<LatLon> border = new ArrayList<LatLon>();
                c.borders.add(border);
                String[] subParts = parts[i].split(" ");
                for (String pointStr : subParts) {
                    // System.out.println(pointStr);
                    String[] partsLatLon = pointStr.split(",");
                    border.add(
                        LatLon.fromDegrees(Double.parseDouble(partsLatLon[1]), Double.parseDouble(partsLatLon[0])));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return c;
    }

    public String toInternal() {
        StringBuilder sb = new StringBuilder();
        sb.append(iso2);
        sb.append("|");
        sb.append(name);

        for (ArrayList<LatLon> border : borders) {
            sb.append("|");
            boolean first = true;
            for (LatLon latLon : border) {
                if (!first) {
                    sb.append(" ");
                }

                first = false;
                sb.append(latLon.longitude.degrees);
                sb.append(",");
                sb.append(latLon.latitude.degrees);
            }
        }

        return sb.toString();
    }

    public static Country fromCSV(String csvStringDefinition) {
        String[] parts = regCsv.split(csvStringDefinition);
        // System.out.println(parts.length+":"+ Arrays.asList(parts));
        String borderStr = parts[0];
        Country c = new Country();
        c.name = parts[6].trim();
        if (c.name.startsWith("\"")) {
            c.name = c.name.substring(1, c.name.length() - 1); // remove beginning and ending quote
            System.out.println("shrink name:" + c.name);
        }

        c.iso2 = parts[3].toUpperCase().trim();
        // System.out.println(borderStr);
        Matcher matcher = regCoordinates.matcher(borderStr);
        while (matcher.find()) {
            ArrayList<LatLon> border = new ArrayList<LatLon>();
            c.borders.add(border);

            borderStr = matcher.group(1);
            // System.out.println(matcher);
            String[] pointsStr = borderStr.split(" ");
            for (String pointStr : pointsStr) {
                parts = pointStr.split(",");
                border.add(LatLon.fromDegrees(Double.parseDouble(parts[1]), Double.parseDouble(parts[0])));
            }
        }
        // System.out.println(name + " " + iso2 + " "+border);
        return c;
    }

    public void addBorderFromKMLCoordinates(String borderStr) {
        ArrayList<LatLon> border = new ArrayList<LatLon>();
        borders.add(border);

        // System.out.println(matcher);
        String[] pointsStr = borderStr.split(" ");
        for (String pointStr : pointsStr) {
            String[] parts = pointStr.split(",");
            border.add(LatLon.fromDegrees(Double.parseDouble(parts[1]), Double.parseDouble(parts[0])));
        }
    }

    @Override
    public String toString() {
        return iso2
            + ": "
            + name
            + " isEU:"
            + isEU
            + " restricted:"
            + isRestricted
            + " readioReg:"
            + radioRegulation
            + " ("
            + borders.size()
            + ")";
    }

    Sector s = null;

    public Sector getSector() {
        if (s == null) {
            MinMaxPair latP = new MinMaxPair();
            MinMaxPair lonP = new MinMaxPair();
            for (ArrayList<LatLon> border : borders) {
                for (LatLon latLon : border) {
                    latP.update(latLon.latitude.degrees);
                    lonP.update(latLon.longitude.degrees);
                }
            }

            s = Sector.fromDegrees(latP.min, latP.max, lonP.min, lonP.max);
        }

        return s;
    }

    Sector sRough = null;

    public Sector getSafetySector() {
        if (sRough == null) {
            sRough = getSector();
            // todo
            double cos = Math.min(s.getMaxLatitude().cos(), s.getMaxLatitude().cos());
            if (cos < 0) {
                cos = 0.0001;
            }

            double addRadLat = SAFETY_MARGIN / Earth.WGS84_POLAR_RADIUS;
            double addRadLon = SAFETY_MARGIN / Earth.WGS84_POLAR_RADIUS / cos;
            MinMaxPair latP = new MinMaxPair();
            MinMaxPair lonP = new MinMaxPair();
            for (ArrayList<LatLon> border : borders) {
                for (LatLon latLon : border) {
                    latP.update(latLon.latitude.radians - addRadLat);
                    latP.update(latLon.latitude.radians + addRadLat);
                    lonP.update(latLon.longitude.radians - addRadLon);
                    lonP.update(latLon.longitude.radians + addRadLon);
                }
            }

            sRough = Sector.fromRadians(latP.min, latP.max, lonP.min, lonP.max);
        }

        return sRough;
    }

    private List<Point> getXYVertices(ArrayList<LatLon> border, double ref_lat_radians, double ref_lon_radians) {
        List<LatLon> pol_vertices = border;
        List<Point> xy_vertices = new LinkedList<Point>();
        for (LatLon p : pol_vertices) {
            double angular_dist_lat = p.getLatitude().getRadians() - ref_lat_radians;
            double angular_dist_lon = p.getLongitude().getRadians() - ref_lon_radians;
            double x_meters = angular_dist_lon * 180 / Math.PI * 60 * Airspace.NM_TO_METER * Math.cos(ref_lat_radians);
            double y_meters = angular_dist_lat * 180 / Math.PI * 60 * Airspace.NM_TO_METER;
            xy_vertices.add(new Point(x_meters, y_meters));
        }

        return xy_vertices;
    }

    private void getCoordinateArray(List<Point> xy_vertices, double[] x, double[] y) {
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

    public boolean contains(LatLon latLon) {
        if (!getSector().contains(latLon)) {
            return false;
        }

        double ref_lat_radians = latLon.getLatitude().getRadians();
        double ref_lon_radians = latLon.getLongitude().getRadians();
        for (ArrayList<LatLon> border : borders) {
            if (contains(ref_lat_radians, ref_lon_radians, border)) {
                return true;
            }
        }

        return false;
    }

    public boolean contains(double ref_lat_radians, double ref_lon_radians, ArrayList<LatLon> border) {
        List<Point> xy_vertices = getXYVertices(border, ref_lat_radians, ref_lon_radians);
        double x[] = new double[xy_vertices.size() + 1];
        double y[] = new double[xy_vertices.size() + 1];
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
            for (int i = 0; i < xy_vertices.size(); i++) {
                // Does the ray hit a vertex, then select another ray
                if (c * (y[i]) == s * (x[i])) {
                    special_case = true;
                }
            }

            if (special_case) {
                // if we hit a vertex, randomly select another ray direction
                // in first quadrant until we not hit a vertex
                // System.out.println("Hit vertex");
                c = Math.random();
            }
        } while (special_case);

        int hits = 0;
        for (int i = 0; i < xy_vertices.size(); i++) {
            double d = c * (y[i] - y[i + 1]) - s * (x[i] - x[i + 1]);
            double a = x[i] * (y[i] - y[i + 1]) - y[i] * (x[i] - x[i + 1]);
            double m = c * y[i] - s * x[i];
            // System.out.println("Checking ("+(int)c+","+(int)s+") ray bewteen ("+(int)x[i]+","+(int)y[i]+") and
            // ("+(int)x[i+1]+","+(int)y[i+1]+")");
            if (((d > 0) && (a > 0) && (m > 0) && (m < d)) || ((d < 0) && (a < 0) && (m < 0) && (m > d))) {
                hits++;
            }
        }

        if (hits % 2 == 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean withinSafetyDistance(LatLon latLon) {
        return distance(latLon) <= SAFETY_MARGIN;
    }

    public boolean withinSafetyDistance(LatLon latLon, double safetDistance) {
        return distance(latLon) <= safetDistance;
    }

    public boolean withinSafetyDistance(Sector sector) {
        return distance(sector) <= SAFETY_MARGIN;
    }

    /**
     * this is only an estimation of the distance of a sector to this country, since only corners and center will be
     * checked!
     *
     * @param sector
     * @return
     */
    public double distance(Sector sector) {
        MinMaxPair minMax = new MinMaxPair();
        minMax.update(distance(sector.getCentroid()));
        for (LatLon corner : sector.getCorners()) {
            minMax.update(distance(corner));
        }

        return minMax.min;
    }

    public double distance(LatLon latLon) {
        // if (!getSafetySector().contains(latLon)) return SAFETY_MARGIN+1;
        int sign = contains(latLon) ? -1 : 1;
        MinMaxPair p = new MinMaxPair();
        double ref_lat_radians = latLon.getLatitude().getRadians();
        double ref_lon_radians = latLon.getLongitude().getRadians();
        for (ArrayList<LatLon> border : borders) {
            p.update(distance(ref_lat_radians, ref_lon_radians, border));
        }

        return sign * p.min;
    }

    private double distance(double ref_lat_radians, double ref_lon_radians, ArrayList<LatLon> border) {
        List<Point> xy_vertices = getXYVertices(border, ref_lat_radians, ref_lon_radians);
        double x[] = new double[xy_vertices.size() + 1];
        double y[] = new double[xy_vertices.size() + 1];
        getCoordinateArray(xy_vertices, x, y);
        MinMaxPair minMax = new MinMaxPair();
        // ref point is in center (0,0)
        for (int i = 0; i < xy_vertices.size(); i++) {
            double dx = (x[i + 1] - x[i]);
            double dy = (y[i + 1] - y[i]);
            double len2 = (dx * dx + dy * dy);
            if (len2 == 0) {
                continue;
            }

            double t = (-x[i] * dx - y[i] * dy) / len2;
            if (t <= 0) {
                minMax.update(Math.sqrt(x[i] * x[i] + y[i] * y[i]));
            } else if (t >= 1) {
                minMax.update(Math.sqrt(x[i] * x[i] + y[i] * y[i]));
            } else {
                double xp = x[i] + t * dx;
                double yp = y[i] + t * dy;
                minMax.update(Math.sqrt(xp * xp + yp * yp));
            }
        }

        return minMax.min;
    }

    public boolean intersect(Sector sector) {
        if (!getSector().intersects(sector)) {
            return false;
        }

        if (contains(sector.getCentroid())) {
            return true;
        }

        for (LatLon corner : sector.getCorners()) {
            if (contains(corner)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Country clone() {
        Country c = new Country();
        for (ArrayList<LatLon> border : borders) {
            c.borders.add(border);
        }

        c.isEU = isEU;
        c.isRestricted = isRestricted;
        c.iso2 = iso2;
        c.name = name;
        c.radioRegulation = radioRegulation;
        c.dataQuality = dataQuality;
        // System.out.println("c:" +c+ isSimilarRegulated(c) );
        return c;
    }
}
