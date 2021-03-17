/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public abstract class AReentryContainer extends AFlightplanContainer implements IReentryPoint, IFlightplanContainer {

    protected int id = ReentryPoint.INVALID_REENTRYPOINT;

    protected IFlightplanContainer parent = null;

    protected AReentryContainer(IFlightplanContainer parent) {
        // Expect.notNull(parent, "parent");
        this.parent = parent;
        reassignIDs();
    }

    protected AReentryContainer(int id, IFlightplanContainer parent) {
        // Expect.notNull(parent, "parent");
        this.parent = parent;
        this.id = id;
    }

    public void flightplanStatementChanged(IFlightplanRelatedObject statement) {
        if (parent != null) {
            parent.flightplanStatementChanged(statement);
        }
    }

    public void flightplanStatementStructureChanged(IFlightplanRelatedObject statement) {
        if (parent != null) {
            parent.flightplanStatementStructureChanged(statement);
        }
    }

    public void flightplanStatementAdded(IFlightplanRelatedObject statement) {
        if (parent != null) {
            parent.flightplanStatementAdded(statement);
        }
    }

    public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement) {
        if (parent != null) {
            parent.flightplanStatementRemove(i, statement);
        }
    }

    public int getId() {
        return id;
    }

    public IFlightplanContainer getParent() {
        return parent;
    }

    public CFlightplan getFlightplan() {
        return parent == null ? null : parent.getFlightplan();
    }

    public void setId(int id) {
        if (this.id != id) {
            this.id = id;
            flightplanStatementChanged(this);
        }
    }

    public void setParent(IFlightplanContainer container) {
        if (parent != container) {
            parent = container;
            if (id == ReentryPoint.INVALID_REENTRYPOINT) {
                reassignIDs();
            }
        } else {
            parent = container;
        }
    }

    public void reassignIDs() {
        if (parent != null) {
            id = -1;
            // System.out.println(parent.getFlightplan().elements);
            // System.out.println(parent.getFlightplan().landingpoint.id);
            setId(parent.getFlightplan().getUnusedId());
        }
    }

    @Override
    public boolean isAddableToFlightplanContainer(Class<? extends IFlightplanStatement> cls) {
        if (!super.isAddableToFlightplanContainer(cls)) {
            return false;
        }

        if (CStartProcedure.class.isAssignableFrom(cls)) {
            return false;
        }

        if (CPreApproach.class.isAssignableFrom(cls)) {
            return false;
        }

        return true;
    }

}
