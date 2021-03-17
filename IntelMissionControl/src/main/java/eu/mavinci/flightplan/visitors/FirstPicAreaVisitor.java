/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.visitors;

import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.visitors.AFlightplanVisitor;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.visitors.AFlightplanVisitor;

public class FirstPicAreaVisitor extends AFlightplanVisitor {

    PicArea picArea = null;

    public PicArea getPicArea() {
        return picArea;
    }

    @Override
    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (picArea == null && fpObj instanceof PicArea) {
            picArea = (PicArea)fpObj;
            return true;
        }

        return false;
    }

}
