/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.coordinatetransform;

import gov.nasa.worldwind.geom.Vec4;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.util.Vector;

public class BursaWolfeParameters {

    /*
     * Finds the optimum set of 7 Bursa-Wolfe-Parameters to match both sets of ECEF (XYZ) coordinates
     */
    @SuppressWarnings("checkstyle:localvariablename")
    public static double[] calculateFromSamples(Vector<EcefCoordinate> orgXyz, Vector<EcefCoordinate> targetXyz)
            throws IllegalArgumentException {
        if (orgXyz.size() != targetXyz.size()) {
            throw new IllegalArgumentException("Input vectors need to have the same length");
        }

        int n = orgXyz.size();

        // 1. Shift to centroid ("center of mass"):
        EcefCoordinate orgCentroid = getCentroid(orgXyz);
        EcefCoordinate targetCentroid = getCentroid(targetXyz);

        Vector<EcefCoordinate> orgSampleCentered =
            EcefCoordinate.shiftVector(orgXyz, new EcefCoordinate(orgCentroid.multiply3(-1)));
        Vector<EcefCoordinate> targetSampleCentered =
            EcefCoordinate.shiftVector(targetXyz, new EcefCoordinate(targetCentroid.multiply3(-1)));

        // 2. Get measure for extent of point cloud, and scale target:
        double orgExtent = getExtent(orgSampleCentered);
        double targetExtent = getExtent(targetSampleCentered);

        double mu = orgExtent / targetExtent;

        Vector<EcefCoordinate> orgSampleCenteredScaled = orgSampleCentered;
        Vector<EcefCoordinate> targetSampleCenteredScaled = EcefCoordinate.scaleVector(targetSampleCentered, mu);

        // Build Nx3 matrices from data sets
        Array2DRowRealMatrix orgMat = new Array2DRowRealMatrix(n, 3);
        for (int i = 0; i < n; i++) {
            orgMat.setEntry(i, 0, orgSampleCenteredScaled.get(i).x);
            orgMat.setEntry(i, 1, orgSampleCenteredScaled.get(i).y);
            orgMat.setEntry(i, 2, orgSampleCenteredScaled.get(i).z);
        }

        Array2DRowRealMatrix targetMat = new Array2DRowRealMatrix(n, 3);
        for (int i = 0; i < n; i++) {
            targetMat.setEntry(i, 0, targetSampleCenteredScaled.get(i).x);
            targetMat.setEntry(i, 1, targetSampleCenteredScaled.get(i).y);
            targetMat.setEntry(i, 2, targetSampleCenteredScaled.get(i).z);
        }

        // 3. Get rotation matrix from Singular Value Decomposition (see https://en.wikipedia.org/wiki/Kabsch_algorithm
        // )
        // H = orgDat3_XYZ' * targetDat3_XYZ;
        RealMatrix h = orgMat.transpose().multiply(targetMat);

        // [U,S,V] = svd(H); //find U, S,V such that U*S*V' = H, with S diagonal and U, V unitary
        SingularValueDecomposition svd = new SingularValueDecomposition(h);

        // R = V*U';
        RealMatrix r = svd.getV().multiply(svd.getU().transpose());

        // if this is a performance issue, just calculaute 3x3 matrix determinant manually...
        if (new LUDecomposition(r).getDeterminant() < 0) {
            double[][] reflectionMatrix = {{1, 0, 0}, {0, 1, 0}, {0, 0, -1}};
            r = svd.getV().multiply(new Array2DRowRealMatrix(reflectionMatrix).multiply(svd.getU().transpose()));
        }

        // we need to map this rotation matrix to the helmert rotation + scaling:
        // mu * [1, rz, -ry; -rz, 1, rx; ry, -rx, 1]

        // Get rx, ry, rz, use average because of numerics:
        double rx_rad = (r.getEntry(1, 2) - r.getEntry(2, 1)) / 2.0;
        double ry_rad = (-r.getEntry(0, 2) + r.getEntry(2, 0)) / 2.0;
        double rz_rad = (r.getEntry(0, 1) - r.getEntry(1, 0)) / 2.0;

        // Get translation [dx, dy, dz]:
        // dvec_m = orgCentroid - (mu * R' * targetCentroid')';
        double[][] a = {{orgCentroid.x}, {orgCentroid.y}, {orgCentroid.z}};
        double[][] b = {{targetCentroid.x}, {targetCentroid.y}, {targetCentroid.z}};
        RealMatrix dvec =
            new Array2DRowRealMatrix(a).add((r.transpose().multiply(new Array2DRowRealMatrix(b))).scalarMultiply(-mu));

        // Convert data:
        double dx_m = dvec.getEntry(0, 0);
        double dy_m = dvec.getEntry(1, 0);
        double dz_m = dvec.getEntry(2, 0);
        double rx_arcsec = rx_rad * 648000 / Math.PI;
        double ry_arcsec = ry_rad * 648000 / Math.PI;
        double rz_arcsec = rz_rad * 648000 / Math.PI;
        double m_ppm = (mu - 1.0) * 1e6;

        double[] bursaWolfeParams = {dx_m, dy_m, dz_m, rx_arcsec, ry_arcsec, rz_arcsec, m_ppm};
        return bursaWolfeParams;
    }

    /*
     * Finds the centroid (mean position) of the set of ECEF (XYZ) coordinates
     */
    private static EcefCoordinate getCentroid(Vector<EcefCoordinate> vec) {
        return new EcefCoordinate(Vec4.computeAveragePoint(vec));
    }

    /*
     * Finds the RMS distance of the set of ECEF (XYZ) coordinates (corresponds to point cloud size)
     */
    private static double getExtent(Vector<EcefCoordinate> vec) {
        // calculate rms of norms
        double sum = 0;
        for (int i = 0; i < vec.size(); i++) {
            sum += vec.get(i).getLengthSquared3();
        }

        return Math.sqrt(sum / vec.size());
    }
}
