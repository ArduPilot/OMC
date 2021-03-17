/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import eu.mavinci.core.flightplan.visitors.IFlightplanVisitor;
import eu.mavinci.core.flightplan.visitors.IFlightplanVisitor;

public interface IFlightplanContainer
        extends IFlightplanStatementChangeListener,
            IFlightplanRelatedObject,
            IFlightplanReassignIDs,
            Iterable<IFlightplanStatement> {
    public void addToFlightplanContainer(IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException;

    public void addToFlightplanContainer(int index, IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException;

    public void addAfterToFlightplanContainer(IFlightplanStatement addAfterThisOne, IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException;

    public boolean isAddableToFlightplanContainer(IFlightplanStatement statement);

    public boolean isAddableToFlightplanContainer(Class<? extends IFlightplanStatement> cls);

    public IFlightplanStatement getFromFlightplanContainer(int i);

    public IFlightplanStatement removeFromFlightplanContainer(IFlightplanStatement statement);

    public IFlightplanStatement removeFromFlightplanContainer(int i);

    public int sizeOfFlightplanContainer();

    // public double getLengthInMeter(double airplaneSpeedInMperSec);
    // public TreeMap<Integer,Double> getProgressMapDistance();

    public boolean applyFpVisitor(IFlightplanVisitor visitor, boolean skipIgnoredPaths);

    public boolean applyFpVisitorFlat(IFlightplanVisitor visitor, boolean skipIgnoredPaths);

    public int getMaxSize();

    public static final int MAX_MEMBER = ReentryPointID.maxNoLines;

    public <T extends IFlightplanStatement> T getFirstElement(Class<T> elementClass);
    public IFlightplanStatement getLastElement();
}
