/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation;

import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.helper.Pair;
import gov.nasa.worldwind.geom.Vec4;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.helper.Pair;

import java.util.LinkedList;
import java.util.Vector;

public class ContourLineGenerator {
    // needs WWFactory.init();
    public static Vector<Vector<Vec4>> getAllContours(double[][] zdata, double dz, MinMaxPair zRange) {
        if ((zdata.length == 0) || (zdata[0].length == 0)) {
            return new Vector<Vector<Vec4>>(0);
        }

        // Find z value for each contour (=zc) line to be calculated:
        int stepCount = (int)Math.ceil(zRange.size() / dz);
        if (stepCount > 0) {
            dz = zRange.size() / stepCount;
        }

        Vector<Double> zcVec = new Vector<Double>();
        for (int i = 0; i <= stepCount; i++) {
            zcVec.add(zRange.min + (i * dz));
        }

        // "Marching squares" algorithm
        // for all dz's, get grid with thresholds and find contours
        Vector<Vector<Vec4>> posVecAll = new Vector<Vector<Vec4>>(zcVec.size());
        for (int c = 0; c < zcVec.size(); c++) {
            LinkedList<Vector<Vec4>> segments = new LinkedList<Vector<Vec4>>();

            boolean[][] thresholdGrid = new boolean[zdata.length][zdata[0].length];
            for (int i = 0; i < zdata.length; i++) {
                for (int j = 0; j < zdata[i].length; j++) {
                    thresholdGrid[i][j] = (zdata[i][j] >= zcVec.get(c));
                }
            }

            // there might be multiple (closed) contour lines per zc ==> track
            // all grid points in "finished" grid
            // also invalidate unused points
            boolean[][] finished = new boolean[zdata.length][zdata[0].length];
            for (int i = 0; i < zdata.length; i++) {
                for (int j = 0; j < zdata[i].length; j++) {
                    if (zdata[i][j] == Double.NEGATIVE_INFINITY) {
                        thresholdGrid[i][j] = false;
                    }

                    int id = getID(thresholdGrid, i, j);
                    finished[i][j] = isInvalid(zdata, i, j) || (id == -1) || (id == 0) || (id == 15);
                }
            }

            // choose next starting point:
            for (int i0 = 0; i0 < finished.length; i0++) {
                for (int j0 = 0; j0 < finished[i0].length; j0++) {
                    if (finished[i0][j0]) {
                        continue;
                    }

                    // calculate contours with starting grid indices i0, j0
                    Vector<Pair<Integer, Integer>> contourIndices = getContourIndices(thresholdGrid, i0, j0);
                    Vector<Vec4> posVec = new Vector<Vec4>(contourIndices.size());

                    for (int q = 0; q < contourIndices.size(); q++) {
                        int i = contourIndices.get(q).first;
                        int j = contourIndices.get(q).second;
                        if ((i >= 0) && (j >= 0)) {
                            finished[i][j] = true;
                        }

                        if (isInvalid(zdata, i, j)) {
                            if (posVec.size() >= 1) {
                                segments.add(posVec);
                            }

                            posVec = new Vector<Vec4>(contourIndices.size() - q);
                        } else {
                            posVec.add(getInterpolatedContourPosition(zdata, i, j, zcVec.get(c)));
                        }
                    }

                    if (posVec.size() > 0) {
                        segments.add(posVec);
                    }

                    if (segments.size() >= 1) {
                        // Add all contours on current z
                        posVecAll.addAll(segments);
                    }
                }
            }
        }

        return posVecAll;
    }

    private static Vec4 getVec4Point(double[][] zdata, int i, int j) {
        // Average z of all 4 points around edge
        double z;
        if (i + 1 < zdata.length) {
            if (j + 1 < zdata[0].length) {
                z = (zdata[i][j] + zdata[i + 1][j] + zdata[i][j + 1] + zdata[i + 1][j + 1]) / 4.0;
            } else {
                z = (zdata[i][j] + zdata[i + 1][j]) / 2.0;
            }
        } else {
            if (j + 1 < zdata[0].length) {
                z = (zdata[i][j] + zdata[i][j + 1]) / 2.0;
            } else {
                z = zdata[i][j];
            }
        }

        return new Vec4(i + 0.5, j + 0.5, z);
    }

    private static boolean isInvalid(double[][] zdata, int i, int j) {
        if ((i < 0) || (j < 0) || (i >= zdata.length - 1) || (j >= zdata[0].length - 1)) {
            return true;
        }

        if ((zdata[i][j] == Double.NEGATIVE_INFINITY)
                || (zdata[i + 1][j] == Double.NEGATIVE_INFINITY)
                || (zdata[i][j + 1] == Double.NEGATIVE_INFINITY)
                || (zdata[i + 1][j + 1] == Double.NEGATIVE_INFINITY)) {
            return true;
        }

        return false;
    }

    private static Vec4 getInterpolatedContourPosition(double zdata[][], int i, int j, double zc) {
        // Bilinear interpolation would be better, but expensive... using interpolation between center and edge with min
        // or max z
        double zs[] = new double[] {zdata[i][j], zdata[i + 1][j], zdata[i][j + 1], zdata[i + 1][j + 1]};

        Vec4 center = getVec4Point(zdata, i, j);
        int s2 = 0;
        if (center.z > zc) {
            // find min:
            double zsmin = Double.MAX_VALUE;
            for (int s = 0; s < 4; s++) {
                if (zs[s] < zsmin) {
                    zsmin = zs[s];
                    s2 = s;
                }
            }
        } else if (center.z < zc) {
            // find max:
            int smax = 0;
            double zsmax = Double.MIN_VALUE;
            for (int s = 0; s < 4; s++) {
                if (zs[s] > zsmax) {
                    zsmax = zs[s];
                    s2 = s;
                }
            }
        } else {
            return center;
        }

        Vec4 pos2;
        switch (s2) {
        case 0:
            pos2 = new Vec4(i, j, zs[s2]);
            break;
        case 1:
            pos2 = new Vec4(i + 1, j, zs[s2]);
            break;
        case 2:
            pos2 = new Vec4(i, j + 1, zs[s2]);
            break;
        default:
            pos2 = new Vec4(i + 1, j + 1, zs[s2]);
            break;
        }

        double z1 = center.z;
        double z2 = zs[s2];

        double interpolationFactor = (zc - z1) / (z2 - z1);
        // if ((interpolationFactor < 0) || (interpolationFactor > 1)) {
        // return null;
        // }

        // linearly interpolate
        return center.add3((pos2.subtract3(center)).multiply3(interpolationFactor));
    }

    /*
     * number each grid point with ID depending on its neighbors
     */
    private static int getID(boolean[][] thresholdGrid, int i, int j) {
        int sum = 0;
        if (isAboveThreshold(thresholdGrid, i, j)) {
            sum += 1;
        }

        if (isAboveThreshold(thresholdGrid, i + 1, j)) {
            sum += 2;
        }

        if (isAboveThreshold(thresholdGrid, i, j + 1)) {
            sum += 4;
        }

        if (isAboveThreshold(thresholdGrid, i + 1, j + 1)) {
            sum += 8;
        }

        return sum;
    }

    private static boolean isAboveThreshold(boolean[][] thresholdGrid, int i, int j) {
        if (i < 0 || i >= thresholdGrid.length || j < 0 || j >= thresholdGrid[0].length) {
            return false;
        } else {
            return thresholdGrid[i][j];
        }
    }

    private static Vector<Pair<Integer, Integer>> getContourIndices(boolean[][] thresholdGrid, int i0, int j0) {
        int i = i0;
        int j = j0;

        boolean stop = false;
        int lastID = -1;

        Vector<Pair<Integer, Integer>> contourIndices = new Vector<Pair<Integer, Integer>>();

        do {
            contourIndices.add(new Pair<Integer, Integer>(i, j));

            // N: j--
            // S: j++
            // W: i--
            // E: i++

            int id = getID(thresholdGrid, i, j);

            switch (id) {
            case 1:
                j--; // N
                break;
            case 2:
                i++; // E
                break;
            case 3:
                i++; // E
                break;
            case 4:
                i--; // W
                break;
            case 5:
                j--; // N
                break;
            case 6:
                if ((lastID == 1) || (lastID == 5) || (lastID == 13)) { // N
                    i--; // W
                    id = 4;
                } else {
                    i++; // E
                    id = 2;
                }

                break;
            case 7:
                i++; // E
                break;
            case 8:
                j++; // S
                break;
            case 9:
                if ((lastID == 2) || (lastID == 3) || (lastID == 7)) { // E
                    j--; // N
                    id = 1;
                } else {
                    j++; // S
                    id = 8;
                }

                break;
            case 10:
                j++; // S
                break;
            case 11:
                j++; // S
                break;
            case 12:
                i--; // W
                break;
            case 13:
                j--; // N
                break;
            case 14:
                i--; // W
                break;
            default:
                throw new IllegalStateException("Contour algorithm error");
            }

            lastID = id;
        } while ((i != i0) || (j != j0));

        // close loop
        contourIndices.add(contourIndices.firstElement());

        return contourIndices;
    }
}
