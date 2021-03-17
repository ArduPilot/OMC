/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.core.helper.MinMaxPair;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Vector;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointShiftingHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PointShiftingHelper.class);
    public static final double SCALE_IN_RADIUS_FIT_NEIGHBOURS = 1.5;
    public static final double SCALE_ANGLE_PANELTS = 0.5 / Math.PI;
    public static final double THRESHOLD = 0.7;
    public static final double VIF_THRESHOLD = 5;

    /**
     * compute the normal vector on a linear regression surface in 3d space
     *
     * @param points
     * @return
     */
    public static Vec4 getNormal(Vector<Vec4> points, Vec4 roughNormal) {
        // the following code is a very rough normal approximation, basically it ignores most of the points and just
        // uses 4 of them.
        // the mathematical was better code below has some places where he isn't working and delivers very extreme
        // almost
        // tangents, but no normals

        // Calculate normal vector from neighbors
        // Use multiple linear regression,
        // https://de.wikipedia.org/wiki/Lineare_Regression
        // Y = X b + e = (1, 1, 1, 1, ...)
        // b = (X^T X)^-1 (X^T Y)

        int N = points.size();
        if (N < 3) return Vec4.UNIT_Z;

        // Build Y vector:
        double[] Yvals = new double[N];
        for (int i = 0; i < N; i++) {
            Yvals[i] = 1.0;
        }

        Array2DRowRealMatrix Y = new Array2DRowRealMatrix(Yvals);
        double[][] Xvals = fillMatrixX(points);

        Array2DRowRealMatrix X = new Array2DRowRealMatrix(Xvals);

        RealMatrix Xt = X.transpose();

        // now it happens quite rare!
        if (checkMulticollinearityCriteria(X)) {
            // depending on rotation of sampling grid, uses direction with beigger span in baseline
            if (Math.abs(points.get(4).y - points.get(5).y) < Math.abs(points.get(2).y - points.get(7).y)) {
                double y = (points.get(2).z - points.get(7).z) / (points.get(2).y - points.get(7).y);
                double x = (points.get(4).z - points.get(5).z) / (points.get(4).x - points.get(5).x);
                Vec4 norm = new Vec4(-x, -y, 1);
                return norm.normalize3();
            } else {
                double x = (points.get(2).z - points.get(7).z) / (points.get(2).x - points.get(7).x);
                double y = (points.get(4).z - points.get(5).z) / (points.get(4).y - points.get(5).y);
                Vec4 norm = new Vec4(-x, -y, 1);
                return norm.normalize3();
            }
        }

        RealMatrix B;
        try {
            B = new LUDecomposition((Xt.multiply(X))).getSolver().getInverse().multiply(Xt.multiply(Y));
        } catch (SingularMatrixException ex) {
            //			Random rnd = new Random();
            //
            //			// Quick fix for singular matrix: add some noise
            //			Vector<Vec4> newPoints = new Vector<Vec4>(N);
            //			for (int i = 0; i < N; i++) {
            //				// TODO: Might need to check order of magnitude of data point
            //				// coordinates
            //				newPoints.add(new Vec4(points.get(i).x + rnd.nextDouble() * 1e-6,
            //						points.get(i).y + rnd.nextDouble() * 1e-6, points.get(i).z + rnd.nextDouble() * 1e-6));
            //			}
            //			return getNormal(newPoints);
            ex.printStackTrace();
            return Vec4.UNIT_Z;
        }

        double[] Bvals = B.getColumn(0);

        Vec4 resVec = new Vec4(Bvals[0], Bvals[1], Bvals[2]).normalize3();

        // System.out.println("resVec: " + resVec.x + ", " + resVec.y + ", " + resVec.z);

        if (resVec.dot3(roughNormal) < 0) {
            resVec = resVec.multiply3(-1);
        }

        return resVec;
    }

    private static boolean checkMulticollinearityCriteria(Array2DRowRealMatrix X) {
        // https://en.wikipedia.org/wiki/Variance_inflation_factor
        Covariance covariance = new Covariance(X);
        RealMatrix covMatrix = covariance.getCovarianceMatrix();
        RealMatrix corrMatrix = new PearsonsCorrelation().covarianceToCorrelation(covMatrix);
        RealMatrix inv = null;
        try {
            inv = new LUDecomposition(corrMatrix).getSolver().getInverse();
        } catch (SingularMatrixException e) {
            e.printStackTrace();
        }

        return inv == null
            || inv.getEntry(0, 0) > VIF_THRESHOLD
            || inv.getEntry(1, 1) > VIF_THRESHOLD
            || inv.getEntry(2, 2) > VIF_THRESHOLD;
    }

    private static double[][] fillMatrixX(Vector<Vec4> points) {
        int n = points.size();
        // Build X vector:
        double[][] Xvals = new double[n][3];

        // Mean vals
        double xm = points.stream().mapToDouble(p -> p.x).sum() / n;
        double ym = points.stream().mapToDouble(p -> p.y).sum() / n;
        double zm = points.stream().mapToDouble(p -> p.z).sum() / n;

        // centered and normalized!
        for (int i = 0; i < n; i++) {
            Xvals[i][0] = points.get(i).x - xm;
            Xvals[i][1] = points.get(i).y - ym;
            Xvals[i][2] = points.get(i).z - zm;
            double l = Math.sqrt(Math.pow(Xvals[i][0], 2) + Math.pow(Xvals[i][1], 2) + Math.pow(Xvals[i][2], 2));
            Xvals[i][0] /= l;
            Xvals[i][1] /= l;
            Xvals[i][2] /= l;
        }

        return Xvals;
    }

    /**
     * gets a center point and some neighbours computes a linear regression surface of all neighbour points it will
     * return the oldCenter shifted to the closest point o the surface and a second poiunt shifted him by alt into the
     * normal direction of the surface
     *
     * @param oldCenter
     * @param neighbors
     * @param alt
     * @return
     */
    public static FlightplanVertex getCenterAndShiftedAlongNormal(
            Vec4 oldCenter, Vector<Vec4> neighbors, Vec4 roughNormal, double alt, Vec4 lineDirection) {
        Vec4 normal;
        if (neighbors.size() < 3) {
            LOGGER.warn("Not enough neighbor points: " + neighbors.size() + " @ " + oldCenter);
            normal = Vec4.UNIT_Z;
            // FIXME: normal is set again right after this
        }

        // Calculate normal vector from neighbors
        normal = getNormal(neighbors, roughNormal); // FIXME: Set here, when above normal = Vec4.UNIT_Z;

        // since falcon cant roll camera, only pitch... so remove side components from normal
        if (lineDirection != null && lineDirection.getLengthSquared3() != 0) {
            normal = normal.projectOnto3(lineDirection).add3(normal.projectOnto3(Vec4.UNIT_Z));
        }

        Vec4 centerPoint = Vec4.computeAveragePoint(neighbors);
        if (centerPoint == null) {
            centerPoint = oldCenter;
        }

        Vec4 centerNew =
            normal.multiply3(centerPoint.dot3(normal))
                .add3(oldCenter)
                .subtract3(normal.multiply3(oldCenter.dot3(normal)));
        FlightplanVertex vert = new FlightplanVertex(centerNew, centerNew.add3(normal.multiply3(alt)));
        vert.setNeigbourPointsOnImage(neighbors);
        return vert;
    }

    public static Vector<FlightplanVertex> getCentersAndShiftedAlongNormals(
            Iterable<FlightplanVertex> points, double searchRadius, double altitude) {
        //		double Rsq = searchRadius * searchRadius;

        Vector<FlightplanVertex> resPoints = new Vector<FlightplanVertex>();

        // For each point, find neighbors within searchRadius (3D)
        for (FlightplanVertex vertOrg : points) {
            if (vertOrg.getTriangle() != null) {
                Vec4 centerPoint = vertOrg.getCenterPoint();
                Ensure.notNull(centerPoint, "centerPoint");
                Vec4 vertFace = vertOrg.getFaceNormal(centerPoint, searchRadius);
                Ensure.notNull(vertFace, "vertFace");
                FlightplanVertex vert =
                    new FlightplanVertex(centerPoint, centerPoint.add3(vertFace.multiply3(altitude)));
                vert.setFixed(vertOrg.isFixed());
                vert.setTriangle(vertOrg.getTriangle());
                vert.setNeigbourPointsOnImage(vertOrg.getNeigbourPointsOnImage());
                resPoints.add(vert);
            } else {
                Vector<Vec4> neighbors = new Vector<Vec4>();
                for (FlightplanVertex vertOther : points) {
                    if (Math.sqrt(vertOrg.distanceAtCenterSquared(vertOther))
                                + (vertOrg.getFaceNormaleAngleToOrZero(searchRadius, vertOther)
                                    * SCALE_ANGLE_PANELTS
                                    * searchRadius)
                            <= searchRadius) {
                        neighbors.add(vertOther.getCenterPoint());
                    }
                }

                Vec4 oldCenter = vertOrg.getCenterPoint();
                Vec4 normal;
                if (neighbors.size() < 3) {
                    LOGGER.warn("Not enough neighbor points: " + neighbors.size() + " @ " + oldCenter);
                    Vec4 oldCenterPoint = vertOrg.getFaceNormal(oldCenter, searchRadius);
                    Ensure.notNull(oldCenterPoint, "oldCenterPoint");
                    normal = vertOrg.getTriangle() != null ? oldCenterPoint : Vec4.UNIT_Z;
                } else {
                    // Calculate normal vector from neighbors
                    normal =
                        getNormal(neighbors, vertOrg.getTriangle() == null ? Vec4.UNIT_Z : vertOrg.getFaceNormal());
                }

                Vec4 centerPoint = Vec4.computeAveragePoint(neighbors);
                if (centerPoint == null) // TODO FIXME REINCLUDE THIS!!
                centerPoint = oldCenter;

                Vec4 centerNew =
                    normal.multiply3(centerPoint.dot3(normal))
                        .add3(oldCenter)
                        .subtract3(normal.multiply3(oldCenter.dot3(normal)));
                FlightplanVertex vert = new FlightplanVertex(centerNew, centerNew.add3(normal.multiply3(altitude)));
                vert.setNeigbourPointsOnImage(neighbors);
                vert.setFixed(vertOrg.isFixed());
                vert.setTriangle(vertOrg.getTriangle());
                vert.setNeigbourPointsOnImage(vertOrg.getNeigbourPointsOnImage());
                resPoints.add(vert);
            }
        }

        return resPoints;
    }

    /**
     * Shifts waypoints into z-coordinate bins. Bins will be equidistant between zmin and zmax with a bin width <=
     * dzPerBinRequested.
     */
    public static Vector<Vector<FlightplanVertex>> getZbinned(
            Iterable<FlightplanVertex> unbinnedVertices, double dzPerBinRequested) {
        MinMaxPair zRange = new MinMaxPair();
        for (FlightplanVertex vert : unbinnedVertices) {
            zRange.update(vert.getWayPoint().z);
        }

        // Use smaller dzPerBin to avoid edge cases
        int Nbins = (int)Math.ceil((zRange.size()) / dzPerBinRequested);
        if (Nbins < 1) {
            Nbins = 1;
        }

        double dzPerBin = (zRange.size()) / (double)Nbins;

        // Calculate binned z values
        double[] binnedZs = new double[Nbins];
        //		System.out.println("size:" + zRange + " " + dzPerBin+ "nbins:"+Nbins);
        for (int ib = 0; ib < Nbins; ib++) {
            binnedZs[ib] = zRange.min + (ib + 0.5) * dzPerBin;
        }

        // Prepare output structure:
        Vector<Vector<FlightplanVertex>> binnedVertices = new Vector<Vector<FlightplanVertex>>(Nbins);
        for (int ib = 0; ib < Nbins; ib++) {
            binnedVertices.add(new Vector<FlightplanVertex>());
        }

        // Sort points into bins:
        for (FlightplanVertex v1 : unbinnedVertices) {
            double z1 = v1.getWayPoint().z;

            // bin number
            int ib = 0;
            if (dzPerBin > 0.0) {
                ib = (int)Math.floor((z1 - zRange.min) / dzPerBin);
            }

            if (ib < 0) {
                ib = 0;
            } else if (ib > Nbins - 1) {
                ib = Nbins - 1;
            }

            //			System.out.println("z:"+z1+" -> bin:" +ib);

            //			FlightplanVertex vNew = v1.clone();
            v1.setWayPoint(new Vec4(v1.getWayPoint().x, v1.getWayPoint().y, binnedZs[ib]));
            binnedVertices.get(ib).add(v1);
        }

        return binnedVertices;
    }

    public static final double MAX_PITCH_OVER_THE_POLE_FALCON_DEG = 20; // means falcon pitch can be +/-110°

    /**
     * @param lastOrientation cam Orientation from the last picture
     * @param lineYaw the curse from the last waypoint to this one
     * @param normal inside plane ref. system this vector should point perfectly upwards inside camera ref. system this
     *     vector should point to out of the backside of the housing
     * @return Orientation of the Drone
     */
    public static Orientation yawPitchRollFromYawAndNormal(
            Vec4 flyingDirection, Angle lineYaw, Orientation lastOrientation, Vec4 normal) {
        // TODO make max backlooking pitch configureable
        // System.out.println("flyingDirection:"+flyingDirection+" \tlineYaw;"+lineYaw + "
        // \tlastOrientation:"+lastOrientation+" \tnormal:" +normal);
        double yaw = Math.atan2(-normal.x, -normal.y);
        double len2d = Math.sqrt(normal.x * normal.x + normal.y * normal.y);
        double pitch = Math.atan2(len2d, normal.z);
        yaw = Math.toDegrees(yaw);
        pitch = Math.toDegrees(pitch);
        //		System.out.println("pitch="+pitch+" yaw:"+yaw +" normal:"+normal);
        if (lineYaw != null && lastOrientation == null) {
            lastOrientation = new Orientation(0, 0, lineYaw.degrees);
        }

        if (lastOrientation != null) {
            double diffYaw = yaw - lastOrientation.getYaw();
            while (diffYaw < -180) diffYaw += 360;
            while (diffYaw >= 180) diffYaw -= 360;
            double pitchFromPole = pitch > 90 ? 180 - pitch : pitch;
            if (Math.abs(pitchFromPole) <= MAX_PITCH_OVER_THE_POLE_FALCON_DEG && Math.abs(diffYaw) > 120) {
                yaw += 180;
                // flip roll sign if roll is not 0
                while (yaw >= 180) yaw -= 360;
                //				if ( pitch>90) System.out.println("UNDERBRIDGE");
                pitch = pitch < 90 ? -pitch : 360 - pitch;
            }
        }
        // System.out.println("new pitch= "+pitch+" yaw:"+yaw);
        return new Orientation(0, pitch, yaw);
    }

    /*
    public static double[] getCamAngleRollPitchYaw(,Vec4 direction){

    } */
}
