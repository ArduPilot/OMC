/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public interface IFlightplanIgnoreable {
    public boolean isIgnore();

    public void setIgnore(boolean ignore);
}
