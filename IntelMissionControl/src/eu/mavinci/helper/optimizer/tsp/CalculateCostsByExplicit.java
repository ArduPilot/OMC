/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.helper.optimizer.tsp;

/**
 * Aufgabe (a).4.b Berechnet die Kosten für eine Zulässige Lösung - aus einer expliziten Lösung
 *
 * @author Marco Moeller
 */
public interface CalculateCostsByExplicit {
    double calculateCostsByExplicit(Object solution);
}
