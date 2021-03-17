/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

import java.util.Vector;

public class ExtractPicAreasVisitor extends AFlightplanVisitor {

    public Vector<CPicArea> picAreas = new Vector<CPicArea>();

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj instanceof CPicArea) {
            picAreas.add((CPicArea)fpObj);
        }

        return false;
    }

}
