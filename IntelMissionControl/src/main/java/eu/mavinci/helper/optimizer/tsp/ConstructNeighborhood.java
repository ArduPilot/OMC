/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.helper.optimizer.tsp;

/**
 * (a).3 Liefert für zulässige Lösung einen Iterator zurück, der über alle nachbarschaften iteriert
 *
 * @author Marco Moeller
 */
public interface ConstructNeighborhood {
    PrototypeIterator constructNeighborhood(Object solution);
}
