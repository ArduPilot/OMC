/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.three;

public class EuclDistFP { // implements TspPath.DistanceFunction<FlightplanVertex> {
    //    double[][] costs;
    //    Vec4[][] directions;
    //    int flightSize;
    //    double directionChangeRadToMeterPanelty;
    //    double collisionPaneltyCost;
    //
    //    public EuclDistFP(
    //            double[][] costs,
    //            Vec4[][] directions,
    //            int flightSize,
    //            double directionChangeRadToMeterPanelty,
    //            double collisionPaneltyCost) {
    //        this.costs = costs;
    //        this.directions = directions;
    //        this.flightSize = flightSize;
    //        this.directionChangeRadToMeterPanelty = directionChangeRadToMeterPanelty;
    //        this.collisionPaneltyCost = collisionPaneltyCost;
    //    }
    //
    //    @Override
    //    public double distance(List<FlightplanVertex> list) {
    //        FlightplanVertex last = null;
    //        double distance = 0;
    //        Vec4 lastDirection = null;
    //        for (FlightplanVertex next : list) {
    //            Vec4 nextDirection;
    //            if (last != null) {
    //                distance += costs[last.idx][next.idx];
    //                nextDirection = directions[last.idx][next.idx];
    //            } else {
    //                distance += costs[next.idx][flightSize]; // from takeoff
    //                nextDirection = directions[next.idx][flightSize];
    //            }
    //
    //            if (lastDirection != null) {
    //                distance += MathHelper.acosFast(lastDirection.dot3(nextDirection)) *
    // directionChangeRadToMeterPanelty;
    //            }
    //
    //            last = next;
    //            lastDirection = nextDirection;
    //        }
    //
    //        if (lastDirection != null) {
    //            distance += costs[last.idx][flightSize]; // to landing
    //            Vec4 nextDirection = directions[last.idx][flightSize];
    //            //					distance += Math.acos(MathHelper.intoRange(lastDirection.dot3(nextDirection),-1,1)); //TODO
    // find a
    //            // good scale translation from radians to meters
    //            distance +=
    //                MathHelper.acosFast(lastDirection.dot3(nextDirection)); // TODO find a good scale translation to
    // meters
    //        }
    //
    //        return distance;
    //    }
    //
    //    @Override
    //    public boolean isAcceptable(List<FlightplanVertex> path) {
    //        FlightplanVertex last = null;
    //        for (FlightplanVertex next : path) {
    //            if (last != null) {
    //                if (costs[last.idx][next.idx] >= collisionPaneltyCost) return false;
    //            } else {
    //                // from takeoff
    //                if (costs[next.idx][flightSize] >= collisionPaneltyCost) return false;
    //            }
    //
    //            last = next;
    //        }
    //
    //        return true;
    //    }

};
