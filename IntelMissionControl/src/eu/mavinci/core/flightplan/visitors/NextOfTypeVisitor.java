/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.AFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

public class NextOfTypeVisitor extends AFlightplanVisitor {

    IFlightplanRelatedObject fpStatement;
    public Object nextObj = null;
    Class<?> cls;
    Class<?> clsExclude;
    boolean found;

    public NextOfTypeVisitor(IFlightplanRelatedObject fpStatement, Class<?> cls) {
        this.fpStatement = fpStatement;
        this.cls = cls;
        if (fpStatement == null) found = true;
    }

    public NextOfTypeVisitor(IFlightplanRelatedObject fpStatement, Class<?> cls, Class<?> clsExclude) {
        this(fpStatement, cls);
        this.clsExclude = clsExclude;
    }

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (found) {
            if (cls.isInstance(fpObj) && (clsExclude == null || !clsExclude.isInstance(fpObj))) {
                nextObj = fpObj;
                return true;
            }
        }

        if (fpObj == fpStatement && !(fpObj instanceof AFlightplanContainer)) {
            found = true;
        }

        return false;
    }

    @Override
    public boolean visitExit(IFlightplanRelatedObject fpObj) {
        if (fpObj == fpStatement) {
            found = true;
        }
        return false;
    }
}
