/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

public class FirstLastOfTypeVisitor extends AFlightplanVisitor {

    public Object first = null;
    public Object last = null;
    Class<?> cls;
    Class<?> clsExclude;

    public FirstLastOfTypeVisitor(Class<?> cls) {
        this.cls = cls;
    }

    public FirstLastOfTypeVisitor(Class<?> cls, Class<?> clsExclude) {
        this(cls);
        this.clsExclude = clsExclude;
    }

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (clsExclude != null && fpObj != null && clsExclude.isInstance(fpObj)) {
            return false;
        }

        if (fpObj != null && cls.isInstance(fpObj)) {
            if (first == null) {
                first = fpObj;
            }

            last = fpObj;
        }

        return false;
    }

}
