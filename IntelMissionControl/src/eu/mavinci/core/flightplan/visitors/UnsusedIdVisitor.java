/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.IReentryPoint;
import eu.mavinci.core.flightplan.ReentryPointID;
import eu.mavinci.core.helper.MinMaxPair;

public class UnsusedIdVisitor extends AFlightplanVisitor {

    public MinMaxPair minMaxId = new MinMaxPair();
    public MinMaxPair minMaxLineNo = new MinMaxPair();

    IFlightplanStatement toExclude = null;

    public UnsusedIdVisitor(IFlightplanStatement toExclude) {
        this.toExclude = toExclude;
    }

    boolean inSideExclude = false;

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (inSideExclude) {
            return false;
        }

        if (fpObj == toExclude) {
            inSideExclude = true;
            return false;
        }

        if (fpObj instanceof IReentryPoint) {
            IReentryPoint rp = (IReentryPoint)fpObj;
            int id = rp.getId();
            minMaxId.update(id);
            if (ReentryPointID.isAutoPlanned(id) && ReentryPointID.isOnMainLine(id)) {
                minMaxLineNo.update(ReentryPointID.getLineNumberPure(id));
            }
        }

        return false;
    }

    @Override
    public boolean visitExit(IFlightplanRelatedObject fpObj) {
        if (toExclude == fpObj) {
            inSideExclude = false;
        }

        return super.visitExit(fpObj);
    }
}
