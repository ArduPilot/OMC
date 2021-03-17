/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

public class PreviousOfTypeVisitor extends AFlightplanVisitor {

    IFlightplanRelatedObject fpStatement;
    public Object prevObj = null;
    Class<?> cls;
    Class<?> clsExclude;

    public PreviousOfTypeVisitor(IFlightplanRelatedObject fpStatement, Class<?> cls) {
        this.fpStatement = fpStatement;
        this.cls = cls;
    }

    public PreviousOfTypeVisitor(IFlightplanRelatedObject fpStatement, Class<?> cls, Class<?> clsExclude) {
        this(fpStatement, cls);
        this.clsExclude = clsExclude;
    }

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj == fpStatement) {
            return true;
        }

        if (cls.isInstance(fpObj) && (clsExclude == null || !clsExclude.isInstance(fpObj))) {
            prevObj = fpObj;
        }

        return false;
    }

}
