/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.helper.optimizer.tsp;

/**
 * (a).4.a Berechnet die Kosten für eine Zulässige Lösung - aus einem Lösungsprototypen
 *
 * @author Marco Moeller
 */
public interface CalculateCostsByPrototype {
    double calculateCostsByPrototype(PrototypeSolution solutionPrototype);
}
