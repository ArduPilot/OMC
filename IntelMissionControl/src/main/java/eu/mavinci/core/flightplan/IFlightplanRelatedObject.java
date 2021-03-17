/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import eu.mavinci.core.obfuscation.IKeepClassname;
import eu.mavinci.core.obfuscation.IKeepClassname;

public interface IFlightplanRelatedObject extends IKeepClassname {

    CFlightplan getFlightplan();

    IFlightplanRelatedObject getCopy();

    void setParent(IFlightplanContainer container);

    IFlightplanContainer getParent();

}
