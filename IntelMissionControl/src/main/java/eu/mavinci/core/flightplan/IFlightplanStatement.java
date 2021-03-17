/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

/**
 * This is an marker interface for stating that this object is insertable into Containers
 *
 * @author marco
 */
public interface IFlightplanStatement extends IFlightplanRelatedObject {

    // dont spefic the clone return type, otherwise I will become somewhere else conflicts!
    // public IFlightplanStatement clone();
}
