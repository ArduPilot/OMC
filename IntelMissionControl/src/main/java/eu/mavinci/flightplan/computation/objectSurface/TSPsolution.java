/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import eu.mavinci.flightplan.computation.FlightplanVertex;
import eu.mavinci.helper.optimizer.tsp.PrototypeSolution;
import java.util.Collections;
import java.util.Vector;

public class TSPsolution implements PrototypeSolution {
    public int[] idx;
    Vector<Vector<FlightplanVertex>> pointClouds;
    Vector<FlightplanVertex> points;
    //	TspCost costFunc;
    //	double costs;

    private TSPsolution(int count) {
        Vector<Integer> vec = new Vector<Integer>(count);

        for (int i = 0; i != count; i++) {
            vec.add(i);
        }

        Collections.shuffle(vec);

        idx = new int[count];
        for (int i = 0; i != count; i++) {
            idx[i] = vec.get(i);
        }
    }

    protected TSPsolution(int[] idx) {
        this.idx = idx;
    }

    @Override
    public TSPsolution clone() {
        TSPsolution s = new TSPsolution(idx.clone());
        s.pointClouds = pointClouds;
        s.points = points;
        return s;
    }

    public static TSPsolution forOneCloud(Vector<FlightplanVertex> points) {
        TSPsolution sol = new TSPsolution(points.size());
        sol.points = points;
        return sol;
    }

    public static TSPsolution forManyClouds(Vector<Vector<FlightplanVertex>> pointClouds) {
        TSPsolution sol = new TSPsolution(pointClouds.size());
        sol.pointClouds = pointClouds;
        return sol;
    }

    public Vector<FlightplanVertex> getPoints() {
        Vector<FlightplanVertex> pointsNew = new Vector<>(points.size());
        for (int i = 0; i != points.size(); i++) {
            pointsNew.add(points.get(idx[i]));
        }

        return pointsNew;
    }

    public Vector<Vector<FlightplanVertex>> getPointClouds() {
        Vector<Vector<FlightplanVertex>> pointCloudsNew = new Vector<>(pointClouds.size());
        for (int i = 0; i != pointClouds.size(); i++) {
            pointCloudsNew.add(pointClouds.get(idx[i]));
        }

        return pointCloudsNew;
    }

    @Override
    public Object OtherSolution() {
        return null;
    }

    @Override
    public Object ModificationInformation() {
        return null;
    }

    @Override
    public Object ThisSolution() {
        return this;
    }
}
