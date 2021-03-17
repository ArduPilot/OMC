/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public interface IFlightplanReassignIDs {
    /** Re Assign new unsused ID to this object. Childs have to be called individually */
    public void reassignIDs();
}
