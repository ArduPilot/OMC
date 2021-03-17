/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

import java.util.Vector;

public class CollectsTypeVisitor<T> extends AFlightplanVisitor {

    public boolean found = false;
    Class<T> type;
    public Vector<T> matches = new Vector<T>();

    public CollectsTypeVisitor(Class<T> type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (type.isInstance(fpObj)) {
            found = true;
            matches.add((T)fpObj);
        }

        return false;
    }

}
