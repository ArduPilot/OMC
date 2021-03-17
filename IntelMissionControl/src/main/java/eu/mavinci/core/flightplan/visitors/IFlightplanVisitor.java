/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

public interface IFlightplanVisitor {
    /** @return true means stop visiting. */
    public boolean visit(IFlightplanRelatedObject fpObj);

    /**
     * This is called for container IFlightplanReleatedObject's after the visiting of all the members.
     *
     * @return true means stop visiting.
     */
    public boolean visitExit(IFlightplanRelatedObject fpObj);

    public void startVisit(IFlightplanRelatedObject fpObj);

    public void startVisitFlat(IFlightplanRelatedObject fpObj);

    /** Visiting the given object and all of this parents up the the root object (i.e. the flightplan). */
    public void startVisitUpToRoot(IFlightplanRelatedObject fpObj);
}
