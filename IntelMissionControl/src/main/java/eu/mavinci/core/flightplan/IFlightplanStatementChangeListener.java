/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

interface IFlightplanStatementChangeListener {
    /**
     * if only properties of a node are changed
     *
     * @param statement
     */
    public void flightplanStatementChanged(IFlightplanRelatedObject statement);

    /**
     * If the subnode structure of a node has (maybe) changed in some way (eg. multiple removes or addings)
     *
     * @param statement
     */
    public void flightplanStatementStructureChanged(IFlightplanRelatedObject statement);

    /**
     * If some new nodes are inserted
     *
     * @param statement the container where the statement is added
     */
    public void flightplanStatementAdded(IFlightplanRelatedObject statement);

    /**
     * A Statement was removed from its container
     *
     * @param statement
     */
    public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement);
}
