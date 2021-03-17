/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import eu.mavinci.helper.optimizer.tsp.ConstructNeighborhood;
import eu.mavinci.helper.optimizer.tsp.PrototypeIterator;

public class TSPneighborhood implements ConstructNeighborhood {

    @Override
    public PrototypeIterator constructNeighborhood(Object solution) {
        return new TSPneighborhoodIterator(solution);
    }

}
