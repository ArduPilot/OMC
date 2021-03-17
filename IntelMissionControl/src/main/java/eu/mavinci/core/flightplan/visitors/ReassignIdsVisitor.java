/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IReentryPoint;
import java.util.TreeSet;

public class ReassignIdsVisitor extends AFlightplanVisitor {

    private TreeSet<Integer> ids = new TreeSet<Integer>();
    private boolean changed;
    private boolean nextFreeIDValid;
    private int nextFreeID;
    private boolean force;

    public ReassignIdsVisitor(boolean force) {
        this.force = force;
        if (force) {
            nextFreeID = 1;
            nextFreeIDValid = true;
        }
    }

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj instanceof CFlightplan) {
            CFlightplan fp = (CFlightplan)fpObj;
            fp.setMute(true);
        }

        if (fpObj instanceof IReentryPoint) {
            IReentryPoint rp = (IReentryPoint)fpObj;
            if (force) {
                if (rp.getId() != nextFreeID) {
                    rp.setId(nextFreeID);
                    nextFreeID++;
                    changed = true;
                }
            } else {
                int id = rp.getId();
                if (ids.contains(id)) {
                    // rp.reassignIDs();//dont do this call, it would become mega expensive since each time the entire
                    // Mission has to be traversed to find a free id
                    if (!nextFreeIDValid) {
                        nextFreeID = fpObj.getFlightplan().getUnusedId();
                        nextFreeIDValid = true;
                    }

                    rp.setId(nextFreeID);
                    ids.add(nextFreeID);
                    nextFreeID++;
                    changed = true;
                } else {
                    ids.add(id);
                }
            }
        }

        return false;
    }

    @Override
    public boolean visitExit(IFlightplanRelatedObject fpObj) {
        if (fpObj instanceof CFlightplan) {
            CFlightplan fp = (CFlightplan)fpObj;
            fp.setSilentUnmute();
            if (changed) {
                fp.flightplanStatementChanged(fp);
            }
        }

        return super.visitExit(fpObj);
    }

    public boolean wasValid() {
        return !changed;
    }
}
