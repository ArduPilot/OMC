/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IReentryPoint;
import eu.mavinci.core.flightplan.ReentryPointID;

public class ExtractByIdVisitor extends AFlightplanVisitor {

    public ExtractByIdVisitor(int id) {
        this.id = id;
    }

    public int id;

    public IReentryPoint rpBefore = null;
    public IFlightplanPositionReferenced posRefBefore = null;
    public IReentryPoint rp = null;

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj instanceof IReentryPoint) {
            IReentryPoint rp = (IReentryPoint)fpObj;
            if (ReentryPointID.equalIDexceptCell(id, rp.getId())) {
                this.rp = rp;
                return true;
            }

            rpBefore = rp;
        }

        if (fpObj instanceof IFlightplanPositionReferenced) {
            posRefBefore = (IFlightplanPositionReferenced)fpObj;
        }

        return false;
    }
}
