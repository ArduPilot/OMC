/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.flightplan.computation.FlightplanVertex;
import eu.mavinci.helper.optimizer.tsp.CalculateCostsByExplicit;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Vector;

public class TspCost implements CalculateCostsByExplicit {

    double[][] edgeCost;
    Angle[][] edgeYawDirectionRad;
    int[][] fromToVertex;
    FlightplanVertex[] vert;
    int vecLength;

    private TspCost() {}

    public static final double Z_PUNISHING_SCALE = 2;
    public static final double ROLL_COST_DEG_TO_METER = 10 / 180;
    public static final double PITCH_COST_DEG_TO_METER = 10 / 180;
    public static final double YAW_COST_DEG_TO_METER = 100 / 180;

    public static TspCost forOneCloud(
            MMesh mesh,
            Vector<FlightplanVertex> points,
            FlightplanVertex before,
            FlightplanVertex after,
            double safetyDistance) {
        TspCost cost = new TspCost();
        cost.vecLength = points.size();
        int max = points.size() + 2;
        cost.edgeCost = new double[max][max];
        cost.edgeYawDirectionRad = new Angle[max][max];
        cost.vert = new FlightplanVertex[max];
        for (int i = 0; i != max - 2; i++) {
            cost.vert[i] = points.get(i);
        }

        cost.vert[max - 2] = before;
        cost.vert[max - 1] = after;

        for (int i = 0; i < max; i++) {
            for (int k = i + 1; k < max; k++) {
                boolean hit = false;
                for (MTriangle triangle : mesh.triangles) {
                    MTriangle.DistanceResult res =
                        triangle.getDistanceToLinesegment(
                            cost.vert[i].getWayPoint(), cost.vert[k].getWayPoint(), safetyDistance);
                    if (res != null && res.distance < safetyDistance) {
                        hit = true;
                        cost.edgeCost[k][i] = cost.edgeCost[i][k] = Double.POSITIVE_INFINITY;
                        break;
                    }
                }

                if (hit) continue;
                Vec4 dCenter = cost.vert[k].getCenterPoint().subtract3(cost.vert[i].getCenterPoint());
                Angle yaw =
                    Angle.fromXY(
                        dCenter.y, dCenter.x); // x and y swapped by intention to get coorect geographic northing angles

                cost.edgeYawDirectionRad[k][i] = yaw;
                cost.edgeYawDirectionRad[i][k] = yaw.add(Angle.POS180);
                Vec4 d = cost.vert[k].getWayPoint().subtract3(cost.vert[i].getWayPoint());

                d = new Vec4(d.x, d.y, d.z * Z_PUNISHING_SCALE); // Z distances are more expensive
                cost.edgeCost[k][i] = cost.edgeCost[i][k] = d.getLength3();
            }
        }

        return cost;
    }

    public static TspCost forMetaClouds(
            MMesh mesh,
            Vector<Vector<FlightplanVertex>> pointsClouds,
            FlightplanVertex before,
            FlightplanVertex after,
            double safetyDistance) {
        TspCost cost = new TspCost();
        cost.vecLength = pointsClouds.size();
        int max = pointsClouds.size() + 2;
        cost.edgeCost = new double[max][max];
        cost.edgeYawDirectionRad = null;
        cost.fromToVertex = new int[max][max];
        cost.vert = null;

        FlightplanVertex[][] vecClouds = new FlightplanVertex[max][];
        for (int i = 0; i != max - 2; i++) {
            Vector<FlightplanVertex> points = pointsClouds.get(i);
            int subMax = points.size();
            vecClouds[i] = new FlightplanVertex[subMax];
            for (int k = 0; k != subMax; k++) {
                vecClouds[i][k] = points.get(k);
            }
        }

        vecClouds[max - 2] = new FlightplanVertex[] {before};
        vecClouds[max - 1] = new FlightplanVertex[] {after};

        for (int i = 0; i != max; i++) {
            FlightplanVertex[] pointsFrom = vecClouds[i];
            for (int k = 0; k < i; k++) {
                double bestCost = Double.POSITIVE_INFINITY;
                int bestFrom = -1;
                int bestTo = -1;
                FlightplanVertex[] pointsTo = vecClouds[k];
                for (int xFrom = 0; xFrom != pointsFrom.length; xFrom++) {
                    for (int xTo = 0; xTo != pointsTo.length; xTo++) {
                        boolean hit = false;
                        for (MTriangle triangle : mesh.triangles) {
                            MTriangle.DistanceResult res =
                                triangle.getDistanceToLinesegment(
                                    pointsTo[xFrom].getWayPoint(), pointsTo[xTo].getWayPoint(), safetyDistance);
                            if (res != null && res.distance < safetyDistance) {
                                hit = true;
                                break;
                            }
                        }

                        if (hit) continue;

                        Vec4 d = pointsTo[xTo].getCenterPoint().subtract3(pointsFrom[xFrom].getCenterPoint());
                        d = new Vec4(d.x, d.y, d.z * Z_PUNISHING_SCALE); // Z distances are more expensive
                        double c = d.getLengthSquared3();
                        if (c < bestCost) {
                            bestCost = c;
                            bestFrom = xFrom;
                            bestTo = xTo;
                        }
                    }
                }

                cost.edgeCost[k][i] = cost.edgeCost[i][k] = Math.sqrt(bestCost);
                cost.fromToVertex[k][i] = bestFrom;
                cost.fromToVertex[i][k] = bestTo;
            }
        }

        return cost;
    }

    public int calculateIdxFrom(int idxFrom, int idxTo) {
        return fromToVertex[idxTo][idxFrom];
    }

    public int calculateIdxTo(int idxFrom, int idxTo) {
        return fromToVertex[idxFrom][idxTo];
    }

    public double calculateCostsByExplicit(Object solution) {
        if (solution instanceof TSPsolution) {
            TSPsolution tmp = (TSPsolution)solution;
            //			if (tmp.costFunc!=null) return tmp.costs;

            double totalCost = 0;
            int idxLast = vecLength; // simulate path to before point
            Orientation lastOrientation = null;
            Vec4 lastV = null;
            for (int i = 0; i < vecLength; i++) {
                int idxThis = tmp.idx[i];
                //				int idxNext = i <vecLength-1?  tmp.idx[i+1] : vecLength+1; //to be able to compute also angle to
                // after point
                totalCost += edgeCost[idxLast][idxThis];
                Vec4 thisV = vert[idxThis].getWayPoint();
                if (totalCost == Double.POSITIVE_INFINITY) return totalCost;
                if (edgeYawDirectionRad != null) {
                    Angle centerYawCourse = edgeYawDirectionRad[idxLast][idxThis];
                    Orientation thisOrientation =
                        vert[idxThis].getRollPitchYawInArbitratyFrame(
                            lastV != null ? thisV.subtract3(lastV) : Vec4.UNIT_X, centerYawCourse, lastOrientation);
                    if (lastOrientation != null) {
                        totalCost += ROLL_COST_DEG_TO_METER * Math.abs(lastOrientation.getRollDiffRad(thisOrientation));
                        totalCost +=
                            PITCH_COST_DEG_TO_METER * Math.abs(lastOrientation.getPitchDiffRad(thisOrientation));
                        totalCost += YAW_COST_DEG_TO_METER * Math.abs(lastOrientation.getYawDiffRad(thisOrientation));
                    }

                    lastOrientation = thisOrientation;
                }

                lastV = thisV;
                idxLast = idxThis;
            }

            totalCost += edgeCost[idxLast][vecLength + 1]; // distance to after point
            //			tmp.costFunc=this;
            //			tmp.costs=totalCost;
            return totalCost;
        }

        return 0;
    }
}
