/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public interface IFlightplanChangeListener {

    /**
     * totally change of FP Structure. E.g. multiple removes/adds
     *
     * @param fp
     */
    public void flightplanStructureChanged(IFlightplanRelatedObject fp);

    /**
     * Internal change of some values inside of the FP nodes
     *
     * @param fpObj
     */
    public void flightplanValuesChanged(IFlightplanRelatedObject fpObj);

    /**
     * Element removed from FP
     *
     * <p>or a total FP is removed if statement == null
     *
     * @param fp
     * @param statement
     */
    public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement);

    /**
     * Element added to FP
     *
     * <p>or a total FP is added if statement == null
     *
     * @param fp
     * @param statement
     */
    public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement);

}
