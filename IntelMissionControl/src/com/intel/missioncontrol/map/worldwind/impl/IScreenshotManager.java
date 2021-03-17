/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.impl;

import com.intel.missioncontrol.concurrent.FluentFuture;
import gov.nasa.worldwind.geom.Sector;
import java.awt.image.BufferedImage;

public interface IScreenshotManager {

    FluentFuture makeBackgroundScreenshot(Sector minSector);

    BufferedImage makeAllLayersScreenshot();

}
