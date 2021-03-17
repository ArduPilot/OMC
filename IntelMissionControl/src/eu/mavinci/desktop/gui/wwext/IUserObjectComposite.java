/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.geom.Position;

import java.awt.Point;

public interface IUserObjectComposite {
    Object getRealUserObject(Position position, Point point);
}
