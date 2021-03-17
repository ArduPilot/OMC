/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public interface IReentryPoint extends IFlightplanStatement, IFlightplanReassignIDs {

    int getId();

    void setId(int id);

}
