/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

import java.util.Vector;

public class ExtractTypeVisitor<T> extends AFlightplanVisitor {

    Class<? extends T> cls;

    public ExtractTypeVisitor(Class<? extends T> cls) {
        this.cls = cls;
    }

    public Vector<T> filterResults = new Vector<T>();

    @SuppressWarnings("unchecked")
    public boolean visit(IFlightplanRelatedObject fpObj) {
        // System.out.println(fpObj);
        if (fpObj != null && cls.isInstance(fpObj)) {
            // System.out.println("filter match!");
            filterResults.add((T)fpObj);
        }

        return false;
    }
}
