/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.routing.tsp;

import java.util.ArrayList;
import java.util.List;

public class SimulatedAnnealing extends TspSolver {

    TspPath<?> travel;

    TspPath<?> bestTravel;

    double startingTemperature = 0;
    int numberOfIterations = 0;
    double coolingRate = 0;

    public SimulatedAnnealing(double startingTemperature, int numberOfIterations, double coolingRate, TspPath<?> path) {
        this.startingTemperature = startingTemperature;
        this.numberOfIterations = numberOfIterations;
        this.coolingRate = coolingRate;
        this.travel = path;
        this.bestTravel = travel;
    }

    public static void main(String[] args) {
        class ExNode {
            double x;
            double y;

            ExNode(double x, double y) {
                this.x = x;
                this.y = y;
            }
        };

        class EuclDist implements TspPath.DistanceFunction<ExNode> {

            @Override
            public double distance(List<ExNode> list) {
                ExNode last = null;
                double distance = 0;
                for (ExNode next : list) {
                    if (last != null) {
                        distance += Math.abs(next.x - last.x) + Math.abs(next.y - last.y);
                    }

                    last = next;
                }

                return distance;
            }

            @Override
            public boolean isAcceptable(List<ExNode> list) {
                return true;
            }

        };

        TspPath<ExNode> path = new TspPath<>();
        path.distanceFunction = new EuclDist();
        path.nodes = new ArrayList<ExNode>();
        //        path.nodes.add(new ExNode(0, 0));
        //        path.nodes.add(new ExNode(1, 2));
        //        path.nodes.add(new ExNode(1, 0));
        //        path.nodes.add(new ExNode(1, 1));
        //        path.nodes.add(new ExNode(10, 1));
        //        path.nodes.add(new ExNode(1, 13));
        //        path.nodes.add(new ExNode(2, 1));

        path.nodes.add(new ExNode(4.295226824469862, 40.33229032554664));
        path.nodes.add(new ExNode(3.848670282866805, 12.337397981784306));
        path.nodes.add(new ExNode(3.802289584651587, 34.57679867721163));
        path.nodes.add(new ExNode(3.534369170665741E-7, 3.312947228550911E-6));

        TspSolver testSolver = (TspSolver)new SimulatedAnnealing(10, 1000000, 0.9999, path);

        //TODO: get rid of the damn background task manager
        testSolver.solve();

        for (var node : testSolver.getPath().nodes) {
            System.out.println(((ExNode)node).x + "," +((ExNode)node).y );
        }
    }

    @Override
    public double solve() {
        Double totalbestDistance = Double.POSITIVE_INFINITY;
        int max = 10;
        //        for (int i = 0; i < max || bestTravel == null || !bestTravel.isAcceptable(); i++) {
        for (int i = 0; i < max || bestTravel == null; i++) {
//            if (task != null) {
//                task.updateProgress(13 + i / (double)max, ObjectFlightplanAlg.maxProgress);
//                task.updateMessage("simluated annealing i:" + i);
//                if (task.isCancelled()) {
//                    return totalbestDistance;
//                }
//            }

            System.out.println("\nSA cooling attemp no:" + (i + 1) + " of " + max);
            double localbest = simann(totalbestDistance);
            if (localbest < totalbestDistance) {
                totalbestDistance = localbest;
            }

            System.out.println("totalbest:" + totalbestDistance + " isAccepted:" + bestTravel.isAcceptable());
        }
        // travel = bestSolution;
        return totalbestDistance;
    }

    public double simann(Double totalbestDistance) {
        System.out.println(
            "Starting SA with temperature: "
                + startingTemperature
                + ", # of iterations: "
                + numberOfIterations
                + " and colling rate: "
                + coolingRate);
        double t = startingTemperature;

        travel.generateInitialTravel();
        double lastDistance = travel.getDistance();
        // double totalbestDistance = travel.getDistance();
        double currentDistance = lastDistance;
        System.out.println("Initial distance of travel: " + lastDistance);
        TspPath<?> bestSolution = new TspPath<>(travel);
        TspPath<?> currentSolution = travel;

        for (int i = 0; i < numberOfIterations; i++) {
            if ( i % 100 == 0) {
                //if (task.isCancelled()) {
                  //  return totalbestDistance;
                //}
            }

            boolean accepted = true;
            if (t > 0.001) {
                if (i > 0) {
                    currentSolution.swapNodesRandomly();
                }

                currentDistance = currentSolution.getDistance();
                //                if(bestDistance == currentDistance) {
                //                	continue;
                //                }

                //                for(var node : currentSolution.nodes) {
                //                    System.out.print(node.);
                //                }

                if (currentDistance < totalbestDistance) {
                    totalbestDistance = currentDistance;
                    bestTravel = new TspPath<>(currentSolution);
                }

                if (currentDistance < lastDistance) {
                    lastDistance = currentDistance;
                    bestSolution = new TspPath<>(currentSolution);
                } else if (Math.random() < Math.exp(-Math.abs((lastDistance - currentDistance) / t))) {
                    lastDistance = currentDistance;
                    // System.out.println("l"+Math.exp((bestDistance - currentDistance) / t));
                    //        System.out.println("x");

                } else {
                    currentSolution.revertSwap();
                    accepted = false;
                }

                t *= coolingRate;
            } else {
                break;
            }

            if (i % 1000000 == 0) {
                // if(accepted)
                System.out.println(
                    "Accepted?:"
                        + accepted
                        + "\tIteration #"
                        + i
                        + "\t Dist:"
                        + currentDistance
                        + "\t bestDist:"
                        + lastDistance
                        + "\t T:"
                        + t
                        + "\tthingy:"
                        + Math.exp(-Math.abs((lastDistance - currentDistance) / t)));
            }
        }

        travel = bestSolution;
        System.out.println("my best:" + bestSolution.getDistance());
        return totalbestDistance;
    }

    @Override
    public TspSolver setPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TspPath<?> getPath() {
        if(this.bestTravel!=null) {
            return this.bestTravel;
        } else {
            bestTravel = new TspPath<>(this.travel);
            return bestTravel;
        }
    }

    @Override
    public double solve(TspPath<?> path) {
        // TODO Auto-generated method stub
        return 0;
    }

}
