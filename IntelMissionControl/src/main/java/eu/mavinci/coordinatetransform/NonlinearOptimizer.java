/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.coordinatetransform;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

import java.util.Arrays;

public class NonlinearOptimizer {
    public static PointValuePair minimize(MultivariateFunction f, double[] initialValues, int maxEval) {
        // Nonlinear Simplex Optimizer (no derivatives needed)
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-6, 1e-10);

        // Get initial simplex like matlab does (fminsearch Algorithm)
        double[] steps = initialValues.clone();
        for (int i = 0; i < steps.length; i++) {
            if (initialValues[i] == 0) {
                steps[i] = 0.00025;
            } else {
                steps[i] *= 0.05;
            }
        }

        NelderMeadSimplex simplex = new NelderMeadSimplex(steps);

        PointValuePair optimum;
        try {
            optimum =
                optimizer.optimize(
                    new MaxEval(maxEval),
                    new ObjectiveFunction(f),
                    GoalType.MINIMIZE,
                    new InitialGuess(initialValues),
                    simplex);
        } catch (TooManyEvaluationsException ex) {
            optimum = simplex.getPoint(0);
        }

        return optimum;
    }

    // usage Example
    public static void main(String[] args) {
        double[] initialValues = new double[] {100, -30};
        int maxEval = 100;

        PointValuePair optimum =
            NonlinearOptimizer.minimize(
                new MultivariateFunction() {
                    @Override
                    public double value(double[] point) {
                        System.out.println("0: " + point[0] + ", 1: " + point[1]);

                        // Use vector length as value to be minimized:
                        return point[0] * point[0] + point[1] * point[1];
                    }
                },
                initialValues,
                maxEval);

        System.out.println(Arrays.toString(optimum.getPoint()) + " : " + optimum.getSecond());
    }
}
