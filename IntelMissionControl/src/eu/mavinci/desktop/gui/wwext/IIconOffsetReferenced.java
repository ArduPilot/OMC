/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.render.WWIcon;

public interface IIconOffsetReferenced extends WWIcon {

    /** @return a number between 0=left border and 1=right border providing the X reference position at the icon */
    public double getXRefpoint();

    /** @return a number between 0=lower border and 1=upper border providing the Y reference position at the icon */
    public double getYRefpoint();
}
