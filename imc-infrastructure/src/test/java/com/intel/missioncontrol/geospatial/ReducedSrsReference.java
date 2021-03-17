/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geospatial;

import com.intel.missioncontrol.geometry.Vec4;
import com.intel.missioncontrol.geospatial.Convert;
import com.intel.missioncontrol.geospatial.Position;
import java.util.ArrayList;
import java.util.List;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

public class ReducedSrsReference {

    /*private final String epsg;
    private final SpatialReference srs = new SpatialReference();
    private final String wkt;

    public ReducedSrsReference(String epsg) {
        this.epsg = epsg;
        this.srs.SetFromUserInput(epsg);
        this.wkt = srs.ExportToPrettyWkt();
    }

    public Vec4 fromWgs84(Position p) throws Exception {
        return callGDALtransform(
            true, epsg, Convert.radiansToDegrees(p.longitude), Convert.radiansToDegrees(p.latitude), p.elevation, wkt);
    }

    public List<Vec4> fromWgs84(List<? extends LatLon> ps) throws Exception {
        ArrayList<Vec4> result = new ArrayList<>();
        int len = ps.size();
        double[] x = new double[len];
        double[] y = new double[len];
        double[] z = new double[len];
        int i = 0;
        for (LatLon p : ps) {
            x[i] = p.longitude.degrees;
            y[i] = p.latitude.degrees;
            if (p instanceof Position) {
                Position p2 = (Position)p;
                z[i] = p2.elevation;
            } else {
                z[i] = Double.NaN;
            }

            result.add(fromWgs84(new Position(p, z[i])));
            i++;
        }
        // Vec4[] result = callGDALtransform(true, id, x, y, z, isPrivate() ? wkt : null);

        return result;
    }

    public Position toWgs84(Vec4 v) throws Exception {
        Vec4 vec = new Vec4(v.x, v.y, Double.isNaN(v.z) ? 0 : v.z);

        Vec4 result = callGDALtransform(false, epsg, v.x, v.y, v.z, wkt);
        return Position.fromDegrees(result.y, result.x, result.z);
    }

    synchronized Vec4 callGDALtransform(
            boolean trueIntoFalseFrom, String id, double x0, double x1, double x2, String ppszInput) throws Exception {
        Vec4[] resArr;
        resArr =
            callGDALtransform(
                trueIntoFalseFrom, id, new double[] {x0}, new double[] {x1}, new double[] {x2}, ppszInput);
        return resArr[0];
    }

    private CoordinateTransformation transformationTo;
    private CoordinateTransformation transformationFrom;

    synchronized Vec4[] callGDALtransform(
            final boolean trueIntoFalseFrom,
            final String id,
            final double[] x0,
            final double[] x1,
            final double[] x2,
            final String ppszInput)
            throws Exception {
        SpatialReference srsWgs84 = new SpatialReference();
        LocalElevationModel elev = null;

        CoordinateTransformation trafo;
        if (trueIntoFalseFrom) {
            if (transformationTo == null) transformationTo = new CoordinateTransformation(srsWgs84, srs);
            trafo = transformationTo;
        } else {
            if (transformationFrom == null) transformationFrom = new CoordinateTransformation(srs, srsWgs84);
            trafo = transformationFrom;
        }

        int N = x0.length;
        boolean flat = Double.isNaN(x2[0]);
        double[][] mat = new double[N][flat ? 2 : 3];
        for (int i = 0; i < N; i++) {
            //			if (elev!=null && trueIntoFalseFrom){
            //				x2[i]-=elev.getElevation(Angle.fromDegrees(x1[i]), Angle.fromDegrees(x0[i]));
            //				System.out.println("pre:"+ Angle.fromDegrees(x1[i])+" "+ Angle.fromDegrees(x0[i])+ "  -> "
            // +elev.getElevation(Angle.fromDegrees(x1[i]), Angle.fromDegrees(x0[i])) );
            //			}
            mat[i][0] = x0[i];
            mat[i][1] = x1[i];
            if (!flat) mat[i][2] = x2[i];
            //			System.out.println("in: " + x0[i] + " " + x1[i] + " " + x2[i]);
        }

        trafo.TransformPoints(mat);

        Vec4[] target = new Vec4[N];

        for (int i = 0; i < N; i++) {
            if (!flat && elev != null) {
                if (trueIntoFalseFrom) {
                    Angle lat = Angle.fromDegrees(x1[i]);
                    Angle lon = Angle.fromDegrees(x0[i]);
                    if (elev.contains(lat, lon)) {
                        mat[i][2] = x2[i] - elev.getElevation(lat, lon);
                        //						System.out.println("pre:"+ Angle.fromDegrees(x1[i])+" "+ Angle.fromDegrees(x0[i])+ "  ->
                        // " +elev.getElevation(Angle.fromDegrees(x1[i]), Angle.fromDegrees(x0[i])) );
                    }
                } else {
                    Angle lat = Angle.fromDegrees(mat[i][1]);
                    Angle lon = Angle.fromDegrees(mat[i][0]);
                    if (elev.contains(lat, lon)) {
                        mat[i][2] = x2[i] + elev.getElevation(lat, lon);
                        //						System.out.println("post:"+ Angle.fromDegrees(x1[i])+" "+ Angle.fromDegrees(x0[i])+ "
                        // -> " +elev.getElevation(Angle.fromDegrees(mat[i][1]), Angle.fromDegrees(mat[i][0])) );
                    }
                }
            }

            if (flat) {
                //				System.out.println("out: " + mat[i][0] + " " + mat[i][1]);
                target[i] = new Vec4(mat[i][0], mat[i][1], Double.NaN);
            } else {
                //				System.out.println("out: " + mat[i][0] + " " + mat[i][1] + " " + mat[i][2]);
                target[i] = new Vec4(mat[i][0], mat[i][1], mat[i][2]);
            }
        }

        return target;
    }*/
}
