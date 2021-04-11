package com.intel.missioncontrol.map.worldwind.impl;

import gov.nasa.worldwind.geom.Sector;
import java.awt.image.BufferedImage;
import org.asyncfx.concurrent.Future;

public interface IScreenshotManager {

    Future makeBackgroundScreenshotAsync(Sector minSector);

    Future<BufferedImage> makeAllLayersScreenshotAsync();

}
