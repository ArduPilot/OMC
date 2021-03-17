/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IReentryPoint;

public class MaxIdVisitor extends AFlightplanVisitor {

    public int max = 0;

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj instanceof IReentryPoint) {
            IReentryPoint rp = (IReentryPoint)fpObj;
            max = Math.max(max, rp.getId());
        }

        return false;
    }
}
