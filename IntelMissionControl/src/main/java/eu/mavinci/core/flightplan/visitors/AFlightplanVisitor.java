/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

public abstract class AFlightplanVisitor implements IFlightplanVisitor {

    boolean skipIgnoredPaths = false;

    public void setSkipIgnoredPaths(boolean skipIgnoredPaths) {
        this.skipIgnoredPaths = skipIgnoredPaths;
    }

    public void startVisit(IFlightplanRelatedObject fpObj) {
        preVisit();
        if (fpObj instanceof IFlightplanContainer) {
            IFlightplanContainer cont = (IFlightplanContainer)fpObj;
            cont.applyFpVisitor(this, skipIgnoredPaths);
        } else {
            visit(fpObj);
        }

        postVisit();
    }

    public void startVisitFlat(IFlightplanRelatedObject fpObj) {
        preVisit();
        if (fpObj instanceof IFlightplanContainer) {
            IFlightplanContainer cont = (IFlightplanContainer)fpObj;
            cont.applyFpVisitorFlat(this, skipIgnoredPaths);
        } else {
            visit(fpObj);
            visitExit(fpObj);
        }

        postVisit();
    }

    public void startVisitUpToRoot(IFlightplanRelatedObject fpObj) {
        preVisit();
        while (fpObj != null) {
            if (!visit(fpObj)) {
                break;
            }

            fpObj = fpObj.getParent();
        }

        postVisit();
    }

    public void preVisit() {}

    public void postVisit() {}

    public boolean visitExit(IFlightplanRelatedObject fpObj) {
        return false;
    }

}
