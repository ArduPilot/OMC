/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

public class CountsTypeVisitor extends AFlightplanVisitor {

    public int count = 0;
    Class<?> type;

    public CountsTypeVisitor(Class<?> type) {
        this.type = type;
    }

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (type.isInstance(fpObj)) {
            count++;
        }

        return false;
    }

}
