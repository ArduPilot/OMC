/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import eu.mavinci.flightplan.Point;

public abstract class CPicAreaCorners extends AFlightplanContainer implements IMuteable {

    CPicArea area;

    protected CPicAreaCorners() {}

    protected CPicAreaCorners(CPicArea parent) {
        area = parent;
    }

    public CPicArea getParent() {
        return area;
    }

    public void setParent(IFlightplanContainer container) {
        area = (CPicArea)container;
    }

    @Override
    public IFlightplanStatement removeFromFlightplanContainer(int i) {
        return super.removeFromFlightplanContainer(i);
    }

    @Override
    public boolean isAddableToFlightplanContainer(Class<? extends IFlightplanStatement> cls) {
        if (sizeOfFlightplanContainer() >= getMaxSize()) {
            return false;
            // if (CStartProcedure.class.isAssignableFrom(cls)) return false;
        }

        /*
         * return CPoint.class.equals(cls); doesn't work, because class Point is not equal to the CPoint, but there is no need to replace
         * the old line because COrigin as far as CLanding Point are assignable from CAPoint but not CPoint
         */
        return Point.class.isAssignableFrom(cls);
    }

    public void flightplanStatementAdded(IFlightplanRelatedObject statement) {
        // System.out.println("flightplanStatementAdded.corners " + statement + " mute:"+mute);
        if (mute) {
            return;
        }

        if (area != null) {
            area.flightplanStatementAdded(statement);
        }
    }

    public void flightplanStatementChanged(IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        if (area != null) {
            area.flightplanStatementChanged(statement);
        }
    }

    public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        if (area != null) {
            area.flightplanStatementRemove(i, statement);
        }
    }

    public void flightplanStatementStructureChanged(IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        if (area != null) {
            area.flightplanStatementStructureChanged(statement);
        }
    }

    public CFlightplan getFlightplan() {
        return area != null ? area.getFlightplan() : null;
    }

    public void reassignIDs() {}

    public Point getPointFromFlightplanContainer(int i) {
        return (Point)super.getFromFlightplanContainer(i);
    }

    protected boolean mute;

    public void setMute(boolean mute) {
        if (mute == this.mute) {
            return;
        }
        // Debug.printStackTrace(mute);

        this.mute = mute;
        if (!mute) {
            flightplanStatementStructureChanged(this);
        }
    }

    public boolean isMute() {
        return mute;
    }

    public void setSilentUnmute() {
        // Debug.printStackTrace(false);
        this.mute = false;
    }

}
