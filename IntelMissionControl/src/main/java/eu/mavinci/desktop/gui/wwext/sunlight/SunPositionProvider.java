/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.sunlight;

import eu.mavinci.core.obfuscation.IKeepAll;
import gov.nasa.worldwind.geom.LatLon;
import eu.mavinci.core.obfuscation.IKeepAll;

/**
 * @author Michael de Hoog
 * @version $Id: SunPositionProvider.java 10406 2009-04-22 18:28:45Z patrickmurris $
 */
public interface SunPositionProvider extends IKeepAll {
    public LatLon getPosition();
}
