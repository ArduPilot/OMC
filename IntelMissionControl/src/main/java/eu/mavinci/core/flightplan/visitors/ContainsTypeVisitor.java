/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

public class ContainsTypeVisitor extends AFlightplanVisitor {

    public boolean found = false;
    Class<?> type;

    public ContainsTypeVisitor(Class<?> type) {
        this.type = type;
    }

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (type.isInstance(fpObj)) {
            found = true;
            return true;
        }

        return false;
    }

}
