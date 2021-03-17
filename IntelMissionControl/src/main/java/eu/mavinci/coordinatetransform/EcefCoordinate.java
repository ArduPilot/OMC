/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.coordinatetransform;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.EllipsoidalGlobe;
import gov.nasa.worldwind.globes.Globe;

import java.util.Vector;

// This class should be replaced by gdal geocentric SpatialReference

public class EcefCoordinate extends Vec4 {
    public EcefCoordinate(Vec4 vec) {
        super(vec.x, vec.y, vec.z);
    }

    public EcefCoordinate(double x, double y, double z) {
        super(x, y, z);
    }

    public double norm() {
        return getLength3();
    }

    public static EcefCoordinate fromLongLatH(double longitude, double latitude, double height, Ellipsoid el) {
        Globe globe =
            new EllipsoidalGlobe(
                el.getSemiMajorAxis(), el.getSemiMinorAxis(), el.getEccentricity() * el.getEccentricity(), null);
        Vec4 vec = globe.computePointFromPosition(Position.fromDegrees(latitude, longitude, height));
        return new EcefCoordinate(vec.z, vec.x, vec.y);

        /*
         * double lambda = longitude * Math.PI/180.0; double phi = latitude * Math.PI/180.0; double h = height;
         * //https://en.wikipedia.org/wiki/Geographic_coordinate_conversion double eSinPhi = el.getEccentricity() * Math.sin(phi); double
         * Nphi = el.getSemiMajorAxis() / Math.sqrt(1 - eSinPhi * eSinPhi); double x = (Nphi + h) * Math.cos(phi) * Math.cos(lambda); double
         * y = (Nphi + h) * Math.cos(phi) * Math.sin(lambda); double z = (Nphi * (1 - el.getEccentricity() * el.getEccentricity()) + h) *
         * Math.sin(phi); return new EcefCoordinate(x, y, z);
         */
    }

    public static Vector<EcefCoordinate> shiftVector(Vector<EcefCoordinate> data, EcefCoordinate shift) {
        Vector<EcefCoordinate> res = new Vector<EcefCoordinate>(data.size());

        for (int i = 0; i < data.size(); i++) {
            res.addElement(new EcefCoordinate(data.get(i).add3(shift)));
        }

        return res;
    }

    public static Vector<EcefCoordinate> scaleVector(Vector<EcefCoordinate> data, double scaleFactor) {
        Vector<EcefCoordinate> res = new Vector<EcefCoordinate>(data.size());

        for (int i = 0; i < data.size(); i++) {
            res.addElement(new EcefCoordinate(data.get(i).multiply3(scaleFactor)));
        }

        return res;
    }

    public static Vector<EcefCoordinate> fromPositionVector(Vector<Position> vec, Ellipsoid el) {
        Vector<EcefCoordinate> res = new Vector<EcefCoordinate>(vec.size());

        for (int i = 0; i < vec.size(); i++) {
            res.addElement(
                fromLongLatH(vec.get(i).longitude.degrees, vec.get(i).latitude.degrees, vec.get(i).elevation, el));
        }

        return res;
    }
}
