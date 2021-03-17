/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwindx.examples.util.SectorSelector;

/**
 * adjusted from SectorSelector in WWJ
 *
 * @author marco
 *     <p>Provides an interactive region selector. To use, construct and call enable/disable. Register a property
 *     listener to receive changes to the sector as they occur, or just wait until the user is done and then query the
 *     result via {@link #getSector()}.
 */
public class MSectorSelector extends SectorSelector {

    public MSectorSelector(WorldWindow worldWindow) {
        super(worldWindow);
    }

    public void setSector(Sector sector) {
        getShape().setSector(sector);
    }
}
