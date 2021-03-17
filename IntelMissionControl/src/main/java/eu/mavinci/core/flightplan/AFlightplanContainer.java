/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import eu.mavinci.core.flightplan.visitors.IFlightplanVisitor;
import eu.mavinci.core.helper.VectorNonEqual;
import java.util.Iterator;

public abstract class AFlightplanContainer
        implements IFlightplanContainer, IFlightplanStatementChangeListener, IFlightplanStatement {

    protected VectorNonEqual<IFlightplanStatement> elements = new VectorNonEqual<IFlightplanStatement>();

    /**
     * <b>Skips checks in {@link #isAddableToFlightplanContainer}, <i>makes {@link
     * eu.mavinci.flightplan.PicArea#recalculate PicArea.recalculate()} up to 200x faster!</i></b>
     *
     * <p>The error checking provided by these methods usually causes the entire FlightPlan object tree to be traversed,
     * each time a FlightPlanStatement is added via {@link #addToFlightplanContainer}. In the case of recalculating a
     * flight plan, this can cause the object tree to be traversed as many times as there are FlightPlanStatements
     *
     * <p>This runtime checking is very expensive way to uncover logic errors. Therefore these checks should be treated
     * like assertions: Only enabled in certain debug builds. Anyways, these errors aren't being handled meaningfully:
     * no one seems seems to be specifically catching {@link FlightplanContainerWrongAddingException}.
     */
    static final boolean SKIP_IS_ADDABLE_ASSERTION_CHECKS = true;

    public boolean isAddableToFlightplanContainer(IFlightplanStatement statement) {
        if (SKIP_IS_ADDABLE_ASSERTION_CHECKS) return true;

        return isAddableToFlightplanContainer(statement.getClass());
    }

    public boolean isAddableToFlightplanContainer(Class<? extends IFlightplanStatement> cls) {
        if (SKIP_IS_ADDABLE_ASSERTION_CHECKS) return true;

        if (sizeOfFlightplanContainer() >= getMaxSize()) {
            return false;
        }

        if (CPicAreaCorners.class.isAssignableFrom(cls)) {
            return false;
        }

        if (CEvent.class.isAssignableFrom(cls)) {
            return false;
        }

        if (CEventList.class.isAssignableFrom(cls)) {
            return false;
        }

        return true;
    }

    public void addToFlightplanContainer(IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        addToFlightplanContainer(sizeOfFlightplanContainer(), statement);
    }

    public int getMaxSize() {
        return MAX_MEMBER;
    }

    @Override
    public void addAfterToFlightplanContainer(IFlightplanStatement addAfterThisOne, IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        int pos = elements.indexOf(addAfterThisOne);
        if (pos < 0) {
            throw new IndexOutOfBoundsException("no such object found:" + addAfterThisOne);
        }

        addToFlightplanContainer(pos + 1, statement);
    }

    @Override
    public void addBeforeToFlightplanContainer(IFlightplanStatement addBeforeThisOne, IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        int pos = elements.indexOf(addBeforeThisOne);
        if (pos < 0) {
            throw new IndexOutOfBoundsException("no such object found:" + addBeforeThisOne);
        }

        addToFlightplanContainer(pos , statement);
    }

    public void addToFlightplanContainer(int index, IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        if (sizeOfFlightplanContainer() >= getMaxSize()) {
            throw new FlightplanContainerFullException();
        }

        if (!isAddableToFlightplanContainer(statement)) {
            throw new FlightplanContainerWrongAddingException(statement.getClass(), this.toString());
        }
        // System.out.println("adding " + statement + " into " + this + " at idx" + index);
        elements.add(index, statement);
        updateParent(statement);
        flightplanStatementAdded(statement);
    }

    protected void updateParent(IFlightplanStatement statement) {
        statement.setParent(this);
    }

    public IFlightplanStatement getFromFlightplanContainer(int i) {
        return elements.get(i);
    }

    public IFlightplanStatement removeFromFlightplanContainer(IFlightplanStatement statement) {
        int i = elements.indexOf(statement);
        return removeFromFlightplanContainer(i);
    }

    public IFlightplanStatement removeFromFlightplanContainer(int i) {
        if (i < 0) {
            return null;
        }

        IFlightplanStatement statement = elements.get(i);
        elements.remove(i);
        // statement.setParent(null);
        flightplanStatementRemove(i, statement);
        return statement;
    }

    public int sizeOfFlightplanContainer() {
        return elements.size();
    }

    public int getNumberOfPicAreas() {
        int numberOfPicAreas = 0;

        for (IFlightplanStatement element : elements) {
            if (element instanceof CPicArea) {
                numberOfPicAreas++;
            }
        }

        return numberOfPicAreas;
    }

    @SuppressWarnings("unchecked")
    public <T extends IFlightplanStatement> T getFirstElement(Class<T> elementClass) {
        if ((elementClass == null) || (elements == null) || (elements.isEmpty())) {
            return null;
        }

        for (IFlightplanStatement element : elements) {
            if (elementClass.isInstance(element)) {
                return (T)element;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public IFlightplanStatement getLastElement() {
        if ((elements == null) || (elements.isEmpty())) {
            return null;
        }

        return elements.lastElement();
    }

    public boolean isEmpty() {
        return ((elements == null) || (elements.isEmpty()));
    }

    public Iterator<IFlightplanStatement> iterator() {
        return elements.iterator();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof AFlightplanContainer) {
            AFlightplanContainer fp = (AFlightplanContainer)o;
            return elements.equals(fp.elements);
        }

        return false;
    }

    // protected TreeMap<Integer,Double> progressMapDistance = null;
    //
    // public double getLengthInMeter(double airplaneSpeedInMperSec) {
    // DistanceVisitor vis = new DistanceVisitor(airplaneSpeedInMperSec);
    // vis.startVisitFlat(this);
    // progressMapDistance = vis.progressMapDistance;
    // return vis.distance;
    // }
    //
    // public TreeMap<Integer, Double> getProgressMapDistance() {
    // return progressMapDistance;
    // }

    public boolean applyFpVisitor(IFlightplanVisitor visitor, boolean skipIgnoredPaths) {
        if (skipIgnoredPaths && this instanceof IFlightplanIgnoreable) {
            IFlightplanIgnoreable ig = (IFlightplanIgnoreable)this;
            if (ig.isIgnore()) {
                return false;
            }
        }

        if (visitor.visit(this)) {
            return true;
        }

        for (IFlightplanStatement fpObj : this) {
            if (fpObj instanceof IFlightplanContainer) {
                IFlightplanContainer container = (IFlightplanContainer)fpObj;
                if (container.applyFpVisitor(visitor, skipIgnoredPaths)) {
                    return true;
                }
            } else {
                if (skipIgnoredPaths && fpObj instanceof IFlightplanIgnoreable) {
                    IFlightplanIgnoreable ig = (IFlightplanIgnoreable)fpObj;
                    if (ig.isIgnore()) {
                        continue;
                    }
                }

                if (visitor.visit(fpObj)) {
                    return true;
                }
            }
        }

        return applyVisitorPost(visitor, skipIgnoredPaths);
    }

    public boolean applyVisitorPost(IFlightplanVisitor visitor, boolean skipIgnoredPaths) {
        if (applyVisitorAdditionalMembers(visitor)) {
            return true;
        }

        return visitor.visitExit(this);
    }

    public boolean applyFpVisitorFlat(IFlightplanVisitor visitor, boolean skipIgnoredPaths) {
        if (skipIgnoredPaths && this instanceof IFlightplanIgnoreable) {
            IFlightplanIgnoreable ig = (IFlightplanIgnoreable)this;
            if (ig.isIgnore()) {
                return false;
            }
        }

        for (IFlightplanStatement fpObj : this) {
            if (skipIgnoredPaths && fpObj instanceof IFlightplanIgnoreable) {
                IFlightplanIgnoreable ig = (IFlightplanIgnoreable)fpObj;
                if (ig.isIgnore()) {
                    continue;
                }
            }

            if (visitor.visit(fpObj)) {
                return true;
            }
        }

        return applyVisitorFlatPost(visitor);
    }

    public boolean applyVisitorFlatPost(IFlightplanVisitor visitor) {
        return applyVisitorAdditionalMembers(visitor);
    }

    public boolean applyVisitorAdditionalMembers(IFlightplanVisitor visitor) {
        return false;
    }

}
