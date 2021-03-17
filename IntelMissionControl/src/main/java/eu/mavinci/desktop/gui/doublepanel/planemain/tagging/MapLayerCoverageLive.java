/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPhoto;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayerPictures;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AerialPinholeUnknownImage;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.ComputeCornerData;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.Renderable;
import java.util.ArrayList;

public class MapLayerCoverageLive extends AMapLayerCoverage implements IAirplaneListenerPhoto {

    private final MapLayerPictures pic;
    private final INotificationObject.ChangeListener hwChangeListener = event -> updateCameraCorners();

    IAirplane plane;

    public MapLayerCoverageLive(IAirplane plane, MapLayerPictures pic) {
        super(false);
        this.plane = plane;
        this.pic = pic;
        plane.addListener(this);
        plane.getHardwareConfiguration().addListener(new INotificationObject.WeakChangeListener(hwChangeListener));
    }

    @Override
    public ArrayList<ComputeCornerData> computeCorners() {
        ArrayList<ComputeCornerData> pol = new ArrayList<>();
        for (Renderable rend : pic.getWWLayer().getRenderables()) {
            if (rend instanceof AerialPinholeUnknownImage) {
                AerialPinholeUnknownImage img = (AerialPinholeUnknownImage)rend;
                ComputeCornerData computeCornerData = img.getComputeCornerData();
                if (computeCornerData == null) {
                    continue;
                }

                computeCornerData.updateLineNumber(img.getPhotoLogLine().lineNumber);
                if (!computeCornerData.isElevationDataReady()) {
                    elevationDataAvaliableTmp = false;
                }

                ArrayList<LatLon> corners = computeCornerData.getGroundProjectedCorners();
                if (corners == null) {
                    continue;
                }

                pol.add(computeCornerData);
            }
        }

        return pol;
    }

    @Override
    public ArrayList<CornerMask> computeMaskCorners() {
        return null;
    }

    @Override
    public boolean containsNonCoverageAbleAOIs() {
        return false;
    }

    @Override
    public SectorType getSectorType() {
        return SectorType.auto_redInside;
    }

    @Override
    public void recv_photo(PhotoData photo) {
        recomputeCoverage();
    }

    public static String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerCoverageLive";

    @Override
    public String getTooltipAddon() {
        return DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class).getString(KEY + ".tooltipAddon");
    }

    @Override
    public String toString() {
        return "Live Coverage of: " + plane + " " + super.toString();
    }

    @Override
    protected void updateCameraCornersBlocking() {
        for (Renderable rend : pic.getWWLayer().getRenderables()) {
            if (rend instanceof AerialPinholeUnknownImage) {
                AerialPinholeUnknownImage img = (AerialPinholeUnknownImage)rend;
                img.recomputeCornersSync();
            }
        }
    }

    @Override
    public boolean shouldShowCoverage() {
        return true;
    }
}
